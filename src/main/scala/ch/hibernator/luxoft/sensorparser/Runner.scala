package ch.hibernator.luxoft.sensorparser

import akka.actor.ActorSystem
import akka.stream.alpakka.csv.scaladsl.{CsvParsing, CsvQuotingStyle, CsvToMap}
import akka.stream.scaladsl.{FileIO, Sink}

import java.io.File
import java.nio.file.{Path, Paths}
import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global

object Runner:

  def main(args: Array[String]): Unit =
    printSomething(args)

    implicit val actorSystem: ActorSystem = ActorSystem()

    val files = findFiles(args.head)
//    processFile(files.head)

    Future
      .sequence(files.map(processFile))
      .map { processedFiles =>
        processedFiles.foldLeft(Map[String, SensorAggregatedData]()) { case (acc, oneFileData) =>
          oneFileData.foldLeft(acc) { case (merged, (sensorId, sensorData)) =>
            println(sensorData)
            if merged.contains(sensorId) then merged + (sensorId -> merged(sensorId).merge(sensorData))
            else merged + (sensorId -> sensorData)
          }
        }
      }
      .map { mergedData =>
        mergedData.values.toSeq.sortBy(_.avgHumidity)
      }
      .foreach(_.foreach(data => println(data)))

  def printSomething(arguments: Seq[String]): Unit =
    println("Hello from Runner")
    println(arguments.mkString(", "))

  def findFiles(directoryPath: String): Seq[File] =
    new File(directoryPath).listFiles().filter(_.isFile)

  def processFile(file: File)(implicit actorSystem: ActorSystem): Future[Map[String, SensorAggregatedData]] =
    val fileName = file.getName
    val filePath = Paths.get(file.getPath)
    val source = FileIO.fromPath(filePath)

    val result = source
      .via(CsvParsing.lineScanner())
      .via(CsvToMap.toMapAsStrings())
//      .runForeach(x => println(x))
      .runFold(Map[String, SensorAggregatedData]()) { case (acc, measurement) =>
        println(acc)
        println(measurement)
        val sensorId = measurement.get("sensor-id")
        val humidity = measurement.get("humidity")
        (for {
          realSensorId <- sensorId
          realHumidity <- humidity
        } yield {
          println(s"sensor $realSensorId, humidity $realHumidity")
          val newSensorAdded =
            if acc.contains(realSensorId) then acc else acc + (realSensorId -> SensorAggregatedData(realSensorId))
          newSensorAdded + (realSensorId -> (newSensorAdded(realSensorId).addMeasurement(realHumidity)))
        }).getOrElse(acc)
      }

    result.onComplete(println)
    result

end Runner
