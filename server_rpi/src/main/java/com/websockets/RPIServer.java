package com.websockets;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.InetSocketAddress;
import org.java_websocket.WebSocket;
import org.java_websocket.handshake.ClientHandshake;
import org.java_websocket.server.WebSocketServer;
import org.json.JSONObject;
import java.util.ArrayList;
import com.fasterxml.jackson.databind.ObjectMapper;

public class RPIServer extends WebSocketServer {
    static BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
    ArrayList<ArrayList> connections = new ArrayList<>();
    ArrayList<String> flutterClients = new ArrayList<>();
    ArrayList<String> androidClients = new ArrayList<>();
    Process ipProcess;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public RPIServer (int port) {
        super(new InetSocketAddress(port));
    }

    @Override
    public void onOpen(WebSocket conn, ClientHandshake clientHandshake) {
        String clientId = getConnectionId(conn);
        String host = conn.getRemoteSocketAddress().getAddress().getHostAddress();
        System.out.println("New client (" + clientId + "): " + host);
        String userAgent = clientHandshake.getFieldValue("User-Agent");
        System.out.println(userAgent);
        if (!doesClientExist(clientId)) {
            if (userAgent != null) {
                if (userAgent.contains("dart")) {
                    flutterClients.add(clientId);
                } else {
                    androidClients.add(clientId);
                }
            }
        }
        if (ipProcess != null) {
            ipProcess.destroy();
        }
        String[] args2 = new String[] {"/home/ieti/bin/text-scroller", "-f", "/home/ieti/dev/bitmap-fonts/bitmap/cherry/cherry-10-b.bdf", "--led-cols=64", "--led-rows=64", "--led-slowdown-gpio=4", "--led-no-hardware-pulse", "Clientes conectados"};
        try {
            ipProcess = new ProcessBuilder(args2).start();
            String jsonData = objectMapper.writeValueAsString(connections);
            conn.send(jsonData);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private String getConnectionId(WebSocket conn) {
        String name = conn.toString();
        return name.replaceAll("org.java_websocket.WebSocketImpl@", "").substring(0, 3);
    }


    @Override
    public void onClose(WebSocket conn, int i, String s, boolean b) {
        try {
            String clientId = getConnectionId(conn);
            if (doesClientExist(clientId)) {
                if (flutterClients.contains(clientId)) {
                    flutterClients.remove(clientId);
                }else{
                    androidClients.remove(clientId);
                }
            }
            if (ipProcess != null) {
                ipProcess.destroy();
            }
            String line = "192.168.0.25";
            String[] args2 = new String[] {"/home/ieti/bin/text-scroller", "-f", "/home/ieti/dev/bitmap-fonts/bitmap/cherry/cherry-10-b.bdf", "--led-cols=64", "--led-rows=64", "--led-slowdown-gpio=4", "--led-no-hardware-pulse", "192.168.0.25"};
            ipProcess = new ProcessBuilder(args2).start();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onMessage(WebSocket conn, String message) {
        String[] args2 = new String[] {
            "/home/ieti/bin/text-scroller",
            "-f", "/home/ieti/dev/bitmap-fonts/bitmap/cherry/cherry-10-b.bdf",
            "--led-cols=64", "--led-rows=64",
            "--led-slowdown-gpio=4", "--led-no-hardware-pulse",
            message
        };

    try {

        if (ipProcess != null) {
            ipProcess.destroy();
        }

        // Start a new process with updated args2
        ipProcess = new ProcessBuilder(args2).start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onError(WebSocket conn, Exception e) {
        // Quan hi ha un error
        e.printStackTrace();
    }

    @Override
    public void onStart() {
        try {
            connections.add(flutterClients);
            connections.add(androidClients);
            String line = "192.168.0.25";
            String[] args2 = new String[] {"/home/ieti/bin/text-scroller", "-f", "/home/ieti/dev/bitmap-fonts/bitmap/cherry/cherry-10-b.bdf", "--led-cols=64", "--led-rows=64", "--led-slowdown-gpio=4", "--led-no-hardware-pulse", line};
            ipProcess = new ProcessBuilder(args2).start();
        } catch (IOException e) {
            e.printStackTrace();
        }
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
    public boolean doesClientExist(String clientId) {
        return flutterClients.contains(clientId) || androidClients.contains(clientId);
    }
}
