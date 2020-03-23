package ru.abramov.network;


import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;


public class ServerSocketThread extends Thread {

    private int port;
    private int timeuot;
    ServerSocketThreadListener listener;


    public ServerSocketThread(ServerSocketThreadListener listener, String name, int port, int timeout) {
        super(name);
        this.port = port;
        this.timeuot = timeout;
        this.listener = listener;
        start();
    }


    @Override
    public void run() {
        listener.onServerStart(this);
//создаем сервер сокет на порте
        try (ServerSocket server = new ServerSocket(port)) {
            server.setSoTimeout(timeuot); // установка Timeout для периодического выхода из цикла, для проверки флага isinterapted
            listener.onServerTimeout(this, server);
            while (!isInterrupted()) {
                Socket socket;
                try {
                    socket = server.accept();
                } catch (SocketTimeoutException e) {
                    listener.onServerTimeout(this, server);
                    continue;
                }
                listener.onSocketAccepted(this, server, socket);
            }
        } catch (IOException e) {
            listener.onServerException(this, e);
        } finally {
            listener.onServerStop(this);
        }
    }
}
