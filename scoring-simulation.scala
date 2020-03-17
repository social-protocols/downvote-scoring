import scala.util.Random
import scala.collection.{ mutable }
import scala.math.Ordering.Float.TotalOrdering

case class Content(quality: Double, timestamp: Long) {
  def age(now: Long) = now - timestamp
}

object Content {
  // TODO: experiment with different distributions:
  // - Gaussian
  // - Exponential
  // def quality() = Random.nextGaussian
  def quality() = Random.nextDouble
  def random(now: Long) = Content(quality = quality(), now)
}

sealed trait Scoring {
  def score(votes: Long, age: Long): Double
  def voteCondition(quality: Double, expectation: Double): Boolean
  def estimatedQuality(votes:Long):Long
  def viewChange(oldVoteCount:Long):Long = oldVoteCount
  def voteChange(oldVoteCount:Long):Long = oldVoteCount + 1
}

case object HackerNews extends Scoring {
  def score(upvotes: Long, age: Long): Double = {
    // https://medium.com/hacking-and-gonzo/how-hacker-news-ranking-algorithm-works-1d9b0cf2c08d
    val gravity = 1.8
    val base = upvotes + 1
    (if (base > 0) Math.pow(base, 0.8) else base) / Math.pow(age + 1, gravity)
  }

  def estimatedQuality(upvotes:Long) = upvotes

  def voteCondition(quality: Double, expectation: Double): Boolean = {
    quality > expectation
  }
}

case object Reddit extends Scoring {
  // TODO
  // https://medium.com/hacking-and-gonzo/how-reddit-ranking-algorithms-work-ef111e33d0d9
}

case object OnlyDownvote extends Scoring {
  def score(downvotes: Long, age: Long): Double = {
    -(downvotes+1) * age
  }
  def estimatedQuality(downvotes:Long) = -downvotes
  def voteCondition(quality: Double, expectation: Double): Boolean = {
    quality < expectation
  }
}

case object RemoveDownvote extends Scoring {
  // every view is a downvote.
  // upvoting button, which removes the downvote
  def score(downvotes: Long, age: Long): Double = {
    -(downvotes+1) * age
  }
  def estimatedQuality(downvotes:Long) = -downvotes
  def voteCondition(quality: Double, expectation: Double): Boolean = {
    quality > expectation
  }
  override def viewChange(oldVoteCount:Long):Long = oldVoteCount + 1
  override def voteChange(oldVoteCount:Long):Long = oldVoteCount - 1
}

val iterations = 100000
val newContentEvery = 50
val frontPageSize = 20
val scoring: Scoring = HackerNews

var collection = mutable.ArrayBuffer.empty[Content]
val votes = mutable.HashMap.empty[Content, Long].withDefaultValue(0)
val views = mutable.HashMap.empty[Content, Long].withDefaultValue(0)
def totalVotes = votes.values.sum
def currentTime = collection.size // different time-step possibilities: every vote is one time-step, every new content is one time step
def sortedCollection = {
  collection = collection.sortBy(c => -scoring.score(votes(c), age = c.age(currentTime)))
  collection
}


def frontpage = sortedCollection.take(frontPageSize)

for (i <- 0 until iterations) {
  if (i % newContentEvery == 0)
    collection += Content.random(currentTime)

  //TODO: the time between loading the frontpage and voting is delayed in reality
  val currentFrontpage = frontpage
  val qualityExpectation = Content.quality() * 1.1 // expectation is never met for everybody
  val selectedContent = frontpage((currentFrontpage.length * (Random.nextDouble * Random.nextDouble)).toInt) // lower indices have higher probability to be voted on
  // val selectedContent = frontpage((currentFrontpage.length * Random.nextDouble).toInt) // lower indices have higher probability to be voted on
  views(selectedContent) += 1
  votes(selectedContent) = scoring.viewChange(votes(selectedContent))
  if (scoring.voteCondition(selectedContent.quality, qualityExpectation))
    votes(selectedContent) = scoring.voteChange(votes(selectedContent))


  // println(frontpage.map(c => f"Q:${c.quality}%.3f age:${c.age(currentTime)}%5.0f votes:${votes(c)}%5.0f score:${scoring.score(votes(c), age = c.age(currentTime))}%8.4f").mkString(s"Frontpage (${totalVotes}):\n", "\n", "\n"))
  // Thread.sleep(100)
}

// write into file and plot with gnuplot:
// :gnuplot> plot 'filename'
println(sortedCollection.map(c => s"${c.quality} ${scoring.estimatedQuality(votes(c))} ${views(c)}").mkString("\n"))
// println(sortedCollection.zipWithIndex.map{case (c, rank) => s"${c.quality} ${rank}"}.mkString("\n"))
