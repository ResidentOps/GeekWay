package com.university.geekway.models

class Cities {

    var id: String = ""
    var cityname: String = ""
    var citydescription: String = ""
    var cityImage: String = ""
    var timestamp: Long = 0
    var uid: String = ""

    constructor()
    constructor(id: String, cityname: String, citydescription: String, cityImage: String, timestamp: Long, uid: String) {
        this.id = id
        this.cityname = cityname
        this.citydescription = citydescription
        this.cityImage = cityImage
        this.timestamp = timestamp
        this.uid = uid
    }
}
