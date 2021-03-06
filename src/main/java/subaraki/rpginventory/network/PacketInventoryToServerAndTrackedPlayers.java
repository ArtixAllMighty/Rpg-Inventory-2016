package subaraki.rpginventory.network;

import io.netty.buffer.ByteBuf;
import net.minecraft.entity.EntityTracker;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.world.WorldServer;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import subaraki.rpginventory.capability.playerinventory.RpgInventoryData;

public class PacketInventoryToServerAndTrackedPlayers implements IMessage {

	public ItemStack stack[] = new ItemStack[6];

	public PacketInventoryToServerAndTrackedPlayers() {
	}

	public PacketInventoryToServerAndTrackedPlayers(EntityPlayer player) {
		RpgInventoryData inv = RpgInventoryData.get(player);

		for(int i = 0; i < stack.length; i ++)
			stack[i] = inv.getInventory().getStackInSlot(i);
	}

	@Override
	public void fromBytes(ByteBuf buf) {
		for (int i = 0; i < stack.length; i++){
			stack[i] = ByteBufUtils.readItemStack(buf);
		}
	}

	@Override
	public void toBytes(ByteBuf buf) {
		for (int i = 0; i < stack.length; i++) {
			ByteBufUtils.writeItemStack(buf, stack[i]);
		}
	}

	public static class HandlerPacketInventoryToServerAndTrackedPlayers implements IMessageHandler<PacketInventoryToServerAndTrackedPlayers, IMessage>{

		@Override
		public IMessage onMessage(PacketInventoryToServerAndTrackedPlayers message,MessageContext ctx) {

			EntityPlayer player = (EntityPlayer)ctx.getServerHandler().player;
			WorldServer server = (WorldServer)player.world;

			server.addScheduledTask( ()->{

				RpgInventoryData rpg = RpgInventoryData.get(player);

				for (int i = 0; i < message.stack.length; i++){
					rpg.getInventory().setStackInSlot(i,message.stack[i]);
				}
				
				EntityTracker tracker = server.getEntityTracker();
				for (EntityPlayer entityPlayer : tracker.getTrackingPlayers(player)){
					IMessage packet = new PacketInventoryToTrackedPlayer(player);
					PacketHandler.NETWORK.sendTo(packet, (EntityPlayerMP) entityPlayer);
				}
			});
			return null;
		}
	}
}
