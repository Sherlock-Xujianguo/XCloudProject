using System.Collections;
using System.Collections.Generic;
using System.Text;
using UnityEngine;

public class SocketTest : MonoBehaviour
{
    // Start is called before the first frame update
    void Start()
    {
        UserManager.Instance.Init("xuhaoran", "123456");
        /*ServerSocket s = ServerSocket.GetInstance();

        Debug.Log(s.Sign());*/
        UserManager.Instance.DecryptFile("C:\\Users\\13974\\XCloud\\EncryptTestEN.txt", "C:\\Users\\13974\\XCloud\\EncryptTestDE.txt");
    }

}
