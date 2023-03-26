package kr.sul.itemdb

import kr.sul.servercore.util.Base64Serialization
import org.bukkit.inventory.ItemStack

class ItemData(val keyName: String, val itemStack: ItemStack, displayName: String? = null, lore: List<String>? = null) {
    init {
        val meta = itemStack.itemMeta
        if (displayName != null) {
            meta.displayName = displayName
        }
        if (!lore.isNullOrEmpty()) {
            meta.lore = lore
        }
        itemStack.itemMeta = meta
    }

    fun toBase64(): String {
        return Base64Serialization.toBase64(itemStack)
    }
}