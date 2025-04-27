package com.addisonia; // Make sure this matches your package!

import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;
// Make sure FleeTask exists in the same package or import it if it's different
// import com.addisonia.FleeTask; 

// Change class name to match the file name if you rename Plugin.java later
public final class Plugin extends JavaPlugin { // Use 'Plugin' to match your current file name

    private BukkitTask fleeTask; // Holds the scheduled task

    // Configuration values (can be loaded from config.yml later)
    private double fleeRadius = 10.0;
    private long checkInterval = 10L; // Check every 10 ticks (0.5 seconds)
    private double fleeSpeedMultiplier = 1.2;

    @Override
    public void onEnable() {
        // Plugin startup logic
        getLogger().info("AnimalFlee (dumb1) plugin enabled!"); // Updated log message

        // Create an instance of our runnable task
        // Pass 'this' (the plugin instance), radius, and speed
        FleeTask taskRunnable = new FleeTask(this, fleeRadius, fleeSpeedMultiplier);

        // Schedule the task to run repeatedly
        this.fleeTask = taskRunnable.runTaskTimer(this, 0L, checkInterval);

        getLogger().info("Animal Flee task scheduled to run every " + checkInterval + " ticks.");
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
        getLogger().info("AnimalFlee (dumb1) plugin disabled!"); // Updated log message

        // Cancel the scheduled task if it exists and is running
        if (this.fleeTask != null && !this.fleeTask.isCancelled()) {
            this.fleeTask.cancel();
            getLogger().info("Animal Flee task cancelled.");
        }
    }

    // Optional: Getters for config values if needed elsewhere
    public double getFleeRadius() {
        return fleeRadius;
    }

    public double getFleeSpeedMultiplier() {
        return fleeSpeedMultiplier;
    }
}