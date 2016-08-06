package subaraki.rpginventory.hooks;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import subaraki.rpginventory.capability.CapabilityInventoryProvider;

public class EventHooks {

	@SubscribeEvent
	public void attachCapabilitiesForEntities(AttachCapabilitiesEvent.Entity event){
		final Entity entity = event.getEntity();

		if (entity instanceof EntityPlayer)
			event.addCapability(CapabilityInventoryProvider.KEY, new CapabilityInventoryProvider((EntityPlayer)entity)); 
	}
}