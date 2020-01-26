import com.amazon.advertising.api.sample.SignedRequestsHelper
import scala.xml._
import java.net._
import scala.io.Source
import scala.actors.Actor._

class RateBooks(accessId: String, secretKey: String, endPoint: String) {

  def rateBooksSequential(isbnNumbers : List[String]) = {
    isbnNumbers.map{ isbn =>
      Map(isbn -> getRatingFromWebService(isbn))
    }
  }

  def rateBooksParallel(isbnNumbers : List[String]) = {
    val caller = self

    isbnNumbers.foreach( isbnNumber =>
      actor{
        caller ! (isbnNumber, getRatingFromWebService(isbnNumber).head)
      }
    )

    isbnNumbers.map{ _ =>
      receive{
        case (isbn: String, (title: String, rank: Int)) =>
          Map(isbn -> Map(title -> rank))
      }
    }
  }

  def getRatingFromWebService(isbn: String): Map[String, Int] = {
    val url = generateSignedURL(isbn)
    try {
      parseXMLFromURL(url)
    }
    catch {
      case exception : Exception => Map("Invalid ISBN/Signature or Host Unavailable" -> 0)
    }
  }

  def parseXMLFromURL(url: String): Map[String, Int] = {

    val xmlString = Source.fromURL(new URL(url)).mkString
    val xml = XML.loadString(xmlString)

    val bookName = (xml \\ "Items" \\ "Items" \\ "ItemAttributes" \\ "Title").text
    val rank = (xml \\ "Items" \\ "Item" \\ "SalesRank").text

    if(rank.toString() == "")
      Map(bookName.toString() -> 0 )
    else
      Map(bookName.toString() -> rank.toString().toInt )
  }

  def generateSignedURL(isbn: String) = {
    val signedRequestsHelper = SignedRequestsHelper.getInstance(endPoint, accessId, secretKey)
    val URLParams = new java.util.HashMap[String, String]()
    URLParams.put("Service","AWSECommerceService")
    URLParams.put("Version", "2009-03-31")
    URLParams.put("Operation", "ItemLookup")
    URLParams.put("AssociateTag", "m03529-20")
    URLParams.put("ItemId", isbn.toString())
    URLParams.put("ResponseGroup", "Large")

    signedRequestsHelper.sign(URLParams)
  }
}
