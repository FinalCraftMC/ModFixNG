/**
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 3
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.
 *
 */

package modfixng.nms.utils.v1_10_R1;

import java.lang.reflect.Field;
import java.util.ArrayList;

import modfixng.nms.utils.NMSUtilsInterface;
import modfixng.utils.ModFixNGUtils;
import net.minecraft.server.v1_10_R1.PlayerInventory;
import net.minecraft.server.v1_10_R1.Slot;
import net.minecraft.server.v1_10_R1.EntityPlayer;
import net.minecraft.server.v1_10_R1.PacketDataSerializer;
import net.minecraft.server.v1_10_R1.PacketPlayOutSetSlot;
import net.minecraft.server.v1_10_R1.Container;
import net.minecraft.server.v1_10_R1.EntityHuman;
import net.minecraft.server.v1_10_R1.IInventory;
import net.minecraft.server.v1_10_R1.ItemStack;
import net.minecraft.server.v1_10_R1.TileEntity;

import org.bukkit.craftbukkit.v1_10_R1.entity.CraftEntity;
import org.bukkit.craftbukkit.v1_10_R1.CraftWorld;
import org.bukkit.craftbukkit.v1_10_R1.entity.CraftPlayer;
import org.bukkit.craftbukkit.v1_10_R1.inventory.CraftItemStack;

import com.comphenix.protocol.events.PacketContainer;

public class NMSUtils implements NMSUtilsInterface {

	@Override
	public boolean hasInventory(org.bukkit.block.Block b) {
		CraftWorld cworld = (CraftWorld) b.getWorld();
		TileEntity te = cworld.getTileEntityAt(b.getX(), b.getY(), b.getZ());
		return te instanceof IInventory;
	}

	@Override
	public boolean hasInventory(org.bukkit.entity.Entity e) {
		CraftEntity centity = (CraftEntity) e;
		return centity.getHandle() instanceof IInventory;
	}

	@Override
	public boolean isInventoryOpen(org.bukkit.entity.Player p) {
		return getPlayerContainer(p).windowId != 0;
	}
 
	@Override
	public String getOpenInventoryName(org.bukkit.entity.Player p) {
		return getPlayerContainer(p).getClass().getName();
	}

	@Override
	public boolean isInventoryValid(org.bukkit.entity.Player p) {
		return getPlayerContainer(p).a(getNMSPlayer(p));
	}

	@Override
	public boolean isTryingToDropOpenCropanalyzer(org.bukkit.entity.Player p, int minecraftslot) throws NoSuchFieldException, SecurityException, IllegalArgumentException, IllegalAccessException {
		if (!getOpenInventoryName(p).equals("ic2.core.item.tool.ContainerCropnalyzer")) {
			return false;
		}
		ItemStack clickeditem = (ItemStack) getPlayerContainer(p).b.get(minecraftslot);
		if (clickeditem.hasTag() && clickeditem.getTag().hasKey("uid")) {
			int clickeduid = clickeditem.getTag().getInt("uid");
			Container container = getPlayerContainer(p);
			Field cropanalyzerField = container.getClass().getDeclaredField("cropnalyzer");
			cropanalyzerField.setAccessible(true);
			Object cropanalyzer = cropanalyzerField.get(container);
			Field itemStackField = cropanalyzer.getClass().getDeclaredField("itemStack");
			itemStackField.setAccessible(true);
			ItemStack opencropanalyzeritemstack = (ItemStack) itemStackField.get(cropanalyzer);
			int openuid = opencropanalyzeritemstack.getTag().getInt("uid");
			return openuid == clickeduid;
		}
		return false;
	}

	@Override
	public boolean isTryingToDropOpenToolBox(org.bukkit.entity.Player p, int minecraftslot) throws NoSuchFieldException, SecurityException, IllegalArgumentException, IllegalAccessException {
		if (!getOpenInventoryName(p).equals("ic2.core.item.tool.ContainerToolbox")) {
			return false;
		}
		ItemStack clickeditem = (ItemStack) getPlayerContainer(p).b.get(minecraftslot);
		if (clickeditem.hasTag() && clickeditem.getTag().hasKey("uid")) {
			int clickeduid = clickeditem.getTag().getInt("uid");
			Container container = getPlayerContainer(p);
			Field tooboxField = container.getClass().getDeclaredField("Toolbox");
			tooboxField.setAccessible(true);
			Object toolbox = tooboxField.get(container);
			Field itemStackField = toolbox.getClass().getSuperclass().getDeclaredField("itemStack");
			itemStackField.setAccessible(true);
			ItemStack opentoolbox = (ItemStack) itemStackField.get(toolbox);
			int openuid = opentoolbox.getTag().getInt("uid");
			return openuid == clickeduid;
		}
		return false;
	}

	@Override
	public void updateSlot(org.bukkit.entity.Player p, int slot, org.bukkit.inventory.ItemStack item) {
		CraftPlayer cplayer = (CraftPlayer) p;
		EntityPlayer nmshuman = cplayer.getHandle();
		nmshuman.playerConnection.sendPacket(new PacketPlayOutSetSlot(0, slot, CraftItemStack.asNMSCopy(item)));
	}

	@Override
	public boolean isBeaconEffectsChoiceValid(PacketContainer packet) {
		PacketDataSerializer serializer = packet.getSpecificModifier(PacketDataSerializer.class).read(0);
		serializer.markReaderIndex();
		int choice1 = serializer.readInt();
		int choice2 = serializer.readInt();
		serializer.resetReaderIndex();
		return ModFixNGUtils.isBeaconEffectValid(choice1) && ModFixNGUtils.isBeaconEffectValid(choice2);
	}

	@Override
	public ArrayList<org.bukkit.inventory.ItemStack> getTopInvetnoryItems(org.bukkit.entity.Player p) {
		ArrayList<org.bukkit.inventory.ItemStack> items = new ArrayList<org.bukkit.inventory.ItemStack>();
		Container container = getPlayerContainer(p);
		for (Slot slot : container.c) {
			if ((slot.getItem() != null) && !(slot.inventory instanceof PlayerInventory)) {
				items.add(CraftItemStack.asCraftMirror(slot.getItem()));
			}
		}
		return items;
	}

	private Container getPlayerContainer(org.bukkit.entity.Player p) {
		return getNMSPlayer(p).activeContainer;
	}

	private EntityHuman getNMSPlayer(org.bukkit.entity.Player p) {
		CraftPlayer cplayer = (CraftPlayer) p;
		EntityHuman nmshuman = cplayer.getHandle();
		return nmshuman;
	}

}