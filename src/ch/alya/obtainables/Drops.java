package ch.alya.obtainables;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Item;
import org.bukkit.entity.PigZombie;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.entity.Skeleton;
import org.bukkit.entity.Zombie;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockListener;
import org.bukkit.event.block.LeavesDecayEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.EntityListener;
import org.bukkit.event.player.PlayerFishEvent;
import org.bukkit.event.player.PlayerListener;
import org.bukkit.inventory.ItemStack;

/** All extra drops: apples from oak leaves, sponge from fishing, chainmail from mobs. */
class Drops {

    private static final Material[] CHAINMAIL = {
            Material.CHAINMAIL_HELMET,
            Material.CHAINMAIL_CHESTPLATE,
            Material.CHAINMAIL_LEGGINGS,
            Material.CHAINMAIL_BOOTS
    };

    private final Obtainables plugin;

    final BlockListener blocks = new Blocks();
    final PlayerListener players = new Players();
    final EntityListener entities = new Entities();

    Drops(Obtainables plugin) {
        this.plugin = plugin;
    }

    // ================================================================== apple

    /** Leaves data: lower 2 bits are the tree type, 0 = oak. */
    private static boolean isOakLeaves(Block b) {
        return b.getType() == Material.LEAVES && (b.getData() & 3) == 0;
    }

    private void maybeDropApple(Block b) {
        if (!isOakLeaves(b) || !plugin.roll(plugin.appleChance)) return;
        Location loc = b.getLocation();
        b.getWorld().dropItemNaturally(loc, new ItemStack(Material.APPLE, 1));
    }

    private class Blocks extends BlockListener {

        public void onLeavesDecay(LeavesDecayEvent event) {
            if (event.isCancelled()) return;
            maybeDropApple(event.getBlock());
        }

        public void onBlockBreak(BlockBreakEvent event) {
            if (event.isCancelled()) return;
            // Shears give the leaves block instead, like in Release 1.0
            ItemStack hand = event.getPlayer().getItemInHand();
            if (hand != null && hand.getType() == Material.SHEARS) return;
            maybeDropApple(event.getBlock());
        }
    }

    // ================================================================== sponge

    private class Players extends PlayerListener {

        public void onPlayerFish(PlayerFishEvent event) {
            if (event.isCancelled()) return;
            if (event.getState() != PlayerFishEvent.State.CAUGHT_FISH) return;
            if (!(event.getCaught() instanceof Item)) return;
            if (!plugin.roll(plugin.spongeChance)) return;
            // replaces the raw fish before it is spawned and pulled to the player
            ((Item) event.getCaught()).setItemStack(new ItemStack(Material.SPONGE, 1));
        }
    }

    // ================================================================== chainmail

    private class Entities extends EntityListener {

        public void onEntityDeath(EntityDeathEvent event) {
            Entity e = event.getEntity();
            boolean zombie = e instanceof Zombie && !(e instanceof PigZombie);
            if (!zombie && !(e instanceof Skeleton)) return;
            if (plugin.chainmailPlayerKillOnly && !killedByPlayer(e)) return;
            if (!plugin.roll(plugin.chainmailChance)) return;

            Material piece = CHAINMAIL[plugin.nextInt(CHAINMAIL.length)];
            event.getDrops().add(new ItemStack(piece, 1));
        }
    }

    /** Direct hit or arrow shot by a player. */
    private static boolean killedByPlayer(Entity e) {
        EntityDamageEvent last = e.getLastDamageCause();
        if (!(last instanceof EntityDamageByEntityEvent)) return false;
        Entity damager = ((EntityDamageByEntityEvent) last).getDamager();
        if (damager instanceof Player) return true;
        return damager instanceof Projectile && ((Projectile) damager).getShooter() instanceof Player;
    }
}
