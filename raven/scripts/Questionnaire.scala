import scala.io.StdIn.readLine
import java.io._

println("Welcome to the Trifecta fitness questionnaire!")
val file = new File("fitness.csv")
val bw = new BufferedWriter(new FileWriter(file))
questionnaire
bw.close()

def questionnaire(): Unit = {
  val answers = questions
  val fitness = classifyFitness
  writeToFile(answers, fitness, bw)
  val option = menu
  if (option == "y" || option == "1") {
    questionnaire
  }
}

def menu(): String = {
  println("\nEnter another record (y or n)?")
  readLine()
}

case class FitnessQuestionnaire(
  sex: String,
  age: Int,
  height: Double,
  weight: Double,
  waist: Double,
  furthestRun: Double,
  furthestRunTime: Int,
  fastestSplit: Int,
  largestHillRun: Double,
  largestHillRunTime: Int,
  furthestRide: Double,
  furthestRideTime: Int,
  topSpeed: Double,
  largestClimb: Double,
  largestClimbTime: Int
)

def questions(): FitnessQuestionnaire = {
  println("\n\nQuestionnaire!\n(M)ale or (F)emale? ")
  val sex = readLine()
  println("Age? ")
  val age = readLine().toInt
  println("Height (in cm)? ")
  val height = readLine().toDouble
  println("Weight (in kg)? ")
  val weight = readLine().toDouble
  println("Waist (in cm)? ")
  val waist = readLine().toDouble
  println("Furthest run (in km)? ")
  val furthestRun = readLine().toDouble*1000
  println("Time (in hours:minutes:seconds)? ")
  val time1 = readLine().split(":")
  val furthestRunTime = if (time1.length == 1) time1(0).toInt
    else if (time1.length == 2) time1(0).toInt*60 + time1(1).toInt
    else time1(0).toInt*3600 + time1(1).toInt*60 + time1(2).toInt
  println("Fastest split (in minutes:second)? ")
  val time2 = readLine().split(":")
  val fastestSplit = if(time2.length == 1) time2(0).toInt
    else time2(0).toInt*60 + time2(1).toInt
  println("Largest hill run (in m)? ")
  val largestHillRun = readLine().toDouble
  println("Time (in hours:minutes:seconds)? ")
  val time3 = readLine().split(":")
  val largestHillRunTime = if (time3.length == 1) time3(0).toInt
    else if (time3.length == 2) time3(0).toInt*60 + time3(1).toInt
    else time3(0).toInt*3600 + time3(1).toInt*60 + time3(2).toInt
  println("Furthest ride (in km)? ")
  val furthestRide = readLine().toDouble*1000
  println("Time (in hours:minutes:seconds)? ")
  val time4 = readLine().split(":")
  val furthestRideTime = if (time4.length == 1) time4(0).toInt
    else if (time4.length == 2) time4(0).toInt*60 + time4(1).toInt
    else time4(0).toInt*3600 + time4(1).toInt*60 + time4(2).toInt
  println("Top speed on flat (in km/h)? ")
  val topSpeed = readLine().toDouble*1000/60
  println("Largest climb (in m)? ")
  val largestClimb = readLine().toDouble
  println("Time (in hours:minutes:seconds)? ")
  val time5 = readLine().split(":")
  val largestClimbTime = if (time5.length == 1) time5(0).toInt
    else if (time5.length == 2) time5(0).toInt*60 + time5(0).toInt
    else time5(0).toInt*3600 + time5(1).toInt*60 + time5(2).toInt
  FitnessQuestionnaire(sex, age, height, weight, waist, furthestRun,
    furthestRunTime, fastestSplit, largestHillRun, largestHillRunTime,
    furthestRide, furthestRideTime, topSpeed, largestClimb, largestClimbTime)
}

case class FitnessLevel(
  general: Int,
  runEndurance: Int,
  runSprint: Int,
  runClimb: Int,
  bikeEndurance: Int,
  bikeSprint: Int,
  bikeClimb: Int
)

def classifyFitness(): FitnessLevel = {
  println("\nFitness Levels!")
  println("General? ")
  val general = readLine().toInt
  println("Run Endurance? ")
  val runEndurance = readLine().toInt
  println("Run Sprint? ")
  val runSprint = readLine().toInt
  println("Run Climb? ")
  val runClimb = readLine().toInt
  println("Bike Endurance? ")
  val bikeEndurance = readLine().toInt
  println("Bike Sprint? ")
  val bikeSprint = readLine().toInt
  println("Bike Climb? ")
  val bikeClimb = readLine().toInt
  FitnessLevel(general, runEndurance, runSprint, runClimb, bikeEndurance,
    bikeSprint, bikeClimb)
}

def writeToFile(answers: FitnessQuestionnaire, fitness: FitnessLevel, bw: BufferedWriter): Unit = {
  val list = List(answers.sex, answers.age, answers.height, answers.weight, answers.waist,
    answers.furthestRun, answers.furthestRunTime, answers.fastestSplit,
    answers.largestHillRun, answers.largestHillRunTime, answers.furthestRide,
    answers.furthestRideTime, answers.topSpeed, answers.largestClimb,
    answers.largestClimbTime, fitness.general, fitness.runEndurance, fitness.runSprint,
    fitness.runClimb, fitness.bikeEndurance, fitness.bikeSprint, fitness.bikeClimb)
  bw.write(list.mkString(",") + "\n")
}
