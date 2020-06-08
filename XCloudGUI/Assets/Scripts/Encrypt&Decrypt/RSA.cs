using System.Collections;
using System.Collections.Generic;
using System.Security.Cryptography;
using System.Text;
using UnityEngine;

public class RSA
{
    public static KeyValuePair<byte[], byte[]> GetKeyPair()
    {
        RSACryptoServiceProvider rsa = new RSACryptoServiceProvider();
        byte[] public_key = rsa.ExportCspBlob(false);
        byte[] private_key = rsa.ExportCspBlob(true);
        return new KeyValuePair<byte[], byte[]>(public_key, private_key);
    }

    public static byte[] GetPublicKey(KeyValuePair<byte[], byte[]> keyPair)
    {
        return keyPair.Key;
    }

    public static byte[] GetPrivateKey(KeyValuePair<byte[], byte[]> keyPair)
    {
        return keyPair.Value;
    }

    public static byte[] Encrypt(byte[] data, byte[] public_key)
    {
        RSACryptoServiceProvider rsa = new RSACryptoServiceProvider();
        rsa.ImportCspBlob(public_key);
        return rsa.Encrypt(data, false);
    }

    public static byte[] Decrypt(byte[] data, byte[] private_key)
    {
        RSACryptoServiceProvider rsa = new RSACryptoServiceProvider();
        rsa.ImportCspBlob(private_key);
        return rsa.Decrypt(data, false);
    }

    public static void test()
    {
        string data = "你好呀，我是客户端";
        KeyValuePair<byte[], byte[]> keyPair = GetKeyPair();
        byte[] public_key = keyPair.Key;
        byte[] private_key = keyPair.Value;
        byte[] result = Encrypt(Encoding.UTF8.GetBytes(data), public_key);
        Debug.Log(Encoding.UTF8.GetString(result));
        Debug.Log(Encoding.UTF8.GetString(Decrypt(result, private_key)));
    }
}
