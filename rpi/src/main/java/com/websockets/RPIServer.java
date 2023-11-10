package com.websockets;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetSocketAddress;
import org.java_websocket.WebSocket;
import org.java_websocket.handshake.ClientHandshake;
import org.java_websocket.server.WebSocketServer;
import org.json.JSONObject;

public class RPIServer extends WebSocketServer {
    static BufferedReader in = new BufferedReader(new InputStreamReader(System.in));

    public RPIServer (int port) {
        super(new InetSocketAddress(port));
    }

    @Override
    public void onOpen(WebSocket conn, ClientHandshake clientHandshake) {
        String clientId = getConnectionId(conn);
        String host = conn.getRemoteSocketAddress().getAddress().getHostAddress();
        System.out.println("New client (" + clientId + "): " + host);
    }

    private String getConnectionId(WebSocket conn) {
        String name = conn.toString();
        return name.replaceAll("org.java_websocket.WebSocketImpl@", "").substring(0, 3);
    }


    @Override
    public void onClose(WebSocket conn, int i, String s, boolean b) {
        String clientId = getConnectionId(conn);
        System.out.println("Client disconnected '" + clientId + "'");
    }

    @Override
    public void onMessage(WebSocket conn, String s) {

    }

    @Override
    public void onError(WebSocket conn, Exception e) {
        // Quan hi ha un error
        e.printStackTrace();
    }

    @Override
    public void onStart() {

    }
    public void runServerBucle () {
        boolean running = true;
        try {
            System.out.println("Starting server");
            start();
            while (running) {
                String line;
                line = in.readLine();
                if (line.equals("exit")) {
                    running = false;
                }
            }
            System.out.println("Stopping server");
            stop(1000);
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }
}