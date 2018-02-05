/**
* This program aims at implementing distance vector protocol using RIPv2 
* Protocol. The maximum hop count allowed is 15.Hop count of 16 is
* equivalent to infinity.
*
* @author  Gaurav Gaur(gxg7435@g.rit.edu)
* @version 1.0
* @since   10/22/2017
*/
import java.io.*;
import java.net.*;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

/**
 * This class acts as a client side of the program.
 * A single thread is used for sending the data to
 * different routers. 
 * @author gaurav
 *
 */
class UDPClient extends Thread {
	int id;
	int clientPort;
	static List<String> currentNetwork = new ArrayList<String>();
	ConcurrentHashMap<String, Destination> routingTable = new ConcurrentHashMap<String, Destination>();
	String filename = "";

	List<Integer> clientPorts = new ArrayList<Integer>();
	List<Integer> clientPorts1 = new ArrayList<Integer>();
	List<String> serverIP = new ArrayList<String>();
	List<String> clientIP = new ArrayList<String>();
	
	/**
	 * Constructor for the client thread of the program.
	 * 
	 * @param id : to identify the thread
	 * @param clientPorts : List of the client ports
	 * @param routingTable2 : Routing table that is static between server and client.
	 * @param filename :name of the config file along with path.
	 */
	public UDPClient(int id, List<Integer> clientPorts, ConcurrentHashMap<String, Destination> routingTable2,
			String filename) {
		this.id = id;
		this.clientPorts1 = clientPorts;
		this.routingTable = routingTable2;
		this.filename = filename;
	}

	/**
	 * This method is used to read the config file and fetch ip addresses,port number
	 * and network address.
	 * @param filename : name of the config file along with path.
	 */
	public void ReadFile(String filename) {

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

	/**
	 * This method is implemented to send the network address to its neighbours.
	 * if there is change in routing table , it will send each entry of its 
	 * routing table to its neighbours.
	 * 
	 * @throws InterruptedException
	 * @throws IOException
	 */
	public void clientCode() throws InterruptedException, IOException {

		DatagramSocket clientSocket = new DatagramSocket();
		InetAddress IPAddress;
		byte[] data;
		DatagramPacket sendPacket;

		while (true) {
			for (int i = 0; i < clientPorts.size(); i++) {
				Thread.sleep(5000);
				IPAddress = InetAddress.getByName(clientIP.get(i));
				for (int j = 0; j < currentNetwork.size(); j++) {

					String sentence = currentNetwork.get(j);
					data = CreateByteArray(clientIP.get(i) + ":" + clientPorts.get(i), sentence, 0);

					sendPacket = new DatagramPacket(data, data.length, IPAddress, clientPorts.get(i));
					clientSocket.send(sendPacket);
				}
				String destinIP = null;
				int hop = 0;
				for (Destination entry : routingTable.values()) {

					destinIP = entry.getDestinationIP();
					hop = entry.getHopCount();

					data = CreateByteArray(entry.getNextIP() + ":" + entry.getportno(), destinIP, hop);

					sendPacket = new DatagramPacket(data, data.length, IPAddress, clientPorts.get(i));
					clientSocket.send(sendPacket);

				}
			}
		}
	}
	
	/**
	 * This method is created to byte array according to rip v2 format.
	 * 
	 * @param ipAddrWhole : ip address of the receiver and its port number.
	 * @param netAddr : network address as identified from the config file.
	 * @param hop : hop count to be sent.
	 * @return
	 */
	public byte[] CreateByteArray(String ipAddrWhole, String netAddr, int hop) {
		byte[] output = new byte[1024];
		int count = 0;
	
		for (int i = 0; i < 8; i++) {
			if(i == 0 || i == 1 || i == 5) {
				output[i] = 2;
			}
			else
				output[i] = 0;
			count++;
		}
				
		String[] tempclassIP = netAddr.split("/");

		String[] finalclassIP = (tempclassIP[0]).split("\\.");

		for (int i = 0; i < finalclassIP.length; i++) {
			output[count] = (byte) Integer.parseInt(finalclassIP[i]);

			count++;
		}

		String netmask = findnetmask(netAddr);
		String[] classIP2 = netmask.split("\\.");

		for (int i = 0; i < classIP2.length; i++) {
			output[count] = (byte) Integer.parseInt(classIP2[i]);
			count++;
		}

		String[] ipAddr1 = ipAddrWhole.split(":");
		String portno = ipAddr1[1];
		String[] classIP3 = ipAddr1[0].split("\\.");

		for (int i = 0; i < classIP3.length; i++) {
			output[count] = (byte) Integer.parseInt(classIP3[i]);
			count++;
		}

		for (int i = 0; i < 4; i++) {
			output[count] = 0;
			if (i == 3)
				output[count] = (byte) hop;
			count++;
		}
		int len = portno.length();
		output[count] = (byte) len;
		count++;

		for (int i = 0; i < len; i++) {
			output[count] = (byte) (Integer.parseInt(portno.charAt(i) + ""));
			count++;
		}
		return output;
	}
	
	/**
	 * This method is used to find the subnet mask from the network address.
	 * 
	 * @param str : network address including CIDR value.
	 * @return subnet mask for the network.
	 */
	public String findnetmask(String str) {
		final int bits = 32 - Integer.parseInt(str.substring(str.indexOf('/') + 1));
		final int mask = (bits == 32) ? 0 : 0xFFFFFFFF - ((1 << bits) - 1);
		return Integer.toString(mask >> 24 & 0xFF, 10) + "." + Integer.toString(mask >> 16 & 0xFF, 10) + "."
				+ Integer.toString(mask >> 8 & 0xFF, 10) + "." + Integer.toString(mask >> 0 & 0xFF, 10);

	}
	
	/**
	 * This method is used by the thread as soon as it starts.
	 */
	public void run() {
		ReadFile(filename);
		try {
			clientCode();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}