package org.synergy.server;


import android.content.Context;
import android.graphics.Point;

import org.synergy.base.Event;
import org.synergy.base.EventJobInterface;
import org.synergy.base.EventQueue;
import org.synergy.base.EventQueueTimer;
import org.synergy.base.EventTarget;
import org.synergy.base.EventType;
import org.synergy.common.screens.ScreenInterface;
import org.synergy.io.Stream;
import org.synergy.io.StreamFilterFactoryInterface;
import org.synergy.io.msgs.MessageType;
import org.synergy.net.NetworkAddress;
import org.synergy.base.Log;
import org.synergy.net.SocketFactoryInterface;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

public class Server implements EventTarget {
    enum EDirection{
        kNoDirection(0),
        kLeft(1),
        kRight(2),
        kTop(3),
        kBottom(4),
        kFirstDirection(1),
        kLastDirection (4),
        kNumDirections (4);
        public int value;
        EDirection(int i) {
            this.value=i;
        }
    };
    enum EDirectionmask{
        kNoDirMask(0) ,
        kLeftMask(2),
        kRightMask(4),
        kTopMask(8),
        kBottomMask(16);
        public int value;
        EDirectionmask(int i) {
            this.value=i;
        }
    };
    enum EScreenSwitchConers{
        kNoCorner(0),
        kTopLeft(1),
        kTopRight(2),
        kBottomLeft(3),
        kBottomRight(4),
        kFirstCorner(1),
        kLastCorner(4);
        public int val;

        EScreenSwitchConers(int i) {
            this.val=i;
        }
    }
    enum EScreenSwitchCornerMasks{
        kNoCornerMask(0),
        kTopLeftMask(1),
        kTopRightMask(2),
        kBottomLeftMask(4),
        kBottomRightMask(8),
        kAllCornersMask(15)
        ;
        public int val;
        EScreenSwitchCornerMasks(int i){
            this.val=i;
        }
    }
    private final Context context;
    private String ServerName;

    private ClientListener m_clientListen;
    private NetworkAddress ServerAddress;
    private Stream m_stream;
    private SocketFactoryInterface socketFactoryInterface;
    private StreamFilterFactoryInterface streamFilterFactoryInterface;

    private ScreenInterface m_serverScreen;

    private Map<String,ClientProxy>  Clients_list;
    private Set<ClientProxy> Clients_set;
    private Map<ClientProxy,EventQueueTimer> Clients_old;
    private PrimaryClient m_clientprimary;
    //clients with focus
    private ClientProxy m_active;
    private ClientProxy m_switchScreen;
    // the sequence number of enter message
    private int m_seqNum;
    //current mouse position in absolute screen coordinates
    private short m_x,m_y;
    private short m_switchX,m_switchY;
    private short m_xDelta,m_yDelta;
    //save info when screen saver activated
    private ClientProxy m_activeSaver;
    private short m_xSaver,m_ySaver;

    public Server(final Context context,final String servername,final NetworkAddress serveraddress,
                  SocketFactoryInterface socketFactory,StreamFilterFactoryInterface streamFilterFactory,
                  ScreenInterface serverscreen,PrimaryClient primaryClient,Stream stream){
        this.context=context;
        this.ServerName=servername;
        this.ServerAddress=serveraddress;
        this.socketFactoryInterface=socketFactory;
        this.streamFilterFactoryInterface=streamFilterFactory;
        this.m_serverScreen=serverscreen;
        this.m_clientprimary=primaryClient;
        this.m_stream=stream;

        assert (null != socketFactoryInterface);
        assert (null !=m_serverScreen);
        assert (null !=m_clientprimary);

        //setupHandlers();
        //addClient(m_clientprimary);

        //initConfig();

        m_clientprimary.enable();

        //todo add function  lock the cursor to primaryclient

    }

    public Object getEventTarget(){
        //todo
        return this;
    }

/*
    private void setupHandlers(){
        EventQueue.getInstance().adoptHandler(EventType.TIMER, this, new EventJobInterface() {
            @Override
            public void run(Event event) {
                handleSwitchWaitTimeout();
            }
        });
        EventQueue.getInstance().adoptHandler(EventType.KeyState_down, m_stream.getEventTarget(), new EventJobInterface() {
            @Override
            public void run(Event event) {
                handleKeyDown();
            }
        });
        EventQueue.getInstance().adoptHandler(EventType.KeyState_up, m_stream.getEventTarget(), new EventJobInterface() {
            @Override
            public void run(Event event) {
                handleKeyUp();
            }
        });
        EventQueue.getInstance().adoptHandler(EventType.KeyState_repeat, m_stream.getEventTarget(), new EventJobInterface() {
            @Override
            public void run(Event event) {
                handleKeyRepeat();
            }
        });
        EventQueue.getInstance().adoptHandler(EventType.PrimaryScreen_buttondown, m_stream.getEventTarget(), new EventJobInterface()      {
            @Override
            public void run(Event event) {
                handleButtonDown();
            }
        });
        EventQueue.getInstance().adoptHandler(EventType.PrimaryScreen_buttonup, m_stream.getEventTarget(), new EventJobInterface()      {
            @Override
            public void run(Event event) {
                handleButtonUp();
            }
        });
        EventQueue.getInstance().adoptHandler(EventType.PrimaryScreen_motionOnPrimary, m_clientprimary.getEventTarget(), new EventJobInterface() {
            @Override
            public void run(Event event) {
                handleMotionprimary();
            }
        });
        EventQueue.getInstance().adoptHandler(EventType.PrimaryScreen_motionOnSecondary, m_clientprimary.getEventTarget(), new EventJobInterface() {
            @Override
            public void run(Event event) {
                handleMotionSecondary();
            }
        });
        EventQueue.getInstance().adoptHandler(EventType.PrimaryScreen_wheel, m_clientprimary.getEventTarget(), new EventJobInterface() {
            @Override
            public void run(Event event) {
                handlePrimaryWheel();
            }
        });
        EventQueue.getInstance().adoptHandler(EventType.PrimaryScreen_screensaveActivate, m_clientprimary.getEventTarget(), new EventJobInterface() {
            @Override
            public void run(Event event) {
                handleScreenSaveActivate();
            }
        });
        EventQueue.getInstance().adoptHandler(EventType.PrimaryScreen_screensaveDeactivate, m_clientprimary.getEventTarget(), new EventJobInterface() {
            @Override
            public void run(Event event) {
                hanldeScreenSaveDeactivate();
            }
        });
        EventQueue.getInstance().adoptHandler(EventType.SERVER_SwitchToScreen, m_stream.getEventTarget(), new EventJobInterface() {
            @Override
            public void run(Event event) {
                handleSwitchScreen();
            }
        });
        EventQueue.getInstance().adoptHandler(EventType.SERVER_SwitchInDirection, m_stream.getEventTarget(), new EventJobInterface() {
            @Override
            public void run(Event event) {
                handleSwitchDirection();
            }
        });
        EventQueue.getInstance().adoptHandler(EventType.PrimaryScreen_fakeinputBegin, m_stream.getEventTarget(), new EventJobInterface() {
            @Override
            public void run(Event event) {
                handleFakeInputBegin();
            }
        });
        EventQueue.getInstance().adoptHandler(EventType.PrimaryScreen_fakeinputEnd, m_stream.getEventTarget(), new EventJobInterface() {
            @Override
            public void run(Event event) {
                handleFakeInputEnd();
            }
        });
    }

    private void adoptClient(BaseClientProxy client){
        assert (null!=client);
        EventQueue.getInstance().adoptHandler(EventType.CLIENTPROXY_DISCONNECTED, client, new EventJobInterface() {
            @Override
            public void run(Event event) {
                handleClientDisconnected(client);
            }
        });
        if(!isScreen()){
            closeclient(client,Unknown);
            return;
        }
        if(!addClient(client)){
            closeclient(client,EBusy);
        }
        sendoptions(client);
        if(null!=m_activeSaver)
            client.screensaver(true);
    }
    public void handleSwitchWaitTimeout(){
        if(isLockedScreen()){
            if(null!=m_switchScreen){
                m_switchScreen=null;
            }
            return;
        }
        switchScreen(m_switchScreen,m_switchX,m_switchY,false);
    }
    public void handleShapeChanged(ClientProxy client){
        if (!Clients_set.contains(client))
            return;
        short x=client.Client_info.m_mx;
        short y=client.Client_info.m_my;
        client.setjumpCursorPos(x,y);
        if(client==m_active){
            m_x=x;
            m_y=y;
        }
        if(client==m_clientprimary){
            if(client==m_active)
                onMouseMovePrimary(m_x,m_y);
            else
                onMouseMoveSecondary(0,0);
        }
    }
    public void handleKeyDown(){
        //todo add keyinfo=;

    }
    public void handleClipboardGrabbed(){
        //todo
    }
    public void handleClipboardChanged(){
        //todo
    }
    private void handleClientDisconnected(ClientProxy client){
        removeActiveClient(client);
        removeOldClient(client);
    }
    public void handleKeyUp(){}
    public void handleKeyRepeat(){}
    public void handleMotionprimary(){}
    public void handleMotionSecondary(){}
    public void handlePrimaryWheel(){}
    public void handleSwitchScreen(){}
    public void handleSwitchDirection(){}
    public void handleButtonDown(){}
    public void handleButtonUp(){}
    public void handleScreenSaveActivate(){
        onScreenSaver(true);

    }
    private void onScreenSaver(boolean activated){
        if(activated){
            // save current screen and position
            m_activeSaver = m_active;
            m_xSaver      = m_x;
            m_ySaver      = m_y;

            // jump to primary screen
            if (m_active != m_clientprimary) {
                switchScreen(m_clientprimary, 0, 0, true);
            }
        }
        else{
            // jump back to previous screen and position.  we must check
            // that the position is still valid since the screen may have
            // changed resolutions while the screen saver was running.
            if (m_activeSaver != NULL && m_activeSaver != m_primaryClient) {
                // check position
                ClientProxy  screen = m_activeSaver;
                short x, y, w, h;
                screen->getShape(x, y, w, h);
                short zoneSize = getJumpZoneSize(screen);
                if (m_xSaver < x + zoneSize) {
                    m_xSaver = x + zoneSize;
                }
                else if (m_xSaver >= x + w - zoneSize) {
                    m_xSaver = x + w - zoneSize - 1;
                }
                if (m_ySaver < y + zoneSize) {
                    m_ySaver = y + zoneSize;
                }
                else if (m_ySaver >= y + h - zoneSize) {
                    m_ySaver = y + h - zoneSize - 1;
                }

                // jump
                switchScreen(screen, m_xSaver, m_ySaver, false);
            }
            // send message to all clients
            for (ClientList::const_iterator index = m_clients.begin();
                 index != m_clients.end(); ++index) {
                BaseClientProxy* client = index->second;
                client->screensaver(activated);
            }
        }
    }
    public void hanldeScreenSaveDeactivate(){
        onScreenSaver(false);
    }
    public void handleFakeInputBegin(){}
    public void handleFakeInputEnd(){}

    public void disconnect(){
        if(1<Clients_list.size()||!Clients_old.isEmpty()){
            closeclients();
        }else{
            EventQueue.getInstance().addEvent(new Event(EventType.SERVER_DISCONNECTED,this));
        }
    }

    public void closeclients(){

        Set<BaseClientProxy> clients_2_rm=new HashSet<BaseClientProxy>();
        Clients_list.forEach((str,baseclient)->{
            if(true)//todo
                clients_2_rm.add(baseclient);
        });
        clients_2_rm.remove(client_primary);

        Iterator<BaseClientProxy> iterator=clients_2_rm.iterator();
        while (iterator.hasNext()){
            BaseClientProxy baseClientProxy=iterator.next();
            clientProxy.close();
            if(Clients_set.contains(clientProxy)){
                EventQueue.getInstance().removeHandlers();

                Clients_list.remove(clientProxy.getname());
                Clients_set.remove(clientProxy);
            }
        }

    }
    private int getActiveprimarySides(){
        int sides=0;
        if(!isLockedToScreenServer()){
            if(hasAnyNeighbor(m_clientprimary,EDirection.kLeft))
                sides |= EDirectionmask.kLeftMask.value;
            if(hasAnyNeighbor(m_clientprimary,EDirection.kRight))
                sides |= EDirectionmask.kRightMask.value;
            if(hasAnyNeighbor(m_clientprimary,EDirection.kTop))
                sides |= EDirectionmask.kTopMask.value;
            if(hasAnyNeighbor(m_clientprimary,EDirection.kBottom))
                sides |= EDirectionmask.kBottomMask.value;
        }
        return sides;
    }
    private boolean isLockedScreen(){
        if(isLockedToScreenServer)
            return true;
        if(m_clientprimary.isLockedToscreen())
            return true;
        return false;
    }
    private short getJumpZoneSize(ScreenInterface screen){
            return screen.getJumpZonesize();
    }
    private void switchScreen(ClientProxy dst,short x,short y,boolean screensaver){
        assert (null!=dst);
        assert (null!=m_active);
        stopSwitch();
        m_x=x;
        m_y=y;
        m_xDelta=0;
        m_yDelta=0;
        if(m_active!=dst){
            if(!m_active.leave()){
                return;
            }

            if(m_active==m_clientprimary){
                //todo
            }

            m_active=dst;
            ++m_seqNum;
            m_active.enter(x,y,,screensaver);
        }else{
            m_active.mouseMove(x,y);
        }

    }
    private void jumpScreen(ClientProxy newscreen){
        assert (null!=newscreen);
        m_active.setjumpCursorPos(m_x,m_y);

        Point point= newscreen.getJumpCursorPos();
        switchScreen(newscreen,(short) point.x,(short) point.y,false);
    }
    private int getCorner(ClientProxy client,short x,short y,short size){
        assert (null!=client);
        client.getshape();
        short xside,yside;
        // check for x,y on the left or right
        if(x<=ax)
            xside=-1;
        else if(x>=ax+aw-1)
            xside=1;
        else
            xside=0;
        // check for x,y on the top or bottom
        if(y<=ay)
            yside=-1;
        else if(y>=ay+ah-1)
            yside=1;
        else
            yside=0;
        // if against the left or right then check if y is within size
        if (xside != 0) {
            if (y < ay + size) {
                return (xside < 0) ? EScreenSwitchCornerMasks.kTopLeftMask.val : EScreenSwitchCornerMasks.kTopRightMask.val;
            }
            else if (y >= ay + ah - size) {
                return (xside < 0) ? EScreenSwitchCornerMasks.kBottomLeftMask.val : EScreenSwitchCornerMasks.kBottomRightMask.val;
            }
        }

        // if against the left or right then check if y is within size
        if (yside != 0) {
            if (x < ax + size) {
                return (yside < 0) ? EScreenSwitchCornerMasks.kTopLeftMask.val : EScreenSwitchCornerMasks.kBottomLeftMask.val;
            }
            else if (x >= ax + aw - size) {
                return (yside < 0) ? EScreenSwitchCornerMasks.kTopRightMask.val : EScreenSwitchCornerMasks.kBottomRightMask.val;
            }
        }

        return EScreenSwitchCornerMasks.kNoCornerMask.val;
    }

 */

//    public void enter(){}
//    public void leave(){}
//    public void keyUp(){}
//    public void keyDown(){}
//    public void keyRepeat(){}
//    public void mouseDown(){}
//    public void mouseUp(){}
//    public void mouseWheel(){}
//    public void resetoptions(){}
//    public void setoptions(){}
//    public void screenSaver(){}
//    public void grabClipboard(){}
//    public void setClipboard(){}

}
