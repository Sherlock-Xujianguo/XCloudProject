package Server;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MainServer {

    class ServerWorkingThread implements Runnable {
        Socket _clientSocket;
        DataInputStream _dis;
        DataOutputStream _dos;

        ServerWorkingThread(Socket socket) throws IOException{
            _clientSocket = socket;

            _dis = new DataInputStream(_clientSocket.getInputStream());
            _dos = new DataOutputStream(_clientSocket.getOutputStream());
        }

        void Close(){
            try {
                _clientSocket.close();
                _dis.close();
                _dos.close();
                System.out.println("Close");
            }
            catch (IOException e) {
                System.out.println(e);
            }
        }

        public void run() {
            while (!_clientSocket.isClosed()) {
                try {

                }
                catch (Exception e) {
                    System.out.println(e);
                }
            }
            Close();
        }
    }

    public static MainServer Instance = new MainServer();
    private MainServer() {}
    static int _port;
    static ServerSocket _serverSocket;
    static ExecutorService _executorService;

    public void Init() throws IOException{
        Init(4001);
    }

    public void Init(int port) throws IOException {
        _port = port;

        _serverSocket = new ServerSocket(_port);
        _executorService = Executors.newCachedThreadPool();
    }

    public void Accept() {
        while (true) {
            try {
                _executorService.execute(
                        new ServerWorkingThread(_serverSocket.accept())
                );
            }
            catch (IOException e) {
                System.out.println(e);
            }
        }
    }



    public static void main(String[] args) {
        try {
            Instance.Init();
            Instance.Accept();
        }
        catch (IOException e) {
            System.out.println(e);
        }
    }
}
