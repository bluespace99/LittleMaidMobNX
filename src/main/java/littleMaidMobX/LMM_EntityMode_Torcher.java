package littleMaidMobX;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.material.MaterialLiquid;
import net.minecraft.entity.Entity;
import net.minecraft.entity.ai.EntityAITasks;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.MathHelper;
import net.minecraft.world.World;

public class LMM_EntityMode_Torcher extends LMM_EntityModeBase {
	
	public static final int mmode_Torcher = 0x0020;


	public LMM_EntityMode_Torcher(LMM_EntityLittleMaid pEntity) {
		super(pEntity);
		isAnytimeUpdate = true;
	}

	@Override
	public int priority() {
		return 6200;
	}

	@Override
	public void init() {
		/* langファイルに移動
		ModLoader.addLocalization("littleMaidMob.mode.Torcher", "Torcher");
		ModLoader.addLocalization("littleMaidMob.mode.F-Torcher", "F-Torcher");
		ModLoader.addLocalization("littleMaidMob.mode.D-Torcher", "D-Torcher");
		ModLoader.addLocalization("littleMaidMob.mode.T-Torcher", "T-Torcher");
		*/
		LMM_TriggerSelect.appendTriggerItem(null, "Torch", "");
	}

	@Override
	public void addEntityMode(EntityAITasks pDefaultMove, EntityAITasks pDefaultTargeting) {
		// Torcher:0x0020
		EntityAITasks[] ltasks = new EntityAITasks[2];
		ltasks[0] = pDefaultMove;
		ltasks[1] = pDefaultTargeting;
		
		owner.addMaidMode(ltasks, "Torcher", mmode_Torcher);
	}

	@Override
	public boolean changeMode(EntityPlayer pentityplayer) {
		ItemStack litemstack = owner.maidInventory.getStackInSlot(0);
		if (litemstack != null) {
			if (litemstack.getItem() == Item.getItemFromBlock(Blocks.torch) || LMM_TriggerSelect.checkWeapon(owner.getMaidMaster(), "Torch", litemstack)) {
				owner.setMaidMode("Torcher");
				if (LMM_LittleMaidMobNX.ac_Torcher != null) {
					pentityplayer.triggerAchievement(LMM_LittleMaidMobNX.ac_Torcher);
				}
				return true;
			}
		}
		return false;
	}

	@Override
	public boolean setMode(int pMode) {
		switch (pMode) {
		case mmode_Torcher :
			owner.setBloodsuck(false);
			owner.aiAttack.setEnable(false);
			owner.aiShooting.setEnable(false);
			return true;
		}
		
		return false;
	}

	@Override
	public int getNextEquipItem(int pMode) {
		int li;
		ItemStack litemstack;
		
		// モードに応じた識別判定、速度優先
		switch (pMode) {
		case mmode_Torcher : 
			for (li = 0; li < LMM_InventoryLittleMaid.maxInventorySize; li++) {
				litemstack = owner.maidInventory.getStackInSlot(li);
				if (litemstack == null) continue;
				
				// 松明
				if (litemstack.getItem() == Item.getItemFromBlock(Blocks.torch) || LMM_TriggerSelect.checkWeapon(owner.getMaidMaster(), "Torch", litemstack)) {
					return li;
				}
			}
			break;
		}
		
		return -1;
	}

	@Override
	public boolean checkItemStack(ItemStack pItemStack) {
		return pItemStack.getItem() == Item.getItemFromBlock(Blocks.torch);
	}

	@Override
	public boolean isSearchBlock() {
		return !owner.isMaidWait()&&(owner.getCurrentEquippedItem()!=null);
	}

	@Override
	public boolean shouldBlock(int pMode) {
		return !(owner.getCurrentEquippedItem() == null);
	}

	public static final double limitDistance_Freedom = 361D;
	public static final double limitDistance_Follow  = 100D;

	protected int getBlockLighting(int px, int py, int pz) {
		World worldObj = owner.worldObj;
		//離れすぎている
		if(owner.isFreedom()){
			//自由行動時
			if(owner.func_180486_cf().distanceSqToCenter(px,py,pz) > limitDistance_Freedom){
				return 15;
			}
		}else{
			//追従時
			if(owner.getMaidMasterEntity()!=null){
				if(owner.getMaidMasterEntity().getPosition().distanceSqToCenter(px,py,pz) > limitDistance_Follow){
					return 15;
				}
			}
		}
		
		if (!owner.isMaidWait()) {
			int a = worldObj.getLight(new BlockPos(px,py,pz),true);
			return a;
		}
		return 15;
	}
	
	@Override
	public boolean checkBlock(int pMode, int px, int py, int pz) {
		int v = getBlockLighting(px, py, pz);
		if (v < 8 && canBlockBeSeen(px, py - 1, pz, true, true, false) && !owner.isMaidWait()) {		
			if (owner.getNavigator().tryMoveToXYZ(px, py, pz, 1.0F) ) {
				//owner.playLittleMaidSound(LMM_EnumSound.findTarget_D, true);
				return true;
			}
		}
		return false;
	}

	@Override
	public boolean executeBlock(int pMode, int px, int py, int pz) {
		ItemStack lis = owner.getCurrentEquippedItem();
		if (lis == null) return false;
		
		if(lis.getItem()!=Item.getItemFromBlock(Blocks.torch)) return false;
		
		int li = lis.stackSize;
		// TODO:当たり判定をどうするか
		if (lis.onItemUse(owner.maidAvatar, owner.worldObj, new BlockPos(px, py - 1, pz), EnumFacing.UP, 0.5F, 1.0F, 0.5F)) {
			owner.setSwing(10, LMM_EnumSound.installation, false);
			
			if (owner.maidAvatar.capabilities.isCreativeMode) {
				lis.stackSize = li;
			}
			if (lis.stackSize <= 0) {
				owner.maidInventory.setInventoryCurrentSlotContents(null);
				owner.getNextEquipItem();
			}
		}
		return false;
	}

	public boolean canPlaceItemBlockOnSide(World par1World, int par2, int par3, int par4, EnumFacing par5,
			EntityPlayer par6EntityPlayer, ItemStack par7ItemStack, ItemBlock pItemBlock) {
		// TODO:マルチ対策用、ItemBlockから丸パクリバージョンアップ時は確認すること
		Block var8 = par1World.getBlockState(new BlockPos(par2, par3, par4)).getBlock();
		
		if (Block.isEqualTo(var8, Blocks.snow)) {
			par5 = EnumFacing.UP;
		} else if (!Block.isEqualTo(var8, Blocks.vine) && !Block.isEqualTo(var8, Blocks.tallgrass) &&
				!Block.isEqualTo(var8, Blocks.deadbush)) {
			if (par5 == EnumFacing.DOWN) {
				--par3;
			}
			if (par5 == EnumFacing.UP) {
				++par3;
			}
			if (par5 == EnumFacing.NORTH) {
				--par4;
			}
			if (par5 == EnumFacing.SOUTH) {
				++par4;
			}
			if (par5 == EnumFacing.WEST) {
				--par2;
			}
			if (par5 == EnumFacing.EAST) {
				++par2;
			}
		}
		
		Material lmat = par1World.getBlockState(new BlockPos(par2, par3, par4)).getBlock().getMaterial();
		if (lmat instanceof MaterialLiquid) {
			return false;
		}
		
		return par1World.canBlockBePlaced(Block.getBlockFromItem(pItemBlock), new BlockPos(par2, par3, par4), false, par5, (Entity)null, par7ItemStack);
	}

	@Override
	public void updateAITick(int pMode) {
		// トーチの設置
		if (pMode == mmode_Torcher && owner.getNextEquipItem()) {
			ItemStack lis = owner.getCurrentEquippedItem();
			int lic = lis.stackSize;
			Item lii = lis.getItem();
			World lworld = owner.worldObj;
			
			// 周囲を検索
			int lxx = MathHelper.floor_double(owner.posX);
			int lyy = MathHelper.floor_double(owner.posY);
			int lzz = MathHelper.floor_double(owner.posZ);
			//			mod_LMM_littleMaidMob.Debug("torch-s: %d, %d, %d", lxx, lyy, lzz);
			int ll = 8;
			int ltx = lxx, lty = lyy, ltz = lzz;
			int lil[] = {lyy, lyy - 1, lyy + 1};
			owner.getAvatarIF().getValue();
			for (int x = -1; x < 2; x++) {
				for (int z = -1; z < 2; z++) {
					for (int lyi : lil) {
						int lv = getBlockLighting(lxx + x, lyi, lzz + z);
						if (ll > lv && lii instanceof ItemBlock &&
								canPlaceItemBlockOnSide(lworld, lxx + x, lyi - 1, lzz + z, EnumFacing.UP, owner.maidAvatar, lis, (ItemBlock)lii)
								&& canBlockBeSeen(lxx + x, lyi - 1, lzz + z, true, false, true)) {
//						if (ll > lv && lworld.getBlockMaterial(lxx + x, lyi - 1, lzz + z).isSolid()
//								&& (lworld.getBlockMaterial(lxx + x, lyi, lzz + z) == Material.air
//								|| lworld.getBlockId(lxx + x, lyi, lzz + z) == Block.snow.blockID)
//								&& canBlockBeSeen(lxx + x, lyi - 1, lzz + z, true, false, true)) {
							ll = lv;
							ltx = lxx + x;
							lty = lyi - 1;
							ltz = lzz + z;
//							mod_LMM_littleMaidMob.Debug("torch: %d, %d, %d: %d", ltx, lty, ltz, lv);
						}
					}
				}
			}
			
			if (ll < 8 && lis.onItemUse(owner.maidAvatar, owner.worldObj, new BlockPos(ltx, lty, ltz), EnumFacing.UP, 0.5F, 1.0F, 0.5F)) {
//				mod_LMM_littleMaidMob.Debug("torch-inst: %d, %d, %d: %d", ltx, lty, ltz, ll);
				owner.setSwing(10, LMM_EnumSound.installation, false);
				owner.getNavigator().clearPathEntity();
				if (owner.maidAvatar.capabilities.isCreativeMode) {
					lis.stackSize = lic;
				}
				if (lis.stackSize <= 0) {
					owner.maidInventory.setInventoryCurrentSlotContents(null);
					owner.getNextEquipItem();
				}
			}

		}
	}

}
