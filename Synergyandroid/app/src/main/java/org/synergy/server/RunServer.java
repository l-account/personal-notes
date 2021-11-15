package org.synergy.server;

import android.content.Context;
import android.os.Looper;
import android.widget.Toast;

import org.synergy.base.Event;
import org.synergy.base.EventJobInterface;
import org.synergy.base.EventQueue;
import org.synergy.base.EventQueueTimer;
import org.synergy.base.EventType;
import org.synergy.base.Log;
import org.synergy.common.screens.BasicScreen;
import org.synergy.common.screens.ScreenInterface;
import org.synergy.io.Stream;
import org.synergy.io.StreamFilterFactoryInterface;
import org.synergy.net.NetworkAddress;
import org.synergy.net.SocketFactoryInterface;
import org.synergy.net.TCPSocketFactory;

public class RunServer {
    public enum EServerState {
        kUninitialized,
        kInitializing,
        kInitializingToStart,
        kInitialized,
        kStarting,
        kStarted
    };

    private Server                m_server;
    private EServerState          m_serverState;
    private ScreenInterface       m_serverScreen;
    private final String          m_servername;
    private NetworkAddress        m_serverAddress;

    private PrimaryClient         m_primaryClient;
    private ClientListener        m_listener;
    private EventQueueTimer       m_timer;

    private boolean               m_suspend;
    private final Context         context;

    private SocketFactoryInterface socketFactory;
    private StreamFilterFactoryInterface streamFilterFactory;
    private Stream                 m_stream;
//    public boolean loadconfig(String config_path){
//        try{
//
//        }catch (Exception e){
//            Log.debug("open config file failed  ");
//            e.printStackTrace();
//        }
//        return false;
//
//    }

    public RunServer(final Context context, final String name, final NetworkAddress serverAddress,
                     SocketFactoryInterface socketFactory, StreamFilterFactoryInterface streamFilterFactory,
                     ScreenInterface screen){
        this.context=context;
        this.m_servername=name;
        m_server=null;
        m_serverState=EServerState.kUninitialized;
        m_serverScreen=screen;
        m_primaryClient=null;
        m_listener=null;
        m_timer=null;
        m_serverAddress=serverAddress;
        this.socketFactory = socketFactory;
        this.streamFilterFactory = streamFilterFactory;

        m_primaryClient=new PrimaryClient(m_servername,m_serverScreen);
        m_server=new Server(this.context,m_servername,serverAddress,socketFactory,streamFilterFactory,screen,m_primaryClient,m_stream);
        assert (socketFactory != null);
        assert (screen != null);
    }

    public void ServerInit(){
        //Looper.prepare();
        //Toast.makeText(context,"in server construction ",Toast.LENGTH_SHORT).show();
        //Looper.loop();
        m_listener=new ClientListener(m_servername,m_serverAddress,socketFactory,m_server);
        assert (null!=m_listener);

    }
    /*
    public void forceReconnect(){
        if(null!=m_server)
            m_server.disconnect();
    }
    //public void resetServer( Event,void){}
    public void handleClientConnected(ClientListener listener){
        ClientProxy client=listener.getNextClient();
        if(null!=client){
            m_server.adoptClient(client);
            updateStatus();
        }
    }
    public void handleClientsDisconnected(){
        EventQueue.getInstance().addEvent(new Event(EventType.QUIT));
    }
    public void closeServer(Server server){
        if(null==server)
            return;
        server.disconnect();
        // wait for clients to disconnect for up to timeout seconds
        double timeout=3.0;
        EventQueueTimer timer=new EventQueueTimer(timeout, true, this, new EventJobInterface() {
            @Override
            public void run(Event event) {
                    handleClientsDisconnected();
            }
        });
        EventQueue.getInstance().adoptHandler(EventType.SERVER_DISCONNECTED, server, new EventJobInterface() {
            @Override
            public void run(Event event) {
                handleClientsDisconnected();
            }
        });

        EventQueue.getInstance().removeHandler(EventType.TIMER,timer);
        EventQueue.getInstance().removeHandler(EventType.SERVER_DISCONNECTED,server);
        timer=null;
    }
    public void stopRetryTimer(){
        if(null!=m_timer){
            EventQueue.getInstance().removeHandler(EventType.TIMER,m_timer);
            m_timer=null;
        }
    }
    public void updateStatus(){
        updateStatus("");
    }
    public void updateStatus(String msg){}
    public void closeClientListener(ClientListener listen){
        if(null!=listen){
            EventQueue.getInstance().removeHandler(EventType.SERVER_CONNECTED,listen);
        }
    }
    public void stopServer(){
        if(m_serverState==EServerState.kStarted){
            closeServer(m_server);
            closeClientListener(m_listener);
            m_server      = null;
            m_listener    = null;
            m_serverState =EServerState.kInitialized;
        }
        else if(EServerState.kStarting==m_serverState){
            stopRetryTimer();
            m_serverState=EServerState.kInitialized;
        }
        assert (null==m_server);
        assert (null==m_listener);
    }
    public void closePrimaryClient(PrimaryClient primaryClient){
        //todo java reference delete
        primaryClient=null;
    }
    public void closeServerScreen(ScreenInterface screen){
        if(null!=screen){
            EventQueue.getInstance().removeHandler(EventType.SCREEN_SUSPEND,screen.getEventTarget());
            EventQueue.getInstance().removeHandler(EventType.SCREEN_RESUME,screen.getEventTarget());
            EventQueue.getInstance().removeHandler(EventType.SCREEN_error,screen.getEventTarget());
        }
    }
    public void cleanupServer(){
        stopServer();
        if(EServerState.kInitialized==m_serverState){
            closePrimaryClient(m_primaryClient);
            closeServerScreen(m_serverScreen);
            m_primaryClient = null;
            m_serverScreen  = null;
            m_serverState   = EServerState.kUninitialized;
        }else if(EServerState.kInitializing==m_serverState ||
        EServerState.kInitializingToStart==m_serverState){
            stopRetryTimer();
            m_serverState=EServerState.kUninitialized;
        }
        assert(m_primaryClient == null);
        assert(m_serverScreen == null);
        assert(m_serverState == EServerState.kUninitialized);
    }
    public boolean initServer(){
        // skip if already initialized or initializing
        if (m_serverState != EServerState.kUninitialized) {
            return true;
        }
        ScreenInterface serverscren=null;
        PrimaryClient primaryClient=null;
        try {
            serverscren=openServerScreen();
            primaryClient=openPrimaryClient(name,serverscren);
            m_serverScreen  = serverscren;
            m_primaryClient = primaryClient;
            m_serverState   = EServerState.kInitialized;
            return true;
        }catch (Exception e){
            closePrimaryClient(primaryClient);
            closeServerScreen(serverscreen);
            e.printStackTrace();
            return false;
        }
    }
    public void retryHandler(){
        // discard old timer
        assert(m_timer != null);
        stopRetryTimer();

        // try initializing/starting the server again
        switch (m_serverState) {
            case kUninitialized:
            case kInitialized:
            case kStarted:
                //assert(0 && "bad internal server state");
                break;

            case kInitializing:
                //LOG((CLOG_DEBUG1 "retry server initialization"));
                m_serverState = EServerState.kUninitialized;
                if (!initServer()) {
                    EventQueue.getInstance().addEvent(new Event(EventType.QUIT));
                }
                break;

            case kInitializingToStart:
                //LOG((CLOG_DEBUG1 "retry server initialization"));
                m_serverState = EServerState.kUninitialized;
                if (!initServer()) {
                    EventQueue.getInstance().addEvent(new Event(EventType.QUIT));
                }
                else if (m_serverState == EServerState.kInitialized) {
                    //LOG((CLOG_DEBUG1 "starting server"));
                    if (!startServer()) {
                        EventQueue.getInstance().addEvent(new Event(EventType.QUIT));
                    }
                }
                break;

            case kStarting:
                //LOG((CLOG_DEBUG1 "retry starting server"));
                m_serverState = EServerState.kInitialized;
                if (!startServer()) {
                    EventQueue.getInstance().addEvent(new Event(EventType.QUIT));
                }
                break;
        }
    }
    public ScreenInterface openServerScreen(){
        ScreenInterface screen=createScreen();
        EventQueue.getInstance().adoptHandler(EventType.SCREEN_error, screen.getEventTarget(), new EventJobInterface() {
            @Override
            public void run(Event event) {
                handleScreenError();
            }
        });
        EventQueue.getInstance().adoptHandler(EventType.SCREEN_SUSPEND, screen.getEventTarget(), new EventJobInterface() {
            @Override
            public void run(Event event) {
                handleSuspend();
            }
        });
        EventQueue.getInstance().adoptHandler(EventType.SCREEN_error, screen.getEventTarget(), new EventJobInterface() {
            @Override
            public void run(Event event) {
                handleResume();
            }
        });
        return screen;
    }

    public boolean startServer(){
        // skip if already started or starting
        if (m_serverState == EServerState.kStarting || m_serverState == EServerState.kStarted) {
            return true;
        }

        // initialize if necessary
        if (m_serverState != EServerState.kInitialized) {
            if (!initServer()) {
                // hard initialization failure
                return false;
            }
            if (m_serverState == EServerState.kInitializing) {
                // not ready to start
                m_serverState = EServerState.kInitializingToStart;
                return true;
            }
            assert(m_serverState == EServerState.kInitialized);
        }
        ClientListener listener=null;
        try{
            listener=openClientListener();
            m_server=openServer(,m_primaryClient);
            listener.setServer(m_server);
            m_server.setListener(listener);
            m_listener=listener;
            Log.debug("started server, waiting for clients");
            m_serverState=EServerState.kStarted;
            return true;
        }catch (Exception e){
            closeClientListener(listener);
            e.printStackTrace();
            return false;
        }
    }
    public ScreenInterface createScreen(){
        // todo
        return new BasicScreen();
    }


    public PrimaryClient openPrimaryClient(final String name, ScreenInterface screen){
        // todo
        return new PrimaryClient(name,screen);
    }
    public void handleScreenError(){
        EventQueue.getInstance().addEvent(new Event(EventType.QUIT));
    }
    public void handleSuspend(){
        if(!m_suspend){
            stopServer();
            m_suspend=true;
        }
    }
    public void handleResume(){
        if(m_suspend){
            startServer();
            m_suspend=false;
        }
    }

    public ClientListener openClientListener(final NetworkAddress address){
        ClientListener listen=new ClientListener(address);
        EventQueue.getInstance().adoptHandler(EventType.LISTENCLIENT_CONNECTED, listen, new EventJobInterface() {
            @Override
            public void run(Event event) {
                handleClientConnected();
            }
        });
        return listen;
    }
    public Server openServer(PrimaryClient primaryClient){
        Server server=new Server(primaryClient,m_serverScreen);
        try{
            EventQueue.getInstance().adoptHandler(EventType.SERVER_DISCONNECTED, server, new EventJobInterface() {
                @Override
                public void run(Event event) {
                    handleNoClients();
                }
            });
            EventQueue.getInstance().adoptHandler(EventType.SERVER_ScreenSwitched, server, new EventJobInterface() {
                @Override
                public void run(Event event) {
                    handleScreenSwitched();
                }
            });
        }catch (Exception e){
            e.printStackTrace();
        }
        return server;
    }
    */

    public void handleNoClients(){
        //todo
    }

    //public int mainLoop(){}

    //public int standardStartup(){}
    //public int foregroundStartup(){}
    //public void startNode(){}
    public Server getServerPtr() { return m_server; }
    public void handleScreenSwitched(){
        //todo
    }
}
