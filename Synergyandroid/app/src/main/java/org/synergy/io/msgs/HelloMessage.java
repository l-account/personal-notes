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

import java.io.DataInputStream;
import java.io.IOException;

import org.synergy.io.MessageDataInputStream;

/**
  * This message does not have a header
  */
public class HelloMessage extends Message {
	private static final MessageType MESSAGE_TYPE=MessageType.HELLO;
    private static final int HELLO_MESSAGE_SIZE = 17;

    private int majorVersion;
    private int minorVersion;
	private String hellostring;

	// server write  construction
	public HelloMessage (int majorVersion, int minorVersion,String str) {
		super(MESSAGE_TYPE);
		this.majorVersion = majorVersion;
        this.minorVersion = minorVersion;
        this.hellostring=str;
	}

	@Override
	protected void writeData() throws IOException {
		dataStream.writeShort(majorVersion);
		dataStream.writeShort(minorVersion);
		dataStream.writeString(hellostring);
	}

	//client read construction
    public HelloMessage (DataInputStream din) throws InvalidMessageException {
        try {
        	MessageDataInputStream mdin = new MessageDataInputStream(din);



	        int packetSize = mdin.readInt ();

	        //if (packetSize != HELLO_MESSAGE_SIZE) {

	        //    throw new InvalidMessageException ("Hello message not the right size: " + packetSize);
	        //}

        	// Read in "Synergy" string
        	mdin.readExpectedString("Synergy");
        	
        	// Read in the major and minor protocol versions
        	majorVersion = mdin.readShort ();
        	minorVersion = mdin.readShort ();
        	hellostring= mdin.readString();
        } catch (IOException e) {
        	throw new InvalidMessageException (e.getMessage());
        }
    }
    
    /*@Override
	public void write(DataOutputStream dout) throws IOException {
		// TODO Auto-generated method stub
    	// Not needed for client
	}*/

	public int getMajorVersion() {
		return majorVersion;
	}

	public int getMinorVersion() {
		return minorVersion;
	}
	
	public String toString () {
		return hellostring+"HelloMessage:" + majorVersion + ":"  + minorVersion;
	}
}
