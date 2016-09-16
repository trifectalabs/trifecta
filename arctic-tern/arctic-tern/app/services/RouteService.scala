package services

import com.trifectalabs.arctic.tern.v0.models._
import play.api.Play.current
import scala.util.Random
import models.TempRoute
import anorm._
import play.api.db.DB

class RouteService {
	val random = new scala.util.Random(System.currentTimeMillis())

	def generateRoute(distance: Double, startLat: Double, startLng: Double, elevation: Double): Route = {
		//val squiggly = scala.math.sqrt(2)/2
		val squiggly = 0.75
    val earthr = 6371000
		//Generate the boundary point for tri and dia methods

		//Generate points uniformly, randomly and independently within a circle of
		//radius (distance) around a location (start)
		//generate the offsets
		//convert distance (adjusted with squiggly) in meters to degrees 
		println("target distance adjusted for squiggly: " + distance * squiggly)
		val distanceindegree = (distance * squiggly / 2)/ 111300
		val w = distanceindegree

		val routes = (0 until 10) map { _ =>
      val t = 2 * scala.math.Pi * random.nextDouble()
      val x = w * scala.math.cos(t)
      val y = w * scala.math.sin(t)

      //adjust x-coordinate for the shrinking of the east-west distances
      val xadjust = x / scala.math.cos(startLat)

      val boundary = new Point(startLat + y, startLng + xadjust)

      //calculate the distance from start to boundary
      val lat1 = startLat * scala.math.Pi / 180
      val lat2 = boundary.lat * scala.math.Pi / 180
      val long1 = startLng * scala.math.Pi / 180
      val long2 = boundary.long * scala.math.Pi / 180
      val d = findDistance(Point(startLat, startLng), boundary)
			val diamond = findDiamond(Point(startLat, startLng), boundary, squiggly, d, distance)

      diamond.foreach(p => println(s"${p.lat},${p.long}"))

      getRouteFromDB(diamond)
    }
		val sortedroutes = routes.sortWith((r1,r2)=> math.abs(r1.distance - distance) < math.abs(r2.distance - distance))

		sortedroutes.foreach(r=>println(r.distance))

    val result = sortedroutes.head
    val str = "LINESTRING("
		Route(
		    id = 0,
		    polyline = result.route.substring(str.length, result.route.length - 1),
		    targetdistance = distance,
		    targetelevation = 0.0,
		    actualdistance = result.distance,
		    actualelevation = 0.0,
		    cost = 0.0
    )
	}

	def createRoute(distance: Double, elevation: Double): Route = {
		val cd1 = 100
		val cd2 = 2.5
		val ce1 = 100
		val ce2 = 1.5
		val d = distance
		val dactual = distance + distance * (random.nextDouble * 0.5)
		val e = elevation
		val eactual = elevation + elevation * (random.nextDouble * 0.5)

		Route(id = 0,
			"",
			d, 
			e, 
			dactual,
			eactual,
			cost = cd1 * scala.math.pow((1 + scala.math.abs(d - dactual) / d), cd2) + 
							ce1 * scala.math.pow((1 + scala.math.abs(e - eactual) / e), ce2)
			)
	}

  val rowParser: RowParser[TempRoute] = new RowParser[TempRoute] {
    def apply(row: Row): SqlResult[TempRoute] = Success {
      TempRoute(
        distance = row[Double]("distance"),
        route = row[String]("route"))
    }
  }

  def getRouteFromDB(coords: List[Point]) : TempRoute = {

    DB.withConnection { implicit c =>
      val result: TempRoute =
        SQL(
          """
            SELECT (r.column).distance, (r.column).route FROM (
              Select trifecta_diamond_main(
              {x1},{y1},
              {x2},{y2},
              {x3},{y3},
              {x4},{y4}
              ) as column
            ) r
          """)
          .on(
            'x1 -> coords(0).lat,
            'y1 -> coords(0).long,
            'x2 -> coords(1).lat,
            'y2 -> coords(1).long,
            'x3 -> coords(2).lat,
            'y3 -> coords(2).long,
            'x4 -> coords(3).lat,
            'y4 -> coords(3).long
          )
          .as(rowParser single)
      result
    }
  }

  def evaluateRoutes(routes: List[Route]): List[Route] = {
    routes
  }
/*******************************************************
  * ___________  _______    __          __      _____  ___    _______   ___       _______
  * ("     _   ")/"      \  |" \        /""\    (\"   \|"  \  /" _   "| |"  |     /"     "|
  * )__/  \\__/|:        | ||  |      /    \   |.\\   \    |(: ( \___) ||  |    (: ______)
  * \\_ /   |_____/   ) |:  |     /' /\  \  |: \.   \\  | \/ \      |:  |     \/    |
  * |.  |    //      /  |.  |    //  __'  \ |.  \    \. | //  \ ___  \  |___  // ___)_
  * \:  |   |:  __   \  /\  |\  /   /  \\  \|    \    \ |(:   _(  _|( \_|:  \(:      "|
     \__|   |__|  \___)(__\_|_)(___/    \___)\___|\____\) \_______)  \_______)\_______) 

  * // *******************************************************/
	def findTriangle(origin: Point, boundary: Point, squiggly: Double, d: Double, distance: Double): List[Point] = {
		//generate a normally distributed distance to travel from start towards boundary
		val firstdistance = random.nextGaussian() * 2 + d/4

		//Find the first point using this distance
		//First we need to find our initial bearing
		val firstbearing = findBearing(origin, boundary)
		
		//Find destination point
		val firstpoint = findDestination(origin, firstbearing, firstdistance)
		
		val seconddistance = (squiggly * distance * (squiggly * distance - 2 * firstdistance)) / (2 * (firstdistance - squiggly * distance))
		val secondbearing = firstbearing + scala.math.Pi / 2
		val secondpoint = findDestination(firstpoint, secondbearing, seconddistance)
		List(origin, firstpoint, secondpoint)
	}

/*******************************************************
  * ________   __          __       ___      ___     ______    _____  ___   ________
  * |"      "\ |" \        /""\     |"  \    /"  |   /    " \  (\"   \|"  \ |"      "\
  * (.  ___  :)||  |      /    \     \   \  //   |  // ____  \ |.\\   \    |(.  ___  :)
  * |: \   ) |||:  |     /' /\  \    /\\  \/.    | /  /    ) :)|: \.   \\  ||: \   ) ||
  * (| (___\ |||.  |    //  __'  \  |: \.        |(: (____/ // |.  \    \. |(| (___\ ||
|:       :)/\  |\  /   /  \\  \ |.  \    /:  | \        /  |    \    \ ||:       :) 
(________/(__\_|_)(___/    \___)|___|\__/|___|  \"_____/    \___|\____\)(________/  
                                                                                                                                                                     
*******************************************************/
	def findDiamond(origin: Point, boundary: Point, squiggly: Double, d: Double, distance: Double): List[Point] = {
		//println("target distance: " + d)
		//Find the first point distance (r3)
		val firstdistance = random.nextGaussian() * 2 + d/4
		val firstbearing = findBearing(origin, boundary)
		//First point at point a
		val firstpoint = findDestination(origin, firstbearing, firstdistance)

		//Find the mid point b
		val middistance = random.nextGaussian() * 2 + firstdistance/4
		val midpoint = findDestination(origin, firstbearing, middistance)

		val oritomid = findDistance(origin, midpoint)
		val midtofirst = findDistance(firstpoint, midpoint)

		val p2 = scala.math.pow(oritomid, 2)
		val q2 = scala.math.pow(midtofirst, 2)
		val squiggly2 = scala.math.pow(squiggly, 2)
		val secondthirddistance = scala.math.sqrt(
															16 * p2 * p2 - 
															32 * p2 * q2 - 
															8 * p2 * squiggly2 * distance * distance +
															16 * q2 * q2 - 
															8 * q2 * squiggly2 * distance * distance + 
															squiggly2 * squiggly2 * scala.math.pow(distance, 4)) / 
															(4 * squiggly * distance)	

		val secondbearing = firstbearing + scala.math.Pi / 2
		val thirdbearing = secondbearing + scala.math.Pi
		val secondpoint = findDestination(midpoint, secondbearing, secondthirddistance)
		val thirdpoint = findDestination(midpoint, thirdbearing, secondthirddistance)
		List(origin, secondpoint, firstpoint, thirdpoint)
	}

	//Helper function to find the distance between a pair of lat/long points
	def findDistance(origin: Point, end: Point): Double = {
		val earthr = 6371000
		val lat1 = origin.lat * scala.math.Pi / 180 
		val lat2 = end.lat * scala.math.Pi / 180
		val long1 = origin.long * scala.math.Pi / 180
		val long2 = end.long * scala.math.Pi / 180
		val a = scala.math.pow(scala.math.sin((lat2 - lat1)/2),2) 	+
		 	scala.math.cos(lat1) *
			scala.math.cos(lat2) * 
			scala.math.pow(scala.math.sin((long2-long1)/2),2)
		val c = 2 * scala.math.atan2(scala.math.sqrt(a),scala.math.sqrt(1-a))
		val d = earthr * c
		d
	}

	//Helper function to find the initial bearing travelling between a pair of lat/long points
	def findBearing(origin: Point, end: Point): Double = {
		val lat1 = origin.lat * scala.math.Pi / 180 
		val lat2 = end.lat * scala.math.Pi / 180
		val long1 = origin.long * scala.math.Pi / 180
		val long2 = end.long * scala.math.Pi / 180
		val theta = scala.math.atan2(
					scala.math.sin(long2 - long1) * scala.math.cos(lat2),
					scala.math.cos(lat1) * scala.math.sin(lat2) - 
					scala.math.sin(lat1) * scala.math.cos(lat2) * 
					scala.math.cos(long2-long1))
		val thetadegree = theta / scala.math.Pi * 180 
		val thetanorm = ((thetadegree + 360) % 360) * scala.math.Pi / 180
		thetanorm
	}

	//Helper function to find the a lat/long point given origin point, bearing and distance
	def findDestination(origin: Point, bearing: Double, distance: Double): Point = {
		val earthr = 6371000
		val lat1 = origin.lat * scala.math.Pi / 180 
		val long1 = origin.long * scala.math.Pi / 180
		val lat2 = scala.math.asin(scala.math.sin(lat1) * scala.math.cos(distance/earthr) +
											scala.math.cos(lat1) * scala.math.sin(distance/earthr) *
											scala.math.cos(bearing)) 
		val long2 = long1 + scala.math.atan2(
							scala.math.sin(bearing) * scala.math.sin(distance/earthr) * scala.math.cos(lat1), 
							scala.math.cos(distance/earthr) - scala.math.sin(lat1) * scala.math.sin(lat2))
		val destination = new Point(lat2 / scala.math.Pi * 180, long2 / scala.math.Pi * 180)
		destination
	}

}
