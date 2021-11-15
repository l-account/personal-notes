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

/**
 * For more information on the Synergy message format, see 
 * http://synergy-foss.org/code/filedetails.php?repname=synergy&path=%2Ftrunk%2Fsrc%2Flib%2Fsynergy%2FProtocolTypes.cpp
 */
public enum MessageType {
    HELLO ("Synergy", "[Init] Hello"),           // Not a standard message
    HELLOBACK ("Synergy", "[Init] Hello Back"),  // Not a standard message
    CNOOP ("CNOP", "[Command] NoOp"),
    CCLOSE ("CBYE", "[Command] Close"),
    CENTER ("CINN", "[Command] Enter"),
    CLEAVE ("COUT", "[Command] Leave"),
    CCLIPBOARD ("CCLP", "[Command] Clipboard"),
    CSCREENSAVER ("CSEC", "[Command] Screensaver"),
    CRESETOPTIONS ("CROP", "[Command] Reset Options"),
    CINFOACK ("CIAK", "[Command] Info Ack"),
    CKEEPALIVE ("CALV", "[Command] Keep Alive"),
    DKEYDOWN ("DKDN", "[Data] Key Down"),
    DKEYREPEAT ("DKRP", "[Data] Key Repeat"),
    DKEYUP ("DKUP", "[Data] Key Up"),
    DMOUSEDOWN ("DMDN", "[Data] Mouse Down"),
    DMOUSEUP ("DMUP", "[Data] Mouse Up"),
    DMOUSEMOVE ("DMMV", "[Data] Mouse Move"),
    DMOUSERELMOVE ("DMRM", "[Data] Mouse Relative Move"),
    DMOUSEWHEEL ("DMWM", "[Data] Mouse Wheel"),
    DCLIPBOARD ("DCLP", "[Data] Clipboard"),
    DINFO ("DINF", "[Data] Info"),
    DSETOPTIONS ("DSOP", "[Data] Set Options"),
    QINFO ("QINF", "[Query] Info"),
    EINCOMPATIBLE ("EICV", "[Error] Incompatible"),
    EBUSY ("EBSY", "[Error] Busy"),
    EUNKNOWN ("EUNK", "[Error] Unknown"),
    EBAD ("EBAD", "[Error] Bad");
    private MessageType (String value, String commonName) {
        this.value = value;
        this.commonName = commonName;
    }

    public static MessageType fromString (String messageValue) {
        for (MessageType t : MessageType.values ()) {
            if (messageValue.equalsIgnoreCase (t.value)) {
                return t;
            }
        }
        throw new IllegalArgumentException ("No MessageType with value " + messageValue);

    }

    public String getValue () {
        return value;
    }

    public String toString () {
        return commonName;
    }

    private String value;
    private String commonName;
}
/*
 /////Format specifiers are:
    - \%\%   -- literal `\%'
    - \%1i  -- converts integer argument to 1 byte integer
    - \%2i  -- converts integer argument to 2 byte integer in NBO
    - \%4i  -- converts integer argument to 4 byte integer in NBO
    - \%1I  -- converts std::vector<UInt8>* to 1 byte integers
    - \%2I  -- converts std::vector<UInt16>* to 2 byte integers in NBO
    - \%4I  -- converts std::vector<UInt32>* to 4 byte integers in NBO
    - \%s   -- converts String* to stream of bytes
    - \%S   -- converts integer N and const UInt8* to stream of N bytes
  ///////Server protocol_types,
const char* const               kMsgHello            = "Synergy%2i%2i%s";
const char* const               kMsgHelloBack        = "Synergy%2i%2i%s%s";
const char* const               kMsgCNoop             = "CNOP";
const char* const               kMsgCClose             = "CBYE";
const char* const               kMsgCEnter             = "CINN%2i%2i%4i%2i";
const char* const               kMsgCLeave             = "COUT";
const char* const               kMsgCClipboard         = "CCLP%1i%4i";
const char* const               kMsgCScreenSaver     = "CSEC%1i";
const char* const               kMsgCResetOptions    = "CROP";
const char* const               kMsgCInfoAck        = "CIAK";
const char* const               kMsgCKeepAlive        = "CALV";
const char* const               kMsgDKeyDown        = "DKDN%2i%2i%2i%s";
const char* const               kMsgDKeyDown1_0        = "DKDN%2i%2i";
const char* const               kMsgDKeyRepeat        = "DKRP%2i%2i%2i%2i%s";
const char* const               kMsgDKeyRepeat1_0    = "DKRP%2i%2i%2i";
const char* const               kMsgDKeyUp            = "DKUP%2i%2i%2i";
const char* const               kMsgDKeyUp1_0        = "DKUP%2i%2i";
const char* const               kMsgDMouseDown        = "DMDN%1i";
const char* const               kMsgDMouseUp        = "DMUP%1i";
const char* const               kMsgDMouseMove        = "DMMV%2i%2i";
const char* const               kMsgDMouseRelMove    = "DMRM%2i%2i";
const char* const               kMsgDMouseWheel        = "DMWM%2i%2i";
const char* const               kMsgDMouseWheel1_0    = "DMWM%2i";
const char* const               kMsgDClipboard        = "DCLP%1i%4i%1i%s";
const char* const               kMsgDInfo            = "DINF%2i%2i%2i%2i%2i%2i%2i";
const char* const               kMsgDSetOptions        = "DSOP%4I";
const char* const               kMsgDFileTransfer    = "DFTR%1i%s";
const char* const               kMsgDDragInfo        = "DDRG%2i%s";
const char* const               kMsgDSecureInputNotification = "SECN%s";
const char* const               kMsgQInfo            = "QINF";
const char* const               kMsgEIncompatible    = "EICV%2i%2i";
const char* const               kMsgEBusy             = "EBSY";
const char* const               kMsgEUnknown        = "EUNK";
const char* const               kMsgEBad            = "EBAD";
 */