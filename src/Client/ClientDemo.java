package Client;

import Core.Debug;
import com.sun.tools.javac.Main;

import java.util.Scanner;

public class ClientDemo {
    public static void main(String args[]) throws Exception {
        MainClient.Instance.SendFile("temp.txt");
    }
}
