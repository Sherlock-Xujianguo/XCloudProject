package Client;

import Core.*;
import com.sun.tools.javac.Main;

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

        String desKeyString = DES.GetKeyString();
        _desKey = DES.Key2Byte(DES.String2Key(desKeyString)); // 生成DES密钥
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

    public void Close() throws Exception{
        _dos.flush();
        _clientSocket.close();
        _dis.close();
        _dos.close();
        Debug.Log("Close");
    }

    public void SendLongString(String inputString) throws Exception {
        _dos.writeUTF(RSA.EncryptByPublicKeyString(inputString, _serverPublicKey));
        _dos.flush();
    }

    public String GetLongString() throws Exception {
        return RSA.DecryptByPrivateKeyString(_dis.readUTF(), _privateKey);
    }

    public void SendFile(String filePath, SendFileCallback sendFileCallback) {
        if (!_isInit) {
            sendFileCallback.OnFail();
            Debug.Log("Main client has not init, please init first");
            return;
        }

        try {
            SendLongString("SendFile");
            File file = new File(filePath);
            SendLongString(file.getName());

            FileInputStream fis = new FileInputStream(file);
            InputStream is = DES.DESInputStream(fis, _desKey);

            int length;
            byte[] buff = new byte[1024 * 1024];
            while ((length = is.read(buff, 0, buff.length)) != -1) {
                _dos.write(buff, 0, length);
                _dos.flush();
            }

            fis.close();
            is.close();

            sendFileCallback.OnSuccess();
        }
        catch (Exception e) {
            Debug.Log(e);
            sendFileCallback.OnFail();
        }
    }

    public void SendFileTree(String path, SendFileCallback sendFileCallback) {
        try {
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
        }
        catch (Exception e) {
            e.printStackTrace();
            sendFileCallback.OnFail();
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
