package example

import collection.mutable
import util.Random.{nextDouble => nextRandomDouble}
import breeze.stats.distributions._

class Submission(
    val id: Int,
    val time: Int,
    val quality: Double,
    var votes: Int = 1,
    var score: Double = 0 // TODO: forumla
)

object Submission {
  def randomQuality = util.Random.nextDouble()
}

object Hello {
  val updateSize = 1500
  val updateIntervalSeconds = 10
  val frontpageSize = 90
  val newPageSize = 90
  val newFrontPageVotingRatio = 0.1 // TODO
  val averageSubmissionArrivalSeconds = 78.290865 // from bigquery 2021
  val minimVotesForFrontpage = 3 //TODO
  val averageVoteArrivalSeconds =
    1.0 / (6106438.0 / 365 / 24 / 3600) // ~5 from bigquery

  def submit(
      timeSeconds: Int,
      submissions: mutable.ArrayBuffer[Submission],
      votes: Int = 1
  ) {
    val nextId = submissions.size
    val newSubmission = new Submission(
      id = nextId,
      time = timeSeconds,
      quality = Submission.randomQuality,
      votes = votes
    )
    submissions += newSubmission
  }

  def score(upvotes: Int, ageSeconds: Int): Double = {
    // http://www.righto.com/2013/11/how-hacker-news-ranking-really-works.html
    val ageHours = ageSeconds / 3600.0
    Math.pow(upvotes - 1, 0.8) / Math.pow(ageHours + 2, 1.8)
  }

  def updateScoresOfNewestSubmissions(
      timeSeconds: Int,
      submissions: mutable.ArrayBuffer[Submission]
  ) {
    submissions.takeRight(updateSize).foreach { sub =>
      val age = sub.time - timeSeconds
      sub.score = score(sub.votes, timeSeconds)
    }
  }

  def frontpage(submissions: mutable.ArrayBuffer[Submission]) = {
    submissions
      .takeRight(updateSize)
      .sortBy(-_.score)
      .filter(_.votes >= minimVotesForFrontpage + 1)
      .take(frontpageSize)
  }
  def newpage(submissions: mutable.ArrayBuffer[Submission]) = {
    submissions.takeRight(newPageSize).reverse
  }

  def usersVote(
      frontpage: mutable.ArrayBuffer[Submission],
      newpage: mutable.ArrayBuffer[Submission]
  ) = {
    if (nextRandomDouble > newFrontPageVotingRatio) {
      val selectedRank = topPageVoteOnRandomRank()
      // frontpage
      // TODO: bias score, rank, quality
      frontpage(selectedRank).votes += 1
    } else {
      // newpage
      val selectedRank = newPageVoteOnRandomRank()
      newpage(selectedRank).votes += 1
    }
  }

  def main(args: Array[String]) = {

    val nextSubmissionArrivalDelay = new Exponential(
      1.0 / averageSubmissionArrivalSeconds
    )

    val nextVoteArrivalDelay = new Exponential(
      1.0 / averageVoteArrivalSeconds
    )

    var timeSeconds = 0
    var nextSubmission = nextSubmissionArrivalDelay.get()
    var nextVote = nextVoteArrivalDelay.get()
    val submissions = mutable.ArrayBuffer.empty[Submission]
    for (_ <- 0 until 1500) {
      submit(
        timeSeconds,
        submissions,
        votes = 5
      ) // TODO: initialize with the 1500 stories of real data
    }

    while (timeSeconds < (60 * 60 * 24)) {

      val submissionArrives = timeSeconds >= nextSubmission
      if (submissionArrives) {
        submit(timeSeconds, submissions)
        nextSubmission += nextSubmissionArrivalDelay.get()
        // println(s"submission at ${timeSeconds / 60.0}")
      }

      if (timeSeconds % updateIntervalSeconds == 0)
        updateScoresOfNewestSubmissions(timeSeconds, submissions)

      val voteArrives = timeSeconds >= nextVote
      if (voteArrives) {
        usersVote(frontpage(submissions), newpage(submissions)) // CONTINUE HERE
        nextVote += nextVoteArrivalDelay.get()
        // println(s"Vote at ${timeSeconds / 60.0}")
      }

      if (timeSeconds % 60 == 0) {
        println(
          "Frontpage:\n" +
            frontpage(submissions)
              .map(s => s"${s.id}, votes: ${s.votes}, score: ${s.score}")
              .mkString("\n")
        )
        println()
      }

      timeSeconds += 1
    }
  }

  // select sum(gain) as rankgain from dataset where samplingWindow >= 3 and newRank is not null and topRank is null and showRank = -1 and askRank = -1 group by newRank order by newRank;
  val voteGainOnNewRankDistribution = new DataDistribution(
    Array[Double](
      1464, 2017, 2162, 1948, 1887, 1784, 1635, 1535, 1445, 1353, 1296, 1171,
      1099, 1069, 1028, 973, 947, 886, 925, 854, 837, 787, 807, 747, 655, 627,
      592, 577, 624, 546, 495, 467, 407, 354, 362, 387, 385, 333, 336, 296, 266,
      321, 293, 259, 253, 254, 273, 233, 260, 246, 228, 206, 233, 187, 193, 235,
      208, 218, 179, 234, 197, 187, 170, 171, 183, 178, 193, 164, 149, 161, 166,
      145, 141, 163, 157, 140, 132, 144, 141, 134, 149, 137, 133, 164, 116, 153,
      157, 133, 154, 147
    )
  )

  // select sum(gain) as rankgain from dataset where samplingWindow >= 3 and topRank is not null group by topRank order by topRank;
  val voteGainOnTopRankDistribution = new DataDistribution(
    Array[Double](
      100481, 57457, 44419, 37889, 33027, 28484, 25084, 23950, 22680, 20959,
      19343, 17739, 17320, 15863, 15862, 15325, 15109, 14478, 14006, 13215,
      12963, 12159, 11869, 11447, 11249, 10979, 10970, 10640, 10569, 10370,
      6871, 5278, 4855, 4199, 4232, 4028, 3920, 3646, 3411, 3573, 3251, 3242,
      3240, 3027, 3129, 2913, 2611, 2616, 2521, 2548, 2408, 2452, 2271, 2190,
      2190, 2128, 2050, 2061, 1946, 1983, 1824, 1592, 1375, 1319, 1275, 1228,
      1247, 1200, 1160, 1151, 1085, 1115, 1022, 1022, 977, 949, 929, 933, 916,
      961, 867, 910, 830, 859, 801, 843, 768, 863, 827, 971
    )
  )

}

class DataDistribution(data: Array[Double]) {
  val normalized = data.map(_ / data.sum)
  val cumulative = normalized.scan(0.0)((sum, next) => sum + next).tail
  def draw() = {
    cumulative.search(scala.util.Random.nextDouble()).insertionPoint
  }
}
