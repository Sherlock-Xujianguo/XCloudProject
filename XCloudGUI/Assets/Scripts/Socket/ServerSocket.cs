using System;
using System.Collections;
using System.Collections.Generic;
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
    public static ServerSocket serverSocket = new ServerSocket();
    private ServerSocket() { }

    public static ServerSocket GetInstance()
    {
        return serverSocket;
    }

    private Socket GetSocket()
    {
        Socket socket = new Socket(AddressFamily.InterNetwork, SocketType.Stream, ProtocolType.Tcp);
        IPAddress address = IPAddress.Parse("127.0.0.1");
        IPEndPoint endpoint = new IPEndPoint(address, 4001);
        IAsyncResult result = socket.BeginConnect(endpoint, new AsyncCallback(ConnectCallback), socket);

        bool success = result.AsyncWaitHandle.WaitOne(5000, true);
        if (!success)
        {
            // 超时处理
            socket.Close();
            Debug.Log("Connect Time Out");
            return null;
        }
        else
        {
            return socket;
        }
    }

    private void WriteUTF8(string str, Socket socket)
    {
        byte[] strBytes = Encoding.UTF8.GetBytes(str);
        socket.Send(strBytes);
    }

    private string GetUTF8(Socket socket)
    {
        byte[] str = new byte[1024];
        int len = socket.Receive(str);
        byte[] rst = new byte[len];
        for (int i = 0; i < len; i++)
        {
            rst[i] = str[i];
        }
        return Encoding.UTF8.GetString(rst);
    }

    private void WriteByte(byte[] data, Socket socket)
    {
        socket.Send(data);
    }

    public int Sign() // return 0注册成功，return-1注册失败
    {
        Socket socket = GetSocket();
        WriteUTF8(UserManager.Instance.GetUserEncryptName(), socket);
        int rst = int.Parse(GetUTF8(socket));
        socket.Close();
        return rst;
    }

    private void ConnectCallback(IAsyncResult asyncResult)
    {
        Debug.Log("Connect Success");
    }

    private void SendCallback(IAsyncResult asyncResult)
    {
        Debug.Log("Send Success");
    }
}
