package client;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.Socket;
import java.net.InetAddress;
import java.io.IOException;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;
import java.util.Formatter;
import java.util.Scanner;

public class Client extends JFrame{

	private SnakesAndLaddersGUI gui; // given instance
	private JTextArea messageBox; // message box
	private JButton rollDiceButton; // button to roll the dice
	private String hostIP; // host IP for server
	private String playerNumber; // player number
	private String finalPosition = ""; // position after the logic
	private Socket connection; // connection to server
	private Scanner input; // input from server
	private Formatter output; // output to server
	private boolean myTurn = false; // my turn
	private boolean gameOver = false; // game is over
	private final static int PLAYER_1 = 0; // constant for first player
	private final static int PLAYER_2 = 1; // constant for second player
	private final static int PLAYER_3 = 2; // constant for third player
	private final static int PLAYER_4 = 3; // constant for fourth player
	private int currentPosition[]; // holds the current position of players
	private int diceNumber; // holds the dice number received from server

	public Client(String host)
	{
		super("Snake-and-Ladders Client"); // set Title of frame
		getContentPane().setLayout(null); // set layout to null
		hostIP = host; // get host address
		gui = new SnakesAndLaddersGUI(); // create new object gui of SnakesAndLaddersGUI
		gui.setBounds(10, 48, 464, 365); // set bounds
		getContentPane().add(gui); // add gui to frame

		rollDiceButton = new JButton("Throw dice and Move"); // create new button
		rollDiceButton.addActionListener(new ActionListener() { // add action listener to the button
			public void actionPerformed(ActionEvent e) {
				try 
				{
					rollDice(); // invokes method roll
				} // end try 
				catch (InterruptedException ie) // catch interrupted exception 
				{
					ie.printStackTrace(); // prints throwable to standard error stream
				} // end catch
			}
		});
		rollDiceButton.setBounds(114, 424, 258, 23); // set bounds for roll button
		getContentPane().add(rollDiceButton); // add button to frame
		
		messageBox = new JTextArea(); // create messageBox of type JTextArea
		messageBox.setBounds(10, 17, 464, 20); // set bounds for the messageBox
		getContentPane().add(messageBox); // add messageBox to frame
		messageBox.setColumns(10); // set 10 columns of messageBox

	 	messageBox.setEditable(false); // set non editable box

	 	setSize(500,490); // set size of window
      	setVisible(true); // show window
    	setResizable(false); // enable non resizable window

		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE); // // exit application when closing window
	
	
		startClient(); // start the client
	} // end client constructor


	private void displayMessage(final String message) // method to display messages on messageBox
	{
		// display message from event-dispatch thread of execution
		SwingUtilities.invokeLater(
				new Runnable()
				{
					public void run() // updates outputArea
					{
						messageBox.setText(message); // add message
					} // end method run
				} // end inner class
			); // end call to swingUtilities
	} // end method displayMessage
		
	// start the client thread
	private void startClient()
	{	
		try // connect to server, get streams and start output thread
		{
			// make connection to server
			connection = new Socket(InetAddress.getByName(hostIP),15024); // create new socket
			input = new Scanner(connection.getInputStream()); // get input stream from socket
			output = new Formatter(connection.getOutputStream()); // get output stream from socket
			currentPosition = new int[4]; // create array of 4 integers called currentPosition
			gui.setNumberOfPlayers(4); // set number of players
			for(int i = 0; i < 4; i++) // loop through each player
			{
				gui.setPosition(i,0); // set the position of each player on the board to 0
				currentPosition[i]= 0; // set currentPosition of each player to 0
			} // end for
			
			while(true)
			{
				if(input.hasNextLine()) // wait for the next message
				{	
					processMessages(input.nextLine()); // process the messages from server
				} // end if
			} // end while
			
		} // end try
		catch(IOException ioe) // catch interrupted exception
		{
			ioe.printStackTrace(); // prints throwable to standard error stream
			displayMessage("Connection not established."); // display message
		} // end catch
	} // end method startClient

	// process incoming messages
	private void processMessages(String message)
	{

			if(message.equals("Player number is")) 
			{
					playerNumber = input.nextLine(); // gets the number of player
					switch(playerNumber)
					{ // displays player number and colour of token on messageBox
						case "1": displayMessage("Player "+ playerNumber + ": Blue token");break;
						case "2": displayMessage("Player "+ playerNumber + ": Green token");break;
						case "3": displayMessage("Player "+ playerNumber + ": Red token");break;
						case "4": displayMessage("Player "+ playerNumber + ": Yellow token");break;
					} // end switch
			} // end if
			else if(message.equals("Starting the game"))
			{
				switch(playerNumber)
				{ // display player number and start the game message
					case "1": displayMessage("Player "+ playerNumber + ": Blue token - Starting the game!");break;
					case "2": displayMessage("Player "+ playerNumber + ": Green token - Starting the game!");break;
					case "3": displayMessage("Player "+ playerNumber + ": Red token - Starting the game!");break;
					case "4": displayMessage("Player "+ playerNumber + ": Yellow token - Starting the game!");break;
				} // end switch
			}
			else if(message.equals("Can roll")) // by receiving this message client can roll the dice
			{
				if(!gameOver) // can roll until game is over
				{
					myTurn = true; // set my turn to true
					switch(playerNumber)
					{ // display the turn of the player
					case "1": displayMessage("Player "+ playerNumber + ": Blue token - Its your turn");break;
					case "2": displayMessage("Player "+ playerNumber + ": Green token - Its your turn");break;
					case "3": displayMessage("Player "+ playerNumber + ": Red token - Its your turn");break;
					case "4": displayMessage("Player "+ playerNumber + ": Yellow token - Its your turn");break;
					} // end switch
				} // end if
			} // end else if
			else if(message.equals("Position of Player 1")) 
			{
				finalPosition = input.nextLine(); // get final position from server
				diceNumber = input.nextInt(); // get dice number from server
				setGUI(PLAYER_1,finalPosition,diceNumber); // invoke setGUI method
			} // end else if
			else if(message.equals("Position of Player 2")) 
			{
				finalPosition = input.nextLine(); // get final position from server
				diceNumber = input.nextInt(); // get dice number from server
				setGUI(PLAYER_2, finalPosition,diceNumber); // invoke setGUI method
			} // end else if
			else if(message.equals("Position of Player 3")) 
			{
				finalPosition = input.nextLine(); // get final position from server
				diceNumber = input.nextInt(); // get dice number from server
				setGUI(PLAYER_3,finalPosition,diceNumber); // invoke setGUI method
			} // end else if
			else if(message.equals("Position of Player 4")) 
			{
				finalPosition = input.nextLine(); // get final position from server
				diceNumber = input.nextInt(); // get dice number from server
				setGUI(PLAYER_4,finalPosition,diceNumber); // invoke setGUI method
			} // end else if
	} // end method processMessage
	
	// update GUI of players after they rolled
	private void setGUI(int player, String finalPosition, int diceNumber)
	{
		int intPosition; // initialise intPosition
		intPosition = Integer.parseInt(finalPosition); // convert from string to integer
		try
		{
			Thread.sleep(300); // wait for 300 ms
			if(intPosition > 0 && intPosition < 100)
			{
				if((currentPosition[player] + diceNumber) < 100)
				{
					for(int i = currentPosition[player]; i < currentPosition[player] + diceNumber; i++) // loop through the different positions to show the movement
					{
						gui.setPosition(player, i); // set new position i for player
						Thread.sleep(200); // wait for 200 ms
					} // end for
				} // end if
				gui.setPosition(player, intPosition); // set final position of movement
				currentPosition[player] = intPosition; // update the current position
			} // end if
			else if(intPosition == 100)
			{
				for(int i = currentPosition[player]; i < currentPosition[player] + diceNumber; i++)
				{
					gui.setPosition(player, i); // set new position i for player
					Thread.sleep(200); // wait for 200 ms
				} // end for
				gui.setPosition(player, intPosition); // set final position of movement
				displayMessage("PLAYER "+ (player + 1) +" WON! - GAME OVER"); // display the winner and game over
				gameOver = true; // set gameOver to true
				sendToServer("Game Over\n"); // send message to server
			} // end else if
		} // end try
		catch(Exception e) // catch any exception
    	{
    		e.printStackTrace(); // prints throwable to standard error stream
    	} // end catch
	} // end method setGUI
	
	private void sendToServer(String message) // send messages to server
	{
		output.format(message); // output the message to server
		output.flush(); // flush the output
	} // end method sendToServer
	
	private void rollDice() throws InterruptedException
	{
		if(myTurn) // check for my turn
		{
			switch(playerNumber)
			{ // display waiting message
				case "1": displayMessage("Player "+ playerNumber + ": Blue token - Wait for your turn");break;
				case "2": displayMessage("Player "+ playerNumber + ": Green token - Wait for your turn");break;
				case "3": displayMessage("Player "+ playerNumber + ": Red token - Wait for your turn");break;
				case "4": displayMessage("Player "+ playerNumber + ": Yellow token - Wait for your turn");break;
			} // end switch
			int intPlayerNumber = Integer.parseInt(playerNumber); // convert from string to integer 
			myTurn = false; // set my turn to false
			sendToServer("Rolling the dice\n" + intPlayerNumber + "\n"); // send rolling dice message to server
		} // end if
	} // end method rollDice
} // end class Client
