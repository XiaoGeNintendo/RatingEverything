package com.hhs.xgn.rating

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.io.File
import java.nio.charset.Charset
import java.util.*
import kotlin.collections.ArrayList


val contests = HashMap<String, Contest>()
var users = HashMap<String, User>()

const val UTF8_BOM = "\uFEFF"
fun removeUTF8BOM(str: String): String {
    return if (str.startsWith(UTF8_BOM)) {
        str.substring(1)
    } else {
        str
    }
}

fun loadContests() {
    val dir = File("contests")
    if (!dir.isDirectory) {
        throw RuntimeException("loadContests: contests does not represent a directory!")
    }
    for (i in dir.listFiles()!!) {
        val text = removeUTF8BOM(i.readText(Charset.forName("utf-8")))
        val contest = parseContest(text)
        println("loadContests: Found contest ${contest.name.trim()}")
        contest.from = i.name
        contests[contest.name.trim()] = contest
    }
}

fun saveContest(c: Contest) {
    val db = File("contests/${c.from}")
    var txt = "${c.name}\n${c.rated}\n${c.desc}\n${c.category}\n${c.head.joinToString(",")}\n"
    for (r in c.rows) {
        txt += "${r.name},${r.points},${
            c.head.filter { it !in arrayOf("name", "point") }.joinToString(separator = ",") { r.additionalInfo[it]!! }
        }\n"
    }
    db.writeText(txt)
}

fun loadUsers() {
    val db = File("users.json")
    if (!db.exists()) {
        db.createNewFile()
    } else if (!db.isFile) {
        throw RuntimeException("loadUsers: users.json is not a file")
    }

    val text = removeUTF8BOM(db.readText(Charset.forName("utf-8")))

    users = Gson().fromJson(text, object : TypeToken<HashMap<String, User>>() {}.type) ?: HashMap()
    println(users)
    println("Load ${users.size} users")
}

fun saveUsers() {
    val text = Gson().toJson(users)
    val db = File("users.json")
    db.writeText(text, Charset.forName("Utf-8"))
}

fun Int.toSignedString(): String {
    return if (this > 0) {
        "+$this"
    } else if (this == 0) {
        "±0"
    } else {
        "$this"
    }
}

fun getRank(name: String, r: Int): String {
    if (name == "Maths") {
        return if (r >= 2000) {
            "Super Strong!!"
        } else if (r >= 1900) {
            "Super Strong"
        } else if (r >= 1700) {
            "Strong"
        } else if (r >= 1450) {
            "Decent"
        } else if (r >= 1350) {
            "OK"
        } else {
            "Weak"
        }
    }
    return if (r >= 3000) {
        "Legendary Grandmaster"
    } else if (r >= 2600) {
        "International Grandmaster"
    } else if (r >= 2400) {
        "Grandmaster"
    } else if (r >= 2300) {
        "International Master"
    } else if (r >= 2100) {
        "Master"
    } else if (r >= 1900) {
        "Candidate Master"
    } else if (r >= 1600) {
        "Expert"
    } else if (r >= 1400) {
        "Specialist"
    } else if (r >= 1200) {
        "Pupil"
    } else {
        "Newbie"
    }
}

fun main(args: Array<String>) {
    println("Loading Rating System...")

    loadContests()
    loadUsers()

    val scanner = Scanner(System.`in`)
    while (true) {
        print(">")
        val line = scanner.nextLine().split(" ").map { it.trim() }
        if (line[0] == "help") {
            println("users -- show all participants")
            println("user <name> -- show a certain participant")
            println("contests -- show all contests")
            println("contest <name> -- show a standing")
            println("pend <name> -- pend rating of a contest")
            println("rank <name> -- show ranking on a given aspect")
        }

        if (line[0] == "users") {
            for (u in users) {
                println("${u.key} -- ${u.value.ratings.map { "${it.key} = ${it.value.rating}" }}")
            }
        }
        if (line[0] == "user") {
            if (line[1] in users.keys) {
                val u = users[line[1]]!!
                println(u.name)
                for (rating in u.ratings) {
                    println("-=${rating.key}=-")
                    val f = rating.value
                    println("Current Rating: ${f.rating} ${getRank(rating.key, f.rating)}")
                    var nowRating = 1500
                    for (c in f.changes) {
                        val r1 = getRank(rating.key, nowRating)
                        val r2 = getRank(rating.key, nowRating + c.delta)
                        println("${c.name} ${c.delta.toSignedString()} $nowRating --> ${nowRating + c.delta} [Points ${c.point} and ranking ${c.rank}] (${if (r1 != r2) "Became $r1 -> $r2" else "∅"})")
                        nowRating += c.delta
                    }
                }
            } else {
                println("No such user")
            }
        }


        if (line[0] == "contests") {
            for (e in contests) {
                println("${e.key} -- ${e.value.desc} Rated=${e.value.rated} Participants=${e.value.rows.size}")
            }
        }
        if (line[0] == "contest") {
            if (line[1] in contests.keys) {
                val c = contests[line[1]]!!
                println("${line[1]} -- Rated=${c.rated} Participants=${c.rows.size}")
                println(c.desc)
                var rank = 1
                print("Rank Name -- Score ")

                for (th in c.head) {
                    print("${if (th !in arrayOf("name", "point")) th else ""} ")
                }
                println()

                for ((now, row) in c.rows.withIndex()) {

                    if (now != 0 && c.rows[now].points != c.rows[now - 1].points) {
                        rank = now + 1
                    }
                    println(
                        "$rank  ${row.name} -- [${row.points}] ${
                            c.head.map {
                                if (it in arrayOf(
                                        "name",
                                        "point"
                                    )
                                ) "" else row.additionalInfo[it]
                            }.joinToString(separator = " ")
                        }"
                    )

                }
            } else {
                println("Did not find such contest")
            }
        }

        if (line[0] == "pend") {
            if (line[1] in contests.keys) {
                val c = contests[line[1]]!!
                if (c.rated) {
                    println("This contest has been rated. Do you want to rate it again? [y/n]")
                    val response = scanner.nextLine().trim()
                    if (response in arrayOf("y", "Y")) {
                        //do as it is
                    } else {
                        println("Cancelled")
                        continue
                    }
                }

                //calculate rating according to CF style
                val calculator = RatingCalculator()
                val previousRatings = HashMap<String, Int>()
                for (r in c.rows) {

                    //add new users to user database
                    if (r.name !in users) {
                        users[r.name] = User(r.name, HashMap())
                    }

                    val u = users[r.name]!!

                    //add default rating
                    if (c.category !in u.ratings) {
                        u.ratings[c.category] = Rating(1500, ArrayList())
                    }

                    previousRatings[u.name] = u.ratings[c.category]!!.rating
                }
                val delta = calculator.calculateRatingChanges(previousRatings, c.rows)

                println("Delta preview:")
                var rank = 1
                println("Rank Name -- Score Delta")
                val ranks=HashMap<String,Int>()
                val points=HashMap<String,Double>()

                for ((now, row) in c.rows.withIndex()) {

                    if (now != 0 && c.rows[now].points != c.rows[now - 1].points) {
                        rank = now + 1
                    }
                    println("$rank  ${row.name} -- [${row.points}] (${delta[row.name]}) ${users[row.name]!!.ratings[c.category]!!.rating} ${users[row.name]!!.ratings[c.category]!!.rating + delta[row.name]!!}")

                    ranks[row.name]=rank
                    points[row.name]=row.points
                }
                println("Is this OK? [y/n]")
                val ok = scanner.nextLine()

                if (ok in arrayOf("y", "Y")) {
                    println("Saving Ratings")
                    for (e in delta) {
                        val r = users[e.key]!!.ratings[c.category]!!
                        r.rating += e.value
                        r.changes += RatingChange(c.name, e.value,ranks[e.key]!!,points[e.key]!!)
                    }
                    c.rated = true
                    if ("Δ" !in c.head) {
                        c.head.add("Δ")
                    }
                    for (r in c.rows) {
                        r.additionalInfo["Δ"] = delta[r.name]!!.toSignedString()
                    }

                    saveUsers()
                    saveContest(c)
                    println("Rating Saved!")
                }else{
                    println("Discarded")
                }
            } else {
                println("No such contest")
            }
        }

        if(line[0]=="rank"){

            val x=ArrayList<Pair<Int,String>>()

            for(u in users){
                if(line[1] in u.value.ratings.keys){
                    x.add(Pair(u.value.ratings[line[1]]!!.rating,u.key))
                }
            }
            println(line[1])
            x.sortBy { -it.first }

            var rk=1
            println("Leaderboard of ${line[1]}")
            for((now, y) in x.withIndex()){
                if(now>0 && x[now].first!=x[now-1].first){
                    rk=now+1
                }

                println("$rk ${y.second} Rating: ${y.first} ${getRank(line[1],y.first)} Contests:${users[y.second]!!.ratings[line[1]]!!.changes.size}")
            }
        }
    }
}
