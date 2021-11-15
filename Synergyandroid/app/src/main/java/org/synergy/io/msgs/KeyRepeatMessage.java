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

public class KeyRepeatMessage extends Message {
	public static final MessageType MESSAGE_TYPE = MessageType.DKEYDOWN;
	
	private short id;
	private short mask;
	private short count;
	private short button;
	private String languageCode;
	public KeyRepeatMessage (DataInputStream din) throws IOException {
		id = din.readShort ();
		mask = din.readShort ();
		count = din.readShort ();
		button = din.readShort ();
		MessageDataInputStream mdin=new MessageDataInputStream(din);
		languageCode=mdin.readString();
	}

	public KeyRepeatMessage(short keyid,short mask,short count,short keybutton,String lang){
		super(MESSAGE_TYPE);
		this.id=keyid;
		this.mask=mask;
		this.count=count;
		this.button=keybutton;
		this.languageCode=lang;
	}
	protected final void writeData() throws IOException {
		dataStream.writeShort(id);
		dataStream.writeShort(mask);
		dataStream.writeShort(count);
		dataStream.writeShort(button);
		dataStream.writeString(languageCode);
	}
	public int getID () {
		return (int) id;
	}
	
	public int getMask () {
		return (int) mask;
	}
	
	public int getCount () { 
		return (int) count;
	}
	
	public int getButton () {
		return (int) button;
	}

	
	
	public String toString () {
		return MESSAGE_TYPE + ":" + id + ":" + mask + ":" + button;
	}
}
