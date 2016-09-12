package subaraki.rpginventory.hooks;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.ai.attributes.IAttributeInstance;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.living.LivingDropsEvent;
import net.minecraftforge.event.entity.living.LivingExperienceDropEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.event.entity.player.PlayerEvent.BreakSpeed;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.PlayerTickEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.WorldTickEvent;
import net.minecraftforge.fml.relauncher.Side;
import subaraki.rpginventory.capability.playerinventory.RpgInventoryCapability;
import subaraki.rpginventory.capability.playerinventory.RpgPlayerInventory;
import subaraki.rpginventory.item.RpgInventoryItem;
import subaraki.rpginventory.item.RpgItems;

public class JeweleryEffectsHandler {

	public static final Map<String, Integer> healEffectMap = new HashMap<String /*playername*/, Integer/*ticktime*/>();

	public static final UUID speedUuid_low = UUID.randomUUID();
	public static final UUID speedUuid_mid = UUID.randomUUID();
	public static final UUID speedUuid_high = UUID.randomUUID();
	public static final UUID speedUuid_highest = UUID.randomUUID();

	public static final AttributeModifier speed_low = new AttributeModifier(speedUuid_low, "speedMod_low", 0.15, 2).setSaved(false);
	public static final AttributeModifier speed_mid = new AttributeModifier(speedUuid_mid, "speedMod_mid", 0.2, 2).setSaved(false);
	public static final AttributeModifier speed_high = new AttributeModifier(speedUuid_high, "speedMod_high", 0.3, 2).setSaved(false);
	public static final AttributeModifier speed_highest = new AttributeModifier(speedUuid_highest, "speedMod_highest", 0.4, 2).setSaved(false);

	public JeweleryEffectsHandler() {
		MinecraftForge.EVENT_BUS.register(this);
	}

	@SubscribeEvent
	public void serverTick(WorldTickEvent event){
		if(!event.side.equals(Side.SERVER))
			return;
		getRegenFromDiamondJewelry(event);
	}

	@SubscribeEvent
	public void onHurtEvent(LivingHurtEvent event){
		getLapisJeweleryStrenghtBonus(event);
		getEmeraldGlovesDamageReduction(event);
	}

	@SubscribeEvent
	public void onBreakBlock(BreakSpeed event){
		getEmeraldRingEffect1(event);
	}

	@SubscribeEvent
	public void onPlayerUpdate(PlayerTickEvent event){

		getGoldenSpeedBoost(event.player);
		getEmeraldRingEffect2(event);
	}

	@SubscribeEvent
	public void onExpEvent(LivingExperienceDropEvent event){
		getEmeraldNecklaceEffect(event);
	}
	
	/////////////////////////////////////////////////////////////////////////////////////////////////////////
	//////////////////////////////////////End Of Subscribed Events///////////////////////////////////////////
	/////////////////////////////////////////////////////////////////////////////////////////////////////////


	private void getEmeraldNecklaceEffect(LivingExperienceDropEvent event) {
		EntityPlayer player = event.getAttackingPlayer();

		if(player == null)
			return;
		if(player.getCapability(RpgInventoryCapability.CAPABILITY,null).getNecklace() == null)
			return;

		ItemStack necklace = player.getCapability(RpgInventoryCapability.CAPABILITY,null).getNecklace();

		if(necklace.getItem() == null)
			return;
		if(!necklace.getItem().equals(RpgItems.emerald_necklace))
			return;
		
		float exp = (float)event.getOriginalExperience();
		float bonus = (float)exp/4f;
		
		event.setDroppedExperience((int)(exp+bonus));
	}

	private void getLapisJeweleryStrenghtBonus(LivingHurtEvent event){
		Entity damager = event.getSource().getSourceOfDamage();
		if(!(damager instanceof EntityPlayer))
			return;

		EntityPlayer player = (EntityPlayer)damager;

		RpgPlayerInventory inventory = player.getCapability(RpgInventoryCapability.CAPABILITY, null);

		float extraDamage = 0;

		if(inventory.getNecklace() != null)
			if(inventory.getNecklace().getItem().getUnlocalizedName().contains("lapis"))
				extraDamage +=1.75;
		if(inventory.getGloves() != null)
			if(inventory.getGloves().getItem().getUnlocalizedName().contains("lapis"))
				extraDamage +=1.75;
		if(inventory.getRing_1() != null)
			if(inventory.getRing_1().getItem().getUnlocalizedName().contains("lapis"))
				extraDamage +=1.75;
		if(inventory.getRing_2() != null)
			if(inventory.getRing_2().getItem().getUnlocalizedName().contains("lapis"))
				extraDamage +=1.75;

		//don't set the damage to the same damage if nothing is worn
		//because that would be stupid i.m.o
		if(extraDamage > 0)
			event.setAmount(event.getAmount()+extraDamage);
	}

	private void getEmeraldGlovesDamageReduction(LivingHurtEvent event){
		if(!(event.getEntityLiving() instanceof EntityPlayer))
			return;

		EntityPlayer player = (EntityPlayer)event.getEntityLiving();

		RpgPlayerInventory inventory = player.getCapability(RpgInventoryCapability.CAPABILITY, null);

		if(inventory.getGloves() == null)
			return;
		if(inventory.getGloves().getItem()== null)
			return;
		if(inventory.getGloves().getItem().getUnlocalizedName().contains("emerald")){
			//reduce damage by one fifth of the damage
			//e.g : 5 damage is reduced to 4.
			event.setAmount(event.getAmount() - MathHelper.floor_float(event.getAmount()/5f));
		}
	}

	private void getRegenFromDiamondJewelry(WorldTickEvent event){

		for(String playername : healEffectMap.keySet()){

			//if the player disconnected, or doesn't exist
			EntityPlayer player = 
					event.world.getMinecraftServer().getPlayerList().getPlayerByUsername(playername);

			if(player == null){
				healEffectMap.remove(playername);
				continue;
			}

			//if the player has a counter, subtract 1 from counter
			if(healEffectMap.get(playername) > 0){
				healEffectMap.put(playername, healEffectMap.get(playername) - 1);
				continue;
			}

			//only calculate new timer when the player needs healing !
			if (player.getHealth() >= player.getMaxHealth())
				continue;

			//if the player has lost health and has no counter : calculate time, and put it in map.
			RpgPlayerInventory inventory = player.getCapability(RpgInventoryCapability.CAPABILITY,null);

			int delay = 75;

			if(inventory.getNecklace() != null)
				if(inventory.getNecklace().getItem().getUnlocalizedName().contains("diamond"))
					delay -=10;

			if(inventory.getGloves() != null)
				if(inventory.getGloves().getItem().getUnlocalizedName().contains("diamond"))
					delay -=10;

			if(inventory.getRing_1() != null)
				if(inventory.getRing_1().getItem().getUnlocalizedName().contains("diamond"))
					delay -=10;

			if(inventory.getRing_2() != null)
				if(inventory.getRing_2().getItem().getUnlocalizedName().contains("diamond"))
					delay -=10;

			if(delay == 75)
				continue;

			//at this point, the player needs healing, and the timer is zero 
			if(healEffectMap.get(playername) == 0)
				player.heal(1);

			//reset timer to calculated delay
			healEffectMap.put(playername, delay);
		}
	}

	private void getEmeraldRingEffect1(BreakSpeed event) {
		if(event.getEntityPlayer() != null)
		{
			RpgPlayerInventory inventory = event.getEntityPlayer().getCapability(RpgInventoryCapability.CAPABILITY, null);
			if(inventory== null)
				return;
			if(inventory.getRing_2() == null)
				return;
			if(! (inventory.getRing_2().getItem() instanceof RpgInventoryItem))
				return;

			if(inventory.getRing_2().getItem().getUnlocalizedName().contains("emerald"))
			{
				float speed = event.getOriginalSpeed();
				event.setNewSpeed(speed*4);
			}
		}
	}

	private void getEmeraldRingEffect2(PlayerTickEvent event) {
		if(event.player.getActivePotionEffects().isEmpty())
			return;

		RpgPlayerInventory inventory = event.player.getCapability(RpgInventoryCapability.CAPABILITY, null);

		if(inventory.getRing_1() == null)
			return;
		if(inventory.getRing_1().getItem() == null)
			return;
		if(!inventory.getRing_1().getItem().getUnlocalizedName().contains("emerald"))
			return;

		for(PotionEffect pe : event.player.getActivePotionEffects()){
			if(pe.getPotion().isBadEffect()){
				event.player.removePotionEffect(pe.getPotion());
				break;
			}
		}
	}

	private void breathUnderWater(EntityPlayer player){
		//		if(!player.isInsideOfMaterial(Material.WATER))
		//			return;

		RpgPlayerInventory inventory = player.getCapability(RpgInventoryCapability.CAPABILITY, null);

		if(inventory.getNecklace() == null)
			return;
		if(inventory.getNecklace().getItem() == null)
			return;
		if(inventory.getNecklace().getItem().getUnlocalizedName().contains("emerald")){
		}
	}

	private void getGoldenSpeedBoost(EntityPlayer player){
		RpgPlayerInventory inventory = player.getCapability(RpgInventoryCapability.CAPABILITY, null);
		int numberofgoldjewels = 0;

		if(inventory.getGloves() != null)
			if(inventory.getGloves().getItem().getUnlocalizedName().contains("gold"))
				numberofgoldjewels++;
		if(inventory.getNecklace() != null)
			if(inventory.getNecklace().getItem().getUnlocalizedName().contains("gold"))
				numberofgoldjewels++;
		if(inventory.getRing_1() != null)
			if(inventory.getRing_1().getItem().getUnlocalizedName().contains("gold"))
				numberofgoldjewels++;
		if(inventory.getRing_2() != null)
			if(inventory.getRing_2().getItem().getUnlocalizedName().contains("gold"))
				numberofgoldjewels++;

		if(numberofgoldjewels == 0)
			return;

		else{
			IAttributeInstance attribute = player.getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED);

			//reset if number of gold objects worn doesn't match the speed boost
			if(attribute.getModifier(speedUuid_low)!=null && numberofgoldjewels != 1)
				attribute.removeModifier(speedUuid_low);
			if(attribute.getModifier(speedUuid_mid)!= null && numberofgoldjewels != 2)
				attribute.removeModifier(speedUuid_mid);
			if(attribute.getModifier(speedUuid_high)!=null && numberofgoldjewels != 3)
				attribute.removeModifier(speedUuid_high);
			if(attribute.getModifier(speedUuid_highest)!=null && numberofgoldjewels != 4)
				attribute.removeModifier(speedUuid_highest);

			//apply speed boost if needed
			switch (numberofgoldjewels){
			case 1:

				if(attribute.getModifier(speedUuid_low)==null)
					attribute.applyModifier(speed_low);
				break;
			case 2:
				if(attribute.getModifier(speedUuid_mid)==null)
					attribute.applyModifier(speed_mid);
				break;
			case 3:
				if(attribute.getModifier(speedUuid_high)==null)
					attribute.applyModifier(speed_high);
				break;
			case 4:
				if(attribute.getModifier(speedUuid_highest)==null)
					attribute.applyModifier(speed_highest);
				break;
			}
		}
	}
}