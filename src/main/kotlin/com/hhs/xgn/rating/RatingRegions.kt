package com.hhs.xgn.rating

data class RatingRegions(val ratingRegions: List<RatingRegion>){

    fun getRegion(rating: Int):RatingRegion?{
        return ratingRegions.firstOrNull{ it.inRegion(rating)}
    }
    fun getRank(rating: Int):String?{
        return getRegion(rating)?.name
    }

    fun getRankColor(rating: Int):String?{
        return getRegion(rating)?.color
    }
}
