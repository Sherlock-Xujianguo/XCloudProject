using JetBrains.Annotations;
using System;
using System.Collections;
using System.Collections.Generic;
using System.IO;
using System.Security.Cryptography;
using System.Text;
using System.Threading;
using UnityEngine;

public class FileManager
{
    public static FileManager Instance = new FileManager();
    private FileManager() { }

    public static string _bindDirectory = Setting.defaultDirectory;
    public static DateTime _startTime;
    public static Thread t = new Thread(WorkThread);

    public int UpdateBindDirectory(string path)
    {
        if (File.Exists(path))
        {
            return 1;
        }
        else if (!Directory.Exists(path))
        {
            return 2;
        }
        _bindDirectory = path;
        return 0;
    }

    public static void DeleteDir(string file)
    {
        try
        {
            //去除文件夹和子文件的只读属性
            //去除文件夹的只读属性
            System.IO.DirectoryInfo fileInfo = new DirectoryInfo(file);
            fileInfo.Attributes = FileAttributes.Normal & FileAttributes.Directory;

            //去除文件的只读属性
            System.IO.File.SetAttributes(file, System.IO.FileAttributes.Normal);

            //判断文件夹是否还存在
            if (Directory.Exists(file))
            {
                foreach (string f in Directory.GetFileSystemEntries(file))
                {
                    if (File.Exists(f))
                    {
                        //如果有子文件删除文件
                        File.Delete(f);
                        Console.WriteLine(f);
                    }
                    else
                    {
                        //循环递归删除子文件夹
                        DeleteDir(f);
                    }
                }

                //删除空文件夹
                Directory.Delete(file);
                Console.WriteLine(file);
            }

        }
        catch (Exception ex) // 异常处理
        {
            Console.WriteLine(ex.Message.ToString());// 异常信息
        }
    }

    public static string GetFileMD5(string filepath)
    {
        FileStream fs = new FileStream(filepath, FileMode.Open, FileAccess.Read, FileShare.Read);
        int bufferSize = 1048576;
        byte[] buff = new byte[bufferSize];
        MD5CryptoServiceProvider md5 = new MD5CryptoServiceProvider();
        md5.Initialize();
        long offset = 0;
        if (fs.Length == 0)
        {
            fs.Close();
            return "null";
        }
        while (offset < fs.Length)
        {
            long readSize = bufferSize;
            if (offset + readSize > fs.Length)
                readSize = fs.Length - offset;
            fs.Read(buff, 0, Convert.ToInt32(readSize));
            if (offset + readSize < fs.Length)
                md5.TransformBlock(buff, 0, Convert.ToInt32(readSize), buff, 0);
            else
                md5.TransformFinalBlock(buff, 0, Convert.ToInt32(readSize));
            offset += bufferSize;
        }
        if (offset >= fs.Length)
        {
            fs.Close();
            byte[] result = md5.Hash;
            md5.Clear();
            StringBuilder sb = new StringBuilder(32);
            for (int i = 0; i < result.Length; i++)
                sb.Append(result[i].ToString("X2"));
            return sb.ToString();
        }
        else
        {
            fs.Close();
            return null;
        }
    }

    public void Start()
    {
        _startTime = DateTime.Parse(PlayerPrefs.GetString("startTime", new DateTime(0).ToString()));
        ServerSocket.Instance.CheckFile();
        ServerSocket.Instance.GetAllFile();
        t.Start();
    }

    private static void WorkThread()
    {
        
        while (true)
        {
            List<string> fileList = new List<string>();
            fileList.Add(_bindDirectory);
            while (fileList.Count != 0)
            {
                if (_startTime > DateTime.Now)
                {
                    Thread.Sleep(10000);
                    continue;
                }
                Debug.Log("!!!!!" + _startTime);

                string path = fileList[0];
                fileList.RemoveAt(0);
                Debug.Log(path);
                if (Directory.Exists(path))
                {
                    DirectoryInfo di = new DirectoryInfo(path);
                    foreach (DirectoryInfo dir in di.GetDirectories())
                    {
                        fileList.Add(dir.ToString());
                        Debug.Log(dir.ToString());
                    }
                    foreach (FileInfo sub in di.GetFiles())
                    {
                        fileList.Add(sub.ToString());
                        Debug.Log(sub.ToString());
                    }

                    if (!ServerSocket.Instance.HasFile(path))
                    {
                        if (File.Exists(path+".meta"))
                        {
                            Debug.Log("delete: " + path);
                            DeleteDir(path);
                            File.Delete(path + ".meta");
                        }
                        else
                        {
                            Debug.Log("Mkdir: " + path);
                            ServerSocket.Instance.MkDir(path);
                            File.Create(path + ".meta").Dispose();
                        }
                    }
                    else if (!File.Exists(path + ".meta"))
                    {
                        File.Create(path + ".meta").Dispose();
                    }
                }
                else if (File.Exists(path)) // 普通文件
                {
                    if (path.EndsWith(".meta")) // 记录文件
                    {
                        if (!File.Exists(path.Substring(0, path.Length-5)) && !Directory.Exists(path.Substring(0, path.Length - 5)))
                        {
                            Debug.Log("del: " + path.Substring(0, path.Length - 5));
                            ServerSocket.Instance.DelFile(path.Substring(0, path.Length - 5));
                            File.Delete(path);
                        }
                    }
                    else
                    {
                        if (!ServerSocket.Instance.HasFile(path)) {
                            if (File.Exists(path + ".meta"))
                            {
                                File.Delete(path);
                                File.Delete(path + ".meta");
                            }
                            else
                            {
                                ServerSocket.Instance.SendFile(path);
                                File.Create(path + ".meta").Dispose();
                            }
                        }
                        else
                        {
                            if (!File.Exists(path + ".meta"))
                            {
                                ServerSocket.Instance.GetFile(path);
                                File.Create(path + ".meta").Dispose();
                            }

                            string cloudMD5 = ServerSocket.Instance.GetMD5(path);
                            string localMD5 = GetFileMD5(path);
                            if (cloudMD5 != localMD5)
                            {
                                ServerSocket.Instance.SendFile(path);
                            }
                        }
                    }
                }
            }
            Debug.Log("!!!!!Sleep");
            Thread.Sleep(10000);
        }
    }
}
