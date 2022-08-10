package de.uscoutz.nexus.networking.packet;

import eu.thesimplecloud.api.service.ICloudService;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.net.InetSocketAddress;
import java.net.Socket;

public abstract class Packet implements Serializable {

    private String password;

    public Packet(String password) {
        this.password = password;
    }

    public Object send(ICloudService iCloudService) {
        return send(iCloudService.getPort()+70);
    }

    public Object send(int port) {

        Object result = "Fehler beim Empfangen des Results";

        Socket socket = null;
        ObjectOutputStream out = null;
        ObjectInputStream in = null;
        try {
            socket = new Socket();
            socket.connect(new InetSocketAddress("127.0.0.1", port));

            out = new ObjectOutputStream(socket.getOutputStream());
            in = new ObjectInputStream(socket.getInputStream());

            out.writeObject(this);
            out.flush();

            result = in.readObject();

        } catch (Exception e) {
            System.out.println("[Bridge] Fehler beim Senden des Packets an 127.0.0.1:"
                    + port + ": "
                    + e.getMessage());
        } finally {
            try {
                if(out != null) {
                    out.close();
                }
                if(in != null) {
                    in.close();
                }
                if(socket != null) {
                    socket.close();
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }

        return result;
    }

    public abstract Object execute();

    public String getPassword() {
        return password;
    }

}
