package server;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.*;
import java.io.*;
import java.util.*;

// server class for game Snakes and Ladders
public class Server extends JFrame {

    private Player[] players; // array of players of instance Player
    private JPanel panel; // panel to hold button
    private JTextArea textArea, statusArea; // for displaying messages
    private JButton startButton; // start the game button
    private JScrollPane scroll; // scroll Pane needed to follow the textArea
    private ServerSocket server; // server socket to connect with players
    private String clientMessage; // receive messages from clients
    private final static int PLAYER_1 =  0; // constant for first player
    private final static int PLAYER_2 =  1; // constant for second player
    private final static int PLAYER_3 =  2; // constant for third player
    private final static int PLAYER_4 =  3; // constant for fourth player
    private int playersConnected = 0; // number of players connected
    private int diceNumber = 0; // dice number
    private int[] position; // position of players
    private boolean canStart = false; // Start the Game
    private boolean Turn_1 = false; // Turn of Player 1
    private boolean Turn_2 = false; // Turn of Player 2
    private boolean Turn_3 = false; // Turn of Player 3
    private boolean Turn_4 = false; //k Turn of Player 4
    private boolean gameOver = false; // game over


    // set up GUI for server
    public Server() {
        super("Snakes-And-Ladders Server"); // set title of window
        position = new int[4]; // initialising array position
        players = new Player[4]; // create array of players
        panel = new JPanel(); // create JPanel
        startButton = new JButton("Start"); // create start button
        textArea = new JTextArea(); // create JTextArea to output processing messages
        statusArea = new JTextArea(); // create JTextArea to display status(state) of the game

        setSize(300,380); // set size of window
        getContentPane().add(textArea, BorderLayout.CENTER); // add textArea to CENTER of the fame
        getContentPane().add(statusArea, BorderLayout.NORTH); // add status Area to NORTH of the frame
        getContentPane().add(panel, BorderLayout.SOUTH); // add panel to SOUTH of the frame
        panel.add(startButton, BorderLayout.SOUTH); // add start button to JFrame
        scroll = new JScrollPane(textArea, JScrollPane.VERTICAL_SCROLLBAR_ALWAYS, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER); // create scroll pane
        getContentPane().add(scroll); // add scrollPane to the frame

        startButton.addActionListener(new ActionListener() { // add action listener to listen to button clicks
            public void actionPerformed(ActionEvent e) {
                if (playersConnected > 1) { // need 2 or more players to start the game
                    if(!canStart) {// do this only once
                        displayMessage("Starting the game\n"); // display message
                        try {
                            sendToAll("Starting the game\n"); // send message to all players
                        } catch (Exception exception) { // catch any exception if error occurs
                            exception.printStackTrace(); // prints throwable to standard error stream
                        } // end catch
                        Turn_1 = true; // set Turn_1 to true
                        statusArea.setText("Status: Waiting for Player 1"); // display the message in the statusArea
                    }
                    canStart = true; // set canStart to true
                } else {
                    displayMessage("Not enough players to start\n"); // display message
                }
            } // end method actionPerformed
        }); // close action listener

        statusArea.setText("Status: Server awaiting connections"); // set status area
        setResizable(false); // set non resizable frame
        textArea.setEditable(false); // set non-editable textArea
        statusArea.setEditable(false); // set non-editable statusArea
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE); // exit application when closing window 
        setVisible(true); // show window
        try {
            server = new ServerSocket(15024,4); // set up server socket
        } catch(IOException ioException) { // catch i/o exception
            ioException.printStackTrace(); // prints throwable to standard error stream
            System.exit(1); // terminates running Java Virtual Machine
        } // end catch

        // creates player instances of each connection
        for(int i = 0; i < players.length; i++) {
            try { // wait for connection, create Player
                players[i] = new Player(server.accept(),i); // create new Player objects when accepting connections
                new Thread(players[i]).start(); // execute player runnable
                playersConnected++; // count connected players
            } catch(IOException ioException) { // catch i/o exception
                ioException.printStackTrace(); // prints throwable to standard error stream
                System.exit(1); // terminates running Java Virtual Machine
            } // end catch
        } // end for
    } // end constructor


    private void displayMessage(final String message) { // method to display messages on textArea
        // display message from event-dispatch thread of execution
        SwingUtilities.invokeLater(
                new Runnable() {
                    public void run() { // updates textArea
                        textArea.append(message); // add message
                    } // end method run
                } // end inner class
            ); // end call to swingUtilities
    } // end method displayMessage
        
    private void sendToAll(String message) { // send message to all clients
        for (int i = 0; i < 4; i++) {
            players[i].output.format(message); // send message to player[i]
            players[i].output.flush(); // flush the output
        } // end for
    } // end method sendToAll

    private void send(String message, int playerNumber) { // method to send a particular player
        players[playerNumber].output.format(message); // send message to specific player
        players[playerNumber].output.flush(); // flush the output
    } // end method send

    // private inner class Player that manages each Player
    private class Player implements Runnable {
        private int playerNumber; // player number
        private Socket connection; // connection to client
        private Scanner input; // input from client
        private Formatter output; // output to client
        private int rollingPlayer; // player who rolls
        public Player(Socket socket, int number) { // Player constructor
            playerNumber = number + 1; // number starts with 0, First player is Player 1
            connection = socket; // represents connection with client
            try { // get streams from sockets
                input = new Scanner(connection.getInputStream()); // get input stream
                output = new Formatter(connection.getOutputStream()); // get output stream
            } catch(IOException ioException) { // catch i/o exception
                ioException.printStackTrace(); // prints throwable to standard error stream
                System.exit(1); // terminates running Java Virtual Machine
            } // end catch
        } // end Player constructor

        // control thread's execution
        public void run() {
            boolean sendFlag = false; // set flag to false
                while(true) { // sends the playerNumber only once
                    if (!sendFlag) {
                        displayMessage("Player " + playerNumber + " connected\n"); // display message
                        output.format("Player number is\n" + playerNumber +"\n"); // send player's number
                        output.flush(); // flush the output
                        sendFlag = true; // set flag to true
                    }//end if
                try {	
                    if(!canStart) { // check if the game can be started
                        try {
                            Thread.sleep(1000); // sleep for 1 second
                        } catch (InterruptedException e) { // catch interrupted exception
                            e.printStackTrace(); // prints throwable to standard error stream
                        }//end catch
                        continue; // go back to the beginning of while loop
                    } // end if
                
                    // send "can roll" message a particular client depending on which turn it is
                    if(Turn_1) {
                        send("Can roll\n",PLAYER_1); // send to player 1
                        Turn_1 = false; // set turn 1 to false
                    } else if (Turn_2) {
                        send("Can roll\n",PLAYER_2); // send to player 2
                        Turn_2 = false; // set turn 2 to false
                    } else if (Turn_3) {
                        send("Can roll\n",PLAYER_3); // send to player 3
                        Turn_3 = false; // set turn 3 to false
                    } else if (Turn_4) {
                        send("Can roll\n",PLAYER_4); // send to player 4
                        Turn_4 = false; // set turn 4 to false
                    } // end else if

                    if(input.hasNextLine()) // waits until the new message is received
                        clientMessage = input.nextLine(); // get message
                    if(clientMessage.equals("Rolling the dice")) {
                        rollingPlayer = input.nextInt(); // get the next line of the message, which is the player number who is rolling
                        rollLogic(rollingPlayer); // process the rolling of the dice and send the position to the client
                    } else if(clientMessage.equals("Game Over")) {
                        gameOver = true; // set gameOver to true
                    }
                } catch(Exception e) { // catch any exception
                    e.printStackTrace(); // prints throwable to standard error stream
                } finally {
                    try { // try to close connection if the game is over
                        if(gameOver) {
                            connection.close(); // close connection to client
                            displayMessage("Player " + playerNumber + " is disconnected\n"); // display message
                            break;
                        } // end if
                    } // end try
                    catch(IOException ioe) { // catch i/o exception
                        ioe.printStackTrace(); // prints throwable to standard error stream
                        System.exit(1);  // terminates running Java Virtual Machine
                    } // end catch
                } // end finally
            } // end while
        } // run
    } // end inner class Player
    private void rollLogic(int player) {
        switch(player) { // cases for each player
        case 1: 
            Turn_2 = true; // set turn 2 to true
            statusArea.setText("Status: Waiting for Player 2"); // prints message in status area
            diceNumber = (int)(Math.random()*6) + 1; // generate random number from 1 to 6
            displayMessage("PLAYER 1 rolling the dice\n"); // display message 
            position[PLAYER_1] += diceNumber; // update position
            if(position[PLAYER_1] > 100) {
                position[PLAYER_1] -= diceNumber; // stay in the same place if the going out of the board
            }
            displayMessage("Position before logic: " + Integer.toString(position[PLAYER_1]) + "\n"); // display message
            position[PLAYER_1] = snakesLadderLogic(position[PLAYER_1]); // perform ladders and snakes transition and update position
            if(position[PLAYER_1] == 100)
                statusArea.setText("Player 1 WON - Game Over"); // display the winner in statusArea 
            sendToAll("Position of Player "+ player + "\n" + position[PLAYER_1] + "\n" + diceNumber+ "\n"); // send the position and dice number to all clients
            break;
        case 2: // control the turn of the players depending on the number of players playing
            if(playersConnected == 2) {
                Turn_1 = true; // set turn 1 to true
                statusArea.setText("Status: Waiting for Player 1"); // prints message in status area
            } else if (playersConnected == 3 || playersConnected == 4) {	
                Turn_3 = true; // set turn 3 to true
                statusArea.setText("Status: Waiting for Player 3"); // prints message in status area
            } // end else if
            diceNumber = (int)(Math.random()*6) + 1; // generate random number from 1 to 6
            displayMessage("PLAYER 2 rolling the dice\n"); // display message 
            position[PLAYER_2] += diceNumber; // update position
            if(position[PLAYER_2] > 100) {
                position[PLAYER_2] -= diceNumber; // stay in the same place if the going out of the board
            }
            displayMessage("Position before logic: "+ Integer.toString(position[PLAYER_2])+"\n"); // display message 
            position[PLAYER_2] = snakesLadderLogic(position[PLAYER_2]); // perform ladders and snakes transition and update position
            if(position[PLAYER_2] == 100)
                statusArea.setText("Player 2 WON - Game Over"); // display the winner in statusArea 
            sendToAll("Position of Player "+ player + "\n" + position[PLAYER_2] + "\n" + diceNumber+ "\n"); // send the position of player 1 and dice number to all clients
            break;
        case 3: // control the turn of the players depending on the number of players playing
            if(playersConnected == 3) {
                Turn_1 = true; // set turn 1 to true
                statusArea.setText("Status: Waiting for Player 1"); // prints message in status area
            } else if (playersConnected == 4) {
                Turn_4 = true; // set turn 4 to true
                statusArea.setText("Status: Waiting for Player 4"); // prints message in status area
            }

            diceNumber = (int)(Math.random()*6) + 1; // generate random number from 1 to 6
            displayMessage("PLAYER 3 rolling the dice\n"); // display message 
            position[PLAYER_3] += diceNumber; // update position
            if(position[PLAYER_3] > 100) {
                position[PLAYER_3] -= diceNumber; // stay in the same place if the going out of the board
            }
            displayMessage("Position before logic: " + Integer.toString(position[PLAYER_3])+"\n"); // display message 
            position[PLAYER_3] = snakesLadderLogic(position[PLAYER_3]); // perform ladders and snakes transition and update position
            if(position[PLAYER_3] == 100)
                statusArea.setText("Player 3 WON - Game Over"); // display the winner in statusArea 
            sendToAll("Position of Player "+ player + "\n" + position[PLAYER_3] + "\n"+ diceNumber+ "\n"); // send the position of player 1 and dice number to all clients
            break;
        case 4: Turn_1 = true; // set turn 1 to true
            statusArea.setText("Status: Waiting for Player 1"); // prints message in status area
            diceNumber = (int)(Math.random()*6) + 1; // generate random number from 1 to 6
            displayMessage("PLAYER 4 rolling the dice\n"); // display message 
            position[PLAYER_4] += diceNumber; // update position
            if(position[PLAYER_4] > 100) {
                position[PLAYER_4] -= diceNumber; // stay in the same place if the going out of the board
            }
            displayMessage("Position before logic: " + Integer.toString(position[PLAYER_4])+"\n"); // display message 
            position[PLAYER_4] = snakesLadderLogic(position[PLAYER_4]); // perform ladders and snakes transition and update position
            if(position[PLAYER_4] == 100)
                statusArea.setText("Player 4 WON - Game Over"); // display the winner in statusArea 
            sendToAll("Position of Player "+ player + "\n" + position[PLAYER_4] + "\n"+ diceNumber+ "\n"); // send the position of player 1 and dice number to all clients
            break;
        } // end switch
    } // end method rollLogic

    private int snakesLadderLogic(int position)
    {	
        // change the position of the token depending on the snakes and ladders logic
        int p = position;
        switch(position) {
            case 1: p = 38;break;
            case 6: p = 16;break;
            case 11: p = 49;break;
            case 14: p = 4;break;
            case 21: p = 60;break;
            case 24: p = 87;break;
            case 31: p = 9;break;
            case 35: p = 54;break;
            case 44: p = 26;break;
            case 51: p = 67;break;
            case 56: p = 53;break;
            case 62: p = 19;break;
            case 64: p = 42;break;
            case 73: p = 92;break;
            case 78: p = 100;break;
            case 84: p = 28;break;
            case 91: p = 71;break;
            case 95: p = 75;break;
            case 98: p = 80;break;
        } // end switch
        return p;
        } // end class snakesLadderLogic
} // end class server
