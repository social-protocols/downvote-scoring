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
@JSImport("src/main/css/tailwind.css", JSImport.Namespace)
object TailwindCss extends js.Object

object Hello {
  TailwindCss // load css

  def main(args: Array[String]) =
    OutWatch.renderReplace[IO]("#app", app).unsafeRunSync()

  val app = {
    val liveNewPage     = Subject.behavior[Seq[Submission]](Nil)
    val liveTopPage     = Subject.behavior[Seq[Submission]](Nil)
    val bestQualityPage = Subject.behavior[Seq[Submission]](Nil)
    val tickTime        = Subject.behavior(15)
    val resetTrigger    = Subject.behavior(())
    val subSteps        = 600
    val tick            = resetTrigger
      .combineLatest(tickTime)
      .switchMap { case (_, tickTime) =>
        Observable
          .intervalMillis(tickTime)
          .map(_ => if (tickTime == 0) subSteps else 1)
      }
      .publish

    tick.value.foreach { substeps =>
      for (_ <- 0 until substeps)
        Simulation.nextStep()
    }

    // visualization runs independently of simulation
    tick.value.sampleMillis(33).foreach { _ =>
      liveNewPage.onNext(Simulation.newpage(Simulation.submissions).toSeq)
      liveTopPage.onNext(Simulation.frontpage(Simulation.submissions).toSeq)
      bestQualityPage.onNext(Simulation.bestQualityFrontpage(Simulation.submissions).toSeq)
    }

    div(
      tick.value.scan(0L)((sum, substeps) => sum + substeps).map(timeSpanFromSeconds),
      managed(SyncIO(tick.connect())),
      SpeedSlider(tickTime),
      div(
        display.flex,
        liveNewPage.map(x => showPage(x)),
        liveTopPage.map(x => showPage(x)),
        bestQualityPage.map(showPage),
      ),
    )
  }

  def showPage(submissions: Seq[Submission]) =
    div(
      cls := "p-5",
      submissions.take(30).map(showSubmission),
    )

  def timeSpanFromSeconds(seconds: Long): String = {
    val ageHours = seconds / 3600
    val ageMin   = (seconds % 3600) / 60
    s"${if (ageHours == 0) s"${ageMin}min" else s"${ageHours}h"}"
  }

  def showSubmission(submission: Submission): HtmlVNode = {
    val title    = s"Story ${submission.id}"
    val subtitle =
      s"${submission.score} points, ${timeSpanFromSeconds(Simulation.timeSeconds - submission.timeSeconds)} ago"

    val qualityAlpha = Math.min(submission.quality / 0.04, 1.0)
    div(
      cls := "mt-2",
      div(
        div(
          cls     := "bg-blue-400 inline-block mr-1 rounded-sm",
          opacity := qualityAlpha,
          width   := "10px",
          height  := "10px",
        ),
        title,
      ),
      div(subtitle, opacity := 0.5),
    )
  }
}
