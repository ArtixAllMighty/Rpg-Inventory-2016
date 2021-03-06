package subaraki.rpginventory.gui.container;

import net.minecraft.item.ItemStack;
import net.minecraftforge.items.SlotItemHandler;
import subaraki.rpginventory.capability.playerinventory.RpgInventoryData;
import subaraki.rpginventory.enums.JewelTypes;
import subaraki.rpginventory.item.RpgInventoryItem;

public class SlotJewels extends SlotItemHandler{

	public SlotJewels(RpgInventoryData itemHandler, int index, int xPosition, int yPosition) {
		super(itemHandler.getInventory(), index, xPosition, yPosition);
	}

	@Override
	public boolean isItemValid(ItemStack stack) {

		if(stack.isEmpty() || !(stack.getItem() instanceof RpgInventoryItem))
			return false;

		RpgInventoryItem jewel = (RpgInventoryItem) stack.getItem();

		switch (getSlotIndex()) {
		case 0:
			return jewel.armorType == JewelTypes.NECKLACE;
		case 1:
			return jewel.armorType == JewelTypes.CRYSTAL;
		case 2: 
			return jewel.armorType == JewelTypes.CLOAK;
		case 3:
			return jewel.armorType == JewelTypes.GLOVES;
		case 4:
		case 5://falltrough : same return for 4 and 5 !
			return jewel.armorType == JewelTypes.RING;
		}
		return false;
	}
}
