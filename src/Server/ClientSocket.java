package Server;

import Core.*;

import javax.sound.sampled.AudioFormat;
import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.security.KeyPair;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ClientSocket {
    class ServerWorkingThread implements Runnable {
        Socket _clientSocket;
        InputStream _is;
        OutputStream _os;
        KeyPair _keyPair;
        byte[] _privateKey;
        byte[] _publicKey;

        public boolean _isInit = false;

        ServerWorkingThread(Socket socket) throws Exception{
            _clientSocket = socket; // 接收连接

            _is = _clientSocket.getInputStream();
            _os =_clientSocket.getOutputStream();
        }

        void Close(){
            try {
                Debug.Log(_clientSocket.getInetAddress() + "Close");
                _clientSocket.close();
                _is.close();
                _os.close();
            }
            catch (IOException e) {
                e.printStackTrace();
            }
        }

        public String GetUTF8() throws IOException{
            byte[] lenbyte = new byte[1024];
            int l = _is.read(lenbyte);
            int len = Integer.parseInt(new String(lenbyte, 0, l, "utf-8"));
            byte[] str = new byte[len];
            _is.read(str);
            return new String(str, "utf-8");
        }

        public void WriteUTF8(String str) throws IOException {
            byte[] strByte = str.getBytes();
            Debug.Log(Integer.toString(strByte.length));
            _os.write(Integer.toString(strByte.length).getBytes());
            _os.flush();
            _os.write(strByte);
            _os.flush();
            return;
        }

        public void Sign() throws IOException{
            WriteUTF8("nihao客户端");
            Close();
        }

        public void run() {
            try {
                while (!_clientSocket.isClosed()) {

                    Sign();
                }
                Close();
            }
            catch (Exception e) {
                if (e instanceof EOFException) {
                    Debug.Log("EOF");
                }
                else {
                    e.printStackTrace();
                }
                Close();
            }
        }
    }

    public static ClientSocket Instance = new ClientSocket();
    private ClientSocket() {}
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

    public void Run() {
        while (true) {
            try {
                _executorService.execute(
                        new ClientSocket.ServerWorkingThread(_serverSocket.accept())
                );
            }
            catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public static void main(String[] args) {
        try {
            Instance.Init();
            Instance.Run();
        }
        catch (IOException e) {
            System.out.println(e);
        }
    }
}
