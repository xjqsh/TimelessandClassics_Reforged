package com.tac.guns.common.container.slot;

import com.mrcrayfish.obfuscate.common.data.SyncedPlayerData;
import com.tac.guns.common.Gun;
import com.tac.guns.common.container.AttachmentContainer;
import com.tac.guns.init.ModSounds;
import com.tac.guns.init.ModSyncedDataKeys;
import com.tac.guns.item.*;
import com.tac.guns.item.TransitionalTypes.TimelessGunItem;
import com.tac.guns.item.attachment.IAttachment;
import com.tac.guns.item.attachment.impl.Attachment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.container.Slot;
import net.minecraft.item.DyeItem;
import net.minecraft.item.ItemStack;
import net.minecraft.util.SoundCategory;

/**
 * Author: Forked from MrCrayfish, continued by Timeless devs
 */
public class LegacyAttachmentSlot extends Slot {
    private AttachmentContainer container;
    private ItemStack weapon;
    private IAttachment.Type type;    //accept specific type of attachment
    private PlayerEntity player;
    private IAttachment.Type[] types;    //accept multiple types of attachment
    private int index;

    public LegacyAttachmentSlot(AttachmentContainer container, IInventory weaponInventory, ItemStack weapon, IAttachment.Type type, PlayerEntity player, int index, int x, int y) {
        super(weaponInventory, index, x, y);
        this.container = container;
        this.weapon = weapon;
        this.type = type;
        this.player = player;
        this.index = index;
    }

    public LegacyAttachmentSlot(AttachmentContainer container, IInventory weaponInventory, ItemStack weapon, IAttachment.Type[] types, PlayerEntity player, int index, int x, int y) {
        super(weaponInventory, index, x, y);
        this.container = container;
        this.weapon = weapon;
        this.types = types;
        this.player = player;
        this.index = index;
    }

    @Override
    public boolean isEnabled() {
        this.weapon.inventoryTick(player.world, player, index, true);
        if ((this.type == IAttachment.Type.EXTENDED_MAG && this.weapon.getOrCreateTag().getInt("AmmoCount") > ((TimelessGunItem) this.weapon.getItem()).getGun().getReloads().getMaxAmmo())
                || SyncedPlayerData.instance().get(player, ModSyncedDataKeys.RELOADING) || EnchantmentHelper.hasBindingCurse(this.container.getWeaponInventory().getStackInSlot(this.index))) {
            return false;
        }
        if ((this.player.getHeldItemMainhand().getItem() instanceof ScopeItem ||
                this.player.getHeldItemMainhand().getItem() instanceof SideRailItem ||
                this.player.getHeldItemMainhand().getItem() instanceof IrDeviceItem ||
                this.player.getHeldItemMainhand().getItem() instanceof GunSkinItem)) {
            return true;
        } else if (this.weapon.getItem() instanceof GunItem) {
            GunItem item = (GunItem) this.weapon.getItem();
            Gun modifiedGun = item.getModifiedGun(this.weapon);
            if (modifiedGun.canAttachType(this.type))
                return true;
            else if (types != null) {
                for (IAttachment.Type x : types) {
                    if (modifiedGun.canAttachType(x))
                        return true;
                }
            }
            return false;
        }
        return false;
    }

    @Override
    public boolean isItemValid(ItemStack stack) {
        //todo: logic of this part has been reorganized, need to confirm if there are any side effects or bugs.
        //gun attachments
        if(this.weapon.getItem() instanceof TimelessGunItem){
            if(!(stack.getItem() instanceof IAttachment))return false;

            TimelessGunItem weapon = (TimelessGunItem) this.weapon.getItem();
            int maxAmmo = weapon.getGun().getReloads().getMaxAmmo();
            if(this.type == IAttachment.Type.EXTENDED_MAG && Gun.getAmmo(this.weapon) > maxAmmo){
                return false;
            }
            if(SyncedPlayerData.instance().get(player, ModSyncedDataKeys.RELOADING)){
                return false;
            }
            //check extra limit from nbt tags
            Gun modifiedGun = weapon.getModifiedGun(this.weapon);
            if(!Attachment.canApplyOn(stack,weapon)){
                return false;
            }
            IAttachment.Type stackType = ((IAttachment<?>) stack.getItem()).getType();

            if(this.type!=null){
                return stackType == this.type && modifiedGun.canAttachType(this.type);
            }else if(types!=null){
                for (IAttachment.Type t : types) {
                    if (stackType == t){
                        return true;
                    }
                }
            }

        }
        //This part still feels messy
        //maybe we should create a new class for dye slots?
        //dye slot
        if(this.type == IAttachment.Type.SCOPE_BODY_COLOR ||
            this.type == IAttachment.Type.SCOPE_GLASS_COLOR ||
            this.type == IAttachment.Type.SCOPE_RETICLE_COLOR)
        {
            if (this.weapon.getItem() instanceof IColored){
                return  ((IColored) this.weapon.getItem()).canColor(this.weapon) && stack.getItem() instanceof DyeItem;
            }
        }

        return false;
    }

    @Override
    public void onSlotChanged() {
        if (this.container.isLoaded()) {
            this.player.world.playSound(null, this.player.getPosX(), this.player.getPosY() + 1.0, this.player.getPosZ(), ModSounds.UI_WEAPON_ATTACH.get(), SoundCategory.PLAYERS, 0.5F, this.getHasStack() ? 1.0F : 0.75F);
        }
    }

    @Override
    public int getSlotStackLimit() {
        return 1;
    }

    @Override
    public boolean canTakeStack(PlayerEntity player) {
        return true;
    }
}