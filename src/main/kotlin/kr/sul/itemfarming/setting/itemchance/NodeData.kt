package kr.sul.itemfarming.setting.itemchance

import org.json.simple.JSONArray
import org.json.simple.JSONObject

open class NodeData(
    var name: String,
    var chance: Double,
    var childNodes: ArrayList<NodeData>?
) {
    open fun convertToJson(): JSONObject {
        val jsonObject = JSONObject()
        jsonObject["name"] = name
        jsonObject["chance"] = chance

        if (childNodes != null) {
            val jsonChildNodes = JSONArray()
            for (childNode in childNodes!!) {
                jsonChildNodes.add(childNode.convertToJson())
            }
            jsonObject["children"] = jsonChildNodes
        }
        return jsonObject
    }
}