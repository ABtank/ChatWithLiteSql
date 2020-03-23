package ru.abramov.chat.client;

import ru.abramov.chat.common.Library;
import ru.abramov.network.SocketThread;
import ru.abramov.network.SocketThreadListener;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;
import java.net.Socket;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Arrays;



public class ClientGUI extends JFrame implements ActionListener, Thread.UncaughtExceptionHandler, SocketThreadListener {

    private static final int WIDTH = 800;
    private static final int HEIGHT = 300;

    private final JTextArea log = new JTextArea();
    private final JPanel panelTop = new JPanel(new GridLayout(2, 4));
    private final JTextField tfIPAddress = new JTextField("127.0.0.1");
    private final JTextField tfPort = new JTextField("8189");
    private final JCheckBox cbAlwaysOnTop = new JCheckBox("Always on top");
    private final JTextField tfLogin = new JTextField("Iurii");
    private final JPasswordField tfPassword = new JPasswordField("123");
    private final JButton btnLogin = new JButton("Login");
    private final JButton btnRegistration = new JButton("Registration/rename");
    private final JTextField tfNickName = new JTextField(null);

    private final JPanel panelBottom = new JPanel(new BorderLayout());
    private final JButton btnDisconnect = new JButton("<html><b><i>Disconnect</i></b></html>");
    private final JTextField tfMessage = new JTextField();
    private final JButton btnSend = new JButton("Send");
    private final String WINDOW_TITLE = "Chat: ";

    private final DateFormat DATE_FORMAT = new SimpleDateFormat("HH:mm:ss: ");

    private boolean boolRegistration = false;
    private boolean shownIoErrors = false;

    private SocketThread socketThread;


     // Лист для юзеров

    private final JList<String> userList = new JList<>();

    private ClientGUI() {
        Thread.setDefaultUncaughtExceptionHandler(this);
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);//установка по центру экрана
        setSize(WIDTH, HEIGHT);
        log.setLineWrap(true);
        setTitle(WINDOW_TITLE);
        log.setEditable(false);//запрещаем писать непосредственно в данном поле


        JScrollPane scrollLog = new JScrollPane(log);
        JScrollPane scrollUser = new JScrollPane(userList);
        JScrollPane scrollMessage = new JScrollPane(tfMessage);

         // Создали список с юзерами

        String[] users = {"Welcome", "to", "our", "chat"};
        userList.setListData(users);

        // Установка размера окна с юзерами

        scrollUser.setPreferredSize(new Dimension(100, 0));
        panelBottom.setPreferredSize(new Dimension(WIDTH, 60));
        panelBottom.setVisible(false);

        // Создаем листнеры

        cbAlwaysOnTop.addActionListener(this);
        btnSend.addActionListener(this);
        btnDisconnect.addActionListener(this);
        btnLogin.addActionListener(this);
        tfMessage.addActionListener(this);
        btnRegistration.addActionListener(this);
        cbAlwaysOnTop.setSelected(true);
        //btnSend.setFocusable(false);

        // Добавляем элементы на панель Top

        panelTop.add(tfIPAddress);
        panelTop.add(tfPort);
        panelTop.add(cbAlwaysOnTop);
        panelTop.add(btnRegistration);
        panelTop.add(tfLogin);
        panelTop.add(tfPassword);
        panelTop.add(tfNickName);
        panelTop.add(btnLogin);
        tfNickName.setVisible(false);


        // Добавляем элементы на панель Bottom

        panelBottom.add(btnDisconnect, BorderLayout.WEST);
        panelBottom.add(scrollMessage, BorderLayout.CENTER);
        panelBottom.add(btnSend, BorderLayout.EAST);

        //Добавляем панели TOP & BOTTOM на основной экран

        add(panelTop, BorderLayout.NORTH);
        add(panelBottom, BorderLayout.SOUTH);
        add(scrollLog, BorderLayout.CENTER);
        add(scrollUser, BorderLayout.WEST);

        setVisible(true);
    }


    private void connect() {
        try {
            Socket socket = new Socket(tfIPAddress.getText(), Integer.parseInt(tfPort.getText()));
            socketThread = new SocketThread(this, "Client", socket);
        } catch (IOException e) {
            showException(Thread.currentThread(), e);
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                new ClientGUI();
            }
        });
        //throw new RuntimeException("Hello from main");

    }

    //Обработка нажатий кнопок на панели

    @Override
    public void actionPerformed(ActionEvent e) {
        Object src = e.getSource();
        if (src == cbAlwaysOnTop) {
            setAlwaysOnTop(cbAlwaysOnTop.isSelected());
        } else if (src == btnSend || src == tfMessage) {
            sendMessage();
        } else if (src == btnLogin) {
            connect();
        } else if (src == btnDisconnect) {
            socketThread.close();
        } else if (src == btnRegistration) {
            boolRegistration = true;
            tfNickName.setVisible(true);
            btnRegistration.setVisible(false);
            btnLogin.setText("Get registry, or rename");

        } else {
            throw new RuntimeException("Unknown source: " + src);
        }
    }


    // Составление строки сообщения и отправка его в лог + запись в файл

    private void sendMessage() {
        String msg = tfMessage.getText();
        String username = tfLogin.getText();
        if ("".equals(msg)) return;
        tfMessage.setText(null);
        tfMessage.requestFocusInWindow();
        socketThread.sendMessage(Library.getTypeBcastClient(msg));
        // putLog(String.format("%s: %s", username, msg));
        wrtMsgToLogFile(msg, username);
    }

    // Запись лога в файл

    private void wrtMsgToLogFile(String msg, String username) {
        String nameFile = String.format("%s_log.txt", username);
        try (FileWriter out = new FileWriter(nameFile, true)) {
            out.write(username + ": " + msg + "\n");
            out.flush();
        } catch (IOException e) {
            if (!shownIoErrors) {
                shownIoErrors = true;
                showException(Thread.currentThread(), e);
            }
        }
    }

    // Определяем количество строк в истории лога

    private int countLinesHistoryMessage(String input) {
        int count = 1;
        try (InputStream is = new FileInputStream(input)) {
            for (int aChar = 0; aChar != -1; aChar = is.read()) {
                if (aChar == '\n') count++;
            }
        } catch (IOException e) {
            if (!shownIoErrors) {
                shownIoErrors = true;
                showException(Thread.currentThread(), e);
            }
        }
        return count;
    }

    // вывод истории сообщений в лог Последних 100 строк

    private void historyMessages() {
        int numberLineInput = 100;
        String username = tfLogin.getText();
        String nameFile = String.format("%s_log.txt", username);
        try (FileReader in = new FileReader(nameFile);
             BufferedReader br = new BufferedReader((in))) {
            String line;
            int count = countLinesHistoryMessage(nameFile);
            int b = 0;
            while ((line = br.readLine()) != null) {
                b++;
                if (b >= count - numberLineInput) putLog(line);
            }
        } catch (IOException e) {
            if (!shownIoErrors) {
                shownIoErrors = true;
                showException(Thread.currentThread(), e);
            }
        }
    }


    private void putLog(String msg) {
        if ("".equals(msg)) return;
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                log.append(msg + "\n");
                log.setCaretPosition(log.getDocument().getLength());
            }
        });
    }

    /**
     * Получение массива строк текста исключения
     * Составление строки с самой важной информацией
     * Вывод ошибки в диалоговом окне
     */
    private void showException(Thread t, Throwable e) {
        String msg;
        //чтоб получить текст исключения создаем массив
        StackTraceElement[] ste = e.getStackTrace();
        if (ste.length == 0) {
            msg = "Empty Stacktrace";
        } else {
            msg = "Exception in " + t.getName() + " " +
                    e.getClass().getCanonicalName() + ": " +
                    e.getMessage() + "\n\t at" + ste[0];
        }
        JOptionPane.showMessageDialog(this, msg, "Exception", JOptionPane.ERROR_MESSAGE);
    }


    @Override
    public void uncaughtException(Thread t, Throwable e) {
        e.printStackTrace();
        showException(t, e);
        System.exit(1);
    }

    /**
     * Socket thread listener methods
     */

    @Override
    public void onSocketStart(SocketThread thread, Socket socket) {
        putLog("Start");
    }

    @Override
    public void onSocketStop(SocketThread thread) {
        panelBottom.setVisible(false);
        panelTop.setVisible(true);
        setTitle(WINDOW_TITLE);
        userList.setListData(new String[0]);
    }

    @Override
    public void onSocketReady(SocketThread thread, Socket socket) {
        panelBottom.setVisible(true);
        panelTop.setVisible(false);
        String login = tfLogin.getText();
        String password = new String(tfPassword.getPassword());
        if (boolRegistration) {
            String nickName = tfNickName.getText();
            thread.sendMessage(Library.getAuthNewClientRequest(login, password, nickName));
            tfNickName.setVisible(false);
            btnRegistration.setVisible(true);
            btnLogin.setText("Login");
            boolRegistration = false;
        } else {
            thread.sendMessage(Library.getAuthRequest(login, password));
        }
    }


    @Override
    public void onReceiveString(SocketThread thread, Socket socket, String msg) {
        handleMessage(msg);
    }

    @Override
    public void onSocketException(SocketThread thread, Exception exception) {
        // showException(thread, exception);
    }

    private void handleMessage(String msg) {
        String[] arr = msg.split(Library.DELIMITER);
        String msgType = arr[0];
        switch (msgType) {
            case Library.AUTH_ACCEPT:
                setTitle(WINDOW_TITLE + " entered with nickname: " + arr[1]);
                log.setText(null);
                File nameFile = new File(String.format("%s_log.txt", tfLogin.getText()));
                if (nameFile.exists()) {
                    historyMessages();
                }
                break;
            case Library.AUTH_DENIED:
                putLog(msg);
                break;
            case Library.MSG_FORMAT_ERROR:
                putLog(msg);
                socketThread.close();
                break;
            case Library.TYPE_BROADCAST:
                putLog(DATE_FORMAT.format(Long.parseLong(arr[1])) +
                        arr[2] + ": " + arr[3]);
                break;
            case Library.USER_LIST:
                String users = msg.substring(Library.USER_LIST.length() +
                        Library.DELIMITER.length());
                String[] usersArr = users.split(Library.DELIMITER);
                Arrays.sort(usersArr);
                userList.setListData(usersArr);
                break;
            case Library.REGISTRATION_DENIED:
                putLog(" Nickname: " + arr[1] + " or login: " + arr[2] + " if exists.");
                break;
            default:
                throw new RuntimeException("Unknown message type: " + msg);
        }
    }
}


