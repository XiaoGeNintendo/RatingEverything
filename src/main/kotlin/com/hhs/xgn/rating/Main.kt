package com.hhs.xgn.rating

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import freemarker.template.Configuration
import freemarker.template.TemplateExceptionHandler
import java.io.File
import java.nio.charset.Charset
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashSet


val contests = HashMap<String, Contest>()
var users = HashMap<String, User>()
lateinit var freemarkerConfiguration: Configuration

const val UTF8_BOM = "\uFEFF"
const val BUILD_ROOT = "build"

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

val mathsRegions = RatingRegions(
    listOf(
        RatingRegion(-9999, 1350, "Weak", "cccccc"),
        RatingRegion(1350, 1450, "OK", "77ff77"),
        RatingRegion(1450, 1600, "Decent", "77ddbb"),
        RatingRegion(1600, 1700, "Strong", "aaaaff"),
        RatingRegion(1700, 1800, "Very Strong", "ff88ff"),
        RatingRegion(1800, 1900, "Super Strong", "ffcc88"),
        RatingRegion(1900, 9999, "Super Strong!!", "ff3333")
    )
)

val csRegions = RatingRegions(
    listOf(
        RatingRegion(-9999, 1200, "Newbie", "cccccc"),
        RatingRegion(1200, 1400, "Pupil", "77ff77"),
        RatingRegion(1400, 1600, "Specialist", "77ddbb"),
        RatingRegion(1600, 1900, "Expert", "aaaaff"),
        RatingRegion(1900, 2100, "Candidate Master", "ff88ff"),
        RatingRegion(2100, 2300, "Master", "ffcc88"),
        RatingRegion(2300, 2400, "International Master", "ffbb55"),
        RatingRegion(2400, 2600, "Grandmaster", "ff7777"),
        RatingRegion(2600, 3000, "International Grandmaster", "ff3333"),
        RatingRegion(3000, 9999, "Legendary Grandmaster", "aa0000")
    )
)

fun getRatingRegions(name: String):RatingRegions{
    return if(name=="Maths"){
        mathsRegions
    }else{
        csRegions
    }
}

fun getRank(name: String, r: Int): String {
    if (name == "Maths") {
        return mathsRegions.getRank(r)!!
    }
    return csRegions.getRank(r)!!
}

fun getRankColor(name: String, r: Int): String {
    if (name == "Maths") {
        return mathsRegions.getRankColor(r)!!
    }
    return csRegions.getRankColor(r)!!
}

fun main(args: Array<String>) {
    println("Loading Rating System...")

    freemarkerConfiguration = Configuration(Configuration.VERSION_2_3_32)
    freemarkerConfiguration.setDirectoryForTemplateLoading(File("templates"))
    freemarkerConfiguration.defaultEncoding = "UTF-8"
    freemarkerConfiguration.templateExceptionHandler = TemplateExceptionHandler.RETHROW_HANDLER
    freemarkerConfiguration.logTemplateExceptions = false
    freemarkerConfiguration.wrapUncheckedExceptions = true
    freemarkerConfiguration.fallbackOnNullLoopVariable = false
    freemarkerConfiguration.sqlDateAndTimeTimeZone = TimeZone.getDefault()

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
            println("build -- generate an interactive website showing the rank")
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
                        println(
                            "${c.name} ${c.delta.toSignedString()} $nowRating --> ${nowRating + c.delta} [Points ${c.point} and ranking ${c.rank}/${c.totalParticipants}(Top ${
                                "%.2f".format(
                                    100.0 * c.rank / c.totalParticipants
                                )
                            }%)] (${if (r1 != r2) "Became $r1 -> $r2" else "∅"})"
                        )
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
//                println(previousRatings)
//                println(c.rows)

                val delta = calculator.calculateRatingChanges(previousRatings, c.rows)

                println("Delta preview:")
                var rank = 1
                println("Rank Name -- Score Delta")
                val ranks = HashMap<String, Int>()
                val points = HashMap<String, Double>()

                for ((now, row) in c.rows.withIndex()) {

                    if (now != 0 && c.rows[now].points != c.rows[now - 1].points) {
                        rank = now + 1
                    }
                    println("$rank  ${row.name} -- [${row.points}] (${delta[row.name]}) ${users[row.name]!!.ratings[c.category]!!.rating} ${users[row.name]!!.ratings[c.category]!!.rating + delta[row.name]!!}")

                    ranks[row.name] = rank
                    points[row.name] = row.points
                }
                println("Is this OK? [y/n]")
                val ok = scanner.nextLine()

                if (ok in arrayOf("y", "Y")) {
                    println("Saving Ratings")
                    for (e in delta) {
                        val r = users[e.key]!!.ratings[c.category]!!
                        r.rating += e.value
                        r.changes += RatingChange(c.name, e.value, ranks[e.key]!!, c.rows.size, points[e.key]!!)
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
                } else {
                    println("Discarded")
                }
            } else {
                println("No such contest")
            }
        }

        if (line[0] == "rank") {

            val x = ArrayList<Pair<Int, String>>()

            for (u in users) {
                if (line[1] in u.value.ratings.keys) {
                    x.add(Pair(u.value.ratings[line[1]]!!.rating, u.key))
                }
            }
            println(line[1])
            x.sortBy { -it.first }

            var rk = 1
            println("Leaderboard of ${line[1]}")
            for ((now, y) in x.withIndex()) {
                if (now > 0 && x[now].first != x[now - 1].first) {
                    rk = now + 1
                }

                println(
                    "$rk ${y.second} Rating: ${y.first} ${
                        getRank(
                            line[1],
                            y.first
                        )
                    } Contests:${users[y.second]!!.ratings[line[1]]!!.changes.size}"
                )
            }
        }

        if (line[0] == "build") {
            println("This will build a website for visualize data in $BUILD_ROOT/ folder. Continue? [y/n]")
            val ok = scanner.nextLine()
            if (ok !in arrayOf("y", "Y")) {
                println("Cancelled")
                continue
            }


            val root = File(BUILD_ROOT)
            if (!root.exists()) {
                root.mkdir()
            }
            if (!root.isDirectory) {
                println("Error: seems $BUILD_ROOT is not a directory. Aborted.")
                continue
            }

            var failed = false

            val allSubject=HashSet<String>()
            for(i in users.map { it.value.ratings.keys }){
                allSubject.addAll(i)
            }

            run {
                println("Creating index")
                val indexFile = File("$BUILD_ROOT/index.html")

                val parameter = HashMap<String, Any>()
                parameter["subjects"] = allSubject
                val template = freemarkerConfiguration.getTemplate("index.ftl")
                template.process(parameter, indexFile.writer(Charset.forName("utf-8")))

            }
            for(subject in allSubject){
                println("Creating: RANK file for $subject")
                val outputFile = File("$BUILD_ROOT/$subject/RANK.html")

                data class FreemarkerStandingRow(val name:String, val rating: Int, val contestCount: Int, var rank:Int=0)

                val standing=ArrayList<FreemarkerStandingRow>()
                for((username, user) in users){
                    if(subject in user.ratings.keys){
                        val r=user.ratings[subject]!!
                        standing.add(FreemarkerStandingRow(username,r.rating,r.changes.size))
                    }
                }
                standing.sortBy { -it.rating }

                for((index, row) in standing.withIndex()){
                    if(index==0){
                        row.rank=1
                    }else if(row.rating<standing[index-1].rating){
                        row.rank=index+1
                    }else{
                        row.rank=standing[index-1].rank
                    }
                }

                val parameter = HashMap<String, Any>()
                parameter["subjectname"] = subject
                parameter["rank"]=standing
                parameter["master"]=object: Any() {
                    fun getRank(r:Int):String{
                        return getRank(subject,r)
                    }
                    fun getRankColor(r:Int):String{
                        return getRankColor(subject,r)
                    }
                }
                val template = freemarkerConfiguration.getTemplate("rank.ftl")
                template.process(parameter, outputFile.writer(Charset.forName("utf-8")))

            }
            for ((name, user) in users) {
                for ((subject, rating) in user.ratings) {
                    val subjectFolder = File("$BUILD_ROOT/$subject")
                    if (!subjectFolder.exists()) {
                        subjectFolder.mkdirs()
                    }
                    if (!subjectFolder.isDirectory) {
                        println("Error: seems $BUILD_ROOT/$subject is not a directory. Aborted.")
                        failed = true
                        break
                    }

                    val filename = "$BUILD_ROOT/$subject/$name.html"
                    println("Creating: $filename")
                    val file = File(filename)
                    val parameter = HashMap<String, Any>()
                    parameter["username"] = name
                    parameter["user"] = user
                    parameter["subjectname"] = subject
                    parameter["ratingHistory"] = rating.getRatingHistory()
                    parameter["ratingChanges"] = rating.changes
                    parameter["rating"] = rating.rating
                    parameter["otherSubjects"] = user.ratings.keys.filter { it!=subject }

                    parameter["master"]=object: Any() {
                        fun getRank(r:Int):String{
                            return getRank(subject,r)
                        }
                        fun getRankColor(r:Int):String{
                            return getRankColor(subject,r)
                        }
                    }
                    parameter["ratingRegionText"] = getRatingRegions(subject).ratingRegions.joinToString(",","[","]"){"{from:${it.l},to:${it.r},color:'#${it.color}',label:{text:'${it.name}',style:{color:'#606060'}}}"}
                    val template = freemarkerConfiguration.getTemplate("user.ftl")
                    template.process(parameter, file.writer(Charset.forName("utf-8")))

                }
                if (failed) {
                    break
                }
            }


        }
    }
}
