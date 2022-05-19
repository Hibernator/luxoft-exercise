package ch.hibernator.luxoft.sensorparser

import ch.hibernator.luxoft.sensorparser.SensorAggregatedData.display

/** Contains aggregate data for one sensor
  * @param name
  *   sensor name/id
  * @param minHumidity
  *   minimal measured humidity
  * @param maxHumidity
  *   maximal measured humidity
  * @param avgHumidity
  *   average humidity
  * @param numSucessfulMeasurements
  *   number of successful measurements
  * @param numFailedMeasurements
  *   number of failed measurements
  */
case class SensorAggregatedData(
    name: String,
    minHumidity: Option[Int] = None,
    maxHumidity: Option[Int] = None,
    avgHumidity: Option[Double] = None,
    numSucessfulMeasurements: Int = 0,
    numFailedMeasurements: Int = 0
) {

  /** Adds a new measurement and recalculates the aggregate data
    * @param measurement
    *   new measurement
    * @return
    *   updated sensor data with recalculated values
    */
  def addMeasurement(measurement: String): SensorAggregatedData =
    if measurement == "NaN" then
      copy(
        numFailedMeasurements = numFailedMeasurements + 1
      )
    else
      val newMeasurement = measurement.toInt
      copy(
        minHumidity =
          if minHumidity.exists(_ > newMeasurement) || minHumidity.isEmpty then Some(newMeasurement) else minHumidity,
        maxHumidity =
          if maxHumidity.exists(_ < newMeasurement) || maxHumidity.isEmpty then Some(newMeasurement) else maxHumidity,
        avgHumidity = avgHumidity
          .map { realAvgHumidity =>
            (realAvgHumidity * numSucessfulMeasurements + newMeasurement) / (numSucessfulMeasurements + 1)
          }
          .orElse(Some(newMeasurement)),
        numSucessfulMeasurements = numSucessfulMeasurements + 1
      )

  /** Merges this aggregated sensor data with data for the same sensor from another source
    * @param otherData
    *   data from the same sensor from another source
    * @return
    *   merged and recalculated aggregated data
    */
  def merge(otherData: SensorAggregatedData): SensorAggregatedData =
    require(name == otherData.name, "Can only merge data from the same sensor")
    copy(
      minHumidity = (for {
        currentMinHumidity <- minHumidity
        otherMinHumidity <- otherData.minHumidity
      } yield {
        if otherMinHumidity < currentMinHumidity then otherMinHumidity else currentMinHumidity
      }).orElse(minHumidity).orElse(otherData.minHumidity),
      maxHumidity = (for {
        currentMaxHumidity <- maxHumidity
        otherMaxHumidity <- otherData.maxHumidity
      } yield {
        if otherMaxHumidity > currentMaxHumidity then otherMaxHumidity else currentMaxHumidity
      }).orElse(maxHumidity).orElse(otherData.maxHumidity),
      avgHumidity = (for {
        currentAvgHumidity <- avgHumidity
        otherAvgHumidity <- otherData.avgHumidity
      } yield {
        (otherAvgHumidity * otherData.numSucessfulMeasurements + currentAvgHumidity * numSucessfulMeasurements) /
          (otherData.numSucessfulMeasurements + numSucessfulMeasurements)
      }).orElse(avgHumidity).orElse(otherData.avgHumidity),
      numSucessfulMeasurements = otherData.numSucessfulMeasurements + numSucessfulMeasurements,
      numFailedMeasurements = otherData.numFailedMeasurements + numFailedMeasurements
    )

  def totalMeasurements: Int = numSucessfulMeasurements + numFailedMeasurements

  override def toString: String = s"$name, ${minHumidity.display}, ${avgHumidity.display}, ${maxHumidity.display}"
}

object SensorAggregatedData {
  extension (number: Option[_])
    /** Returns a string representation of the number if it's defined, or NaN otherwise
      */
    def display: String = number.map(_.toString).getOrElse("NaN")
}
