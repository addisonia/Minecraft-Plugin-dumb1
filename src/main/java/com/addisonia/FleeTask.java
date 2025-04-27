package com.addisonia; // Make sure this matches your package!

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
// Import specific animal interfaces we want to check
import org.bukkit.entity.Animals;
import org.bukkit.entity.AnimalTamer;
import org.bukkit.entity.Chicken;
import org.bukkit.entity.Cow;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Pig;
import org.bukkit.entity.Player;
import org.bukkit.entity.Rabbit;
import org.bukkit.entity.Sheep;
import org.bukkit.entity.Tameable;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

// No longer need Set/HashSet/Arrays
import java.util.List;
import java.util.UUID;
import java.util.logging.Level;

public class FleeTask extends BukkitRunnable {

    private final Plugin plugin;
    private final double fleeRadius;
    private final double fleeRadiusSquared;
    private final double fleeSpeedMultiplier;

    // Keep debug mode for now, can set to false later
    private static final boolean DEBUG_MODE = true;

    // No longer need the FLEEING_ANIMALS set

    public FleeTask(Plugin plugin, double fleeRadius, double fleeSpeedMultiplier) {
        this.plugin = plugin;
        this.fleeRadius = fleeRadius;
        this.fleeRadiusSquared = fleeRadius * fleeRadius;
        this.fleeSpeedMultiplier = fleeSpeedMultiplier;
        if (DEBUG_MODE) {
            plugin.getLogger().info("FleeTask initialized. Radius: " + fleeRadius + ", Speed Multiplier: " + fleeSpeedMultiplier);
        }
    }

    @Override
    public void run() {
        long currentTick = Bukkit.getServer().getCurrentTick();

        if (DEBUG_MODE && currentTick % 100 == 0) {
            plugin.getLogger().info("FleeTask running...");
        }

        for (Player player : Bukkit.getOnlinePlayers()) {
            Location playerLoc = player.getLocation();
            UUID playerUUID = player.getUniqueId();

            if (DEBUG_MODE && currentTick % 100 == 0) {
                plugin.getLogger().info("Checking player: " + player.getName() + " at " + playerLoc.getWorld().getName());
            }

            List<Entity> nearbyEntities = player.getNearbyEntities(fleeRadius, fleeRadius, fleeRadius);

            if (DEBUG_MODE && !nearbyEntities.isEmpty() && currentTick % 40 == 0) {
                plugin.getLogger().info("Found " + nearbyEntities.size() + " nearby entities for player " + player.getName());
            }

            for (Entity entity : nearbyEntities) {
                // Check if it's an Animal first
                if (entity instanceof Animals) {
                    Animals animal = (Animals) entity;
                    String animalType = animal.getType().toString();
                    String actualClassName = animal.getClass().getName(); // Keep for logging if needed

                    if (DEBUG_MODE) {
                        plugin.getLogger().info("Found animal: " + animalType + " near " + player.getName() + " | Actual Class: " + actualClassName);
                    }

                    // *** REPLACED SET CHECK WITH INSTANCEOF CHECKS ***
                    boolean shouldFlee = false;
                    if (animal instanceof Cow ||
                        animal instanceof Sheep ||
                        animal instanceof Pig ||
                        animal instanceof Chicken ||
                        animal instanceof Rabbit) {
                        shouldFlee = true;
                    }

                    if (shouldFlee) {
                        if (DEBUG_MODE) {
                            plugin.getLogger().info("Animal " + animalType + " IS a flee target (instanceof check passed).");
                        }

                        // --- Additional Checks (Remain the same) ---
                        if (animal.isLeashed()) {
                             if (DEBUG_MODE) plugin.getLogger().info("Animal " + animalType + " is leashed. Skipping.");
                            continue;
                        }
                        if (!animal.getPassengers().isEmpty()) {
                             if (DEBUG_MODE) plugin.getLogger().info("Animal " + animalType + " is being ridden. Skipping.");
                            continue;
                        }
                        if (animal instanceof Tameable) {
                            Tameable tameable = (Tameable) animal;
                            if (tameable.isTamed()) {
                                AnimalTamer owner = tameable.getOwner();
                                if (owner != null && playerUUID.equals(owner.getUniqueId())) {
                                     if (DEBUG_MODE) plugin.getLogger().info("Animal " + animalType + " is tamed by " + player.getName() + ". Skipping.");
                                    continue;
                                }
                            }
                        }

                        // --- Flee Logic (Remains the same) ---
                        Location animalLoc = animal.getLocation();
                        if (!playerLoc.getWorld().equals(animalLoc.getWorld())) {
                             if (DEBUG_MODE) plugin.getLogger().warning("Player " + player.getName() + " and animal " + animalType + " are in different worlds! Skipping.");
                            continue;
                        }

                        double distanceSq = playerLoc.distanceSquared(animalLoc);
                         if (DEBUG_MODE) {
                             plugin.getLogger().info("Distance squared to " + animalType + ": " + distanceSq + " (Radius squared: " + fleeRadiusSquared + ")");
                         }

                        if (distanceSq < fleeRadiusSquared) {
                             if (DEBUG_MODE) {
                                 plugin.getLogger().info("Player " + player.getName() + " is CLOSE to " + animalType + ". Attempting to make it flee.");
                             }
                            Vector direction = animalLoc.toVector().subtract(playerLoc.toVector());
                            if (direction.lengthSquared() > 0.01) {
                                Vector fleeVector = direction.normalize().multiply(fleeSpeedMultiplier);
                                fleeVector.setY(fleeVector.getY() * 0.2);
                                 if (DEBUG_MODE) {
                                     plugin.getLogger().info("Applying velocity to " + animalType + ": " + fleeVector.toString());
                                 }
                                animal.setVelocity(fleeVector);
                            } else {
                                 if (DEBUG_MODE) {
                                     plugin.getLogger().info("Animal " + animalType + " is too close to player " + player.getName() + " to calculate flee vector. Skipping velocity set.");
                                 }
                            }
                        }
                    } else {
                        // Log if the animal is not one of the types we check
                        if (DEBUG_MODE) {
                             plugin.getLogger().info("Animal " + animalType + " is not one of the types checked with instanceof. Skipping flee logic.");
                        }
                    }
                }
            }
        }
    }
}
