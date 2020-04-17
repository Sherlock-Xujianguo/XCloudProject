package Client;

import Core.*;

import java.io.*;
import java.net.Socket;
import java.security.KeyPair;
import java.util.Scanner;

public class MainClient {
    public static MainClient Instance = new MainClient();
    MainClient() {}
    String _hostName;
    int _port;
    Socket _clientSocket;
    DataInputStream _dis;
    DataOutputStream _dos;
    KeyPair _keyPair;
    String _privateKey;
    String _publicKey;
    String _serverPublicKey;
    byte[] _serverPublicKeyByte;
    byte[] _desKey;

    public boolean _isInit = false;

    public void Init() throws Exception {
        Init("127.0.0.1", 4001);
    }

    public void Init(String hostName, int port) throws Exception {
        CreateDefaultDirectory();

        _clientSocket = new Socket(hostName, port); // 请求连接
        _hostName = hostName;
        _port = port;

        _dis = new DataInputStream(_clientSocket.getInputStream());
        _dos = new DataOutputStream(_clientSocket.getOutputStream());

        _keyPair = RSA.GetKeyPair(); // 生成RSA公私钥对
        _privateKey = RSA.GetPrivateKeyString(_keyPair);
        _publicKey = RSA.GetPublicKeyString(_keyPair);

        _serverPublicKey = _dis.readUTF(); // 接收公钥
        Debug.Log("Server public key is: " + _serverPublicKey);

        _dos.writeUTF(_publicKey);
        _dos.flush();
        Debug.Log("Client public key is: " + _publicKey);

        String desKeyString = AES.GetKeyString();
        _desKey = AES.StringKey2Byte(desKeyString); // 生成DES密钥
        Debug.Log("DES Key: " + desKeyString);
        SendLongString(desKeyString);

        String serverDESKey = GetLongString(); // 接收传回的DES密钥
        if (serverDESKey.equals(desKeyString)) { // 确认密钥完整性
            Debug.Log("Init Finish");
            _isInit = true;
        }
        else {
            Debug.Log("DES Key is not correct, close connect!");
            Close();
        }
    }

    public boolean IsInit() {
        return _isInit;
    }

    private void CreateDefaultDirectory() {
        File file = new File(Setting.Client._defaultDirectoryPath);
        if (!file.exists()) {
            file.mkdirs();
        }
    }

    private void Close() {
        try {
            _dos.flush();
            _clientSocket.close();
            _dis.close();
            _dos.close();
            CleanTempFile();
            Debug.Log("Close");
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void CleanTempFile() {
        File file = new File(Setting.Client._tempFile);
        if (file.exists()) {
            file.delete();
        }
    }

    private void SendLongString(String inputString) throws Exception {
        _dos.writeUTF(RSA.EncryptByPublicKeyString(inputString, _serverPublicKey));
        _dos.flush();
    }

    private String GetLongString() throws Exception {
        return RSA.DecryptByPrivateKeyString(_dis.readUTF(), _privateKey);
    }

    public void SendFile(String filePath, SendFileCallback sendFileCallback) {
        try {
            Init();

            SendLongString("SendFile");
            File file = new File(filePath);
            SendLongString(file.getName());

            FileInputStream fis = new FileInputStream(file);

            int length;
            byte[] buff = new byte[1024];
            while ((length = fis.read(buff, 0, buff.length)) != -1) {
                byte[] encrypyByte = AES.EncrypyByte(buff, _desKey);
                Debug.Log(encrypyByte);
                _dos.writeUTF(Integer.toString(encrypyByte.length));

                _dos.write(encrypyByte, 0, encrypyByte.length);
                _dos.flush();
            }
            _dos.writeUTF(Integer.toString(-1));

            fis.close();
            sendFileCallback.OnSuccess();

            Close();
        }
        catch (Exception e) {
            e.printStackTrace();
            sendFileCallback.OnFail();
            Close();
        }
    }

    public void GetFile(String filePath) {
        try {
            Init();

            SendLongString("GetFile");
            SendLongString(filePath);
            int isExists = Integer.parseInt(GetLongString());
            if (isExists == 0) {
                Debug.Log(filePath + ": File not exsits");
                Close();
                return;
            }
            File file = new File(Setting.Client._defaultDirectoryPath + Setting._envSep + filePath);
            FileOutputStream fos = new FileOutputStream(file);

            int length;
            while ((length = Integer.parseInt(_dis.readUTF())) != -1) {
                byte[] buff = new byte[length];
                _dis.read(buff);
                byte[] decrypyByte = AES.DecrypyByte(buff, _desKey);

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

    public void SendFileTree(String path, SendFileCallback sendFileCallback) {
        try {
            Init();

            FileTree.SaveClientFileTree(path);
            SendLongString("SendFileTree");
            File file = new File(Setting.Client._fileTreeDataName);

            FileInputStream fis = new FileInputStream(file);

            int length;
            byte[] buff = new byte[1024 * 1024];
            while ((length = fis.read(buff, 0, buff.length)) != -1) {
                _dos.write(buff, 0, length);
                _dos.flush();
            }

            fis.close();
            sendFileCallback.OnSuccess();

            Close();
        }
        catch (Exception e) {
            e.printStackTrace();
            sendFileCallback.OnFail();
            Close();
        }
    }

    public void GetFileTree() {
        try {
            Init();

            SendLongString("GetFileTree");
            File dir = new File(Setting.Client._fileTreeDataPath);
            Debug.Log(dir);
            if (!dir.exists()) {
                dir.mkdirs();
            }

            File file = new File(Setting.Client._fileTreeDataName);

            FileOutputStream fos = new FileOutputStream(file);

            int length;
            byte[] buff = new byte[1024 * 1024];
            while ((length = _dis.read(buff, 0, buff.length)) != -1) {
                fos.write(buff, 0, length);
                fos.flush();
            }

            fos.close();

            FileNode fn = FileTree.GetFileTree(Setting.Client._fileTreeDataName);
            fn.print();
            FileTree.RestoreClientFileTree(fn);

            Close();
        }
        catch (Exception e) {
            e.printStackTrace();
            Close();
        }
    }

    public interface SendFileCallback {
        void OnFail();
        void OnSuccess();
    }

    public static void main(String[] args) throws Exception {
        MainClient.Instance.Init();

        Scanner sc = new Scanner(System.in);
        for (int i = 0; i < 3; i++) {
            MainClient.Instance.SendLongString(sc.nextLine());
        }
        MainClient.Instance.Close();
    }

}
