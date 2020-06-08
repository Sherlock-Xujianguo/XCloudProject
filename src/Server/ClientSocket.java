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
            byte[] str = new byte[1024];
            int len = _is.read(str);
            byte[] rst = new byte[len];
            for (int i = 0; i < len; i++) {
                rst[i] = str[i];
            }
            return new String(rst, "utf-8");
        }

        public void WriteUTF8(String str) throws IOException {
            _os.write(str.getBytes());
            return;
        }

        public void Sign() throws IOException{
            String userMD5 = GetUTF8();
            Debug.Log(userMD5);
            File userDir = new File(Setting.Server._defaultDirectoryPath + Setting._envSep + userMD5);
            if (userDir.exists()) {
                WriteUTF8("-1");
            }
            else {
                userDir.mkdir();
                WriteUTF8("0");
            }
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
