package com.futebol.colaborativo.api;

import org.java_websocket.WebSocket;
import org.java_websocket.handshake.ClientHandshake;
import org.java_websocket.server.WebSocketServer;

import java.net.InetSocketAddress;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public class EventSocket extends WebSocketServer {

    private static final Set<WebSocket> connections =
        Collections.synchronizedSet(new HashSet<>());

    private static volatile String lastMessage = null;

    public EventSocket(int port) {
        super(new InetSocketAddress(port));
    }

    @Override
    public void onOpen(WebSocket conn, ClientHandshake handshake) {
        connections.add(conn);
        if (lastMessage != null) {
            conn.send(lastMessage);
        }
        System.out.println("✅ WebSocket conectado: " + conn.getRemoteSocketAddress());
    }

    @Override
    public void onClose(WebSocket conn, int code, String reason, boolean remote) {
        connections.remove(conn);
        System.out.println("❌ WebSocket desconectado");
    }

    @Override
    public void onMessage(WebSocket conn, String message) {

    }

    @Override
    public void onError(WebSocket conn, Exception ex) {
        System.err.println("Erro WS: " + ex.getMessage());
    }

    @Override
    public void onStart() {
        System.out.println("🚀 WebSocket rodando na porta " + getPort());
    }

    public static void enviarMensagemParaClientes(String message) {
        lastMessage = message;
        synchronized (connections) {
            for (WebSocket conn : connections) {
                conn.send(message);
            }
        }
    }
}