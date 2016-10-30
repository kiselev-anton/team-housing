package controllers

import javax.inject.{Inject, Singleton}

import com.github.tototoshi.play2.json4s.native._
import models._
import org.json4s._
import org.json4s.ext.JodaTimeSerializers
import play.api.mvc._

case class MeterViewModel(id: Integer,
                          title: String,
                          unit: String) {}

case class MeterGroup(title: String,
                      meters: List[MeterViewModel]) {}

@Singleton
class Meters @Inject() (json4s: Json4s) extends Controller {
  import json4s._
  implicit val formats = DefaultFormats ++ JodaTimeSerializers.all

  def findByFlatId(flatId: Int) = Action {
    Ok(Extraction.decompose(
      Meter.findByFlatId(flatId)
        .groupBy(m => m.`type`)
        .map{ case (title: String, meters: List[Meter]) =>
          MeterGroup(title,
            meters.map{ case Meter(meterId, _, _, _, meterUnitId, active, _) =>
              MeterViewModel(meterId, title, MeterUnit.get(meterUnitId).description)}
          )
        }))
  }
}