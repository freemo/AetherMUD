package com.planet_ink.coffee_mud.system.I3.packets;

import com.planet_ink.coffee_mud.system.I3.server.Server;
import java.util.Vector;

public class WhoPacket extends Packet {
    public Vector who = null;

	public WhoPacket()
	{
		super();
        type = Packet.WHO_REQUEST;
	}
	
    public WhoPacket(Vector v) throws InvalidPacketException {
        super(v);
        if( v.size() == 6 ) {
            type = Packet.WHO_REQUEST;
        }
        else {
            type = Packet.WHO_REPLY;
            who = (Vector)v.elementAt(6);
        }
    }

    public void send() throws InvalidPacketException {
        if( type == Packet.WHO_REPLY && who == null ) {
            throw new InvalidPacketException();
        }
        super.send();
    }

    public String toString() {
            String str = "({\"who-reply\",5,\"" + Server.getMudName() +
                         "\",0,\"" + target_mud + "\",\"" + target_name + "\",({";
            int i;

            for(i=0; i<who.size(); i++) {
                Vector v = (Vector)who.elementAt(i);
                String nom = (String)v.elementAt(0);
                int idle = ((Integer)v.elementAt(1)).intValue();
                String xtra = (String)v.elementAt(2);

                str += "({\"" + nom + "\"," + idle + ",\"" + xtra + "\",}),";
            }
            str += "}),})";

        return str;
    }
}
