package client;

import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.UIManager;

public class ClientTest extends JFrame {

	public static void main(String[] args) {
		
		UIManager.put("OptionPane.okButtonText", "Connect"); // changes the OK button to Connect button
		String ip = JOptionPane.showInputDialog("Enter server's IP address"); // input dialog window to enter the IP address
		Client application = new Client(ip); // create new client application
	} // end main
} // end class test