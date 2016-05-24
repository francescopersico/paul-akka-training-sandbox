package collaboration.controllers

import play.api.libs.concurrent.Execution.Implicits.defaultContext
import play.api.mvc._
import scala.concurrent._
import scala.concurrent.duration._

class DiscussionsController extends Controller {
  
  def discussion(forumId: String, discussionId: String) = Action.async {
    Future { BadRequest("Not implemented.") }
  }
  
  def startDiscussion(forumId: String) = Action.async {
    Future { BadRequest("Not implemented.") }
  }
}
