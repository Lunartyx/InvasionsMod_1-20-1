package invmod.common.item;

import invmod.common.mod_Invasion;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.EnumAction;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemSword;
import net.minecraft.world.World;

public class ItemInfusedSword extends ItemSword
{

  public ItemInfusedSword()
  {
    super(ToolMaterial.EMERALD);
    //amount of entity hits it takes to recharge sword.
    this.setMaxDamage(40);
    this.setCreativeTab(mod_Invasion.tabInvmod);
    this.setUnlocalizedName("infusedSword");
    this.setMaxStackSize(1);
  }

  @SideOnly(Side.CLIENT)
  public void registerIcons(IIconRegister par1IconRegister)
  {
    this.itemIcon = par1IconRegister.registerIcon("invmod:" + getUnlocalizedName().substring(5));
  }
  @Override
  public boolean isDamageable()
  {
    return false;
  }
  
  @Override
  public boolean hitEntity(ItemStack itemstack, EntityLivingBase entityliving, EntityLivingBase entityliving1)
  {
    if (this.isDamaged(itemstack))
    {
    	 this.setDamage(itemstack, this.getDamage(itemstack)-1);
      
    }
    return true;
  }

  //get break speed
 //should be getStrVsBlock
@Override
 public float func_150893_a(ItemStack par1ItemStack, Block par2Block)
 {
    if (par2Block == Blocks.web)
    {
      return 15.0F;
    }

    Material material = par2Block.getMaterial();
    return (material != Material.plants) && (material != Material.vine) && (material != Material.coral) && (material != Material.leaves) && (material != Material.sponge)&& (material != Material.cactus) ? 1.0F : 1.5F;
  }

  @Override
  public EnumAction getItemUseAction(ItemStack par1ItemStack)
  {
    return EnumAction.none;
  }
  @Override
  public int getMaxItemUseDuration(ItemStack par1ItemStack)
  {
    return 0;
  }
  
  @Override
  public ItemStack onItemRightClick(ItemStack itemstack, World world, EntityPlayer entityplayer)
  {
    if (itemstack.getItemDamage() == 0)
    {
    	//if player isSneaking then refill hunger else refill health
    	if(entityplayer.isSneaking())
    	{
    		entityplayer.getFoodStats().addStats(6, 0.5f);
    		world.playSoundAtEntity(entityplayer, "random.burp", 0.5F, world.rand.nextFloat() * 0.1F + 0.9F);
    	}else{
      entityplayer.heal(6.0F);
      //spawn heart particles around the player
      world.spawnParticle("heart", entityplayer.posX + 1.5D, entityplayer.posY, entityplayer.posZ, 0.0D, 0.0D, 0.0D);
      world.spawnParticle("heart", entityplayer.posX - 1.5D, entityplayer.posY, entityplayer.posZ, 0.0D, 0.0D, 0.0D);
      world.spawnParticle("heart", entityplayer.posX, entityplayer.posY, entityplayer.posZ + 1.5D, 0.0D, 0.0D, 0.0D);
      world.spawnParticle("heart", entityplayer.posX, entityplayer.posY, entityplayer.posZ - 1.5D, 0.0D, 0.0D, 0.0D);
    	}
    	
    	  itemstack.setItemDamage(this.getMaxDamage());
    }

    return itemstack;
  }

  //check which blocks the item can destory
  //should be canHarvestBlock
  @Override
  public boolean func_150897_b(Block block)
  {
    return block == Blocks.web;
  }

  @Override
  public boolean onBlockDestroyed(ItemStack par1ItemStack, World par2World, Block block, int par4, int par5, int par6, EntityLivingBase par7EntityLivingBase)
  {
    return true;
  }
}