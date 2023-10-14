/* This code is copied from Codeforces and translated to Kotlin
 *
 * Copyright by Mike Mirzayanov
 */
package com.hhs.xgn.rating

import java.util.*
import kotlin.math.*

/**
 * @author Mike Mirzayanov (mirzayanovmr@gmail.com)
 */
class RatingCalculator {

    fun calculateRatingChanges(
        previousRatings: Map<String, Int>,
        standingsRows: ArrayList<StandingRow>
    ): Map<String, Int> {
        val contestants = ArrayList<Contestant>(standingsRows.size)
        for (standingsRow in standingsRows) {
            val rank = -1
            val party = standingsRow.name
            contestants.add(
                Contestant(
                    party, rank, standingsRow.points,
                    previousRatings[party]!!
                )
            )
        }
        process(contestants)
        val ratingChanges = HashMap<String, Int>()
        for (contestant in contestants) {
            ratingChanges[contestant.party] = contestant.delta
        }
        return ratingChanges
    }

    /**
     * @param a Participant a
     * @param b Participant b
     * @return Probability a wins b
     */
    private fun getEloWinProbability(a: Contestant, b: Contestant): Double {
        return getEloWinProbability(a.rating.toDouble(), b.rating.toDouble())
    }


    private fun getSeed(contestants: List<Contestant>, rating: Int): Double {
        val extraContestant = Contestant("", 0, 0.0, rating)
        var result = 1.0
        for (other in contestants) {
            result += getEloWinProbability(other, extraContestant)
        }
        return result
    }

    private fun getRatingToRank(contestants: List<Contestant>, rank: Double): Int {
        var left = 1
        var right = 8000
        while (right - left > 1) {
            val mid = (left + right) / 2
            if (getSeed(contestants, mid) < rank) {
                right = mid
            } else {
                left = mid
            }
        }
        return left
    }

    private fun reassignRanks(contestants: ArrayList<Contestant>) {
        sortByPointsDesc(contestants)
        for (contestant in contestants) {
            contestant.rank = 0.0
            contestant.delta = 0
        }
        var first = 0
        var points = contestants[0].points
        for (i in 1..<contestants.size) {
            if (contestants[i].points < points) {
                for (j in first..<i) {
                    contestants[j].rank = i.toDouble()
                }
                first = i
                points = contestants[i].points
            }
        }
        run {
            val rank = contestants.size.toDouble()
            for (j in first..<contestants.size) {
                contestants[j].rank = rank
            }
        }
    }

    private fun sortByPointsDesc(contestants: ArrayList<Contestant>) {
        contestants.sortWith { o1, o2 -> -o1.points.compareTo(o2.points) }
    }

    private fun process(contestants: ArrayList<Contestant>) {
        if (contestants.isEmpty()) {
            return
        }
        reassignRanks(contestants)
        for (a in contestants) {
            a.seed = 1.0
            for (b in contestants) {
                if (a != b) {
                    a.seed += getEloWinProbability(b, a)
                }
            }
        }
        for (contestant in contestants) {
            val midRank = sqrt(contestant.rank * contestant.seed)
            contestant.needRating = getRatingToRank(contestants, midRank)
            contestant.delta = (contestant.needRating - contestant.rating) / 2
        }
        sortByRatingDesc(contestants)

        // Total sum should not be more than zero.
        run {
            var sum = 0
            for (c in contestants) {
                sum += c.delta
            }
            val inc = -sum / contestants.size - 1
            for (contestant in contestants) {
                contestant.delta += inc
            }
        }

        // Sum of top-4*sqrt should be adjusted to zero.
        run {
            var sum = 0
            val zeroSumCount = min(
                (4 * Math.round(sqrt(contestants.size.toDouble()))).toInt().toDouble(),
                contestants.size.toDouble()
            )
                .toInt()
            for (i in 0..<zeroSumCount) {
                sum += contestants[i].delta
            }
            val inc = min(max((-sum / zeroSumCount).toDouble(), -10.0), 0.0).toInt()
            for (contestant in contestants) {
                contestant.delta += inc
            }
        }
        validateDeltas(contestants)
    }

    private fun validateDeltas(contestants: ArrayList<Contestant>) {
        sortByPointsDesc(contestants)
        for (i in contestants.indices) {
            for (j in i + 1..<contestants.size) {
                if (contestants[i].rating > contestants[j].rating) {
                    ensure(
                        contestants[i].rating + contestants[i].delta >= contestants[j].rating + contestants[j].delta,
                        "First rating invariant failed: " + contestants[i].party + " vs. " + contestants[j].party + "."
                    )
                }
                if (contestants[i].rating < contestants[j].rating) {
                    if (contestants[i].delta < contestants[j].delta) {
                        println(1)
                    }
                    ensure(
                        contestants[i].delta >= contestants[j].delta,
                        "Second rating invariant failed: " + contestants[i].party + " vs. " + contestants[j].party + "."
                    )
                }
            }
        }
    }

    private fun ensure(b: Boolean, message: String) {
        if (!b) {
            throw RuntimeException(message)
        }
    }

    private fun sortByRatingDesc(contestants: ArrayList<Contestant>) {
        contestants.sortWith { o1, o2 -> -o1.rating.compareTo(o2.rating) }
    }

    class Contestant(party: String, rank: Int, points: Double, rating: Int) {
        val party: String
        var rank: Double
        val points: Double
        val rating: Int
        var needRating = 0
        var seed = 0.0
        var delta = 0

        init {
            this.party = party
            this.rank = rank.toDouble()
            this.points = points
            this.rating = rating
        }
    }

    companion object {
        private const val INITIAL_RATING = 1500
        private fun getEloWinProbability(ra: Double, rb: Double): Double {
            return 1.0 / (1 + 10.0.pow((rb - ra) / 400.0))
        }
    }
}
