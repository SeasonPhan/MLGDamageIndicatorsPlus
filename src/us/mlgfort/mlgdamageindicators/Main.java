package us.mlgfort.mlgdamageindicators;

import com.gmail.filoghost.holographicdisplays.api.Hologram;
import com.gmail.filoghost.holographicdisplays.api.HologramsAPI;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityRegainHealthEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import us.mlgfort.mlgdamageindicators.CommandHandler;

import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Created on 7/7/2017.
 *
 * @author RoboMWM
 *
 * Heavily altered by PizzaParrot on 10/20/2019
 */
public class Main extends JavaPlugin implements Listener
{
    JavaPlugin instance;
    Set<Hologram> activeHolograms = new HashSet<>();
    DecimalFormat df = new DecimalFormat("#.#");
    
    int maxDuration;
    int damagePerTick;
    double bounceSpread;
    double bounceHeightMax;
    double bounceHeightMin;
    boolean showFullHearts;
    boolean dynamicDuration;
    boolean critState;
    double critValue;
    String messageCrit;
    String messageDamage;
    String messageHealing;

    public void onEnable()
    {
        instance = this;
        instance.getServer().getPluginManager().registerEvents(this, instance);
        df.setRoundingMode(RoundingMode.HALF_UP);
		getCommand("damageindicators").setExecutor(new CommandHandler(this));
        loadConfig();
        saveDefaultConfig();
    }

    public void onDisable()
    {
        getLogger().info("Cleaning up any active damage indicator holograms...");
        getLogger().info(String.valueOf(cleanupDamageIndicators()) + " holograms removed.");
    }
    
    void loadConfig()
    {
    	showFullHearts = getConfig().getBoolean("show-full-hearts");
    	bounceSpread = getConfig().getDouble("hologram-spread");
    	bounceHeightMin = getConfig().getDouble("hologram-height-min");
    	bounceHeightMax = getConfig().getDouble("hologram-height-max");
    	maxDuration = getConfig().getInt("max-duration");
    	dynamicDuration = getConfig().getBoolean("dynamic-duration");
    	damagePerTick = getConfig().getInt("damage-per-tick");
    	messageDamage = getConfig().getString("message-damage");
    	messageHealing = getConfig().getString("message-healing");
    	critState = getConfig().getBoolean("enable-crit");
    	critValue = getConfig().getDouble("crit-value");
    	messageCrit = getConfig().getString("message-crit");
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
    
    int getMaxLength()
    {
    	if (maxDuration < 0)
    		return 60;
    	if (maxDuration > 1200)
    		return 1200;
    	else
    		return maxDuration;
    }
    
    int getDamageTick()
    {
    	if (damagePerTick <= 0)
    		return 1;
    	else
    		return damagePerTick;
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    void onDisplayDamageIndicator(EntityDamageByEntityEvent event)
    {
        if (event.getFinalDamage() <= 0.05D)
            return;
        if (!(event.getEntity() instanceof LivingEntity))
            return;
        if (event.getEntityType() == EntityType.ARMOR_STAND) //Besides not actually being alive, can lead to AoE damage causing "damage" to the damage indicators.
            return;
        LivingEntity livingEntity = (LivingEntity)event.getEntity();
        Location location;
        if (event.getEntityType() == EntityType.PLAYER)
            location = livingEntity.getLocation().add(0, 2.2D, 0);
        else
            location = livingEntity.getEyeLocation();
        displayIndicator(location, event.getFinalDamage(), true);
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    void onDisplayHealthRegenIndicator(EntityRegainHealthEvent event)
    {
        if (event.getAmount() <= 0.05D)
            return;
        if (!(event.getEntity() instanceof LivingEntity))
            return;
        LivingEntity livingEntity = (LivingEntity)event.getEntity();
        Location location;
        if (event.getEntityType() == EntityType.PLAYER)
            location = livingEntity.getLocation().add(0, 2.2D, 0);
        else
            location = livingEntity.getEyeLocation();
        displayIndicator(location, event.getAmount(), false);
    }

    public static Double r4nd0m(double min, double max) {
        return ThreadLocalRandom.current().nextDouble(min, max);
    }

    public void displayIndicator(final Location location, final double value, final boolean isDamage)
    {
    	double x;
    	double y;
    	double z;
    	if (bounceSpread < 16 && bounceSpread > 0)
    	{
    		x = r4nd0m(-bounceSpread, bounceSpread);
    		z = r4nd0m(-bounceSpread, bounceSpread);
    	}
    	else if (bounceSpread == 0)
    	{
    		x = z = 0;
    	}
    	else 
    	{
            x = r4nd0m(-0.5D, 0.5D);
            z = r4nd0m(-0.5D, 0.5D);
            Bukkit.getServer().getConsoleSender().sendMessage(ChatColor.RED + "[MLGDamageIndicators] Invalid value for hologram spread. Please check your config!");
    	}
    	
    	if (bounceHeightMin > bounceHeightMax)
    	{
    		bounceHeightMin = bounceHeightMax;
    		Bukkit.getServer().getConsoleSender().sendMessage(ChatColor.RED + "[MLGDamageIndicators] Minimum bounce height exceeds maximum bounce height. Please check your config!");
    	}
    	if (bounceHeightMin < 0 || bounceHeightMin > 16)
    	{
    		bounceHeightMin = 0;
    		Bukkit.getServer().getConsoleSender().sendMessage(ChatColor.RED + "[MLGDamageIndicators] Invalid value for minimum hologram bounce height. Please check your config!");
    	}
    	if (bounceHeightMax < 0 || bounceHeightMax > 16)
    	{
    		bounceHeightMax = 1;
    		Bukkit.getServer().getConsoleSender().sendMessage(ChatColor.RED + "[MLGDamageIndicators] Invalid value for maximum hologram bounce height. Please check your config!");
    	}
    	
        y = r4nd0m(bounceHeightMin, bounceHeightMax);
        
        long duration;
        if (dynamicDuration)
        {
        	duration = ((long)value / getDamageTick()) + 10L;
            if (duration > getMaxLength())
            	duration = getMaxLength(); //Cap to config's max duration.
        }
        else
        	duration = getMaxLength();

        final Hologram hologram = HologramsAPI.createHologram(instance, location.add(x, 0D, z));
        
        String getDamage;
        String getHealing;
        String getCrit;
        
        if (showFullHearts)
        {
            getDamage = messageDamage.replace("%amount%", df.format(value / 2));
            getHealing = messageHealing.replace("%amount%", df.format(value / 2));
            getCrit = messageCrit.replace("%amount%", df.format(value / 2));
        } 
        else 
        {
        	getDamage = messageDamage.replace("%amount%", df.format(value));
            getHealing = messageHealing.replace("%amount%", df.format(value));
            getCrit = messageCrit.replace("%amount%", df.format(value));
        }
        
        if (isDamage)
        {
        	if (critState && value > critValue)
        		hologram.appendTextLine(ChatColor.translateAlternateColorCodes('&', getCrit));
        	else
        		hologram.appendTextLine(ChatColor.translateAlternateColorCodes('&', getDamage));
        }
        else
        	hologram.appendTextLine(ChatColor.translateAlternateColorCodes('&', getHealing));
        activeHolograms.add(hologram);

        new BukkitRunnable()
        {
            int phase = 0;

            public void run()
            {
                phase++;
                if (phase >= 2)
                {
                    hologram.delete();
                    activeHolograms.remove(hologram);
                    this.cancel();
                    return;
                }
                hologram.teleport(hologram.getLocation().add(0D, y, 0D));
            }
        }.runTaskTimer(instance, 1L, duration);
    }
}
