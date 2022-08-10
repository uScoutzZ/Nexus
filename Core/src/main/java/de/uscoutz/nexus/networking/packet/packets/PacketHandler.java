package de.uscoutz.nexus.networking.packet.packets;

import de.uscoutz.nexus.NexusPlugin;
import de.uscoutz.nexus.networking.packet.Packet;
import org.bukkit.scheduler.BukkitRunnable;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

public class PacketHandler implements Runnable {

    private Socket socket;
    private NexusPlugin plugin;

    public PacketHandler(Socket socket, NexusPlugin plugin) {
        this.socket = socket;
        this.plugin = plugin;
    }

    @Override
    public void run() {
        ObjectOutputStream objectOutputStream = null;
        ObjectInputStream objectInputStream = null;
        try {
            objectInputStream = new ObjectInputStream(socket.getInputStream());
            objectOutputStream = new ObjectOutputStream(socket.getOutputStream());

            Object packetIn = objectInputStream.readObject();

            if (packetIn instanceof Packet) {
                Packet packet = (Packet) packetIn;
                if (packet.getPassword().equals("123")) {
                    ObjectOutputStream finalObjectOutputStream = objectOutputStream;
                    new BukkitRunnable() {
                        @Override
                        public void run() {
                            Object obj = packet.execute();
                            if(!socket.isClosed()) {
                                try {
                                    finalObjectOutputStream.writeObject(obj);
                                    finalObjectOutputStream.flush();
                                } catch (IOException e) {
                                    throw new RuntimeException(e);
                                }
                                System.out.println("[Bridge] Packet erfolgreich verarbeitet.");
                            }
                        }
                    }.runTask(plugin);

                } else {
                    System.out.println("[Bridge] ACHTUNG! Das Packet enthält das falsche Passwort("
                            + packet.getPassword() + ") - " + this.socket.getInetAddress().toString().replace("/", "")
                            + ":" + this.socket.getPort());
                    objectOutputStream.writeObject("[Bridge] Falsches Passwort!");
                    objectOutputStream.flush();
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (objectOutputStream != null) {
                    objectOutputStream.close();
                }
                if (objectInputStream != null) {
                    objectInputStream.close();
                }
                if (this.socket != null) {
                    this.socket.close();
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }
}
