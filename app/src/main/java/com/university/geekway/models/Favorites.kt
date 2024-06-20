package com.university.geekway.models

class Favorites {

    var id: String = ""

    constructor()
    constructor(id: String, cityId: String, categoryId: String, timestamp: Long) {
        this.id = id
    }
}