package org.synergy.server;

import org.synergy.base.Event;
import org.synergy.base.EventJobInterface;
import org.synergy.base.EventQueue;
import org.synergy.base.EventQueueTimer;
import org.synergy.base.EventType;
import org.synergy.base.Log;
import org.synergy.client.ServerProxy;
import org.synergy.io.Stream;
import org.synergy.io.msgs.ClipboardDataMessage;
import org.synergy.io.msgs.ClipboardMessage;
import org.synergy.io.msgs.CloseMessage;
import org.synergy.io.msgs.EnterMessage;
import org.synergy.io.msgs.InfoAckMessage;
import org.synergy.io.msgs.InfoMessage;
import org.synergy.io.msgs.KeepAliveMessage;
import org.synergy.io.msgs.KeyDownMessage;
import org.synergy.io.msgs.KeyRepeatMessage;
import org.synergy.io.msgs.KeyUpMessage;
import org.synergy.io.msgs.LeaveMessage;
import org.synergy.io.msgs.MessageHeader;
import org.synergy.io.msgs.MessageType;
import org.synergy.io.msgs.MouseDownMessage;
import org.synergy.io.msgs.MouseMoveMessage;
import org.synergy.io.msgs.MouseRelMoveMessage;
import org.synergy.io.msgs.MouseUpMessage;
import org.synergy.io.msgs.MouseWheelMessage;
import org.synergy.io.msgs.NoOpMessage;
import org.synergy.io.msgs.QueryInfoMessage;
import org.synergy.io.msgs.ResetOptionsMessage;
import org.synergy.io.msgs.ScreenSaverMessage;
import org.synergy.io.msgs.SetOptionsMessage;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class ClientProxy extends BaseClientProxy {
    public class ClientInfo{
        /*!
        The position of the upper-left corner of the screen.  This is
        typically 0,0.
        */
        public short       m_x,m_y;
        /*!
        The size of the screen in pixels.
        */
        public short       m_w, m_h;
        /*!
      The current location of the mouse cursor.
      */
        public short        m_mx, m_my;
        //! Obsolete (jump zone size)
        public short          obsolete1;
    }
    private Stream m_stream;
    private Server m_server;

    private EventQueueTimer m_keepAliveTimer;
   //private EventQueueTimer m_heartbeatTimer;
    private final double m_keepAliveRate=3.0;
    private double m_heartbeatAlarm;
    private final double HeartBeatsUntilDeath=3.0;

    private DataOutputStream dout;
    private DataInputStream din;
    private Parser parser;
    public  ClientInfo Client_info=new ClientInfo();
    private boolean HandshakeComplete=false;
    private final byte s_clipboardEnd=2;

    // To define what should parse and process messages
    private interface Parser{
        public boolean parse()throws IOException;
    }

    public ClientProxy(String clientname,Server theserver,Stream stream){
    super(clientname);
    assert (null !=theserver);
    assert (null!=stream);
    m_server=theserver;

    m_stream=stream;
    installStreamHandlers();

    Log.debug("querying client "+super.screen_name);
    try{
    QueryInfoMessage queryInfoMessage=new QueryInfoMessage();
    dout=new DataOutputStream(m_stream.getOutputStream());
    queryInfoMessage.write(dout);
    }
    catch (Exception e){
        Log.debug("clientproxy construct error  ");
        e.printStackTrace();
    }

    parser=new Parser() {
        @Override
        public boolean parse() throws IOException {
            return parseHandshakeMessage();
        }
    };

      EventQueue.getInstance().addEvent(new Event(EventType.STREAM_INPUT_READY,m_stream.getEventTarget()));
    }

    /*
    ////////////    clientproxy construction in server part
     */
    public void handleData(){
        Log.debug ("server handle data called");
        try {
            this.din = new DataInputStream (m_stream.getInputStream ());
            this.dout = new DataOutputStream (m_stream.getOutputStream ());

            while (true) {
                if ( !parser.parse ()) {
                    Log.error ("invalid message from client");
                }
            }
        } catch (IOException e) {
            e.printStackTrace ();
            // TODO
        }

    }
    /**
     * Handle messages before handshake is complete
     */
    protected boolean parseHandshakeMessage () throws IOException {
        // Read the header
        MessageHeader header = new MessageHeader (din);
        //Log.debug ("Received Header: " + header);
        switch (header.getType ()) {
            case CNOOP:
                //todo dont need
                NoOpMessage cnop=new NoOpMessage();
                cnop.write(dout);
                break;
            case DINFO:
                parser=new Parser() {
                    @Override
                    public boolean parse() throws IOException {
                        return parseMessage();
                    }
                };

                if(recvInfo()) {
                    EventQueue.getInstance().addEvent(new Event(EventType.CLIENTPROXY_READY,this));
                    HandshakeComplete=true;

                    addhertBeatTimer();
                    //writeKeepAlive();
                }
                break;
//            case DSETOPTIONS:
//                SetOptionsMessage setOptionsMessage = new SetOptionsMessage (header, din);
//
//                setOptions (setOptionsMessage);
//
//                // handshake is complete
//                Log.debug ("Handshake is complete");
//                parser = new Parser() {
//                    public Result parse () throws IOException {
//                        return parseMessage ();
//                    }
//                };
//
//                client.handshakeComplete ();
//                break;
//            case CRESETOPTIONS:
//                resetOptions (new ResetOptionsMessage(din));
//                break;
            default:
                return false;
        }

        return true;
    }

    /**
     * Handle messages after the handshake is complete
     */
    byte[] messageDataBuffer = new byte[256];
    protected boolean parseMessage () throws IOException {
        // Read the header
        MessageHeader header = new MessageHeader (din);

        // NOTE: as this is currently designed an improperly consumed message
        // will break the handling of the next message,  The message data should
        // be fully read into a buffer and that buffer passed into the message
        // for handling...

        switch (header.getType ()) {
//            case DMOUSEMOVE:
//                // Cut right to the chase with mouse movements since
//                //  they are the most abundant
//                short ax = din.readShort ();
//                short ay = din.readShort ();
//                client.mouseMove (ax, ay);
//                break;
//
//            case DMOUSERELMOVE:
//                short rx = din.readShort ();
//                short ry = din.readShort ();
//                client.relMouseMove(rx, ry);
//                break;
//
//            case DMOUSEWHEEL:
//                mouseWheel (new MouseWheelMessage(din));
//                break;
//
//            case DKEYDOWN:
//                keyDown (new KeyDownMessage(din));
//                break;
//
//            case DKEYUP:
//                keyUp (new KeyUpMessage(din));
//                break;
//
//            case DKEYREPEAT:
//                keyRepeat (new KeyRepeatMessage(din));
//                break;
//
//            case DMOUSEDOWN:
//                mouseDown (new MouseDownMessage(din));
//                break;
//
//            case DMOUSEUP:
//                mouseUp (new MouseUpMessage(din));
//                break;
//
//            case CKEEPALIVE:
//                // echo keep alives and reset alarm
//                new KeepAliveMessage().write (dout);
//                resetKeepAliveAlarm ();
//                break;
//
//            case CNOOP:
//                // accept and discard no-op
//                break;
//
//            case CENTER:
//                enter (new EnterMessage(header, din));
//                break;
//
//            case CLEAVE:
//                leave (new LeaveMessage(din));
//                break;
//
//            case CCLIPBOARD:
//                grabClipboard (new ClipboardMessage(din));
//                break;
//
//            case CSCREENSAVER:
//                byte screenSaverOnFlag = din.readByte();
//                screensaver (new ScreenSaverMessage(din, screenSaverOnFlag));
//                break;
//
//            case QINFO:
//                queryInfo ();
//                break;
//
//            case CINFOACK:
//                //infoAcknowledgment (new InfoAckMessage (din));
//                infoAcknowledgment ();
//
//                break;
//
//            case DCLIPBOARD:
//                setClipboard (new ClipboardDataMessage(header, din));
//                break;
//
//            case CRESETOPTIONS:
//                resetOptions (new ResetOptionsMessage (din));
//                break;
//
//            case DSETOPTIONS:
//                SetOptionsMessage setOptionsMessage = new SetOptionsMessage (header, din);
//                setOptions (setOptionsMessage);
//                break;
//
//            case CCLOSE:
//                // server wants us to hangup
//                Log.debug1 ("recv close");
//                // client.disconnect (null);
//                return Result.DISCONNECT;
//
//            case EBAD:
//                Log.error ("client disconnected due to a protocol error");
//                // client.disconnect("server reported a protocol error");
//                return Result.DISCONNECT;
            case DINFO:
                if(recvInfo()){
                    EventQueue.getInstance().addEvent(new Event(EventType.SHAPE_CHANGED,this));
                    return true;
                }else {
                    return false;
                }
            case CNOOP:break;
            case CCLIPBOARD:
                return recvGrabClipboard(header);
            case DCLIPBOARD:
                return recvClipboard();
            case CKEEPALIVE:
                writeKeepAlive();
                return true;
            default:
                return false;
        }

        return false;

    }

    private void addhertBeatTimer(){
        m_heartbeatAlarm=HeartBeatsUntilDeath*m_keepAliveRate;
        Log.debug("m_heartbeatAlarm :"+m_heartbeatAlarm);
        if(null!=m_keepAliveTimer){
            m_keepAliveTimer.cancel();
            m_keepAliveTimer=null;
        }
        if(0.0<m_heartbeatAlarm){
            m_keepAliveTimer=new EventQueueTimer(m_heartbeatAlarm, true, this, new EventJobInterface() {
                @Override
                public void run(Event event) {
                    handleKeepAliveAlarm();
                }
            });
        }
    }
    private void writeKeepAlive() throws IOException {
        addhertBeatTimer();
        KeepAliveMessage keepalive=new KeepAliveMessage();
        keepalive.write(dout);
    }
    private boolean recvInfo() throws IOException {

        short x,y,w,h,unknown,cursor_x,cursor_y;
        try{
            //this.din=new DataInputStream(m_stream.getInputStream());
            InfoMessage info=new InfoMessage(this.din);

            Client_info.m_x=x= info.getScreenX();
            Client_info.m_y=y= info.getCursorY();
            Client_info.m_w=w= info.getScreenWidth();
            Client_info.m_h=h= info.getScreenHeight();
            unknown= info.getUnknown();
            Client_info.m_mx=cursor_x= info.getCursorX();
            Client_info.m_my=cursor_y= info.getCursorY();
            Log.debug("receive client "+ info.toString());
            if (w <= 0 || h <= 0) {
                return false;
            }
            if (cursor_x < x || cursor_x >= x + w || cursor_y < y || cursor_y >= y + h) {
                cursor_x = (short) (x + w/2);
                cursor_y = (short) (y + h/2);
            }
        }catch (Exception e )
        {
            Log.debug("dinfo parse failed ");
            e.printStackTrace();
            return false;}

        Log.debug("send info ack to "+super.screen_name);
        InfoAckMessage infoack=new InfoAckMessage();
        infoack.write(dout);
        return true;
    }
    private boolean recvGrabClipboard(MessageHeader messageHeader){
        byte id;
        int seqNum;
        try{
            ClipboardDataMessage clipboardData=new ClipboardDataMessage(messageHeader,din);
            id=clipboardData.getId();
            seqNum=clipboardData.getSequenceNumber();
        }catch (IOException e){
            e.printStackTrace();
            return false;
        }
        Log.debug("receive client "+super.screen_name+" clipboard "+id+" ,seqnum "+seqNum);
        if(id>=s_clipboardEnd)
            return false;

        return true;
    }
    private boolean recvClipboard(){
        // todo
        return false;
    }

    public void close(){
        assert (m_stream != null);
        Log.debug("send close to "+super.screen_name);

        try{
            dout = new DataOutputStream (m_stream.getOutputStream ());
            CloseMessage closeMessage=new CloseMessage();
            closeMessage.write(dout);

        }catch (Exception e){
            e.printStackTrace();
        }

        /*
        EventQueue.getInstance().adoptHandler(
                EventType.STREAM_INPUT_READY, stream.getEventTarget(),
                new EventJobInterface() {
                    @Override
                    public void run(Event event) { handleclose(); }
                }
        );
        */

    }
    public void disconnect(){
        cleanHandlers();
        EventQueue.getInstance().addEvent(new Event(EventType.CLIENTPROXY_DISCONNECTED,this));
    }
    private void handleDisconnect(){
        Log.debug("client "+super.screen_name+" has disconnected ");
        disconnect();
   }
    private void handleKeepAliveAlarm(){
        Log.debug("client "+super.screen_name+" is dead ");
       //Log.debug("error writing to client "+super.screen_name);
       disconnect();
   }
    private void handleWriteError(){
       Log.debug("client "+super.screen_name+" is dead ");
       disconnect();
   }
    private void installStreamHandlers(){
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
       EventQueue.getInstance().adoptHandler(EventType.TIMER, this, new EventJobInterface() {
           @Override
           public void run(Event event) {
               handleKeepAliveAlarm();
           }
       });

   }
    public void cleanHandlers(){
       EventQueue.getInstance().removeHandler(EventType.STREAM_INPUT_READY, m_stream.getEventTarget());
       EventQueue.getInstance().removeHandler(EventType.STREAM_OUTPUT_ERROR, m_stream.getEventTarget());
       EventQueue.getInstance().removeHandler(EventType.STREAM_INPUT_SHUTDOWN, m_stream.getEventTarget());
       EventQueue.getInstance().removeHandler(EventType.STREAM_OUTPUT_SHUTDOWN, m_stream.getEventTarget());
       EventQueue.getInstance().removeHandler(EventType.TIMER,this);
       if(null!=m_keepAliveTimer){
           m_keepAliveTimer=null;
       }
   }

   /*
   ////////   clientproxy(read from client) will call  method (actual in server )
    */
    //todo
    public boolean leave()throws IOException{
        Log.debug("send leave to"+super.screen_name);
        LeaveMessage leave=new LeaveMessage();
        dout = new DataOutputStream (m_stream.getOutputStream ());
        leave.write(dout);
        return true;
    }
    public void enter(short xAbs,short yAbs,int seqNum,short mask)throws IOException{
        Log.debug("send enter to "+super.screen_name);
        EnterMessage enter=new EnterMessage(xAbs,yAbs,seqNum,mask);
        dout = new DataOutputStream (m_stream.getOutputStream ());
        enter.write(dout);
    }
    public void keyUp(int keyid,int keymask,int keybutton)throws IOException{
        Log.debug("send key up to"+super.screen_name);
        KeyUpMessage keyup=new KeyUpMessage(keyid,keymask,keybutton);
        dout = new DataOutputStream (m_stream.getOutputStream ());
        keyup.write(dout);
    }
    public void keyDown(int keyid,int mask,int keybutton,String lang) throws IOException {
        Log.debug("send key down to "+super.screen_name);
        KeyDownMessage keydown=new KeyDownMessage(keyid,mask,keybutton,lang);
        dout = new DataOutputStream (m_stream.getOutputStream ());
        keydown.write(dout);
    }
    public void keyReapeat(short keyid,short mask,short count,short keybutton,String lang) throws IOException {
        Log.debug("send key repeat to "+super.screen_name);
        KeyRepeatMessage keyrepeat=new KeyRepeatMessage(keyid,mask,count,keybutton,lang);
        dout = new DataOutputStream (m_stream.getOutputStream ());
        keyrepeat.write(dout);
    }
    public void mouseUp(byte button)throws IOException{
        Log.debug("send mouse up to"+super.screen_name);
        MouseUpMessage mouseup=new MouseUpMessage(button);
        dout = new DataOutputStream (m_stream.getOutputStream ());
        mouseup.write(dout);
    }
    public void mouseDown(byte button)throws IOException{
        Log.debug("send mouse down to"+super.screen_name);
        MouseDownMessage mousedown=new MouseDownMessage(button);
        dout = new DataOutputStream (m_stream.getOutputStream ());
        mousedown.write(dout);
    }
    public void mouseMove(short xabs,short yabs)throws IOException{
        Log.debug("send mouse move to"+super.screen_name);
        MouseMoveMessage mousemove=new MouseMoveMessage(xabs,yabs);
        dout = new DataOutputStream (m_stream.getOutputStream ());
        mousemove.write(dout);
    }
    public void mouseWheel(short xDelta,short yDelta) throws IOException {
        Log.debug("send mouse wheel to "+super.screen_name);
        MouseWheelMessage mousewheel=new MouseWheelMessage(xDelta,yDelta);
        dout = new DataOutputStream (m_stream.getOutputStream ());
        mousewheel.write(dout);
    }
    public void relMouseMove(short xRel,short yRel) throws IOException {
        Log.debug("send mouse relative move to "+super.screen_name);
        MouseRelMoveMessage mouseRelMove=new MouseRelMoveMessage(xRel,yRel);
        dout = new DataOutputStream (m_stream.getOutputStream ());
        mouseRelMove.write(dout);
    }


}
