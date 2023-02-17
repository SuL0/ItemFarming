package kr.sul.itemfarming.setting.itemchance

import org.json.simple.JSONObject

class ItemNodeData(
    name: String,
    chance: Double,
    var item: ItemForNodeData
) : NodeData(name, chance, null) {
    override fun convertToJson(): JSONObject {
        val jsonObject = super.convertToJson()
        jsonObject["item"] = item.convertItemForJsonFormat()
        jsonObject["amount"] = item.amount
        return jsonObject
    }
}