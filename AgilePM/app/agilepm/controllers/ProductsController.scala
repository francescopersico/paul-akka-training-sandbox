package agilepm.controllers

import agilepm.models._
import agilepm.views._
import akka.actor._
import akka.pattern.ask
import akka.util.Timeout
import javax.inject._
import play.api.libs.concurrent.Execution.Implicits.defaultContext
import play.api.libs.functional.syntax._
import play.api.libs.json._
import play.api.mvc._
import scala.concurrent._
import scala.concurrent.duration._

class ProductsController @Inject() (system: ActorSystem) extends Controller {

  implicit val timeout = Timeout(5.seconds)

  // curl -i -H 'Content-Type: application/json' -X POST -d '{"name":"Product1","description":"This is product 1.", "requestDiscussion":true}' http://localhost:9004/agilepm/products
  
  def createProduct() = Action.async { request =>
    import ProductData._
    
    val data = request.body.asJson.get.as[ProductData]
    
    val productId = Product.nextId
    val product = system.actorOf(Props(classOf[Product], productId), productId)
    
    val future = product ? CreateProduct(data.name, data.description, data.requestDiscussion)

    future.mapTo[CreateProductResult].map { result =>
      Created(Json.toJson(IdentifiedProductData(result.productId, result.name, result.description, result.discussionRequested, "")))
        .withHeaders(LOCATION -> productLocation(result.productId))
    }
  }
  
  //  curl -X GET http://localhost:9004/agilepm/products/:id

  def product(id: String) = Action.async {
    val future = ProductsViews() ? QueryProductView(id)
    
    future.mapTo[ProductViewResult].map { result =>
      if (result.view.isDefined) {
        val view = result.view.get
        Ok(Json.toJson(IdentifiedProductData(result.productId, view.name, view.description, view.discussionRequested, view.discussionId)))
      } else
        NotFound(s"Product does not exist:  $id")
    }
  }
  
  private def productLocation(productId: String): String = {
    
    s"/agilepm/products/${productId}"
  }
}

case class ProductData(name: String, description: String, requestDiscussion: Boolean)

object ProductData {
  
  implicit val productDataWrites: Writes[ProductData] = (
    (JsPath \ "name").write[String] and
    (JsPath \ "description").write[String] and
    (JsPath \ "requestDiscussion").write[Boolean]
  )(unlift(ProductData.unapply))

  implicit val productDataReads: Reads[ProductData] = (
    (JsPath \ "name").read[String] and
    (JsPath \ "description").read[String] and
    (JsPath \ "requestDiscussion").read[Boolean]
  )(ProductData.apply _)
}

case class IdentifiedProductData(
    productId: String,
    name: String,
    description: String,
    requestDiscussion: Boolean,
    discussionId: String)

object IdentifiedProductData {
  
  implicit val productDataWrites: Writes[IdentifiedProductData] = (
    (JsPath \ "productId").write[String] and
    (JsPath \ "name").write[String] and
    (JsPath \ "description").write[String] and
    (JsPath \ "requestDiscussion").write[Boolean] and
    (JsPath \ "discussionId").write[String]
  )(unlift(IdentifiedProductData.unapply))

  implicit val productDataReads: Reads[IdentifiedProductData] = (
    (JsPath \ "productId").read[String] and
    (JsPath \ "name").read[String] and
    (JsPath \ "description").read[String] and
    (JsPath \ "requestDiscussion").read[Boolean] and
    (JsPath \ "discussionId").read[String]
  )(IdentifiedProductData.apply _)
}
