package simulation

import collection.mutable
import util.Random.{nextDouble => nextRandomDouble}

class Submission(
    val id: Int,
    val timeSeconds: Int,
    val quality: Double,
    var votes: Int = 1,
    var score: Double = 0,
)

object Submission {
  def randomQuality = util.Random.nextDouble()
}

object Simulation {

  def submit(
      timeSeconds: Int,
      submissions: mutable.ArrayBuffer[Submission],
      votes: Int = 1,
  ) = {
    val nextId        = submissions.size
    val newSubmission = new Submission(
      id = nextId,
      timeSeconds = timeSeconds,
      quality = Submission.randomQuality,
      votes = votes,
    )
    submissions += newSubmission
  }

  def score(upvotes: Int, ageSeconds: Int): Double = {
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
      sub.score = score(sub.votes, ageSeconds)
    }
  }

  def frontpage(submissions: mutable.ArrayBuffer[Submission]) = {
    submissions
      .takeRight(Data.updateSize)
      .sortBy(-_.score)
      .filter(_.votes >= Data.minimVotesForFrontpage + 1)
      .take(Data.frontpageSize)
  }
  def newpage(submissions: mutable.ArrayBuffer[Submission]) = {
    submissions.takeRight(Data.newPageSize).reverse
  }

  def usersVote(
      frontpage: mutable.ArrayBuffer[Submission],
      newpage: mutable.ArrayBuffer[Submission],
  ) = {
    if (nextRandomDouble() > Data.newFrontPageVotingRatio) {
      // frontpage
      var didVote = false
      while (!didVote) {
        val selectedRank = Data.voteGainOnTopRankDistribution.sample(1).head
        if (nextRandomDouble() < frontpage(selectedRank).quality) {
          frontpage(selectedRank).votes += 1
          didVote = true
        }
      }
    } else {
      // newpage
      val selectedRank = Data.voteGainOnNewRankDistribution.sample(1).head
      newpage(selectedRank).votes += 1
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
      votes = 5,
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
