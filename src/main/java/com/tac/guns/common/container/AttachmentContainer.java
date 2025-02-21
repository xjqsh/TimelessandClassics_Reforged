package com.tac.guns.common.container;

import com.tac.guns.common.Gun;
import com.tac.guns.common.container.slot.AttachmentSlot;
import com.tac.guns.common.container.slot.SlotType;
import com.tac.guns.init.ModContainers;
import com.tac.guns.item.TransitionalTypes.TimelessGunItem;
import com.tac.guns.item.attachment.IAttachment;
import com.tac.guns.item.attachment.impl.Attachment;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;

/**
 * Author: Forked from MrCrayfish, continued by Timeless devs
 */
public class AttachmentContainer extends Container {
    private ItemStack weapon;
    private IInventory playerInventory;
    private IInventory weaponInventory = new Inventory(IAttachment.Type.values().length) {
        @Override
        public void markDirty() {
            super.markDirty();
            AttachmentContainer.this.onCraftMatrixChanged(this);
        }
    };

    private boolean loaded = false;

    public AttachmentContainer(int windowId, PlayerInventory playerInventory, ItemStack stack) // reads from attachments inv
    {
        this(windowId, playerInventory);
        if (this.weapon.getItem() instanceof TimelessGunItem) {
            ItemStack[] attachments = new ItemStack[SlotType.values().length+1];
            for (int i = 0; i < SlotType.values().length; i++) {
                attachments[i] = Gun.getAttachment(SlotType.values()[i], stack);
            }
            for (int i = 0; i < SlotType.values().length; i++) {
                this.weaponInventory.setInventorySlotContents(i, attachments[i]);
            }
        }
        this.loaded = true;
    }

    public AttachmentContainer(int windowId, PlayerInventory playerInventory) {
        super(ModContainers.ATTACHMENTS.get(), windowId);
        this.weapon = playerInventory.getCurrentItem();
        this.playerInventory = playerInventory;
        // weapon
        if (this.weapon.getItem() instanceof TimelessGunItem) {
            for (int i = 0; i < 7; i++) {
                 if (i > 3) {
                    this.addSlot(new AttachmentSlot(
                            this, this.weaponInventory, this.weapon, SlotType.values()[i],
                            playerInventory.player, i, 155, 17 + (i - 4) * 18
                    ));
                } else{
                     this.addSlot(new AttachmentSlot(
                             this, this.weaponInventory, this.weapon, SlotType.values()[i],
                             playerInventory.player, i, 5, 17 + i * 18
                     ));
                 }
            }
        }
        // inv
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 9; j++) {
                this.addSlot(new Slot(playerInventory, j + i * 9 + 9, 8 + j * 18, 102 + i * 18));
            }
        }
        // hot bars
        for (int i = 0; i < 9; i++) {
            if (i == playerInventory.currentItem) {
                this.addSlot(new Slot(playerInventory, i, 8 + i * 18, 160) {
                    @Override
                    public boolean canTakeStack(PlayerEntity playerIn) {
                        return false;
                    }
                });
            } else {
                this.addSlot(new Slot(playerInventory, i, 8 + i * 18, 160) {
                    @Override
                    public boolean canTakeStack(PlayerEntity playerIn) {
                        return true;
                    }
                });
            }
        }
    }

    public boolean hasExMag() {
        return Gun.getAttachment(IAttachment.Type.EXTENDED_MAG, this.weapon) != ItemStack.EMPTY;
    }

    public boolean isLoaded() {
        return this.loaded;
    }

    @Override
    public boolean canInteractWith(PlayerEntity playerIn) {
        return true;
    }

    @Override
    public void onCraftMatrixChanged(IInventory inventoryIn) {
        CompoundNBT attachments = new CompoundNBT();

        if (this.weapon.getItem() instanceof TimelessGunItem) {
            for (int i = 0; i < 7; i++) {
                ItemStack attachment = this.getSlot(i).getStack();
                if (attachment.getItem() instanceof IAttachment<?>){
                    checkAndWrite(attachment, attachments);
                }
            }
        }

        CompoundNBT tag = this.weapon.getOrCreateTag();
        tag.put("Attachments", attachments);
        super.detectAndSendChanges();
    }

    private void checkAndWrite(ItemStack attachment, CompoundNBT attachments) {
        if( Attachment.canApplyOn(attachment, (TimelessGunItem) this.weapon.getItem()) ){
            attachments.put(( (IAttachment<?>) attachment.getItem()).getType().getTagKey(), attachment.write(new CompoundNBT()));
//            attachments.put(( (IAttachment<?>) attachment.getItem()).getSlot().getTagKey(), attachment.write(new CompoundNBT()));
        }

    }

    @Override
    public ItemStack transferStackInSlot(PlayerEntity playerIn, int index) {
        ItemStack copyStack = ItemStack.EMPTY;
        Slot slot = this.inventorySlots.get(index);

        if (slot != null && slot.getHasStack()) {
            ItemStack slotStack = slot.getStack();
            copyStack = slotStack.copy();

            if (index < this.weaponInventory.getSizeInventory()) {
                if (!this.mergeItemStack(slotStack, this.weaponInventory.getSizeInventory(), this.inventorySlots.size(), true)) {
                    return ItemStack.EMPTY;
                }
            } else if (!this.mergeItemStack(slotStack, 0, this.weaponInventory.getSizeInventory(), false)) {
                return ItemStack.EMPTY;
            }
            if (slotStack.isEmpty()) {
                slot.putStack(ItemStack.EMPTY);
            } else {
                slot.onSlotChanged();
            }
        }

        return copyStack;
    }

    public IInventory getPlayerInventory() {
        return this.playerInventory;
    }

    public IInventory getWeaponInventory() {
        return this.weaponInventory;
    }
}
