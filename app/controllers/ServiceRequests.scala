/**
  * Created by VladVin on 17.11.2016.
  */
package controllers

import models.{Flat, ServiceRequest}
import javax.inject.{Inject, Singleton}

import com.github.tototoshi.play2.json4s.native._
import org.joda.time.DateTime
import org.json4s._
import org.json4s.ext.JodaTimeSerializers
import play.api.data.Form
import play.api.data.Forms._
import play.api.mvc.{Action, Controller}

case class ServiceRequestViewModel(
    id: Long,
    description: String,
    rating: Option[Int],
    status: Option[Int],
    nextVisitDate: Option[DateTime],
    totalCost: Float = 0.0f
)

case class RequestsInfo(sreqs: Seq[ServiceRequestViewModel],
                        flatNumber: Int)

@Singleton
class ServiceRequests @Inject()(json4s: Json4s) extends Controller {
    import json4s._

    implicit val formats = DefaultFormats ++ JodaTimeSerializers.all
      def get(requestId: Long) = Action {
          val serviceRequest = ServiceRequest.get(requestId)
          val nextVisitDate = ServiceRequest.findNextVisitDate(requestId)
          Ok(Extraction.decompose(ServiceRequestViewModel(
            id=serviceRequest.id,
            description = serviceRequest.description,
            rating = serviceRequest.rating,
            status = serviceRequest.status,
            nextVisitDate = nextVisitDate
          )))
      }

      case class ServiceRequestForm(description: String,
                                    status: Int,
                                    rating: Int)

      private val serviceRequestForm = Form(
        mapping(
          "description" -> text,
          "status" -> number,
          "rating" -> number
        )(ServiceRequestForm.apply)(ServiceRequestForm.unapply)
      )

      def update(requestId: Long) = Action { implicit req =>
        serviceRequestForm.bindFromRequest.fold(
          formWithErrors => BadRequest(
            formWithErrors.errors
              .foldLeft("")((res, message) =>
                res + message.message + ",")),
          form => {
            ServiceRequest.update(requestId, form.description, form.rating, form.status)
            Ok
          }
        )
      }

    def all(flatId: Int) = Action {
        val flatNumber = Flat.findById(flatId.toInt)
            .map(f => f.flatNumber).getOrElse(-1)

        val requests = ServiceRequest.findAllForFlat(flatId)
        var serviceReqs = Seq[ServiceRequestViewModel]()
        requests.foreach(sr => serviceReqs = serviceReqs :+ ServiceRequestViewModel(
                id = sr.id,
                description = sr.description,
                rating = sr.rating,
                status = sr.status,
                nextVisitDate = ServiceRequest.findNextVisitDate(sr.id),
                totalCost = ServiceRequest.getTotalCost(sr.id)))

        Ok(Extraction.decompose(
            RequestsInfo(serviceReqs, flatNumber)
        ))
    }
}