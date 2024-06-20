package com.university.geekway.models

class RecommendPlaces {

    var id: String = ""
    //var cityId: String = ""
    //var categoryId: String = ""
    //var timestamp: Long = 0

    constructor()
    constructor(id: String, cityId: String, categoryId: String, timestamp: Long) {
        this.id = id
        //this.cityId = cityId
        //this.categoryId = categoryId
        //this.timestamp = timestamp
    }
}