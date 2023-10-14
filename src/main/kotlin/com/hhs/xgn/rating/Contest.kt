package com.hhs.xgn.rating

import java.lang.RuntimeException
import kotlin.collections.*

fun parseRows(lines: List<String>, start: Int): ArrayList<StandingRow>{
    var th=lines[start].split(",")
    th=th.map { it.trim() }
    if(th[0]!="name"){
        throw RuntimeException(th[0]+" should be name in table header")
    }
    if(th[1]!="point"){
        throw RuntimeException(th[1]+" should be point in table header")
    }

    val ret= ArrayList<StandingRow>()
    for(i in start+1..<lines.size){
        val single=lines[i].split(",").map{it.trim()}
        if(single.size!=th.size){
            println("Warning: Skipping malformed line: $single")
            continue
        }
        val map=HashMap<String,String>()
        for(j in 2..<single.size){
            map[th[j]]=single[j]
        }
        ret+=StandingRow(single[0],single[1].toDouble(),map)
    }

    ret.sortBy { -it.points }
    return ret
}

fun parseContest(data: String):Contest{
    val lines=data.split("\n")
    var th=lines[4].split(",")
    th=th.map { it.trim() }
    return Contest(lines[0].trim(),lines[2].trim(),lines[1].trim()=="true",parseRows(lines,4),ArrayList(th),lines[3].trim())
}

data class Contest(val name: String, val desc:String,
                   var rated:Boolean, val rows:ArrayList<StandingRow>, val head:ArrayList<String>, val category:String,
                   var from:String=""){

}
