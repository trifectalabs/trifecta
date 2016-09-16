package resources

import com.trifectalabs.osprey.v0.models._
import play.api.libs.json._
import java.util.UUID

object StreamPattern {
  def matchStream(stream: kiambogo.scrava.models.Streams, internalActID: UUID) = {
    val streamType = stream.`type` match {
      case "time" => StreamType("Time")
      case "latlng" => StreamType("LatLng")
      case "distance" => StreamType("Distance")
      case "altitude" => StreamType("Altitude")
      case "velocity_smooth" => StreamType("Velocity")
      case "heartrate" => StreamType("Heartrate")
      case "cadence" => StreamType("Cadence")
      case "watts" => StreamType("Power")
      case "watts_calc" => StreamType("PowerCalc")
      case "temp" => StreamType("Temperature")
      case "moving" => StreamType("Moving")
      case "grade_smooth" => StreamType("Grade")
      case _ => throw new RuntimeException("type not found")
    }
    Stream(
      internalActID,
      streamType,
      stream.data.asInstanceOf[List[Any]].mkString(","),
      stream.series_type,
      stream.original_size,
      stream.resolution) 
  }
}
