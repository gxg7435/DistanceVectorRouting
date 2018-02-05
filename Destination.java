/**
* This program aims at implementing distance vector protocol using RIPv2 
* Protocol. The maximum hop count allowed is 15.Hop count of 16 is
* equivalent to infinity.
*
* @author  Gaurav Gaur(gxg7435@g.rit.edu)
* @version 1.0
* @since   10/22/2017
*/
import java.io.Serializable;

/**
 * This class is used to store network address,next IP address and hop count.
 * @author gaurav
 *
 */
public class Destination implements Serializable {
	
	private static final long serialVersionUID = 1L;
	String destinationIP, nextIP;
	int hopCount,portno;
	
	/**
	 * Constructor that initialize the member variables for the class.
	 * 
	 * @param destinationIP : network address
	 * @param nextIP : IP address of the next neighbouring router.
	 * @param hopCount : hop count for the link.
	 */
	public Destination(String destinationIP, String nextIP, int hopCount) {
		this.destinationIP = destinationIP;
		this.nextIP = nextIP;
		this.hopCount = hopCount;
	}
	/**
	 * getter for the port number
	 * @return : port number
	 */
	public int getportno() {
		return portno;
	}
	
	/**
	 * getter for the network address.
	 * @return : network address
	 */
	public String getDestinationIP() {
		return destinationIP;
	}
	
	/**
	 * setter for the network address.
	 * @param destinationIP : network address
	 */
	public void setDestinationIP(String destinationIP) {
		this.destinationIP = destinationIP;
	}
	
	/**
	 * Setter for port number.
	 * @param portno : port number.
	 */
	public void setportno(int portno) {
		this.portno = portno;
	}
	
	/**
	 * getter for the IP of next neighbouring router.
	 * @return IP address of next neighbouring router.
	 */
	public String getNextIP() {
		return nextIP;
	}

	/**
	 * setter for the IP of next neighbouring router. 
	 * @param nextIP : IP address of next neighbouring router.
	 */
	public void setNextIP(String nextIP) {
		this.nextIP = nextIP;
	}

	/**
	 * getter for the hop count
	 * @return : hop count
	 */
	public int getHopCount() {
		return hopCount;
	}

	/**
	 * setter for the hop count
	 * @param hopCount : hop count
	 */
	public void setHopCount(int hopCount) {
		this.hopCount = hopCount;
	}

	/**
	 * Used to display all properties of class object
	 */
	public String toString() {
		return "Destination Address= " + getDestinationIP() + "Next Hop= " + getNextIP() + "Hop Count= "
				+ getHopCount();
	}
}
