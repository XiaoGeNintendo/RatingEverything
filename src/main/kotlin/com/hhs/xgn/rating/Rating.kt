package com.hhs.xgn.rating

data class RatingChange(val name: String, val delta: Int, val rank: Int, val point: Double)
data class Rating(var rating: Int, var changes: ArrayList<RatingChange>)
