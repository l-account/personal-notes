/*
 * synergy -- mouse and keyboard sharing utility
 * Copyright (C) 2010 Shaun Patterson
 * Copyright (C) 2010 The Synergy Project
 * Copyright (C) 2009 The Synergy+ Project
 * Copyright (C) 2002 Chris Schoeneman
 * 
 * This package is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * found in the file COPYING that should have accompanied this file.
 * 
 * This package is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.synergy.io.msgs;

import org.synergy.base.Log;
import org.synergy.io.MessageDataInputStream;

import java.io.DataInputStream;
import java.io.IOException;

public class InfoMessage extends Message {
	private static final MessageType MESSAGE_TYPE = MessageType.DINFO;

	short screenX;
	short screenY;
	short screenWidth;
	short screenHeight;
	short unknown; // TODO: I haven't figured out what this is used for yet
	short cursorX;
	short cursorY;
	
	/*
	client construct DINFO message
	 */
    public InfoMessage (int screenX, int screenY, int screenWidth, int screenHeight,
    					 int cursorX, int cursorY) {
    	super (MESSAGE_TYPE);

    	this.screenX = (short) screenX;
    	this.screenY = (short) screenY;
    	this.screenWidth = (short) screenWidth;
    	this.screenHeight = (short) screenHeight;
    	this.unknown = 0; // TODO: see above
    	this.cursorX = (short) cursorX;
    	this.cursorY = (short) cursorY;
    }

    @Override
    protected final void writeData () throws IOException {
        dataStream.writeShort (screenX);
        dataStream.writeShort (screenY);
        dataStream.writeShort (screenWidth);
        dataStream.writeShort (screenHeight);
        dataStream.writeShort (unknown);
        dataStream.writeShort (cursorX);
        dataStream.writeShort (cursorY);
    }
    
    @Override
    public final String toString () {
    	return "InfoMessage:" +
                screenX + ":" + 
                screenY + ":" + 
                screenWidth + ":" + 
                screenHeight + ":" + 
                unknown + ":" + 
                cursorX + ":" + 
                cursorY;
    	
    }
    /*
     server read infomessage from client
     */
	public InfoMessage(DataInputStream din) throws InvalidMessageException{
		try{
			/*
			MessageDataInputStream mdin=new MessageDataInputStream(din);
			int packagesize=mdin.readInt();
			Log.debug("  info :"+packagesize);
			this.screenX=mdin.readShort();
			this.screenY = mdin.readShort();
			this.screenWidth = mdin.readShort();
			this.screenHeight = mdin.readShort();
			this.unknown = mdin.readShort();
			this.cursorX = mdin.readShort();
			this.cursorY = mdin.readShort();
			 */
			this.screenX=din.readShort();
			this.screenY = din.readShort();
			this.screenWidth = din.readShort();
			this.screenHeight = din.readShort();
			this.unknown = din.readShort();
			this.cursorX = din.readShort();
			this.cursorY = din.readShort();
		}catch (IOException e){
			throw new InvalidMessageException(e.getMessage());
		}
	}
	public short getScreenX(){return this.screenX;}
	public short getScreenY(){return this.screenY;}
	public short getScreenWidth(){return this.screenWidth;}
	public short getScreenHeight(){return this.screenHeight;}
	public short getCursorX(){return this.cursorX;}
	public short getCursorY(){return this.cursorY;}
	public short getUnknown(){return this.unknown;}
}
