package ch.alya.obtainables;

import net.minecraft.server.Container;
import net.minecraft.server.EntityPlayer;
import net.minecraft.server.Packet102WindowClick;
import net.minecraft.server.Packet103SetSlot;
import net.minecraft.server.Slot;
import net.minecraft.server.SlotResult;
import org.bukkit.Material;
import org.bukkit.craftbukkit.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.packet.PacketListener;
import org.bukkit.event.packet.PacketReceivedEvent;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * The vanilla client still thinks all TNT stacks, no matter the data value.
 * After an inventory click the server trusts the client's prediction and sends
 * no correction, so the client can show merged stacks that the server kept apart.
 *
 * Only clicks on a slot holding TNT can cause this. One tick later, only the
 * clicked slot and the cursor are sent again. Shift-clicks move items to
 * unknown slots, so then the whole window is sent, followed by the crafting
 * result: the client recalculates the result from the matrix and clears it,
 * because it does not know plugin recipes.
 *
 * Uses Poseidon's PacketReceivedEvent, which fires before the click is handled.
 */
class InventorySync extends PacketListener {

    private static final int TNT = Material.TNT.getId();

    private final JavaPlugin plugin;

    InventorySync(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    public void onPacketReceived(PacketReceivedEvent event) {
        if (event.isCancelled() || !(event.getPacket() instanceof Packet102WindowClick)) return;

        Packet102WindowClick click = (Packet102WindowClick) event.getPacket();
        // e = what the client saw in the clicked slot
        if (click.e == null || click.e.id != TNT) return;

        final Player player = event.getPlayer();
        if (player == null) return;

        final int windowId = click.a;
        final int slot = click.b;
        final boolean shift = click.f;

        plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() {
            public void run() {
                if (player.isOnline()) resync(((CraftPlayer) player).getHandle(), windowId, slot, shift);
            }
        }, 1L);
    }

    private static void resync(EntityPlayer ep, int windowId, int slot, boolean shift) {
        Container c = ep.activeContainer;
        if (shift || c.windowId != windowId || slot < 0 || slot >= c.e.size()) {
            ep.updateInventory(c); // all slots and the cursor
            for (int i = 0; i < c.e.size(); i++) {
                Slot s = (Slot) c.e.get(i);
                if (s instanceof SlotResult) sendSlot(ep, c.windowId, i, s);
            }
        } else {
            sendSlot(ep, c.windowId, slot, (Slot) c.e.get(slot));
            ep.netServerHandler.sendPacket(new Packet103SetSlot(-1, -1, ep.inventory.j())); // cursor
        }
    }

    private static void sendSlot(EntityPlayer ep, int windowId, int index, Slot s) {
        ep.netServerHandler.sendPacket(new Packet103SetSlot(windowId, index, s.getItem()));
    }
}
