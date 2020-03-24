package NetTransformTools;

import java.io.*;
import java.net.Socket;

public class ClientTest {
    public static void main(String args[]) throws IOException {
        Socket sock = new Socket(args[0], 9999);



        File myFile = new File(args[1]);

        byte[] buffer = new byte[1024 * 50];
        BufferedInputStream bis = new BufferedInputStream(new FileInputStream(myFile));
        BufferedOutputStream bos = new BufferedOutputStream(sock.getOutputStream());

        DataOutputStream outputStream = new DataOutputStream(sock.getOutputStream());
        outputStream.writeUTF(myFile.getName());
        outputStream.flush();

        int len;
        while ((len = bis.read(buffer)) > 0) {
            bos.write(buffer, 0, len);
            System.out.print("#");
        }

        bis.close();
        bos.flush();
        bos.close();
        sock.close();
        System.out.println("Done");
    }
}
