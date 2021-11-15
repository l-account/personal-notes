package org.synergy.server;

import android.graphics.Point;

import org.synergy.common.screens.ScreenInterface;
import org.synergy.io.msgs.MessageType;

public class BaseClientProxy {

    protected String screen_name;
    private int m_x,m_y;

    public BaseClientProxy(){}

    public BaseClientProxy(String name){
        this.screen_name=name;
        m_x=0;
        m_y=0;
        //todo
        // setup screeninterface
    }
    public void setjumpCursorPos(int x,int y){
        m_x=x;
        m_y=y;
    }
    public Point getJumpCursorPos(){
        return new Point(m_x,m_y);
    }

    public boolean isPrimaryClient(){return false;}
    public String getname(){return screen_name;}
}
