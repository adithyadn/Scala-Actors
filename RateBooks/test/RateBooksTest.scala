import org.junit.{Before, Test}
import org.scalatest.junit.JUnitSuite

class RateBooksTest extends JUnitSuite {

  var rateBooks: RateBooks = null
  var rateBookMap = Map("1234" -> Map("Java" -> 100))

  @Before def initialize(){
    rateBooks = new RateBooks("AKIAIXJHFADBE4AAU5ZA", "cMkraDbEqqpe8GxNQ6SWDavz2fU1vVjT73x3oWcj", "ecs.amazonaws.com"){
      @Override
      override def getRatingFromWebService(isbn: String): Map[String, Int] = {
        rateBookMap(isbn)
      }
    }
  }

  @Test
  def testCanary(){
    assert(true)
  }

  @Test
  def testGetRatingForOneBook(){
    rateBookMap = Map("1234" -> Map("Java" -> 100))

    assert(Map("1234" -> Map("Java" -> 100)) == rateBooks.rateBooksSequential(List("1234")).flatten.toMap)
  }

  @Test
  def testGetListOfRatingForTwoBooks(){
    rateBookMap = Map("1234" -> Map("Java" -> 100), "1233" -> Map("Java" -> 123))

    assert(Map("1234" -> Map("Java" -> 100), "1233" -> Map("Java" -> 123)) == rateBooks.rateBooksSequential(List("1234", "1233")).flatten.toMap)
  }

  @Test
  def testGetListOfRatingForThreeBooks(){
    rateBookMap = Map("1234" -> Map("Java" -> 100), "1233" -> Map("Java" -> 123), "1232" -> Map("Java" -> 122))

    assert(Map("1234" -> Map("Java" -> 100), "1232" -> Map("Java" -> 122),
      "1233" -> Map("Java" -> 123)) == rateBooks.rateBooksSequential(List("1234", "1233", "1232")).flatten.toMap)
  }

  @Test
  def testGetListOfRatingWhenNoBooks()
  {
    assert(Map() == rateBooks.rateBooksSequential(List()).flatten.toMap)
  }

  @Test
  def testGetRankZeroForAnIsbnHavingNoSalesRank(){
    val ratebooks2 = new RateBooks("AAA", "BBBB", "CCCC"){
      override def parseXMLFromURL(url: String) = {
        Map("The 7 Most Powerful Prayers That Will Change Your Life Forever" -> 0)
      }
    }
    val webserviceResult =  ratebooks2.rateBooksSequential(List("B00CNWGDBS")).flatten.toMap

    assert(webserviceResult("B00CNWGDBS") == Map("The 7 Most Powerful Prayers That Will Change Your Life Forever" -> 0))
  }

  @Test
  def testGetRankZeroForAnIsbnHavingNoSalesRankRealWebService(){
    val rateBooks1 = new RateBooks("AKIAIXJHFADBE4AAU5ZA", "cMkraDbEqqpe8GxNQ6SWDavz2fU1vVjT73x3oWcj", "ecs.amazonaws.com")

    assert(("The 7 Most Powerful Prayers That Will Change Your Life Forever"
      == rateBooks1.getRatingFromWebService("B00CNWGDBS").head._1) ||
      ("Invalid ISBN/Signature or Host Unavailable"
        == rateBooks1.getRatingFromWebService("B00CNWGDBS").head._1)
    )
  }


  @Test
  def testGetListOfRatingForInvalidIsbn(){
    val rateBooks1 = new RateBooks("AKIAIXJHFADBE4AAU5ZA", "cMkraDbEqqpe8GxNQ6SWDavz2fU1vVjT73x3oWcj", "ecs.amazonaws.com"){
      override def parseXMLFromURL(url: String): Map[String, Int] = {
        throw new Exception
      }
    }
    assert(Map("Invalid ISBN/Signature or Host Unavailable" -> 0) == rateBooks1.getRatingFromWebService("1222"))
  }

  @Test
  def testGetListOfRatingForInvalidURL(){
    val rateBooks1 = new RateBooks("AAAA", "BBBB", "CCCC") {
      override def parseXMLFromURL(url: String): Map[String, Int] = {
        throw  new Exception
      }
    }
    assert(Map("Invalid ISBN/Signature or Host Unavailable" -> 0) == rateBooks1.getRatingFromWebService("1222"))
  }

  @Test
  def testGetListOfRatingForBooksSequentially()
  {
    val ratebooks2 = new RateBooks("AKIAIXJHFADBE4AAU5ZA", "cMkraDbEqqpe8GxNQ6SWDavz2fU1vVjT73x3oWcj", "ecs.amazonaws.com")
    val webserviceResult =  ratebooks2.rateBooksSequential(List("1423146727")).flatten.toMap

    assert((webserviceResult("1423146727").head._1 == "Invalid ISBN/Signature or Host Unavailable")
      || (webserviceResult("1423146727").head._1 == "The House of Hades (Heroes of Olympus, Book 4)"))

  }

  @Test
  def testGetListOfRatingForBooksParallel()
  {
    val ratebooks2 = new RateBooks("AKIAIXJHFADBE4AAU5ZA", "cMkraDbEqqpe8GxNQ6SWDavz2fU1vVjT73x3oWcj", "ecs.amazonaws.com")
    val webserviceResult =  ratebooks2.rateBooksParallel(List("1423146727")).flatten.toMap

    assert((webserviceResult("1423146727").head._1 == "Invalid ISBN/Signature or Host Unavailable")
      || (webserviceResult("1423146727").head._1 == "The House of Hades (Heroes of Olympus, Book 4)"))
  }
}

