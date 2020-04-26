using System.Collections;
using System.Collections.Generic;
using UnityEngine;

public class MainMenu : MonoBehaviour
{
    public void Open()
    {
        OpenDirectory.Open("C:\\Users\\13974\\Documents\\XCloud");
    }

    public void Test()
    {
        AndroidJavaClass client = new AndroidJavaClass("Client.MainClient");
        AndroidJavaObject MainClient = client.GetStatic<AndroidJavaObject>("Instance");
        Debug.Log(MainClient.Call<string>("TestJar", "hello world"));
    }
}
