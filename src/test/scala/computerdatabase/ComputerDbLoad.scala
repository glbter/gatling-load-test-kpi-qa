package computerdatabase

import io.gatling.core.Predef._
import io.gatling.http.Predef._

import scala.concurrent.duration.DurationInt

class ComputerDbLoad extends Simulation {

  val httpProtocol = http
    .baseUrl("http://computer-database.gatling.io")
    .acceptHeader("text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,*/*;q=0.8")
    .acceptEncodingHeader("gzip, deflate, br")
    .acceptLanguageHeader("uk-UA,uk;q=0.8,en-US;q=0.5,en;q=0.3")
    .userAgentHeader("Mozilla/5.0 (Windows NT 6.1; Win64; x64; rv:94.0) Gecko/20100101 Firefox/94.0")

  val scn1 = scenario("get roots")
    .exec(http("request root")
      .get("/")
      .check(
        status is 200,
        regex("""\d+ computers found""")))
    .exec(http("request root computers")
      .get("/computers")
      .check(
        status is 200,
        regex("""\d+ computers found""")))
    .exec(http("request root for new")
      .get("/computers/new")
      .check(
        status is 200,
        regex("""Add a computer""")))
    .pause(5)


  val scn2 = scenario("switch orders")
    .exec(http("order companyName by desc")
      .get("/computers?p=0&n=10&s=companyName&d=desc")
      .check(status is 200))
    .exec(http("order companyName by acs")
      .get("/computers?p=0&n=10&s=companyName&d=asc")
      .check(status is 200))
    .pause(6)
    .exec(http("order discontinued by desc")
      .get("/computers?p=0&n=10&s=discontinued&d=desc")
      .check(status is 200))
    .exec(http("order discontinued by acs")
      .get("/computers?p=0&n=10&s=discontinued&d=asc")
      .check(status is 200))
    .pause(1)
    .exec(http("order introduced by desc")
      .get("/computers?p=0&n=10&s=introduced&d=desc")
      .check(status is 200))
    .exec(http("order introduced by acs")
      .get("/computers?p=0&n=10&s=introduced")
      .check(status is 200))
    .pause(3, 7)
    .exec(http("order name by desc")
      .get("/computers?p=0&n=10&s=name&d=desc")
      .check(status is 200))
    .exec(http("order name by acs")
      .get("/computers?p=0&n=10&s=name")
      .check(status is 200))
    .pause(3, 7)


  val scn3 = scenario("25 elems page")
    .exec(http("page 2, 25 elems")
      .get("/computers?p=2&n=25")
      .check(status is 200))
    .exec(http("page 3, 25 elems")
      .get("/computers?p=3&n=25")
      .check(status is 200))
    .exec(http("page 3, 25 elems, order by name")
      .get("/computers?p=2&n=25&s=name")
      .check(status is 200))


  val scn4 = scenario("create, update")
    .exec(http("create req1")
      .post("/computers")
      .formParam("""name""", """apple m1 macbook""")
      .formParam("""introduced""", """2020-09-30""")
      .formParam("""discontinued""", """""")
      .formParam("""company""", """1""")
      .check(status is 200))
    .exec(http("create req2")
      .post("/computers")
      .formParam("""name""", """apple m11 super-puper-book""")
      .formParam("""introduced""", """2020-09-30""")
      .formParam("""discontinued""", """""")
      .formParam("""company""", """37""")
      .check(status is 200))
    .exec(http("update req")
      .post("/computers/508")
      .formParam("""name""", """CER-203""")
      .formParam("""introduced""", """2020-09-30""")
      .formParam("""discontinued""", """""")
      .formParam("""company""", """2""")
      .check(status is 200))


  val scn5 = scenario("find by name")
    .exec(http("find macbook")
      .get("/computers?f=macbook"))
    .exec(http("find apple")
      .get("/computers?f=apple"))
    .exec(http("find ibm")
      .get("/computers?f=ibm"))
    .exec(http("find atari")
      .get("/computers?f=atari"))

  setUp(
    scn1.inject(constantUsersPerSec(1) during(1 minute))
      .protocols(httpProtocol),
    scn2.inject(atOnceUsers(1))
      .protocols(httpProtocol),
    scn3.inject(rampUsersPerSec(1).to(2).during(15.seconds).randomized)
      .protocols(httpProtocol),
    scn4.inject(rampConcurrentUsers(1).to(2).during(20.seconds))
      .protocols(httpProtocol),
    scn5.inject(atOnceUsers(1))
      .protocols(httpProtocol)
  )
}
