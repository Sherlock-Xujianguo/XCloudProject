package Server;

import Core.*;

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

        private void CreateDefaultDirectory() {
            File file = new File(Setting.Server._defaultDirectoryPath);
            if (!file.exists()) {
                file.mkdirs();
            }
        }

        void Close(){
            try {
                Debug.Log(_clientSocket.getInetAddress() + "Close");
                _clientSocket.close();
                _dis.close();
                _dos.close();
                CleanTempFile();
            }
            catch (IOException e) {
                e.printStackTrace();
            }
        }

        private void CleanTempFile() {
            File file = new File(Setting.Server._tempFile);
            if (file.exists()) {
                file.delete();
            }
        }

        public void run() {
            try {
                CreateDefaultDirectory();

                _keyPair = RSA.GetKeyPair(); // 生成RSA公私钥对
                _privateKey = RSA.GetPrivateKeyString(_keyPair);
                _publicKey = RSA.GetPublicKeyString(_keyPair);

                _dos.writeUTF(_publicKey); // 发送公钥
                _dos.flush();

                _clientPublicKey = _dis.readUTF();
                Debug.Log("Client public key is: " + _clientPublicKey);

                String desKeyString = GetLongString();
                _desKey = AES.StringKey2Byte(desKeyString); // 获取DES密钥
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
                    if (command_line.equals("SendFileTree")) {
                        GetFileTree();
                    }
                    if (command_line.equals("GetFileTree")) {
                        SendFileTree();
                    }
                    if (command_line.equals("GetFile")) {
                        SendFile();
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
                String filePath = GetLongString();
                Debug.Log(filePath);
                File file = new File(Setting.Server._defaultDirectoryPath + Setting._envSep + filePath);
                File parent = new File(file.getParent();
                if (!parent.exists()) {
                    parent.mkdirs();
                }

                FileOutputStream fos = new FileOutputStream(file);

                int length;
                while ((length = Integer.parseInt(_dis.readUTF())) != -1) {
                    byte[] buff = new byte[length];
                    _dis.read(buff, 0, length);
                    byte[] decrypyByte = AES.DecrypyByte(buff, _desKey);
                    Debug.Log(decrypyByte);
                    fos.write(decrypyByte, 0, decrypyByte.length);
                    fos.flush();
                }

                fos.close();
                Close();
            }
            catch (Exception e) {
                e.printStackTrace();
                Close();
            }
        }

        private void SendFile() {
            try {
                String filePath = GetLongString();
                File file = new File(Setting.Server._defaultDirectoryPath + Setting._envSep + filePath);
                if (!file.exists()) {
                    SendLongString("0");
                    Debug.Log(filePath + ": File not exsits");
                    Close();
                    return;
                }
                else {
                    SendLongString("1");
                }
                FileInputStream fis = new FileInputStream(file);

                int length;
                byte[] buff = new byte[1024];
                while ((length = fis.read(buff, 0, buff.length)) != -1) {
                    Debug.Log(buff);

                    byte[] encrypyByte = AES.EncrypyByte(buff, _desKey);

                    _dos.writeUTF(Integer.toString(encrypyByte.length));

                    _dos.write(encrypyByte, 0, encrypyByte.length);
                    _dos.flush();
                }
                _dos.writeUTF(Integer.toString(-1));

                fis.close();
                Close();
            }
            catch (Exception e) {
                e.printStackTrace();
                Close();
            }
        }

        private void GetFileTree() {
            try {
                File dir = new File(Setting.Server._fileTreeDataPath);
                Debug.Log(dir);
                if (!dir.exists()) {
                    dir.mkdirs();
                }

                File file = new File(Setting.Server._fileTreeDataName);

                FileOutputStream fos = new FileOutputStream(file);

                int length;
                byte[] buff = new byte[1024 * 1024];
                while ((length = _dis.read(buff, 0, buff.length)) != -1) {
                    fos.write(buff, 0, length);
                    fos.flush();
                }

                fos.close();

                FileNode fn = FileTree.GetFileTree(Setting.Server._fileTreeDataName);
                fn.print();
                FileTree.RestoreServerFileTree(fn);

                Close();
            }
            catch (Exception e) {
                e.printStackTrace();
                Close();
            }
        }

        private void SendFileTree() {
            try {
                FileTree.SaveServerFileTree(Setting.Server._defaultDirectoryPath);
                File file = new File(Setting.Server._fileTreeDataName);

                FileInputStream fis = new FileInputStream(file);

                int length;
                byte[] buff = new byte[1024 * 1024];
                while ((length = fis.read(buff, 0, buff.length)) != -1) {
                    _dos.write(buff, 0, length);
                    _dos.flush();
                }

                fis.close();
                Close();
            }
            catch (Exception e) {
                e.printStackTrace();
                Close();
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
