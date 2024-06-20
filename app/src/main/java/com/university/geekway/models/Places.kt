package com.university.geekway.models

class Places {

    var uid: String = ""
    var id: String = ""
    var placename: String = ""
    var placedescription: String = ""
    var categoryname: String = ""
    var categoryId: String = ""
    var cityname: String = ""
    var cityId: String = ""
    var placeImage: String = ""
    var placePublic: String = ""
    var placeWeb: String = ""

    var placeAge: String = ""
    //var Comments: HashM = ""

    var placeAddress: String = ""
    var placeTime: String = ""
    var placeTelephone: String = ""
    var placeRating: String = ""
    var placeComments: String = ""
    var placeLat: String = ""
    var placeLng: String = ""
    //var placeUrl: String = ""
    var timestamp: Long = 0
    var isFavorite = false
    var isRecommend = false

    constructor()
    constructor(
        uid: String,
        id: String,
        placename: String,
        placedescription: String,
        categoryname: String,
        categoryId: String,
        cityname: String,
        cityId: String,
        placeImage: String,
        placePublic: String,
        placeWeb: String,

        placeAge: String,
        //Comments: Class,

        placeAddress: String,
        placeTime: String,
        placeTelephone: String,
        placeRating: String,
        placeComments: String,
        placeLat: String,
        placeLng: String,
        //placeUrl: String,
        timestamp: Long,
        isFavorite: Boolean,
        isRecommend: Boolean
    ) {
        this.uid = uid
        this.id = id
        this.placename = placename
        this.placedescription = placedescription
        this.categoryname = categoryname
        this.categoryId = categoryId
        this.cityname = cityname
        this.cityId = cityId
        this.placeImage = placeImage
        this.placePublic = placePublic
        this.placeWeb = placeWeb

        this.placeAge = placeAge
        //this.Comments = Comments

        this.placeAddress = placeAddress
        this.placeTime = placeTime
        this.placeTelephone = placeTelephone
        this.placeRating = placeRating
        this.placeComments = placeComments
        this.placeLat = placeLat
        this.placeLng = placeLng
        //this.placeUrl = placeUrl
        this.timestamp = timestamp
        this.isFavorite = isFavorite
        this.isRecommend = isRecommend
    }
}
