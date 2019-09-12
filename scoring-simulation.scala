import scala.util.Random
import scala.collection.{ IterableLike, mutable }

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

case object OnlyDownvote extends Scoring {
  def score(downvotes: Long, age: Long): Double = {
    -(downvotes+1) * age
  }
  def estimatedQuality(downvotes:Long) = -downvotes
  def voteCondition(quality: Double, expectation: Double): Boolean = {
    quality < expectation
  }
}

val iterations = 3000
val newContentEvery = 10
val frontPageSize = 10
val scoring: Scoring = HackerNews

var collection = mutable.ArrayBuffer.empty[Content]
val votes = mutable.HashMap.empty[Content, Long].withDefaultValue(0)
def totalVotes = votes.values.sum
def currentTime = totalVotes // every vote is one time-step
def sortedCollection = {
  collection = collection.sortBy(c => -scoring.score(votes(c), age = c.age(currentTime)))
  collection
}
def frontpage = sortedCollection.take(frontPageSize)

for (i <- 0 until iterations) {
  if (i % newContentEvery == 0)
    collection += Content.random(currentTime)

  val currentFrontpage = frontpage
  val qualityExpectation = Content.quality()
  val selectedContent = frontpage((currentFrontpage.length * (Random.nextDouble * Random.nextDouble)).toInt) // lower indices have higher probability to be voted on
  // val selectedContent = frontpage((currentFrontpage.length * Random.nextDouble).toInt) // lower indices have higher probability to be voted on
  if (scoring.voteCondition(selectedContent.quality, qualityExpectation))
    votes(selectedContent) += 1

  // println(frontpage.map(c => f"Q:${c.quality}%.3f age:${c.age(currentTime)}%5.0f votes:${votes(c)}%5.0f score:${scoring.score(votes(c), age = c.age(currentTime))}%8.4f").mkString(s"Frontpage (${totalVotes}):\n", "\n", "\n"))
  // Thread.sleep(100)
}

// write into file and plot with gnuplot:
// :gnuplot> plot 'filename'
println(sortedCollection.map(c => s"${c.quality} ${scoring.estimatedQuality(votes(c))}").mkString("\n"))
// println(sortedCollection.zipWithIndex.map{case (c, rank) => s"${c.quality} ${rank}"}.mkString("\n"))
