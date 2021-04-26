package example

import collection.mutable
import util.Random.{nextDouble => nextRandomDouble}

class Submission(
    val id: Int,
    val time: Int,
    val quality: Double,
    var votes: Int = 1,
    var score: Double = 0 // TODO: forumla
)

object Submission {
  def randomQuality = nextRandomDouble
}

object Hello {
  val updateSize = 1500
  val updateIntervalSeconds = 10
  val frontpageSize = 90
  val newPageSize = 90
  val newFrontPageVotingRatio = 0.1 // TODO

  def submitProcess(
      timeSeconds: Int
  ): Boolean = timeSeconds % 72 == 0
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

  def updateFrontpageScores(
      timeSeconds: Int,
      submissions: mutable.ArrayBuffer[Submission]
  ) {
    submissions.takeRight(updateSize).foreach { sub =>
      val age = sub.time - timeSeconds
      sub.score = score(sub.votes, timeSeconds)
    }
  }

  def frontpage(submissions: mutable.ArrayBuffer[Submission]) = {
    //TODO: upvotes including myself (>= 3) or not (>= 4)?
    submissions
      .takeRight(updateSize)
      .sortBy(-_.score)
      .filter(_.votes >= 3)
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
      // frontpage
      // TODO: bias score, rank, quality
      frontpage(0).votes += 1
    } else {
      // newpage
      newpage(0).votes += 1
    }
  }

  def main(args: Array[String]) = {

    var timeSeconds = 0
    val submissions = mutable.ArrayBuffer.empty[Submission]
    submit(
      timeSeconds,
      submissions,
      votes = 5
    ) // TODO: initialize with the 1500 stories of real data

    while (timeSeconds < 1000) {

      if (submitProcess(timeSeconds)) {
        submit(timeSeconds, submissions)
        println(s"submission at $timeSeconds")
      }

      if (timeSeconds % updateIntervalSeconds == 0)
        updateFrontpageScores(timeSeconds, submissions)

      usersVote(frontpage(submissions), newpage(submissions))

      timeSeconds += 1
    }
  }
}
