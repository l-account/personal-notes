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

import org.synergy.io.MessageDataInputStream;

import java.io.DataInputStream;
import java.io.IOException;

public class KeyDownMessage extends Message {
	public static final MessageType MESSAGE_TYPE = MessageType.DKEYDOWN;
	
	private int id;
	private int mask;
	private int button;
	private String languageCode;
	
	public KeyDownMessage (DataInputStream din) throws IOException {
		id = din.readUnsignedShort ();
		mask = din.readUnsignedShort ();
		button = din.readUnsignedShort ();
		MessageDataInputStream mdin=new MessageDataInputStream(din);
		languageCode=mdin.readString();
	}

	public KeyDownMessage(int keyid,int mask,int keybutton,String lang ){
		super(MESSAGE_TYPE);
		this.id=keyid;
		this.mask=mask;
		this.button=keybutton;
		this.languageCode=lang;
	}
    protected final void writeData()throws IOException{
		dataStream.writeShort(id);
		dataStream.writeShort(mask);
		dataStream.writeShort(button);
		dataStream.writeString(languageCode);
	}
	public int getID () {
		return id;
	}
	
	public int getMask () {
		return mask;
	}
	
	public int getButton () {
		return button;
	}
	

	public String toString () {
		return MESSAGE_TYPE + ":" + id + ":" + mask + ":" + button;
	}
}
