package NetTransformTools;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;

public class ServerTest {
    public static void main(String args[]) throws IOException {
        ServerSocket serverSocket = new ServerSocket(9999);
        Socket sock = serverSocket.accept();

        byte[] buffer = new byte[1024 * 50];
        BufferedInputStream bis = new BufferedInputStream(sock.getInputStream());
        BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream("tempFile"));

        int len = 0;
        while ((len = bis.read(buffer)) > 0) {
            bos.write(buffer, 0, len);
            System.out.println("#");
        }

        bis.close();
        bos.flush();
        bos.close();
        sock.close();
        serverSocket.close();
        System.out.println("Done");

    }
}
