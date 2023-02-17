package kr.sul.itemfarming.farming

import kr.sul.itemfarming.setting.itemchance.ItemNodeData
import kr.sul.itemfarming.setting.itemchance.NodeData
import kr.sul.servercore.util.ItemBuilder.loreIB
import kr.sul.servercore.util.ItemBuilder.nameIB
import org.bukkit.Material
import org.bukkit.inventory.ItemStack
import kotlin.random.Random

class ItemChanceWrapper(
    private val nodeAtTheTop: NodeData
) {

    fun getRandomItem(): List<ItemStack> {
        val list = arrayListOf<ItemStack>()
        for (i in 1..1) {
            val leaftNode = getLeafNodeBasedOnChance(nodeAtTheTop)
            if (leaftNode is ItemNodeData) {
                list.add(leaftNode.item.get())
            } else {
                ItemStack(Material.PAPER).nameIB("§4§lERROR").loreIB(" §f-> node:${leaftNode.name}")
            }
        }
        return list
    }

    private fun getLeafNodeBasedOnChance(node: NodeData): NodeData {
        if (node.childNodes == null) {
            return node
        }
        val totalChance = node.childNodes!!.sumOf { it.chance }
        val randomValue = Random.nextDouble(0.0, totalChance)  // 0(포함)~totalChance(미포함) 이므로, <= 가 아닌 < 써야 함.  if (확률 < 지정된확률)
        var cumulativeChance = 0.0
        for (childNode in node.childNodes!!) {
            cumulativeChance += childNode.chance
            if (randomValue < cumulativeChance) {
                return getLeafNodeBasedOnChance(childNode)
            }
        }
        throw Exception("Error during picking up random node based on their chance")
    }
}