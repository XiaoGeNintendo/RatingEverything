package com.hhs.xgn.rating

data class RatingChange(val name: String, val delta: Int, val rank: Int, val totalParticipants: Int, val point: Double){
    /**
     * Used in freemarker only
     */
    fun getSignedDelta():String{
        return delta.toSignedString()
    }
}
data class Rating(var rating: Int, var changes: ArrayList<RatingChange>) {
    fun getRatingHistory(): String {
        var initial=1500
        var out="["
        out+="""
            {
                description:"Initial Rating",
                y: $initial
            },
        """.trimIndent()
        for(change in changes){
            initial+=change.delta
            out+="""
                {
                    description:"${change.name} (${change.delta.toSignedString()})",
                    y: $initial
                },
            """.trimIndent()
        }
        return "$out]"
    }
}
