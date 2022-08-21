package de.uscoutz.nexus.regions;

import net.kyori.adventure.text.Component;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.EntityType;
import org.bukkit.util.BoundingBox;

public class Region {

    private BoundingBox boundingBox;

    public Region(World world, int minX, int maxX, int minZ, int maxZ, String s) {
        minX -= 1;
        minZ -= 1;
        maxX += 1;
        maxZ += 1;
        boundingBox = new BoundingBox(minX, 0, minZ, maxX, 100, maxZ);

        /*ArmorStand min = (ArmorStand) world.spawnEntity(new Location(world, minX, -50, minZ), EntityType.ARMOR_STAND);
        min.setCustomNameVisible(true);
        min.customName(Component.text("min" + s));

        ArmorStand max = (ArmorStand) world.spawnEntity(new Location(world, maxX, -50, maxZ), EntityType.ARMOR_STAND);
        max.setCustomNameVisible(true);
        max.customName(Component.text("max " + s));*/
    }

    public boolean overlap(int minX, int maxX, int minZ, int maxZ) {
        return new BoundingBox(minX, 0, minZ, maxX, 100, maxZ).overlaps(boundingBox);
    }
}
