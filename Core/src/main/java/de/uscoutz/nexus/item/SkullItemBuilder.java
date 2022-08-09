package de.uscoutz.nexus.item;

import com.destroystokyo.paper.profile.PlayerProfile;
import com.destroystokyo.paper.profile.ProfileProperty;
import org.bukkit.Bukkit;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Base64;
import java.util.UUID;

public class SkullItemBuilder extends ItemBuilder<SkullMeta> {

    private static final Class<?> gameProfileClass = NmsHelper.getClass("com.mojang.authlib.GameProfile");
    private static final Field propertyMapField = NmsHelper.getField(gameProfileClass, "properties");
    private static Class<?> propertyClass = NmsHelper.getClass("com.mojang.authlib.properties.Property");
    private static Method putProperty = NmsHelper.getMethod(NmsHelper.getClass("com.mojang.authlib.properties.PropertyMap"), "put", Object.class, Object.class);

    public SkullItemBuilder(ItemStack itemStack) {
        super(itemStack);
    }

    public SkullItemBuilder owner(String owner) {
        getMeta().setOwner(owner);
        return this;
    }

    public SkullItemBuilder texture(String texture) {
        try {
            final Object gameProfile = gameProfileClass.getConstructor(UUID.class, String.class).newInstance(UUID.randomUUID(), null);
            final Object propertyMap = propertyMapField.get(gameProfile);
            final Object property = propertyClass.getConstructor(String.class, String.class).newInstance("textures", texture);
            putProperty.invoke(propertyMap, "textures", property);
            NmsHelper.setField(getMeta(), "profile", gameProfile);
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
            e.printStackTrace();
        }
        return this;
    }

    public SkullItemBuilder skinURL(String skinURL) {
        SkullMeta meta = getMeta();
        meta.setOwningPlayer(Bukkit.getOfflinePlayer("Notch"));
        PlayerProfile profile = meta.getPlayerProfile();
        String base64encoded = Base64.getEncoder().encodeToString(("{textures:{SKIN:{url:\"" + skinURL + "\"}}}").getBytes());
        ProfileProperty property = new ProfileProperty("textures", base64encoded);

        profile.getProperties().add(property);
        meta.setPlayerProfile(profile);
        return this;
    }

    public SkullItemBuilder numberSkull(int number) {
        String skinURL = "";
        if(number == 0) {
            skinURL = "http://textures.minecraft.net/texture/a3a487b1f81c9ecc6e18857c6566529e7efa23eef59814fe57d64df8e2cf1";
        } else if(number == 1) {
            skinURL = "http://textures.minecraft.net/texture/bf61269735f1e446becff25f9cb3c823679719a15f7f0fbc9a03911a692bdd";
        } else if(number == 2) {
            skinURL = "http://textures.minecraft.net/texture/7d81a32d978f933deb7ea26aa326e4174697595a426eaa9f2ae5f9c2e661290";
        } else if(number == 3) {
            skinURL = "http://textures.minecraft.net/texture/ceadaded81563f1c87769d6c04689dcdb9e8ca01da35281cd8fe251728d2d";
        } else if(number == 4) {
            skinURL = "http://textures.minecraft.net/texture/6c608c2db525d6d77f7de4b961d67e53e9d7bacdaff31d4ca10fbbf92d66";
        } else if(number == 5) {
            skinURL = "http://textures.minecraft.net/texture/1144c5193435199c135bd47d166ef1b4e2d3218383df9d34e3bb20d9f8e593";
        } else if(number == 6) {
            skinURL = "http://textures.minecraft.net/texture/f61f7e38556856eae5566ef1c44a8cc64af8f3a58162b1dd8016a8778c71c";
        } else if(number == 7) {
            skinURL = "http://textures.minecraft.net/texture/6e1cf31c49a24a8f37849fc3c5463ab64cc9bceb6f276a5c44aedd34fdf520";
        } else if(number == 8) {
            skinURL = "http://textures.minecraft.net/texture/61c9c09d52debc465c32542c68be42bda6f6753fe1deba257327ac5a0c3ad";
        } else if(number == 9) {
            skinURL = "http://textures.minecraft.net/texture/2dcf39f4bcd98484b0b479a7992d9270fe3a59b9b1a806d7a64ffb5b551ad";
        }

        return skinURL(skinURL);
    }
}
