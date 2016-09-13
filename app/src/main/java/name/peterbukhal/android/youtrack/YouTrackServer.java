package name.peterbukhal.android.youtrack;

import com.google.gson.Gson;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.util.Arrays;

import name.peterbukhal.android.youtrack.proto.Ping;

/**
 * Created on 13/09/16 by
 *
 * @author Peter Bukhal (petr@taxik.ru)
 */
public final class YouTrackServer {

    public static void main(String args[]) throws Exception {
        int port = 9876;
        int bufferSize = 1024;
        int inputType = 0;

        for (int index = 0; index <= args.length - 1; index++) {
            String arg = args[index];

            if (arg.equals("--port")) {
                port = Integer.valueOf(args[index + 1]);
            } else if (arg.equals("--buffer-size")) {
                bufferSize = Integer.valueOf(args[index + 1]);
            } else if (arg.equals("--input-type")) {
                inputType = args[index + 1].equals("json") ? 1 : 0;
            }
        }

        final DatagramSocket serverSocket = new DatagramSocket(port);

        final Gson gson = new Gson();
        byte[] receiveData = new byte[bufferSize];

        while (true) {
            final DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
            serverSocket.receive(receivePacket);

            try {
                switch (inputType) {
                    case 0: {
                        System.out.println(Ping.ADAPTER.decode(Arrays.copyOf(
                                receiveData, receivePacket.getLength())).toString());
                    } break;
                    case 1: {
                        System.out.println(gson.fromJson(new String(Arrays.copyOf(
                                receiveData, receivePacket.getLength())),
                                name.peterbukhal.android.youtrack.json.Ping.class).toString());
                    } break;
                }
            } catch (Exception e) {
                System.out.println("Decode error");
            }
        }
    }

}
