package com.university.geekway.models

class Comments {
    var id = ""
    var date = ""
    var placeId = ""
    var timestamp = ""
    var comment = ""
    var rating = ""
    var uid = ""

    constructor()
    constructor(id: String, date: String, placeId: String, timestamp: String, comment: String, rating: String, uid: String){
        this.id = id
        this.date = date
        this.placeId = placeId
        this.timestamp = timestamp
        this.comment = comment
        this.rating = rating
        this.uid = uid
    }
}