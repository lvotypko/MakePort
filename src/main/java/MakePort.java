import java.net.InetAddress;
import java.net.UnknownHostException;

import java.util.logging.Logger;

// generate unique port number based on given IP and a mask

public class MakePort {

	private static transient Logger logger = Logger.getLogger("MakePort");

	public static void getUniquePort() {
		
		InetAddress localHost = null;
		int initPort=Integer.getInteger("init.port",45688).intValue();
		int mask=Integer.getInteger("netmask",24).intValue();
		int port=0;
		try {
			String baseIP = System.getProperty("base.addr",null); 
			if (baseIP == null) {
				localHost = InetAddress.getLocalHost();
			} else {
				localHost = InetAddress.getByName(baseIP);
			}
                        if(!localHost.getHostAddress().contains(".")) {
                                System.err.println("There is no ipv4 address");
                                return;
                        }
			logger.fine("host: " + localHost.getHostName());
			logger.fine("ip: " + localHost.getHostAddress());
			if (localHost.isLoopbackAddress())
				throw new RuntimeException("That's a loopback address! Needs to be something else.");

			byte[] localHostN = localHost.getAddress();
			int localHostOctets = localHostN.length;
			logger.fine("number of IP octets: " + localHostOctets);

			int ignoreOctets = mask / 8;
			int crossOctetBits = mask % 8;
			byte crossOctetMask = 0; // will calc later

			int i = 0;
			// this is not really needed but good for debugging
			for(i = 0; i < ignoreOctets; i++) {
				localHostN[i] = 0;
			}

			if (crossOctetBits != 0) {
				assert (crossOctetBits > 0) && (crossOctetBits < 8);

				for(i = 0; i < 8 - crossOctetBits; i++)
					crossOctetMask = (byte) (crossOctetMask<<1 | 1);
				logger.fine("number of bits to zero in the intersecting octet: " + crossOctetBits);
				logger.fine("mask to use for the operation: " + crossOctetMask);
				localHostN[ignoreOctets] = (byte) (localHostN[ignoreOctets] & crossOctetMask);
			}

			logger.fine("we need to convert this to an integer: " + InetAddress.getByAddress(localHostN).getHostAddress());

			for (i = ignoreOctets; i < localHostN.length; i++) {
				port = port<<8 | 0x000000FF & localHostN[i]; 
			}
			logger.fine("this is the converted value and we will add it to the initPort value: " + port);
			port = initPort + port;

			if ((port < 0) || (port > 0xffff))
				throw new RuntimeException("Port calculated to be '" + port + "' but that should be impossible.");

			System.out.println(port);
		} catch (Exception e) {
			e.printStackTrace();
			System.exit(1);
		}	
	}	
	
	public static void main(String[] args) {
		getUniquePort();
	}

}
