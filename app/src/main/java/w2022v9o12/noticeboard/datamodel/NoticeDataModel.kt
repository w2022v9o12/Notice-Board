package w2022v9o12.noticeboard.datamodel

data class NoticeDataModel(
    var date: String = "",
    var title: String = "",
    var content: String = "",
    var isClick: Boolean = false
)