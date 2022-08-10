package de.uscoutz.nexus.networking;

import de.uscoutz.nexus.NexusPlugin;
import de.uscoutz.nexus.networking.packet.packets.PacketHandler;
import org.bukkit.Bukkit;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class NetworkServer {

    private int port;

    private ServerSocket serverSocket;

    private NexusPlugin plugin;

    public NetworkServer(int port, NexusPlugin nexusPlugin) {
        this.port = port;
        this.plugin = nexusPlugin;

        //setupDatabase();
    }

    public void start() {
        new Thread(() -> {

            try {
                serverSocket = new ServerSocket(port);
                System.out.println("[Server] Server wurde auf dem Port: " + port + " gestartet");

                while (!serverSocket.isClosed()) {
                    Socket socket = serverSocket.accept();
                    System.out.println("[Server] Neues Packet von: " + socket.getInetAddress().toString().replace("/", "") + socket.getPort());
                    new PacketHandler(socket, plugin).run();
                }

            } catch (IOException e) {
                System.out.println("[Server] Konnte nicht auf den Port: " + port + " gestartet werden");
                throw new RuntimeException(e);
            }

        }).start();
    }

    /*private void setupDatabase() {
        plugin.getDatabaseAdapter().query("CREATE TABLE IF NOT EXISTS serviceInfo (servername VARCHAR(30), servicePort INT(5))");

        if(plugin.getDatabaseAdapter().keyExists("serviceInfo", "servicePort", port)) {
            plugin.getDatabaseAdapter().delete("serviceInfo", "servicePort", port);
        }

        plugin.getDatabaseAdapter().set("serviceinfo", Bukkit.getServer().getName(), port);
    }*/

}
