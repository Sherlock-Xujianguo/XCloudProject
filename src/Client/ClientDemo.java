package Client;

import Core.Debug;
import com.sun.tools.javac.Main;

import java.util.Scanner;

public class ClientDemo {
    public static void main(String args[]) throws Exception {
//        MainClient.Instance.SendFile("C:\\Users\\13974\\Documents\\XCloudProject\\src\\Core\\b_d1f86e7a9efb1421bd7f0b996593abd9.jpg", new MainClient.SendFileCallback() {
//            @Override
//            public void OnFail() {
//                Debug.Log("Fail");
//            }
//            @Override
//            public void OnSuccess() {
//                Debug.Log("Yep");
//            }
//        });

//        MainClient.Instance.SendFileTree("C:\\Users\\13974\\Documents\\XCloudProject\\src", new MainClient.SendFileCallback() {
//            @Override
//            public void OnFail() {
//                Debug.Log("Fail");
//            }
//            @Override
//            public void OnSuccess() {
//                Debug.Log("Yep");
//            }
//        });

//        MainClient.Instance.SendFile("C:\\Users\\13974\\Documents\\XCloudProject\\src\\Core\\temp.txt", new MainClient.SendFileCallback() {
//            @Override
//            public void OnFail() {
//                Debug.Log("Fail");
//            }
//            @Override
//            public void OnSuccess() {
//                Debug.Log("Yep");
//            }
//        });
//
        //MainClient.Instance.GetFileTree();

        MainClient.Instance.SendDirectory();
    }
}
