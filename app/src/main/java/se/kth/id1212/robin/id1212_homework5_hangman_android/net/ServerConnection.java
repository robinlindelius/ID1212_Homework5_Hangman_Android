package se.kth.id1212.robin.id1212_homework5_hangman_android.net;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.Serializable;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.StringJoiner;

import se.kth.id1212.robin.id1212_homework5_hangman_android.OutputHandler;
import se.kth.id1212.robin.id1212_homework5_hangman_android.util.Constants;
import se.kth.id1212.robin.id1212_homework5_hangman_android.util.MessageType;

/**
 * Created by Robin on 2017-12-16.
 */

public class ServerConnection implements Serializable {
    private static final int TIMEOUT_HALF_MINUTE = 30000;
    private static final int TIMEOUT_HALF_HOUR = 1800000;

    private Socket socket;

    private PrintWriter toServer;
    private BufferedReader fromServer;

    private volatile boolean connected;

    public void connect(String host, int port, OutputHandler outputHandler) throws IOException {
        socket = new Socket();
        socket.connect(new InetSocketAddress(host, port), TIMEOUT_HALF_MINUTE);
        socket.setSoTimeout(TIMEOUT_HALF_HOUR);
        connected = true;
        boolean autoFlush = true;
        toServer = new PrintWriter(socket.getOutputStream(), autoFlush);
        fromServer = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        new Thread(new Listener(outputHandler)).start();
    }

    public void disconnect() throws IOException {
        sendMessageToServer(MessageType.DISCONNECT.toString());
        socket.close();
        socket = null;
        connected = false;
    }

    public void sendGuess(String guess){
        sendMessageToServer(MessageType.GUESS.toString(), guess);
    }

    public void startNewGame() {
        sendMessageToServer(MessageType.START.toString());
    }

    public void getRules() {
        sendMessageToServer(MessageType.RULES.toString());
    }


    private void sendMessageToServer(String... strings) {
        StringJoiner joiner = new StringJoiner(Constants.MESSAGE_DELIMITER);
        for (String string : strings) {
            joiner.add(string);
        }
        toServer.println(joiner.toString());
    }


    private class Listener implements Runnable{

        private final OutputHandler outputHandler;

        public Listener(OutputHandler outputHandler) {
            this.outputHandler = outputHandler;
        }

        @Override
        public void run() {
            try{
                while(true) {
                    outputHandler.handleOutput(parseServerMessage(fromServer.readLine()));

                }
            } catch (Throwable connectionFailure) {
                if (connected) {
                    outputHandler.handleOutput("Lost connection.");
                }
            }
        }

        private String parseServerMessage(String serverMessage) throws Exception {
            String[] messageElements = serverMessage.split(Constants.MESSAGE_DELIMITER);
            if (MessageType.valueOf(messageElements[Constants.MESSAGE_TYPE_INDEX]) != MessageType.GAME) {
                throw new Exception("Corrupt message received from server: " + serverMessage);
            }
            return messageElements[Constants.MESSAGE_BODY_INDEX];
        }
    }
}
