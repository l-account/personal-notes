package org.synergy.server;

import android.graphics.Point;
import android.graphics.Rect;

import org.synergy.base.EventTarget;
import org.synergy.common.screens.ScreenInterface;

public class PrimaryClient extends BaseClientProxy{
    private ScreenInterface m_screen;
    private short m_fakeInputCount;
    public PrimaryClient(String screen_name,ScreenInterface primary_screen){
        super(screen_name);
        this.m_screen=primary_screen;
        m_fakeInputCount=0;
    }
    public Point getCursorCenter(){
       return m_screen.getCursorPos();
    }
    public short getJumpZonesize(){
        return m_screen.getJumpZonesize();
    }
    public boolean isPrimaryClient(){return true;}
    public boolean isLockedToscreen(){
        return m_screen.isLockedToscreen();
    }
    public Object getEventTarget(){
        return m_screen.getEventTarget();
    }
    public Rect getShape(){
        return m_screen.getShape();
    }
    public void enable(){
        m_screen.enable();
    }
    public void disable(){
        m_screen.disable();
    }
    public void enter(short xAbs,short yAbs,int seqNum,int mask,boolean screensaver){
        m_screen.setSequenceNumber(seqNum);
        if(!screensaver){
            m_screen.warpCursor(xAbs,yAbs);
        }
        m_screen.enter(mask);
    }
    public boolean leave(){
        return m_screen.leave();
    }
    public void mousemove(short x,short y){
        m_screen.warpCursor(x,y);
    }
    public void setoptions(){
        m_screen.setoptions();
    }
    public void resetOptions(){
        m_screen.resetoptions();
    }
}
