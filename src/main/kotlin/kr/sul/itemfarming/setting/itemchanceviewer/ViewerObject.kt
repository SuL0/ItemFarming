package kr.sul.itemfarming.setting.itemchanceviewer

import com.shampaggon.crackshot2.addition.WeaponNbtParentNodeMgr
import com.shampaggon.crackshot2.addition.util.CrackShotAdditionAPI
import kr.sul.itemfarming.setting.itemchance.ItemForNodeData
import kr.sul.itemfarming.setting.itemchance.ItemNodeData
import kr.sul.itemfarming.setting.itemchance.NodeData
import kr.sul.servercore.util.ItemBuilder.clearLoreIB
import kr.sul.servercore.util.ItemBuilder.loreIB
import org.bukkit.Material
import org.bukkit.inventory.ItemStack

open class ViewerObject(
    private val parentNode: NodeData,
    var node: NodeData
) {
    fun hasChildNode(): Boolean {
        return node.childNodes != null
    }

    fun putItem(item: ItemStack?): Boolean {
        val name = node.name
        val chance = node.chance
        if (node.childNodes != null) {
            return false
        }
        if (item == null) {
            replaceNode(NodeData(name, chance, null))
        } else {
            val itemForNodeData = if (CrackShotAdditionAPI.isValidCrackShotWeapon(item)) {
                ItemForNodeData.CrackShotItem(WeaponNbtParentNodeMgr.getWeaponParentNodeFromNbt(item)!!, item.amount)
            } else {
                ItemForNodeData.NormalItem(item, item.amount)
            }
            replaceNode(ItemNodeData(name, chance, itemForNodeData))
        }
        return true
    }
    private fun replaceNode(nodeToReplace: NodeData) {
        val index = parentNode.childNodes!!.indexOf(node)
        parentNode.childNodes!!.remove(node)
        parentNode.childNodes!!.add(index, nodeToReplace)
        this.node = nodeToReplace
    }

    // render
    fun render(): ItemStack {
        val baseItem = if (node is ItemNodeData) {
            (node as ItemNodeData).item.get().clone()
        } else if (node.childNodes != null) {
            ItemStack(Material.GRAY_WOOL)
        } else {
            ItemStack(Material.WHITE_WOOL)
        }

        val lore = baseItem.lore
        baseItem.clearLoreIB()
        baseItem.loreIB("§6Name: §f${node.name}")
        baseItem.loreIB("§6Chance: §f${node.chance}")
        if (node.childNodes != null) {
            baseItem.loreIB("§c§lCLICK ⇒")
        }
        baseItem.loreIB("§f")
        baseItem.loreIB("§f")
        lore?.forEach { baseItem.loreIB(it) }
        return baseItem
    }
}