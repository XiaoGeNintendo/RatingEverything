package com.hhs.xgn.rating

data class User(val name:String, val ratings: HashMap<String,Rating>)
