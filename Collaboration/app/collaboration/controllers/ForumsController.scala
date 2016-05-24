package collaboration.controllers

import play.api.libs.concurrent.Execution.Implicits.defaultContext
import play.api.mvc._
import scala.concurrent._
import scala.concurrent.duration._

class ForumsController extends Controller {
  
  def forum(forumId: String) = Action.async {
    Future { BadRequest("Not implemented.") }
  }
  
  def startForum() = Action.async {
    Future { BadRequest("Not implemented.") }
  }
}
