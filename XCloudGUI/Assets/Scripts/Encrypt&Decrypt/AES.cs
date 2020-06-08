using System;
using System.Collections;
using System.Collections.Generic;
using System.Security.Cryptography;
using System.Text;
using UnityEngine;

public class AES
{
    public static byte[] AesEncrypt(byte[] str, byte[] key)
    {
        if (str == null) return null;

        RijndaelManaged rm = new RijndaelManaged
        {
            Key =key,
            Mode = CipherMode.ECB,
            Padding = PaddingMode.PKCS7
        };

        ICryptoTransform cTransform = rm.CreateEncryptor();
        return cTransform.TransformFinalBlock(str, 0, str.Length);
    }

    public static byte[] AesDecrypt(byte[] str, byte[] key)
    {
        if (str == null) return null;

        RijndaelManaged rm = new RijndaelManaged
        {
            Key = key,
            Mode = CipherMode.ECB,
            Padding = PaddingMode.PKCS7
        };

        ICryptoTransform cTransform = rm.CreateDecryptor();
        return cTransform.TransformFinalBlock(str, 0, str.Length);
    }

    public static void TestAES()
    {
        string str = "嘿，你好漂亮";
        byte[] result = AesEncrypt(GetBytes(str), GetBytes("12345678876543211234567887654abc"));
        Debug.Log(GetString(result));
        Debug.Log(GetString(AesDecrypt(result, GetBytes("12345678876543211234567887654abc"))));
    }

    public static byte[] GetBytes(string str)
    {
        return Encoding.UTF8.GetBytes(str);
    }

    public static string GetString(byte[] str)
    {
        return Encoding.UTF8.GetString(str);
    }
}
