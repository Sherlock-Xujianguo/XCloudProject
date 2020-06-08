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
        // Debug.Log("Server public key is: " + _serverPublicKey);

        _dos.writeUTF(_publicKey);
        _dos.flush();
        // Debug.Log("Client public key is: " + _publicKey);

        String desKeyString = AES.GetKeyString();
        _desKey = AES.StringKey2Byte(desKeyString); // 生成DES密钥
        // Debug.Log("DES Key: " + desKeyString);
        SendLongString(desKeyString);

        String serverDESKey = GetLongString(); // 接收传回的DES密钥
        if (serverDESKey.equals(desKeyString)) { // 确认密钥完整性
            // Debug.Log("Init Finish");
            _isInit = true;
        }
        else {
            // Debug.Log("DES Key is not correct, close connect!");
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
            // Debug.Log("Close");
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

    public void SendFile(String filePath) {
        try {
            Init();

            SendLongString("SendFile");
            SendLongString(filePath);

            File file = new File(Setting.Client._defaultDirectoryPath + Setting._envSep + filePath);
            FileInputStream fis = new FileInputStream(file);

            int length;
            byte[] buff = new byte[1024];
            while ((length = fis.read(buff, 0, buff.length)) != -1) {
                byte[] encrypyByte = AES.EncrypyByte(buff, _desKey);

                _dos.writeUTF(Integer.toString(encrypyByte.length));
                _dos.writeUTF(Integer.toString(length));

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

    public void SendFileTree() {
        try {
            Init();

            FileTree.SaveClientFileTree(Setting.Client._defaultDirectoryPath);
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

            Close();
        }
        catch (Exception e) {
            e.printStackTrace();
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

    public void GetDirectory() {
        GetFileTree();
        GetDirectory(Setting.Client._defaultDirectoryPath, "");
    }


    private void GetDirectory(String path, String tempPath) {
        File file = new File(path);
        File[] fileList = file.listFiles();
        if (fileList == null || fileList.length == 0) {
            return;
        }

        for (File f:fileList) {
            String targetFile = tempPath + Setting._envSep + f.getName();
            if (f.isDirectory()) {
                GetDirectory(path + Setting._envSep + f.getName(), targetFile);
            }
            else {
                GetFile(targetFile);
            }
        }
    }

    public void SendDirectory() {
        SendFileTree();
        SendDirectory(Setting.Client._defaultDirectoryPath, "");
    }

    private void SendDirectory(String path, String tempPath) {
        File file = new File(path);
        File[] fileList = file.listFiles();
        if (fileList == null || fileList.length == 0) {
            return;
        }

        for (File f:fileList) {
            String targetFile = tempPath + Setting._envSep + f.getName();
            if (f.isDirectory()) {
                SendDirectory(path + Setting._envSep + f.getName(), targetFile);
            }
            else {
                SendFile(targetFile);
            }
        }
    }

    public interface SendFileCallback {
        void OnFail();
        void OnSuccess();
    }

    public void TestJar(String test) {
        System.out.println(test);
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
