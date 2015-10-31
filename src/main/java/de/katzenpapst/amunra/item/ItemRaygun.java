package de.katzenpapst.amunra.item;

import de.katzenpapst.amunra.AmunRa;
import de.katzenpapst.amunra.entity.EntityBaseLaserArrow;
import de.katzenpapst.amunra.entity.EntityLaserArrow;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.IIcon;
import net.minecraft.world.World;

public class ItemRaygun extends ItemAbstractRaygun {

	protected IIcon itemEmptyIcon;

	protected float energyPerShot = 100;

	public ItemRaygun(String assetName) {
		super(assetName);

	}



    @Override
	protected boolean fire(ItemStack itemStack, EntityPlayer entityPlayer, World world) {
    	if(!entityPlayer.capabilities.isCreativeMode) {
    		this.setElectricity(itemStack, this.getElectricityStored(itemStack) - this.energyPerShot);
    	}
    	if (!world.isRemote)
        {
    		world.playSoundAtEntity(entityPlayer, AmunRa.TEXTUREPREFIX+"weapon.lasergun.shot", 1.0F, 1.0F / (itemRand.nextFloat() * 0.4F + 1.2F) + 0.5F);
    		//LaserArrow entityarrow = new LaserArrow(world, entityPlayer);
    		EntityBaseLaserArrow ent = new EntityLaserArrow(world, entityPlayer);
    		world.spawnEntityInWorld(ent);
        }
    	return true;
    }


}