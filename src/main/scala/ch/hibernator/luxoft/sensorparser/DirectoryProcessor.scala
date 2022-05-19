package ch.hibernator.luxoft.sensorparser

import akka.actor.ActorSystem
import akka.stream.alpakka.csv.scaladsl.{CsvParsing, CsvToMap}
import akka.stream.scaladsl.FileIO

import java.io.File
import java.nio.file.Paths
import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global

/** Processor of the files containing the sensor data in a directory
  * @param directoryPath
  *   path to the directory
  */
class DirectoryProcessor(directoryPath: String) {
  implicit val actorSystem: ActorSystem = ActorSystem()

  /** Processes all the files and aggregates the sensor data
    * @return
    *   result with aggregated data for the sensors
    */
  def processDirectory(): Future[ProcessingResult] =
    val files = findFiles(directoryPath)
    Future
      .sequence(files.map(processFile))
      .map { processedFiles =>
        processedFiles.foldLeft(Map[String, SensorAggregatedData]()) { case (acc, oneFileData) =>
          oneFileData.foldLeft(acc) { case (merged, (sensorId, sensorData)) =>
            if merged.contains(sensorId) then merged + (sensorId -> merged(sensorId).merge(sensorData))
            else merged + (sensorId -> sensorData)
          }
        }
      }
      .map { mergedData =>
        ProcessingResult(files.size, mergedData.values.toSeq).sortSensors
      }

  def stop(): Unit = actorSystem.terminate()

  private def findFiles(directoryPath: String): Seq[File] =
    new File(directoryPath).listFiles().filter(_.isFile)

  private def processFile(file: File)(implicit actorSystem: ActorSystem): Future[Map[String, SensorAggregatedData]] =
    val filePath = Paths.get(file.getPath)
    val source = FileIO.fromPath(filePath)

    source
      .via(CsvParsing.lineScanner())
      .via(CsvToMap.toMapAsStrings())
      .runFold(Map[String, SensorAggregatedData]()) { case (acc, measurement) =>
        val sensorId = measurement.get("sensor-id")
        val humidity = measurement.get("humidity")
        (for {
          realSensorId <- sensorId
          realHumidity <- humidity
        } yield {
          val newSensorAdded =
            if acc.contains(realSensorId) then acc else acc + (realSensorId -> SensorAggregatedData(realSensorId))
          newSensorAdded + (realSensorId -> newSensorAdded(realSensorId).addMeasurement(realHumidity))
        }).getOrElse(acc)
      }
}
