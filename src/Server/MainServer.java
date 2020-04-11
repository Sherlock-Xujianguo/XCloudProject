package Server;

import Core.DES;
import Core.Debug;
import Core.RSA;

import javax.swing.*;
import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.security.KeyPair;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MainServer {

    class ServerWorkingThread implements Runnable {
        Socket _clientSocket;
        DataInputStream _dis;
        DataOutputStream _dos;
        KeyPair _keyPair;
        String _privateKey;
        String _publicKey;
        String _clientPublicKey;
        byte[] _clientPublicKeyByte;
        byte[] _desKey;

        public boolean _isInit = false;

        ServerWorkingThread(Socket socket) throws Exception{
            _clientSocket = socket; // 接收连接

            _dis = new DataInputStream(_clientSocket.getInputStream());
            _dos = new DataOutputStream(_clientSocket.getOutputStream());
        }

        void Close(){
            try {
                Debug.Log(_clientSocket.getInetAddress() + "Close");
                _clientSocket.close();
                _dis.close();
                _dos.close();
            }
            catch (IOException e) {
                e.printStackTrace();
            }
        }

        public void run() {
            try {
                _keyPair = RSA.GetKeyPair(); // 生成RSA公私钥对
                _privateKey = RSA.GetPrivateKeyString(_keyPair);
                _publicKey = RSA.GetPublicKeyString(_keyPair);

                _dos.writeUTF(_publicKey); // 发送公钥
                _dos.flush();

                _clientPublicKey = _dis.readUTF();
                Debug.Log("Client public key is: " + _clientPublicKey);

                String desKeyString = GetLongString();
                _desKey = DES.Key2Byte(DES.String2Key(desKeyString)); // 获取DES密钥
                Debug.Log("DES Key: " + desKeyString);

                SendLongString(desKeyString); // 重新加密后传回

                _isInit = true;
            }
            catch (Exception e) {
                e.printStackTrace();
                Close();
                return;
            }


            while (!_clientSocket.isClosed()) {
                try {
                    String command_line = GetLongString();
                    Debug.Log(command_line);

                    if (command_line.equals("SendFile")) {
                        GetFile();
                    }

                }
                catch (Exception e) {
                    if (e instanceof EOFException) {
                        Debug.Log("EOF");
                    }
                    else {
                        e.printStackTrace();
                    }
                    break;
                }
            }
            Close();
        }

        private String GetLongString() throws Exception {
            return RSA.DecryptByPrivateKeyString(_dis.readUTF(), _privateKey);
        }

        private void SendLongString(String inputString) throws Exception {
            _dos.writeUTF(RSA.EncryptByPublicKeyString(inputString, _clientPublicKey));
        }

        private void GetFile() {
            try {
                String fileName = GetLongString();
                File file = new File(fileName);

                FileOutputStream fos = new FileOutputStream(file);
                OutputStream os = DES.DESOutputStream(fos, _desKey);

                int length;
                byte[] buff = new byte[1024 * 1024];
                while ((length = _dis.read(buff, 0, buff.length)) != -1) {
                    os.write(buff, 0, length);
                    os.flush();
                }

            }
            catch (Exception e) {
                e.printStackTrace();
            }
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

    public void Run() {
        while (true) {
            try {
                _executorService.execute(
                        new ServerWorkingThread(_serverSocket.accept())
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
