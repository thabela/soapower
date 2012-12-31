package controllers

import play.api.mvc._
import play.api.data._
import play.api.data.Forms._
import play.api.data.validation.Constraints._

import anorm._

import models._
import play.api.libs.json._
import play.api.libs.json.JsObject
import play.api.libs.json.JsString

object Environments extends Controller {

  // use by Json : from scala to json
  private implicit object StatsDataWrites extends Writes[Environment] {
    def writes(data: Environment): JsValue = {
      JsObject(
        List(
          "0" -> JsString(data.name),
          "1" -> JsNumber(data.hourRecordXmlDataMin),
          "2" -> JsNumber(data.hourRecordXmlDataMax),
          "3" -> JsNumber(data.nbDayKeepXmlData),
          "4" -> JsNumber(data.nbDayKeepAllData),
          "5" -> JsString("<a href=\"environments/"+data.id+"\"><i class=\"icon-edit\"></i> Edit</a>")
        ))
    }
  }

  /**
   * This result directly redirect to the application home.
   */
  val Home = Redirect(routes.Environments.index)

  /**
   * Display the list of services.
   */
  def index = Action { implicit request =>
    Ok(views.html.environments.index())
  }

  /**
   * List to Datable table.
   *
   * @return JSON
   */
  def listDatatable = Action { implicit request =>
    val data = Environment.list
    Ok(Json.toJson(Map(
      "iTotalRecords" -> Json.toJson(data.size),
      "iTotalDisplayRecords" -> Json.toJson(data.size),
      "aaData" -> Json.toJson(data)
    ))).as(JSON)
  }

  /**
   * Describe the environment form (used in both edit and create screens).
   */
  val environmentForm = Form(
    mapping(
      "id" -> ignored(NotAssigned: Pk[Long]),
      "name" -> nonEmptyText,
      "hourRecordXmlDataMin" -> number(min=0, max=23),
      "hourRecordXmlDataMax" -> number(min=0, max=24),
      "nbDayKeepXmlData" -> number(min=0, max=10),
      "nbDayKeepAllData" -> number(min=2, max=50)) (Environment.apply)(Environment.unapply))

  /**
   * Display the 'edit form' of a existing Environment.
   *
   * @param id Id of the environment to edit
   */
  def edit(id: Long) = Action {
    Environment.findById(id).map { environment =>
      Ok(views.html.environments.editForm(id, environmentForm.fill(environment)))
    }.getOrElse(NotFound)
  }

  /**
   * Handle the 'edit form' submission
   *
   * @param id Id of the environment to edit
   */
  def update(id: Long) = Action { implicit request =>
    environmentForm.bindFromRequest.fold(
      formWithErrors => BadRequest(views.html.environments.editForm(id, formWithErrors)),
      environment => {
        Environment.update(id, environment)
        Home.flashing("success" -> "Environment %s has been updated".format(environment.name))
      })
  }

  /**
   * Display the 'new environment form'.
   */
  def create = Action {
    Ok(views.html.environments.createForm(environmentForm.fill(new Environment(NotAssigned, "", 6, 22, 2, 5))))
  }

  /**
   * Handle the 'new environment form' submission.
   */
  def save = Action { implicit request =>
    environmentForm.bindFromRequest.fold(
      formWithErrors => BadRequest(views.html.environments.createForm(formWithErrors)),
      environment => {
        Environment.insert(environment)
        Home.flashing("success" -> "Environment %s has been created".format(environment.name))
      })
  }

  /**
   * Handle environment deletion.
   */
  def delete(id: Long) = Action {
    Environment.delete(id)
    Home.flashing("success" -> "Environment has been deleted")
  }

}
