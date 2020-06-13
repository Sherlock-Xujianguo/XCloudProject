using System;
using System.Collections;
using System.Collections.Generic;
using System.IO;
using System.Linq.Expressions;
using System.Net;
using System.Net.Sockets;
using System.Text;
using System.Threading;
using Unity.UIWidgets.material;
using UnityEditor;
using UnityEngine;

public class ServerSocket
{
    public static ServerSocket Instance = new ServerSocket();
    private ServerSocket() { }

    private Socket GetSocket()
    {
        Socket socket = new Socket(AddressFamily.InterNetwork, SocketType.Stream, ProtocolType.Tcp);
        IPAddress address = IPAddress.Parse("127.0.0.1");
        IPEndPoint endpoint = new IPEndPoint(address, 4001);
        socket.Connect(endpoint);
        return socket;
    }

    private void WriteUTF8(string str, Socket socket)
    {
        byte[] strBytes = Encoding.UTF8.GetBytes(str);
        IAsyncResult result = socket.BeginSend(strBytes, 0, strBytes.Length, SocketFlags.None, null, socket);
        while (!result.IsCompleted) { Thread.Sleep(100); }
        return;
    }

    private string GetUTF8(Socket socket)
    {
        byte[] str = new byte[1024];
        int len = socket.Receive(str);
        return Encoding.UTF8.GetString(str, 0, len);
    }

    private void WriteByte(byte[] data, Socket socket)
    {
        socket.Send(data);
    }

    public int Sign() // return 0注册成功，return-1注册失败
    {
        Socket socket = GetSocket();
        WriteUTF8("Sign", socket);
        WriteUTF8(UserManager.Instance.GetUserEncryptName(), socket);
        int rst = int.Parse(GetUTF8(socket));
        socket.Close();
        return rst;
    }

    public int LogIn()
    {
        Socket socket = GetSocket();
        WriteUTF8("LogIn", socket);
        WriteUTF8(UserManager.Instance.GetUserEncryptName(), socket);
        int rst = int.Parse(GetUTF8(socket));
        socket.Close();
        return rst;
    }

    public void SendFile(string filePath)
    {
        Socket socket = GetSocket();
        WriteUTF8("SendFile", socket);
        WriteUTF8(UserManager.Instance.userEncryptName, socket);
        WriteUTF8(ChangeFilePath(filePath), socket);
        WriteUTF8(FileManager.GetFileMD5(filePath), socket);
        WriteUTF8(new FileInfo(filePath).Length.ToString(), socket);

        FileStream fread = new FileStream(filePath, FileMode.Open, FileAccess.Read);
        byte[] buf = new byte[1024];
        int len;
        while ((len = fread.Read(buf, 0, buf.Length)) > 0)
        {
            byte[] realBuf = new byte[len];
            for (int i = 0; i < len; i++)
            {
                realBuf[i] = buf[i];
            }
            byte[] encryptBuf = UserManager.Instance.Encrypt(realBuf);
            IAsyncResult result = socket.BeginSend(encryptBuf, 0, encryptBuf.Length, SocketFlags.None, null, socket);
            while (!result.IsCompleted) { Thread.Sleep(1); }
        }
        socket.Close();
    }

    public void CheckFile()
    {
        Socket socket = GetSocket();
        WriteUTF8("CheckFile", socket);
        WriteUTF8(UserManager.Instance.userEncryptName, socket);

        byte[] buf = new byte[1024];
        int len;
        while ((len = socket.Receive(buf, buf.Length, 0)) > 0)
        {
            string path = Encoding.UTF8.GetString(buf, 0, len);
            path = Setting.defaultDirectory + path;
            DirectoryInfo dir = new DirectoryInfo(path);
            Debug.Log(dir.Parent.ToString());
            if (!Directory.Exists(dir.Parent.ToString())) {
                Directory.CreateDirectory(dir.Parent.ToString());
            }
            if (!File.Exists(path))
            {
                File.Create(path).Dispose();
            }
        }

        socket.Close();
    }

    public void GetAllFile()
    {
        List<string> fileList = new List<string>();
        fileList.Add(FileManager._bindDirectory);
        while (fileList.Count != 0)
        {
            string path = fileList[0];
            fileList.RemoveAt(0);
            if (Directory.Exists(path))
            {
                DirectoryInfo di = new DirectoryInfo(path);
                foreach (DirectoryInfo dir in di.GetDirectories())
                {
                    fileList.Add(dir.ToString());
                }
                foreach (FileInfo sub in di.GetFiles())
                {
                    fileList.Add(sub.ToString());
                }
            }

            else if (File.Exists(path))
            {
                if (path.EndsWith(".meta"))
                {
                    continue;
                }
                string cloudMD5 = GetMD5(path);
                string localMD5 = FileManager.GetFileMD5(path);
                if (localMD5 != cloudMD5)
                {
                    Debug.Log("GetFile: " + path);
                    GetFile(path);
                }
            }
        }
    }

    public string GetMD5(string path)
    {
        Socket socket = GetSocket();
        WriteUTF8("GetMD5", socket);
        WriteUTF8(UserManager.Instance.userEncryptName, socket);
        WriteUTF8(ChangeFilePath(path), socket);

        return GetUTF8(socket);
    }

    public void GetFile(string filePath)
    {
        Socket socket = GetSocket();
        WriteUTF8("GetFile", socket);
        WriteUTF8(UserManager.Instance.userEncryptName, socket);
        WriteUTF8(ChangeFilePath(filePath), socket);


        FileStream fwrite = new FileStream(filePath, FileMode.Create, FileAccess.Write);
        byte[] buf = new byte[1040];
        int len;
        while ((len = socket.Receive(buf, buf.Length, 0)) > 0)
        {
            byte[] realBuf = new byte[len];
            for (int i = 0; i < len; i++)
            {
                realBuf[i] = buf[i];
            }
            byte[] decryptBuf = UserManager.Instance.Decrypt(realBuf);
            fwrite.Write(decryptBuf, 0, decryptBuf.Length);
        }
        fwrite.Close();
        socket.Close();
    }

    public bool HasFile(string filePath)
    {
        Socket socket = GetSocket();
        WriteUTF8("HasFile", socket);
        WriteUTF8(UserManager.Instance.userEncryptName, socket);
        WriteUTF8(ChangeFilePath(filePath), socket);
        Debug.Log(ChangeFilePath(filePath));

        int result = int.Parse(GetUTF8(socket)); 
        socket.Close();
        if (result == 0)
        {
            return true;
        }
        else
        {
            return false;
        }
    }

    public void MkDir(string path)
    {
        Socket socket = GetSocket();
        WriteUTF8("MkDir", socket);
        WriteUTF8(UserManager.Instance.userEncryptName, socket);
        WriteUTF8(ChangeFilePath(path), socket);
        socket.Close();
    }

    public void DelFile(string path)
    {
        Socket socket = GetSocket();
        WriteUTF8("DelFile", socket);
        WriteUTF8(UserManager.Instance.userEncryptName, socket);
        WriteUTF8(ChangeFilePath(path), socket);
        socket.Close();
    }

    public static string ChangeFilePath(string filePath)
    {
        string rst = filePath.Replace(FileManager._bindDirectory, "");
        if (rst == "") rst = "\\";
        return rst;
    }

    private void ConnectCallback(IAsyncResult asyncResult)
    {
        Debug.Log("Connect Success");
    }
}
