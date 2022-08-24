package de.uscoutz.nexus.utilities;

import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.io.BukkitObjectInputStream;
import org.bukkit.util.io.BukkitObjectOutputStream;
import org.yaml.snakeyaml.external.biz.base64Coder.Base64Coder;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.EOFException;
import java.io.IOException;

public class InventorySerializer {

    public static String toBase64(Inventory inventory) {
        try {
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            BukkitObjectOutputStream dataOutput = new BukkitObjectOutputStream(outputStream);
            dataOutput.writeInt(inventory.getSize());

            for(int i = 0; i < inventory.getSize(); ++i) {
                dataOutput.writeObject(inventory.getItem(i));
            }

            dataOutput.close();
            return Base64Coder.encodeLines(outputStream.toByteArray());
        } catch (Exception var4) {
            throw new IllegalStateException("Unable to save item stacks", var4);
        }
    }

    public static Inventory fromBase64(String data) {
        Inventory inventory = Bukkit.createInventory(null, 9, Component.text("An error occurred"));
        try {
            ByteArrayInputStream inputStream = new ByteArrayInputStream(Base64Coder.decodeLines(data));
            BukkitObjectInputStream dataInput = new BukkitObjectInputStream(inputStream);

            try {
                inventory = Bukkit.createInventory(null, dataInput.readInt(), Component.text(" "));
            } catch (Exception var6) {
                inventory = Bukkit.createInventory(null, 36, Component.text(" "));
            }

            for(int i = 0; i < inventory.getSize(); ++i) {
                try {
                    inventory.setItem(i, (ItemStack)dataInput.readObject());
                } catch (EOFException exception) {
                    //
                }
            }

            dataInput.close();
            return inventory;
        } catch (ClassNotFoundException | IOException exception) {
            exception.printStackTrace();
        }

        return inventory;
    }
}
