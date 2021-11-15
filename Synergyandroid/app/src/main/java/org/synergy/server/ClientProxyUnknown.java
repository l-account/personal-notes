package org.synergy.server;

import org.synergy.base.Event;
import org.synergy.base.EventJobInterface;
import org.synergy.base.EventQueue;
import org.synergy.base.EventQueueTimer;
import org.synergy.base.EventType;
import org.synergy.base.Log;
import org.synergy.io.Stream;
import org.synergy.io.msgs.HelloBackMessage;
import org.synergy.io.msgs.HelloMessage;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class ClientProxyUnknown {
    private ClientProxy m_clientProxy;
    private Server m_server;
    private EventQueueTimer queueTimer;
    private Stream m_stream;
    private boolean m_ready;
    private String client_name;

    public ClientProxyUnknown(Server ss,double timeout,Stream stream){
        assert (null !=ss);
        assert (null!=stream);
        this.m_clientProxy=null;
        this.m_server=ss;
        this.m_ready=false;
        this.m_stream=stream;
        EventQueue.getInstance().adoptHandler(EventType.TIMER, m_stream.getEventTarget(), new EventJobInterface() {
            @Override
            public void run(Event event) {
                handletimeout();
            }
        });

        //say hello
        Log.debug("sever is saying hello ");
        try{
            DataOutputStream dout=new DataOutputStream(m_stream.getOutputStream());
            HelloMessage helloMessage=new HelloMessage(1,7,"server say hello ");
            helloMessage.write(dout);
        } catch (IOException e) {
            e.printStackTrace();
        }

        setupStreamHandlers();
    }

    //! Get the client proxy
    //    /*!
    //    Returns the client proxy created after a successful handshake
    //    (i.e. when this object sends a success event).  Returns NULL
    //    if the handshake is unsuccessful or incomplete.
    //    */
    public ClientProxy orphanClientProxy(){
        if(m_ready){
            cleanHandlers();
            return m_clientProxy;
        }else
            return null;
    }

    private void setupStreamHandlers(){
        assert (null !=m_stream );
        Log.debug1("setup stream handlers");
        EventQueue.getInstance().adoptHandler(EventType.STREAM_INPUT_READY, m_stream.getEventTarget(),
                new EventJobInterface() {
                    @Override
                    public void run(Event event) {
                        handleData();
                    }
                });
        EventQueue.getInstance().adoptHandler(EventType.STREAM_OUTPUT_ERROR, m_stream.getEventTarget(),
                new EventJobInterface() {
                    @Override
                    public void run(Event event) {
                        handleWriteError();
                    }
                });
        EventQueue.getInstance().adoptHandler(EventType.STREAM_INPUT_SHUTDOWN, m_stream.getEventTarget(),
                new EventJobInterface() {
                    @Override
                    public void run(Event event) {
                        handleDisconnect();
                    }
                });
        EventQueue.getInstance().adoptHandler(EventType.STREAM_OUTPUT_SHUTDOWN, m_stream.getEventTarget(),
                new EventJobInterface() {
                    @Override
                    public void run(Event event) {
                        handleWriteError();
                    }
                });
    }

    private void setupProxyHandlers(){
        assert (null !=m_clientProxy);
        Log.debug("register clientproxy ready "+m_clientProxy);
        EventQueue.getInstance().adoptHandler(EventType.CLIENTPROXY_READY, m_clientProxy,
                new EventJobInterface() {
                    @Override
                    public void run(Event event) {
                        handleready();
                    }
                });
        EventQueue.getInstance().adoptHandler(EventType.CLIENTPROXY_DISCONNECTED, m_clientProxy,
                new EventJobInterface() {
                    @Override
                    public void run(Event event) {
                        handleDisconnect();
                    }
                });
    }

    private void handleready(){
        m_ready=true;
        cleanHandlers();
        EventQueue.getInstance().addEvent(new Event(EventType.CLIENTPROXYUNKNOWN_SUCCESS,
                this));
        Log.debug("clientproxy handleready ");
    }

    private void  handleDisconnect(){
        Log.debug1("new client disconnected ");
        m_ready=false;
        cleanHandlers();
        EventQueue.getInstance().addEvent(new Event(EventType.CLIENTPROXYUNKNOWN_FAIL,
                this));
    }

    private void cleanHandlers(){
        if(null != m_stream){
            EventQueue.getInstance().removeHandler(EventType.STREAM_INPUT_READY,m_stream.getEventTarget());
            EventQueue.getInstance().removeHandler(EventType.STREAM_OUTPUT_ERROR,m_stream.getEventTarget());
            EventQueue.getInstance().removeHandler(EventType.STREAM_INPUT_SHUTDOWN,m_stream.getEventTarget());
            EventQueue.getInstance().removeHandler(EventType.STREAM_OUTPUT_SHUTDOWN,m_stream.getEventTarget());
        }

        if(null !=m_clientProxy){
            Log.debug("unregister clientproxy hanlers ");
            EventQueue.getInstance().removeHandler(EventType.CLIENTPROXY_READY,m_clientProxy);
            EventQueue.getInstance().removeHandler(EventType.CLIENTPROXY_DISCONNECTED,m_clientProxy);
        }
    }


    private void handleData()  {
        Log.debug1("parsing hello reply ");
        int major=0,min=0;
        String client_name;
        try{
        // Read in the Hello Message reply
        DataInputStream din = new DataInputStream (m_stream.getInputStream ());
        HelloBackMessage helloBackMessage = new HelloBackMessage(din);

        major=helloBackMessage.getMajorVersion();
        min=helloBackMessage.getMinorVersion();
        client_name=helloBackMessage.getClineName();
        Log.debug("read helloback message ："+helloBackMessage.getClientLanguage());
        assert (1 ==major);
        assert (7 ==min);

        /*
        //  start construct clientproxy
         */
        cleanHandlers();
        m_clientProxy=new ClientProxy(client_name,m_server,m_stream);
        Log.debug("ClientProxy over in here");
        if(null != m_clientProxy){
            Log.debug("creat clientproxy ");
            setupProxyHandlers();
        }
        else {
            Log.debug("cant creat proxy for client "+client_name);
            EventQueue.getInstance().addEvent(new Event(EventType.CLIENTPROXYUNKNOWN_FAIL,
                    this));
        }
        }
        catch (Exception e){
            e.printStackTrace();
        }
    }

    private void handleWriteError(){
        Log.debug1("error communicating with new client ");
        m_ready=false;
        cleanHandlers();
        EventQueue.getInstance().addEvent(new Event(EventType.CLIENTPROXYUNKNOWN_FAIL,
                this));
    }

    private void handletimeout(){
        Log.debug1("new client is unresponsive ");
        m_ready=false;
        cleanHandlers();
        EventQueue.getInstance().addEvent(new Event(EventType.CLIENTPROXYUNKNOWN_FAIL,
                this));
    }





}
