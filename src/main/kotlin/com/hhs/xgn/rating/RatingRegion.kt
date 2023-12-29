package com.hhs.xgn.rating

data class RatingRegion(val l:Int,
                        /**
                         * Exclusive
                         */
                        val r:Int, val name: String,
                        /**
                         * rrggbb format
                         */
                        val color: String){
    fun inRegion(rating: Int):Boolean{
        return rating in l..<r
    }
}
