/**
  * Created by VladVin on 17.11.2016.
  */
package controllers

import javax.inject.{Inject, Singleton}

import com.github.tototoshi.play2.json4s.native._
import models.{Flat, ServiceRequest, Visited}
import org.joda.time.DateTime
import org.json4s._
import org.json4s.ext.JodaTimeSerializers
import play.api.data.Form
import play.api.data.Forms._
import play.api.mvc.{Action, Controller}

case class ServiceRequestViewModel(id: Long,
                                   description: String,
                                   rating: Option[Int],
                                   status: Option[Int],
                                   nextVisitDate: DateTime,
                                   totalCost: Double = 0.0d)

object ServiceRequestViewModel {
    def apply(sr: ServiceRequest): ServiceRequestViewModel = new ServiceRequestViewModel(
        id = sr.id,
        description = sr.description,
        rating = sr.rating,
        status = sr.status,
        nextVisitDate = ServiceRequest.findNextVisitDate(sr.id).orNull,
        totalCost = Visited.getTotalCost(sr.id)
    )

    def apply(sreqs: Seq[ServiceRequest], flatId: Long, flatNumber: Int): RequestsInfo = RequestsInfo(
        sreqs = sreqs.map(ServiceRequestViewModel.apply),
        flatId = flatId,
        flatNumber = flatNumber
    )
}

case class RequestsInfo(sreqs: Seq[ServiceRequestViewModel],
                        flatId: Long,
                        flatNumber: Int)

@Singleton
class ServiceRequests @Inject()(json4s: Json4s) extends Controller {

    import json4s._

    implicit val formats = DefaultFormats ++ JodaTimeSerializers.all
    private val serviceRequestForm = Form(
        mapping(
            "description" -> text,
            "status" -> number,
            "rating" -> number
        )(ServiceRequestForm.apply)(ServiceRequestForm.unapply)
    )

    private val serviceRequestCreateForm = Form(
        mapping(
            "description" -> text
        )(ServiceRequestCreateForm.apply)(ServiceRequestCreateForm.unapply)
    )

    def get(requestId: Long) = Action {
        val serviceRequest = ServiceRequest.get(requestId)
        Ok(Extraction.decompose(ServiceRequestViewModel(serviceRequest)))
    }

    def create(flatId: Long) = Action { implicit req =>
        serviceRequestCreateForm.bindFromRequest.fold(
            formWithErrors => BadRequest(
                formWithErrors.errors
                    .foldLeft("")((res, message) =>
                        res + message.message + ",")),
            form => {
                ServiceRequest.create(flatId, form.description)
                Ok
            }
        )
    }

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
            .map(ServiceRequestViewModel.apply)

        Ok(Extraction.decompose(
            RequestsInfo(requests, flatId, flatNumber)
        ))
    }

    def allActive() = Action {
        Ok(Extraction.decompose(
            Flat.all().map(f =>
                ServiceRequestViewModel(
                    ServiceRequest.findAllActiveForFlat(f.flatId),
                    f.flatId,
                    f.flatNumber)
            )
        ))
    }

    case class ServiceRequestForm(description: String,
                                  status: Int,
                                  rating: Int)

    case class ServiceRequestCreateForm(description: String)
}
