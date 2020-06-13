package Server;

import Core.*;

import javax.sound.sampled.AudioFormat;
import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.security.KeyPair;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
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
            _os = _clientSocket.getOutputStream();
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
            byte[] buf = new byte[1024];
            int len = _is.read(buf, 0, buf.length);
            return new String(buf, 0, len);

        }

        public void WriteUTF8(String str) throws Exception {
            _os.write(str.getBytes(), 0, str.getBytes().length);
            _os.flush();
            Thread.sleep(100);
        }

        public void Sign() throws Exception{
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

        public void GetFile() throws Exception {
            String userName = GetUTF8();
            Debug.Log(userName);
            String filePath = GetUTF8();
            Debug.Log(filePath);
            String MD5 = GetUTF8();
            Debug.Log(MD5);
            Long fileSize = Long.parseLong(GetUTF8());
            String path = Setting.Server._defaultDirectoryPath + Setting._envSep + userName + filePath;
            Debug.Log(path);
            new FileInfo(path, MD5, fileSize, new Date().getTime()).SaveFileInfo();
            File file = new File(path);
            if (!new File(file.getParent()).exists()){
                new File(file.getParent()).mkdirs();
            }
            FileOutputStream fo = new FileOutputStream(file);
            int len;
            byte[] buf = new byte[1040];
            while ((len = _is.read(buf, 0, buf.length)) > 0) {
                fo.write(buf, 0, len);
            }
            fo.close();
            Close();
        }

        public void LogIn() throws Exception {
            String userMD5 = GetUTF8();
            Debug.Log(userMD5);
            File userDir = new File(Setting.Server._defaultDirectoryPath + Setting._envSep + userMD5);
            if (userDir.exists()) {
                WriteUTF8("0");
            }
            else {
                WriteUTF8("-1");
            }
            Close();
        }

        public void SendFile() throws Exception {
            String userName = GetUTF8();
            Debug.Log(userName);
            String filePath = GetUTF8();
            Debug.Log(filePath);
            String path = Setting.Server._defaultDirectoryPath + Setting._envSep + userName + filePath;
            Debug.Log(path);

            File file = new File(path);
            FileInputStream fi = new FileInputStream(file);
            int len;
            byte[] buf = new byte[1040];
            while ((len = fi.read(buf)) > 0) {
                _os.write(buf, 0, len);
                _os.flush();
                Thread.sleep(1);
            }
            fi.close();
            Close();
        }

        public void CheckFile() throws Exception {
            String userName = GetUTF8();
            List<File> fileList = new ArrayList<>();
            fileList.add(new File(Setting.Server._defaultDirectoryPath + Setting._envSep + userName));
            while (fileList.size() > 0) {
                File file = fileList.get(0);
                fileList.remove(0);
                if (file.isDirectory()) {
                    File[] subFile = file.listFiles();
                    if (subFile == null || subFile.length == 0) {
                        continue;
                    }
                    for (int i = 0; i < subFile.length; i++) {
                        fileList.add(subFile[i]);
                    }
                } else {
                    String path = ChangePath(file.toString(), userName);
                    Debug.Log(path);
                    WriteUTF8(path);
                }
            }
            Close();
        }

        public String ChangePath(String path, String userName) {
            path = path.replace(Setting.Server._defaultDirectoryPath + Setting._envSep, "");
            return path.replace(userName, "");
        }

        public void HasFile() throws Exception {
            String userName = GetUTF8();
            String path = GetUTF8();
            Debug.Log(path);
            path = Setting.Server._defaultDirectoryPath + Setting._envSep + userName + path;
            Debug.Log(path);
            if (new File(path).exists()) {
                WriteUTF8("0");
            }
            else {
                WriteUTF8("-1");
            }
            Close();
        }

        public void MkDir() throws Exception {
            String userName = GetUTF8();
            String path = GetUTF8();
            path = Setting.Server._defaultDirectoryPath + Setting._envSep + userName + path;
            Debug.Log(path);
            new File(path).mkdirs();
            Close();
        }

        public void DelFile() throws Exception {
            String userName = GetUTF8();
            String path = GetUTF8();
            path = Setting.Server._defaultDirectoryPath + Setting._envSep + userName + path;
            Debug.Log(path);
            File file = new File(path);
            if (file.exists()) {
                file.delete();
            }
            file = new File(path + ".meta");
            if (file.exists()) {
                file.delete();
            }
            Close();
        }

        public void GetMD5() throws Exception {
            String userName = GetUTF8();
            String path = GetUTF8();
            path = Setting.Server._defaultDirectoryPath + Setting._envSep + userName + path;
            FileInfo fileInfo = new FileInfo(path+".meta");
            WriteUTF8(fileInfo.MD5);
        }

        public void run() {
            try {
                while (true) {
                    String CommandLind = GetUTF8();
                    Debug.Log("Command: " + CommandLind);
                    if (CommandLind.equals( "SendFile")) {
                        GetFile();
                        break;
                    }
                    else if (CommandLind.equals("GetFile")) {
                        SendFile();
                        break;
                    }
                    else if (CommandLind.equals("CheckFile")) {
                        CheckFile();
                        break;
                    }
                    else if (CommandLind.equals("LogIn")) {
                        LogIn();
                        break;
                    }
                    else if (CommandLind.equals("Sign")) {
                        Sign();
                        break;
                    }
                    else if (CommandLind.equals("HasFile")) {
                        HasFile();
                        break;
                    }
                    else if (CommandLind.equals("MkDir")) {
                        MkDir();
                        break;
                    }
                    else if (CommandLind.equals("DelFile")) {
                        DelFile();
                        break;
                    }
                    else if (CommandLind.equals("GetMD5")) {
                        GetMD5();
                        break;
                    }
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
