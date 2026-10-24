package ch.alya.obtainables;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.event.block.BlockListener;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.plugin.java.JavaPlugin;

import java.lang.reflect.Field;

/**
 * "Alpha TNT": TNT with data value 1 keeps the pre-Beta 1.7 behaviour and is
 * primed by punching. Vanilla b1.7.3 still supports it, but the item loses its
 * data when placed (ItemBlock.filterData returns 0) and the block primes
 * instead of dropping, so it can never be collected.
 */
class AlphaTnt extends BlockListener {

    static final short DATA = 1;

    /** TNT surrounded by 4 redstone dust -> 1 Alpha TNT. */
    static void registerRecipe(JavaPlugin plugin) {
        ShapedRecipe recipe = new ShapedRecipe(new ItemStack(Material.TNT, 1, DATA));
        recipe.shape(" r ", "rtr", " r ");
        recipe.setIngredient('r', Material.REDSTONE);
        recipe.setIngredient('t', Material.TNT); // data 0, so Alpha TNT cannot be re-crafted
        plugin.getServer().addRecipe(recipe);
    }

    /**
     * TNT items ignore their data value when stacking (Item.bj = "usesData" is
     * false), so Alpha TNT merges into normal TNT and turns back into it.
     * Setting the flag keeps both apart. TNT has no durability, so nothing else
     * depends on it. Reflection, so the plugin does not need NMS at compile time.
     */
    static boolean setSeparateStacks(boolean separate) {
        try {
            Class<?> itemClass = Class.forName("net.minecraft.server.Item");
            Object[] byId = (Object[]) itemClass.getField("byId").get(null);
            Object tnt = byId[Material.TNT.getId()];
            Field usesData = itemClass.getDeclaredField("bj");
            usesData.setAccessible(true);
            usesData.setBoolean(tnt, separate);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Keep the data value of the item on the placed block.
     * Not event.getItemInHand(): ItemBlock creates that with new ItemStack(block),
     * so its data is always 0. The real stack is still in the player's hand,
     * it is only decremented after the event.
     */
    public void onBlockPlace(BlockPlaceEvent event) {
        if (event.isCancelled() || !event.canBuild() || event.getPlayer() == null) return;
        ItemStack hand = event.getPlayer().getItemInHand();
        if (hand == null || hand.getType() != Material.TNT || hand.getDurability() != DATA) return;

        Block placed = event.getBlockPlaced();
        if (placed.getType() == Material.TNT) {
            placed.setData((byte) DATA);
        }
    }
}
