package kr.sul.itemfarming.setting.itemchance

import kr.sul.servercore.util.Base64Serialization
import org.json.simple.JSONArray
import org.json.simple.JSONObject
import org.json.simple.parser.JSONParser

object NodeDataJsonConverter {

    // TODO
    fun exportToJson(nodeAtTheTop: NodeData): JSONObject {
        return nodeAtTheTop.convertToJson()
    }


    fun importJson(jsonStr: String): NodeData {
        val nodeAtTheTopJsonObject = JSONParser().parse(jsonStr) as JSONObject
        return recursiveConvert(nodeAtTheTopJsonObject)
    }
    private fun recursiveConvert(jsonObject: JSONObject): NodeData {
        var children: ArrayList<NodeData>? = null
        if (jsonObject.containsKey("children")) {
            children = arrayListOf()
            for (child in (jsonObject["children"] as JSONArray).map { it as JSONObject }) {
                children.add(recursiveConvert(child))
            }
        }

        // is ItemNodeData
        if (jsonObject.containsKey("item") && jsonObject.containsKey("amount")) {
            val amount = jsonObject["amount"].toString().toInt()
            val itemForNodeData = if ((jsonObject["item"] as String)
                    .startsWith(ItemForNodeData.NormalItem.identificationCharacter)) {
                ItemForNodeData.NormalItem(
                    Base64Serialization.fromBase64((jsonObject["item"] as String).removePrefix(ItemForNodeData.NormalItem.identificationCharacter)),
                    amount
                )
            } else if ((jsonObject["item"] as String)
                    .startsWith(ItemForNodeData.CrackShotItem.identificationCharacter)) {
                ItemForNodeData.CrackShotItem(
                    (jsonObject["item"] as String).removePrefix(ItemForNodeData.CrackShotItem.identificationCharacter),
                    amount
                )
            } else {
                throw Exception("ItemForNodeData의 타입을 인식하기 위한 prefix에 문제 > ${jsonObject["item"] as? String}")
            }

            return ItemNodeData(jsonObject["name"] as String, jsonObject["chance"].toString().toDouble(), itemForNodeData)
        }
        else {
            return NodeData(jsonObject["name"] as String, jsonObject["chance"].toString().toDouble(), children)
        }
    }
}