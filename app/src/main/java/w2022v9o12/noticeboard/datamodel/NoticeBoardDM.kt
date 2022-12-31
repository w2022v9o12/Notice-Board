package w2022v9o12.noticeboard.datamodel

data class NoticeBoardDM (
    var uid: String = "",

    var title: String = "",
    var content: String = "",

    var date: String = "",
    var author: String = "",

    var isImageExit: Boolean = false,

    var isClick: Boolean = true
)