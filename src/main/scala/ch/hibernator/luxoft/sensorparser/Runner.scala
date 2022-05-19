package ch.hibernator.luxoft.sensorparser

import akka.actor.ActorSystem
import akka.stream.alpakka.csv.scaladsl.{CsvParsing, CsvQuotingStyle, CsvToMap}
import akka.stream.scaladsl.{FileIO, Sink}

import java.io.File
import java.nio.file.{Path, Paths}
import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global

/** Sensor data processing entry point
  */
object Runner:

  def main(args: Array[String]): Unit =
    val directoryProcessor = new DirectoryProcessor(args.head)

    directoryProcessor
      .processDirectory()
      .onComplete { result =>
        result.foreach(println)
        directoryProcessor.stop()
      }

end Runner
