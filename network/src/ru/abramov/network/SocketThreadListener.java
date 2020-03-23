package ru.abramov.network;

import java.net.Socket;

public interface SocketThreadListener {
    void onSocketStart(SocketThread thread, Socket socket);
    void onSocketStop(SocketThread thread);
    // готов
    void onSocketReady(SocketThread thread, Socket socked);
    //получение строки
    void onReceiveString(SocketThread thread, Socket socket,String msg);
    void onSocketException(SocketThread thread, Exception exception);
}