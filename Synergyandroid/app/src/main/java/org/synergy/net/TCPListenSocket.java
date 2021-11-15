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
package org.synergy.net;

import org.synergy.base.Event;
import org.synergy.base.EventQueue;
import org.synergy.base.EventType;
import org.synergy.base.Log;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketAddress;

public class TCPListenSocket extends ListenSocketInterface {

	private ServerSocket serverSocket;

	public TCPListenSocket(){
		Log.debug("in TCPListenSocket construction");
		try{
			serverSocket=new ServerSocket();
		}catch (Exception e){
			e.printStackTrace();
		}
	}

	public TCPSocket accept () throws IOException {
		Log.debug("in TCPListenSocket accept,waiting client");
		TCPSocket client=null;
		try {
			client=new TCPSocket(serverSocket.accept());

		}catch (Exception e){
			e.printStackTrace();
		}
		if(null!=client)
			sendEvent(EventType.STREAM_INPUT_READY,client.getEventTarget());

		return client;
	}
	
	public void bind (final NetworkAddress address) throws IOException {
		Log.debug("in TCPListenSocket binding");
		serverSocket.bind(new InetSocketAddress(address.getAddress(),address.getPort()));
		sendEvent(EventType.LISTENSOCKET_CONNECTING);
	}
	
	public void close () throws IOException{
		serverSocket.close();
	}
	
	public Object getEventTarget () {
		return this;
	}
	private void sendEvent (EventType eventType) throws IOException {
		EventQueue.getInstance().addEvent(new Event(eventType, getEventTarget (), null));

	}
	private void sendEvent(EventType eventType,Object object)throws IOException{
		EventQueue.getInstance().addEvent(new Event(eventType,object,null));
	}
}
