package slimeknights.tconstruct.library.tools.item;

import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Multimap;
import net.minecraft.entity.ai.attributes.Attribute;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.item.ItemStack;
import net.minecraft.util.IItemProvider;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import slimeknights.tconstruct.library.materials.definition.IMaterial;
import slimeknights.tconstruct.library.materials.definition.MaterialId;
import slimeknights.tconstruct.library.tools.helper.TooltipUtil;
import slimeknights.tconstruct.library.tools.nbt.IModifierToolStack;
import slimeknights.tconstruct.library.utils.TooltipFlag;
import slimeknights.tconstruct.library.utils.Util;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

/**
 * Interface to implement for tools that also display in the tinker station
 */
public interface ITinkerStationDisplay extends IItemProvider {
  /**
   * The "title" displayed in the GUI
   */
  default ITextComponent getLocalizedName() {
    return new TranslationTextComponent(asItem().getTranslationKey());
  }

  /** @deprecated use {@link #getStatInformation(IModifierToolStack, PlayerEntity, List, TooltipFlag)} */
  @Deprecated
  default List<ITextComponent> getStatInformation(IModifierToolStack tool, List<ITextComponent> tooltips, TooltipFlag tooltipFlag) {
    return TooltipUtil.getDefaultStats(tool, tooltips, tooltipFlag);
  }

  /**
   * Returns the tool stat information for this tool
   * @param tool         Tool to display
   * @param tooltips     List of tooltips for display
   * @param tooltipFlag  Determines the type of tooltip to display
   */
  default List<ITextComponent> getStatInformation(IModifierToolStack tool, @Nullable PlayerEntity player, List<ITextComponent> tooltips, TooltipFlag tooltipFlag) {
    tooltips = getStatInformation(tool, tooltips, tooltipFlag);
    TooltipUtil.addAttributes(this, tool, player, tooltips, TooltipUtil.SHOW_MELEE_ATTRIBUTES, EquipmentSlotType.MAINHAND);
    return tooltips;
  }

  /**
   * Allows making attribute tooltips more efficient by not parsing the tool twice
   * @param tool   Tool to check for attributes
   * @param slot   Slot with attributes
   * @return  Attribute map
   */
  default Multimap<Attribute,AttributeModifier> getAttributeModifiers(IModifierToolStack tool, EquipmentSlotType slot) {
    return ImmutableMultimap.of();
  }

  /** @deprecated use {@link #getCombinedItemName(ItemStack, ITextComponent, Collection)} */
  @Deprecated
  static ITextComponent getCombinedItemName(ITextComponent itemName, Collection<IMaterial> materials) {
    return getCombinedItemName(ItemStack.EMPTY, itemName, materials);
  }

  /**
   * Combines the given display name with the material names to form the new given name
   *
   * @param itemName the standard display name
   * @param materials the list of materials
   * @return the combined item name
   */
  static ITextComponent getCombinedItemName(ItemStack stack, ITextComponent itemName, Collection<IMaterial> materials) {
    if (materials.isEmpty() || materials.stream().allMatch(IMaterial.UNKNOWN::equals)) {
      return itemName;
    }

    if (materials.size() == 1) {
      IMaterial material = materials.iterator().next();
      // direct name override for this tool
      if (!stack.isEmpty()) {
        MaterialId id = material.getIdentifier();
        String key = stack.getTranslationKey() + ".material." + id.getNamespace() + "." + id.getPath();
        if (Util.canTranslate(key)) {
          return new TranslationTextComponent(key);
        }
      }
      // name format override
      if (Util.canTranslate(material.getTranslationKey() + ".format")) {
        return new TranslationTextComponent(material.getTranslationKey() + ".format", itemName);
      }

      return new TranslationTextComponent(materials.iterator().next().getTranslationKey()).appendSibling(new StringTextComponent(" ")).appendSibling(itemName);
    }

    // multiple materials. we'll have to combine
    StringTextComponent name = new StringTextComponent("");

    Iterator<IMaterial> iter = materials.iterator();

    IMaterial material = iter.next();
    name.appendSibling(new TranslationTextComponent(material.getTranslationKey()));

    while (iter.hasNext()) {
      material = iter.next();
      name.appendString("-").appendSibling(new TranslationTextComponent(material.getTranslationKey()));
    }

    name.appendString(" ").appendSibling(itemName);

    return name;
  }
}
