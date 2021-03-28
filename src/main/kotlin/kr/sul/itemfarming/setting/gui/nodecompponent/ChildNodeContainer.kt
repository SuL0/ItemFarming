package kr.sul.itemfarming.setting.gui.nodecompponent

// T에 nullable이 들어갈 수가 있어서, Any로 non-null만 들어올 수 있게 범위를 지정해줌
interface ChildNodeContainer<T: Any> {
    val childNodeList: ArrayList<T>
}