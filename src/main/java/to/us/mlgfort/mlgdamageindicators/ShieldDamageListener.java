package to.us.mlgfort.mlgdamageindicators;

import org.bukkit.ChatColor;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import to.us.tf.absorptionshields.event.ShieldDamageEvent;

/**
 * Created on 7/7/2017.
 *
 * @author RoboMWM
 */
public class ShieldDamageListener implements Listener
{
    MLGDamageIndicators instance;

    ShieldDamageListener(MLGDamageIndicators plugin)
    {
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
        instance = plugin;
    }

    @EventHandler
    private void onShieldDamage(ShieldDamageEvent event)
    {
        if (!(event.getBaseEvent() instanceof EntityDamageByEntityEvent))
            return;
        if (event.getDamage() <= 0.05D)
            return;
        instance.displayIndicator(event.getVictim().getLocation(), event.getDamage() / 2D, true, ChatColor.LIGHT_PURPLE);
    }
}
