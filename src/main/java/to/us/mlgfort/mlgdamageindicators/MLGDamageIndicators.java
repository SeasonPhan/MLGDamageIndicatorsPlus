package to.us.mlgfort.mlgdamageindicators;

import com.gmail.filoghost.holographicdisplays.api.Hologram;
import com.gmail.filoghost.holographicdisplays.api.HologramsAPI;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityRegainHealthEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Created on 7/7/2017.
 *
 * @author RoboMWM
 */
public class MLGDamageIndicators extends JavaPlugin implements Listener
{
    JavaPlugin instance;
    Set<Hologram> activeHolograms = new HashSet<>();
    DecimalFormat df = new DecimalFormat("#.#");

    public void onEnable()
    {
        instance = this;
        instance.getServer().getPluginManager().registerEvents(this, instance);
        if (instance.getServer().getPluginManager().getPlugin("AbsorptionShields") != null)
            instance.getServer().getPluginManager().registerEvents(new ShieldDamageListener(this), instance);
        df.setRoundingMode(RoundingMode.HALF_UP);
    }

    public void onDisable()
    {
        getLogger().info("Cleaning up any active damage indicator holograms...");
        getLogger().info(String.valueOf(cleanupDamageIndicators()) + " holograms removed.");
    }

    public int cleanupDamageIndicators()
    {
        for (Hologram hologram : activeHolograms)
        {
            hologram.delete();
        }
        int i = activeHolograms.size();
        activeHolograms.clear();
        return i;
    }

    //I'm trying to remember why I use highest instead of Monitor. Maybe for plugins that alter behavior (e.g. respawns, minigames) in damage event?
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    void onDisplayDamageIndicator(EntityDamageByEntityEvent event)
    {
        if (event.getFinalDamage() <= 0.05D)
            return;
        if (!(event.getEntity() instanceof LivingEntity))
            return;
        Entity entity = event.getEntity();
        displayIndicator(entity.getLocation(), event.getFinalDamage() / 2D, true);
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    void onDisplayHealthRegenIndicator(EntityRegainHealthEvent event)
    {
        if (event.getAmount() <= 0.05D)
            return;
        if (!(event.getEntity() instanceof LivingEntity))
            return;
        Entity entity = event.getEntity();
        displayIndicator(entity.getLocation(), event.getAmount() / 2D, false);
    }

    public static Double r4nd0m(double min, double max) {
        return ThreadLocalRandom.current().nextDouble(min, max);
    }

    public void displayIndicator(final Location location, final double value, final boolean isDamage)
    {
        ChatColor color;
        if (isDamage)
            color = ChatColor.RED;
        else
            color = ChatColor.GREEN;
        displayIndicator(location, value, isDamage, color);
    }

    public void displayIndicator(final Location location, final double value, final boolean isDamage, ChatColor color)
    {
        double x = r4nd0m(-0.3D, 0.3D);
        double z = r4nd0m(-0.3D, 0.3D);
        Hologram hologram = HologramsAPI.createHologram(instance, location.add(x, 2D, z));
        if (isDamage)
            hologram.appendTextLine(color + "-" + df.format(value));
        else
            hologram.appendTextLine(color + "+" + df.format(value));
        activeHolograms.add(hologram);

        new BukkitRunnable()
        {
            int duration = 2;

            public void run()
            {
                duration--;
                if (duration <= 0)
                {
                    hologram.delete();
                    activeHolograms.remove(hologram);
                    this.cancel();
                    return;
                }
                hologram.teleport(hologram.getLocation().add(0D, 1D, 0D));
            }
        }.runTaskTimer(instance, 1L, ((long)value / 2) + 20L); //Increase display duration by a second per 40 hearts of damage.
    }
}
