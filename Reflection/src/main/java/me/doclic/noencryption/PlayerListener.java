package me.doclic.noencryption;

import me.doclic.noencryption.compatibility.Compatibility;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

public class PlayerListener implements Listener {
    @EventHandler(priority = EventPriority.LOWEST)
    public void onPlayerJoin (PlayerJoinEvent e) {
        final Player player = e.getPlayer();
        Compatibility.VERSION_HANDLER.listen(player);
    }
}