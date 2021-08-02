package simulation

import outwatch._
import outwatch.dsl._
import outwatch.dsl.tags.extra.article
import scala.scalajs.js
import js.JSConverters._
import scala.scalajs.js.annotation._
import org.scalajs.dom.console
import org.scalajs.dom
import cats.effect.SyncIO
import scala.util.Try
import collection.mutable
import colibri.Observable
import colibri.Cancelable
import colibri.Subject

object SpeedSlider {
  def apply(tickTime: Subject[Int]) = {
    val max = 300
    val min = 0

    val buttonStyle = cls := "bg-blue-500 rounded text-white px-2"

    div(
      display.flex,
      button("slow", onClick.use(max) --> tickTime, buttonStyle),
      input[Any](
        tpe := "range",
        width := "500px",
        onInput.value.map(-_.toInt) --> tickTime,
        value <-- tickTime.map((v) => (-v).toString),
        minAttr := s"-${max}",
        maxAttr := s"-${min}"
      ),
      tickTime,
      button("50", onClick.use(50) --> tickTime, buttonStyle),
      button("15", onClick.use(15) --> tickTime, buttonStyle),
      button("fast", onClick.use(min) --> tickTime, buttonStyle)
    )
  }

}
