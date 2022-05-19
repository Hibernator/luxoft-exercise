package ch.hibernator.luxoft.sensorparser

import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should

import java.io.File
import scala.concurrent.Await
import scala.concurrent.duration.Duration

class DirectoryProcessorTest extends AnyFlatSpec with should.Matchers {
  "A DirectoryProcessor" should "parse the sensor data and aggregate them" in {
    val directoryProcessor = new DirectoryProcessor("./src/test/resources")

    val actualResult = Await.result(directoryProcessor.processDirectory(), Duration.Inf)

    actualResult.numFiles shouldBe 4
    actualResult.numProcessedMeasurements shouldBe 13
    actualResult.numFailedMeasurements shouldBe 5
    actualResult.sensorData should have size 4
    actualResult.sensorData.head should matchPattern {
      case SensorAggregatedData("s2", Some(78), Some(88), Some(82.0), _, _) =>
    }
    actualResult.sensorData.tail.head should matchPattern {
      case SensorAggregatedData("s1", Some(10), Some(98), Some(54.0), _, _) =>
    }
    actualResult.sensorData(2) should matchPattern {
      case SensorAggregatedData("s4", Some(10), Some(30), Some(20.0), _, _) =>
    }
    actualResult.sensorData.last should matchPattern { case SensorAggregatedData("s3", None, None, None, _, _) => }

    println(actualResult)
  }
}
