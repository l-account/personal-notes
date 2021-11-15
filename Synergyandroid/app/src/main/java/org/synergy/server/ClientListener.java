package org.synergy.server;

import org.synergy.base.Event;
import org.synergy.base.EventJobInterface;
import org.synergy.base.EventQueue;
import org.synergy.base.EventType;
import org.synergy.base.Log;
import org.synergy.client.Client;
import org.synergy.io.Stream;
import org.synergy.io.StreamFilterFactoryInterface;
import org.synergy.net.DataSocketInterface;
import org.synergy.net.ListenSocketInterface;
import org.synergy.net.NetworkAddress;
import org.synergy.net.SocketFactoryInterface;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Deque;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

public class ClientListener {
    private Server m_server;
    private NetworkAddress m_serverAddress;
    private SocketFactoryInterface m_socketFactory;
    private ListenSocketInterface m_lisensocket;
    private Stream m_stream;
    private StreamFilterFactoryInterface m_streamFilterFactory;
    private Set<DataSocketInterface> m_clientsockets=new HashSet<DataSocketInterface>();
    private List<ClientProxy> m_waitingClients=new ArrayList<ClientProxy>();
    private Set<ClientProxyUnknown> m_newClients=new HashSet<ClientProxyUnknown>();
    private String m_servername;
    private ClientProxyUnknown  clientunknown;

    public ClientListener(final String Servername,final NetworkAddress serveraddress,
                          SocketFactoryInterface socketFactory,Server server) {
        this.m_servername = Servername;
        this.m_serverAddress = serveraddress;
        this.m_server = server;
        this.m_socketFactory = socketFactory;

        assert (null != m_socketFactory);
        this.m_lisensocket=m_socketFactory.createListen();
        try{
        m_serverAddress.resolve();

        Log.debug("start listen socket ");
        EventQueue.getInstance().adoptHandler(EventType.LISTENSOCKET_CONNECTING, m_lisensocket, new EventJobInterface() {
            @Override
            public void run(Event event) throws IOException {
                handleClientConnecting();
            }
        });

        m_lisensocket.bind(m_serverAddress);

        }catch (Exception e){
            e.printStackTrace();
        }
    }


    public void handleClientConnecting()  {
        try{
        DataSocketInterface socket=m_lisensocket.accept();
        if(null!= socket){
            Log.debug("get client socket ");
            m_clientsockets.add(socket);
            if(null!=m_stream){
                Log.debug("m_stream != null");
                return;
            }
            m_stream=socket;
            //ClientProxy clientProxy=new ClientProxy(m_servername,m_server,m_stream);//todo 待完善
            //EventQueue.getInstance().addEvent(new Event(EventType.STREAM_INPUT_READY,m_stream.getEventTarget()));
             clientunknown=new ClientProxyUnknown(m_server,30.0,m_stream);
            Log.debug("clientunknown over in here ");
            m_newClients.add(clientunknown);
            EventQueue.getInstance().adoptHandler(EventType.CLIENTPROXYUNKNOWN_SUCCESS, clientunknown, new EventJobInterface() {
                @Override
                public void run(Event event) {
                    handleClientConnection(clientunknown);
                }
            });
            EventQueue.getInstance().adoptHandler(EventType.CLIENTPROXYUNKNOWN_FAIL, clientunknown, new EventJobInterface() {
                @Override
                public void run(Event event) {
                    handleClientConnection(clientunknown);
                }
            });

        }}catch (Exception e){
            e.printStackTrace();
        }

    }

    public void handleClientConnection(ClientProxyUnknown v_client){
        Log.debug("****** handle clientproxy unknown success  ");
        assert (1 == m_newClients.size());
        //get the real client proxy todo
        //ClientProxy real_client=v_client.orphanClientProxy();
        ClientProxy real_client=v_client.orphanClientProxy();
        Log.debug("get real clientproxy ");//todo: not run here
        boolean handshakeOk=false;
        if(null != real_client){
            handshakeOk=true;
            m_waitingClients.add(real_client);
            EventQueue.getInstance().addEvent(new Event(EventType.LISTENCLIENT_CONNECTED,this));
            //watch for client in queue to disconnect
            EventQueue.getInstance().adoptHandler(EventType.CLIENTPROXY_DISCONNECTED, real_client, new EventJobInterface() {
                @Override
                public void run(Event event) {
                    handleClientDisconnected(real_client);
                }
            });
        }

        EventQueue.getInstance().removeHandler(EventType.CLIENTPROXYUNKNOWN_FAIL,real_client);
        EventQueue.getInstance().removeHandler(EventType.CLIENTPROXYUNKNOWN_SUCCESS,real_client);
        m_newClients.remove(v_client);


    }

    public void handleClientDisconnected(ClientProxy client){
        for (ClientProxy cc:m_waitingClients
             ) {if (client==cc){
                 m_waitingClients.remove(client);
                 EventQueue.getInstance().removeHandler(EventType.CLIENTPROXY_DISCONNECTED,client);
        }

        }
    }

    private void removeHandlers(){
        //todo
    }
}
