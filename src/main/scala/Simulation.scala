package simulation

import collection.mutable
import util.Random.{nextDouble => nextRandomDouble}

class Submission(
    val id: Int,
    val timeSeconds: Int,
    val quality: Double,
    var score: Int = 1, // = upvotes + 1
    var rankingFormulaValue: Double = 0,
)

object Submission {
  def randomQuality = Data.qualityDistribution.sample(1).head
}

object Simulation {

  def submit(
      timeSeconds: Int,
      submissions: mutable.ArrayBuffer[Submission],
      score: Int = 1,
  ) = {
    val nextId        = submissions.size
    val newSubmission = new Submission(
      id = nextId,
      timeSeconds = timeSeconds,
      quality = Submission.randomQuality,
      score = score,
    )
    submissions += newSubmission
  }

  def rankingFormula(upvotes: Int, ageSeconds: Int): Double = {
    // http://www.righto.com/2013/11/how-hacker-news-ranking-really-works.html
    val ageHours = ageSeconds / 3600.0
    Math.pow(upvotes - 1.0, 0.8) / Math.pow(ageHours + 2, 1.8)
  }

  def updateScoresOfNewestSubmissions(
      timeSeconds: Int,
      submissions: mutable.ArrayBuffer[Submission],
  ) = {
    submissions.takeRight(Data.updateSize).foreach { sub =>
      val ageSeconds = timeSeconds - sub.timeSeconds
      sub.rankingFormulaValue = rankingFormula(sub.score, ageSeconds)
    }
  }

  def bestQualityFrontpage(submissions: mutable.ArrayBuffer[Submission]) = {
    submissions
      .takeRight(Data.updateSize)
      .sortBy(-_.quality)
      .take(Data.frontpageSize)
  }
  def frontpage(submissions: mutable.ArrayBuffer[Submission]) = {
    submissions
      .takeRight(Data.updateSize)
      .sortBy(-_.rankingFormulaValue)
      .filter(_.score >= Data.minScoreToAppearOnFrontpage)
      .take(Data.frontpageSize)
  }
  def newpage(submissions: mutable.ArrayBuffer[Submission]) = {
    submissions.takeRight(Data.newPageSize).reverse
  }

  def usersVote(
      frontpage: mutable.ArrayBuffer[Submission],
      newpage: mutable.ArrayBuffer[Submission],
  ) = {
    val x = 1.0
    if (nextRandomDouble() > Data.newFrontPageVotingRatio) {
      // frontpage
      var didVote       = false
      val selectedRanks = Data.voteGainOnTopRankDistribution.sample(1000).distinct.toArray
      var i             = 0
      while (!didVote && i < selectedRanks.size) {
        val selectedRank = selectedRanks(i)
        if (nextRandomDouble() < x * frontpage(selectedRank).quality) {
          frontpage(selectedRank).score += 1
          didVote = true
        }
        i += 1
      }
    } else {
      // newpage
      var didVote = false
      while (!didVote) {
        val selectedRank = Data.voteGainOnNewRankDistribution.sample(1).head
        if (nextRandomDouble() < x * frontpage(selectedRank).quality) {
          newpage(selectedRank).score += 1
          didVote = true
        }
      }
    }
  }

  var timeSeconds    = 0
  var nextSubmission = Data.nextSubmissionArrivalDelay.sample(1).head
  var nextVote       = Data.nextVoteArrivalDelay.sample(1).head
  val submissions    = mutable.ArrayBuffer.empty[Submission]
  for (_ <- 0 until 1500) {
    submit(
      timeSeconds,
      submissions,
      score = 5,
    ) // TODO: initialize with the 1500 stories of real data
  }

  def nextStep() = {

    val submissionArrives = timeSeconds >= nextSubmission
    if (submissionArrives) {
      submit(timeSeconds, submissions)
      nextSubmission += Data.nextSubmissionArrivalDelay.sample(1).head
    }

    if (timeSeconds % Data.updateIntervalSeconds == 0)
      updateScoresOfNewestSubmissions(timeSeconds, submissions)

    val voteArrives = timeSeconds >= nextVote
    if (voteArrives) {
      usersVote(frontpage(submissions), newpage(submissions))
      nextVote += Data.nextVoteArrivalDelay.sample(1).head
    }

    timeSeconds += 1
  }
}
