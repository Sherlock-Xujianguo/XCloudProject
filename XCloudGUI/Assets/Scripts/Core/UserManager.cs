using System;
using System.Collections;
using System.Collections.Generic;
using System.IO;
using System.Security.Cryptography;
using System.Text;
using UnityEngine;

public class UserManager
{
    public static UserManager Instance = new UserManager();
    private UserManager() { }
    private string userName;
    private string userPasswd;
    private byte[] userAESKey;
    public string userEncryptName;

    public void Init(string userName, string userPasswd)
    {
        this.userName = userName;
        this.userPasswd = userPasswd;
        userAESKey = GetUserMD5();
        userEncryptName = GetUserEncryptName();
    }

    private byte[] GetUserMD5()
    {
        MD5 md5 = new MD5CryptoServiceProvider();
        byte[] rst = md5.ComputeHash(Encoding.UTF8.GetBytes(userPasswd));
        return rst;
    }

    public string GetUserEncryptName()
    {
        return Convert.ToBase64String(AES.Encrypt(Encoding.UTF8.GetBytes(userName), GetUserMD5()));
    }

    public byte[] Encrypt(byte[] data)
    {
        return AES.Encrypt(data, userAESKey);
    }

    public byte[] Decrypt(byte[] data)
    {
        return AES.Decrypt(data, userAESKey);
    }

    public void EncryptFile(string sourcePath, string targetPath)
    {
        FileStream fread = new FileStream(sourcePath, FileMode.Open, FileAccess.Read);
        FileStream fwrite = new FileStream(targetPath, FileMode.Create, FileAccess.Write);

        byte[] buf = new byte[1024];
        int realLength;
        while ((realLength = fread.Read(buf, 0, buf.Length)) > 0 )
        {
            byte[] realBuf = new byte[realLength];
            for (int i = 0; i < realLength; i++)
            {
                realBuf[i] = buf[i];
            }
            byte[] encryptBuf = AES.Encrypt(realBuf, userAESKey);
            Debug.Log(encryptBuf.Length);
            fwrite.Write(encryptBuf, 0, encryptBuf.Length);
        }
        fread.Close();
        fwrite.Close();
    }

    public void DecryptFile(string sourcePath, string targetPath)
    {
        FileStream fread = new FileStream(sourcePath, FileMode.Open, FileAccess.Read);
        FileStream fwrite = new FileStream(targetPath, FileMode.Create, FileAccess.Write);

        byte[] buf = new byte[1024];
        int realLength;
        while ((realLength = fread.Read(buf, 0, buf.Length)) > 0)
        {
            byte[] realBuf = new byte[realLength];
            for (int i = 0; i < realLength; i++)
            {
                realBuf[i] = buf[i];
            }
            Debug.Log(realBuf.Length);
            byte[] decryptBuf = AES.Decrypt(realBuf, userAESKey);
            Debug.Log(decryptBuf.Length);
            fwrite.Write(decryptBuf, 0, decryptBuf.Length);
        }
        fread.Close();
        fwrite.Close();
    }

    public static void Test()
    {
        Instance.Init("xuhaoran", "12345661");
        Debug.Log(Instance.GetUserEncryptName());
    }
}
