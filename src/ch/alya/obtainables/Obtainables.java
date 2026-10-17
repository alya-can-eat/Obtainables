package ch.alya.obtainables;

import org.bukkit.Material;
import org.bukkit.event.Event;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.util.config.Configuration;

import java.util.Random;
import java.util.logging.Logger;

/**
 * Makes blocks and items obtainable that exist in Beta 1.7.3 but cannot be
 * collected in survival: sponge, cobweb, ice, apple and chainmail armour.
 * The world itself is never modified; everything happens through drops,
 * fishing and crafting recipes.
 */
public class Obtainables extends JavaPlugin {

    private static final Logger LOG = Logger.getLogger("Minecraft");

    private final Random random = new Random();

    // chances in percent (0 disables)
    double appleChance;
    double spongeChance;
    double chainmailChance;
    boolean chainmailPlayerKillOnly;

    private boolean iceRecipe;
    private boolean cobwebRecipe;

    // ================================================================== lifecycle

    public void onEnable() {
        Configuration cfg = getConfiguration();
        cfg.load();

        appleChance = cfg.getDouble("apple-chance", 0.5);              // Release 1.0: 1/200
        spongeChance = cfg.getDouble("sponge-fishing-chance", 1.0);
        chainmailChance = cfg.getDouble("chainmail-chance", 2.0);
        chainmailPlayerKillOnly = cfg.getBoolean("chainmail-player-kill-only", true);
        iceRecipe = cfg.getBoolean("ice-recipe", true);
        cobwebRecipe = cfg.getBoolean("cobweb-recipe", true);

        cfg.setProperty("apple-chance", appleChance);
        cfg.setProperty("sponge-fishing-chance", spongeChance);
        cfg.setProperty("chainmail-chance", chainmailChance);
        cfg.setProperty("chainmail-player-kill-only", chainmailPlayerKillOnly);
        cfg.setProperty("ice-recipe", iceRecipe);
        cfg.setProperty("cobweb-recipe", cobwebRecipe);
        cfg.save();

        registerRecipes();

        Drops drops = new Drops(this);
        PluginManager pm = getServer().getPluginManager();
        pm.registerEvent(Event.Type.BLOCK_BREAK, drops.blocks, Event.Priority.Monitor, this);
        pm.registerEvent(Event.Type.LEAVES_DECAY, drops.blocks, Event.Priority.Monitor, this);
        pm.registerEvent(Event.Type.PLAYER_FISH, drops.players, Event.Priority.Highest, this);
        pm.registerEvent(Event.Type.ENTITY_DEATH, drops.entities, Event.Priority.Normal, this);

        LOG.info("[Obtainables] Enabled. Apple " + appleChance + " %, sponge " + spongeChance
                + " %, chainmail " + chainmailChance + " %, ice recipe " + (iceRecipe ? "on" : "off")
                + ", cobweb recipe " + (cobwebRecipe ? "on" : "off"));
    }

    public void onDisable() {
        // Bukkit 1060 cannot remove single recipes, they stay until the server restarts.
    }

    // ================================================================== recipes

    private void registerRecipes() {
        if (iceRecipe) {
            // 9 snow blocks -> 1 ice
            ShapedRecipe ice = new ShapedRecipe(new ItemStack(Material.ICE, 1));
            ice.shape("###", "###", "###");
            ice.setIngredient('#', Material.SNOW_BLOCK);
            getServer().addRecipe(ice);
        }
        if (cobwebRecipe) {
            // 5 string in an X -> 1 cobweb
            ShapedRecipe web = new ShapedRecipe(new ItemStack(Material.WEB, 1));
            web.shape("s s", " s ", "s s");
            web.setIngredient('s', Material.STRING);
            getServer().addRecipe(web);
        }
    }

    // ================================================================== helpers

    /** True with the given chance in percent. */
    boolean roll(double percent) {
        return percent > 0 && random.nextDouble() * 100.0 < percent;
    }

    int nextInt(int bound) {
        return random.nextInt(bound);
    }
}
