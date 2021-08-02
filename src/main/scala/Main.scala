package simulation

import outwatch._
import outwatch.dsl._
import colibri._
import cats.effect.{IO, SyncIO}

import scala.scalajs.js
import scala.scalajs.js.annotation._

// @js.native
// @JSImport("../../../../src/main/css/index.css", JSImport.Namespace)
// object Css extends js.Object

@js.native
@JSImport("../../../../src/main/css/tailwind.css", JSImport.Namespace)
object TailwindCss extends js.Object

object Hello {
  def main(args: Array[String]) = {

    Outwatch.renderReplace[IO]("#app", app).unsafeRunSync()

  }

  val app = {
    val liveNewPage  = Subject.behavior[Seq[Submission]](Nil)
    val liveTopPage  = Subject.behavior[Seq[Submission]](Nil)
    val tickTime     = Subject.behavior(15)
    val resetTrigger = Subject.behavior(())
    val subSteps     = 600
    val tick         = resetTrigger
      .switchMap(_ =>
        tickTime
          .switchMap(tickTime =>
            Observable
              .intervalMillis(tickTime)
              .map(_ => if (tickTime == 0) subSteps else 1),
          ),
      )
      .publish

    tick.foreach { substeps =>
      for (_ <- 0 until substeps)
        Simulation.nextStep()
    }

    tick.sampleMillis(500).foreach { _ =>
      liveTopPage.onNext(Simulation.frontpage(Simulation.submissions).toSeq)
      liveNewPage.onNext(Simulation.newpage(Simulation.submissions).toSeq)
    }

    div(
      tick.scan(0L)((sum, substeps) => sum + substeps).map(timeSpan),
      Modifier.managed(SyncIO(tick.connect())),
      SpeedSlider(tickTime),
      div(
        display.flex,
        liveNewPage.map(x => showPage(x)),
        liveTopPage.map(showPage),
      ),
    )
  }

  def showPage(submissions: Seq[Submission]) = {
    div(
      submissions.map(showSubmission),
    )
  }

  def timeSpan(seconds: Long) = {
    val ageHours = seconds / 3600
    val ageMin   = (seconds % 3600) / 60
    s"${if (ageHours == 0) s"${ageMin}min" else s"${ageHours}h"}"
  }

  def showSubmission(submission: Submission) = {
    val title     = s"Quality: ${submission.quality}"
    val subtitle  = s"${submission.votes} points, ${timeSpan(Simulation.timeSeconds - submission.timeSeconds)} ago"
    val textColor = s"rgba(0,0,255,${submission.quality})"
    div(
      div(title, color := textColor),
      div(subtitle, opacity := 0.5),
    )
  }
}
