
/**
 * This program aims at implementing distance vector protocol using RIPv2 
 * Protocol. The maximum hop count allowed is 15.Hop count of 16 is
 * equivalent to infinity.
 *
 * @author  Gaurav Gaur(gxg7435@g.rit.edu)
 * @version 1.0
 * @since   10/22/2017
 */

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;

/**
 * This class is implemented to act as a server side of the program. As per the
 * server ports identified in the config file, we have created that many
 * threads. So if a thread is connected to four other routers,it will listen on
 * four threads. One thread is used for senidng the data.
 */

class UDPServer extends Thread {
	int serverPort;
	int id;
	static ConcurrentHashMap<String, Destination> routingTable = new ConcurrentHashMap<String, Destination>();
	static int port;
	static InetAddress ip;

	public static List<Integer> serverPorts = new ArrayList<Integer>();
	public static List<Integer> clientPorts = new ArrayList<Integer>();
	public static List<String> currentNetwork = new ArrayList<String>();
	public static List<String> serverIP = new ArrayList<String>();
	public static List<String> clientIP = new ArrayList<String>();

	/**
	 * Constructor for the server thread of program.
	 * 
	 * @param id:
	 *            To identify each thread.
	 * @param serverPorts2
	 *            : port number on which process is listening.
	 * @param routingTable
	 *            : To store the routing information.
	 */

	public UDPServer(int id, int serverPorts2, ConcurrentHashMap<String, Destination> routingTable) {
		serverPort = serverPorts2;
		this.id = id;
		UDPServer.routingTable = routingTable;
	}

	/**
	 * This is the main method for the program. We have created server and client
	 * threads in this method.
	 * 
	 * @param args[0]
	 *            : the name and path of config file.
	 * @throws Exception
	 */
	public static void main(String args[]) throws Exception {
		String filename = "";
		if (args.length == 1) {
			filename = args[0];
			ReadFile(filename);

			for (int i = 0; i < serverPorts.size(); i++) {
				UDPServer t1 = new UDPServer(i, serverPorts.get(i), routingTable);
				t1.start();
			}

			UDPClient t5 = new UDPClient(1, clientPorts, routingTable, filename);
			t5.start();
		} else {
			System.out.println("Enter file name");
		}
	}

	/**
	 * This method will update the routing table for each thread process and will
	 * print the routing table whenever there is a change in routing table
	 * 
	 * @param serverSocket
	 *            : To receive the packet from client.
	 * @throws IOException
	 * @throws ClassNotFoundException
	 * @throws InterruptedException
	 */
	public void serverCode(DatagramSocket serverSocket)
			throws IOException, ClassNotFoundException, InterruptedException {
		for (int i = 0; i < currentNetwork.size(); i++) {
			Destination dest2 = new Destination(currentNetwork.get(i), "0.0.0.0", 0);
			routingTable.put(dest2.getDestinationIP(), dest2);
		}

		System.out.println("******************************************************************");
		for (Entry<String, Destination> entry : routingTable.entrySet()) {
			System.out.println((entry.getValue().destinationIP) + "		" + entry.getValue().nextIP + "		"
					+ entry.getValue().hopCount);
		}

		System.out.println("******************************************************************");
		System.out.println();
		synchronized (this) {

			while (true) {

				byte[] receiveData = new byte[1024];
				DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
				serverSocket.receive(receivePacket);

				byte[] data = receivePacket.getData();
				int portReceived = receivePacket.getPort();
				Destination dest = recievedByteArray(data, portReceived);

				int counter = 0;
				for (Destination entry : routingTable.values()) {

					if (dest.getDestinationIP().equals(entry.getDestinationIP())) {
						counter++;
					}
				}

				if (counter == 0 && dest.getHopCount() < 15) {
					dest.setHopCount(dest.getHopCount() + 1);
					routingTable.put(dest.getDestinationIP(), dest);
				}

				System.out.println("******************************************************************");
				int count = 0;
				for (Entry<String, Destination> entry : routingTable.entrySet()) {
					count = 0;
					for (int j = 0; j < currentNetwork.size(); j++) {
						if (entry.getValue().destinationIP.equals(currentNetwork.get(j))) {
							count++;
						}
					}

					if (count == 0) {
						System.out.println((entry.getValue().destinationIP) + "		" + entry.getValue().nextIP + ":"
								+ entry.getValue().getportno() + "		" + entry.getValue().hopCount);
					}

					else {
						System.out.println((entry.getValue().destinationIP) + "		" + entry.getValue().nextIP
								+ "		" + entry.getValue().hopCount);
					}

				}

				System.out.println("******************************************************************");
				System.out.println();
			}
		}

	}

	/**
	 * This method is implemented to fetch network address,next ip address, hop
	 * count and network id(based on subnet mask).
	 * 
	 * @param data:
	 *            the byte array that is received from the client.
	 * @param port:
	 *            port number for a particular entry in routing table.
	 * @return : Object of destination class that includes destination IP, next
	 *         IP,hop count and port number.
	 */
	public Destination recievedByteArray(byte[] data, int port) {

		String destinationIP = "", nextIP = "", HopCount = "", subnetMask = "";
		int hop, portno;
		for (int i = 8; i < 12; i++) {
			if (data[i] < 0 && i != 11) {
				destinationIP += (data[i] & 0xFF) + ".";
			} else if (data[i] >= 0 && i != 11) {
				destinationIP += (data[i]) + ".";
			} else if (data[i] < 0 && i == 11) {
				destinationIP += (data[i] & 0xFF);
			} else {
				destinationIP += (data[i]);
			}
		}

		for (int i = 12; i < 16; i++) {
			if (data[i] < 0 && i != 15) {
				subnetMask += (data[i] & 0xFF) + ".";
			} else if (data[i] >= 0 && i != 15) {
				subnetMask += (data[i]) + ".";
			} else if (data[i] < 0 && i == 15) {
				subnetMask += (data[i] & 0xFF);
			} else {
				subnetMask += (data[i]);
			}
		}

		String netid = findnetid(subnetMask);

		for (int i = 16; i < 20; i++) {
			if (data[i] < 0 && i != 19) {
				nextIP += (data[i] & 0xFF) + ".";
			} else if (data[i] >= 0 && i != 19) {
				nextIP += (data[i]) + ".";
			} else if (data[i] < 0 && i == 19) {
				nextIP += (data[i] & 0xFF);
			} else {
				nextIP += (data[i]);
			}
		}

		for (int i = 20; i < 24; i++) {
			HopCount += data[i];
		}
		hop = Integer.parseInt(HopCount);

		int len = data[24];
		String portStringValue = "";
		for (int i = 25; i < (25 + len); i++) {
			portStringValue += data[i];
		}
		portno = Integer.parseInt(portStringValue);

		Destination dest = new Destination(destinationIP + "/" + netid, nextIP, hop);
		dest.setportno(portno);
		return dest;
	}

	/**
	 * This method is implemented to fetch network id from subnet mask.
	 * 
	 * @param subnetMask
	 *            : subnet mask retreived from the client.
	 * @return netid for the network.
	 */
	public String findnetid(String subnetMask) {
		String[] eachclassmask = subnetMask.split("\\.");

		int count = 0;
		for (int i = 0; i < eachclassmask.length; i++) {
			count += converttobinary(eachclassmask[i]);
		}

		return (count + "");
	}

	/**
	 * This method is implemented to convert a string into binary form.
	 * 
	 * @param eachclassmask
	 *            : After splitting by dot, value of each string.
	 * @return number of 1's in the string after converting to binary.
	 */
	public int converttobinary(String eachclassmask) {
		int number = Integer.parseInt(eachclassmask);
		String result = "";
		for (int i = 8; i > 0; i--) {
			result += number % 2;
			number = number / 2;
		}
		int count = 0;
		for (int j = 0; j < result.length(); j++) {
			if (result.charAt(j) == '1')
				count++;
		}
		return count;
	}

	/**
	 * This method is used by the thread as soon as we start it.
	 */
	public void run() {
		try {
			DatagramSocket serverSocket = new DatagramSocket(serverPort);
			serverCode(serverSocket);

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * This method is used to read the config file and fetch ip addresses,port
	 * number and network address.
	 * 
	 * @param filename
	 *            : name of the config file along with path.
	 */
	public static void ReadFile(String filename) {

		final String FILENAME = filename; // "C:/Users/gaurav/eclipse-workspace/DistanceVector/src/Config1";
		BufferedReader br = null;
		FileReader fr = null;

		try {
			fr = new FileReader(FILENAME);
			br = new BufferedReader(fr);

			String sCurrentLine;

			while ((sCurrentLine = br.readLine()) != null) {
				if (sCurrentLine.startsWith("LINK")) {
					String sArray[] = sCurrentLine.split(" ");
					String ip = sArray[1].split(":")[0];
					serverIP.add(ip);
					String portno = sArray[1].split(":")[1];
					serverPorts.add(Integer.parseInt(portno));

					String ip2 = sArray[2].split(":")[0];
					clientIP.add(ip2);
					String portno2 = sArray[2].split(":")[1];
					clientPorts.add(Integer.parseInt(portno2));

				} else {
					String net[] = sCurrentLine.split(" ");
					currentNetwork.add(net[1]);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}