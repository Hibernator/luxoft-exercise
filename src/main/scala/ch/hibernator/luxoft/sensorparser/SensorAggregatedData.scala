package ch.hibernator.luxoft.sensorparser

import ch.hibernator.luxoft.sensorparser.SensorAggregatedData.display

case class SensorAggregatedData(
    name: String,
    minHumidity: Int = 101,
    maxHumidity: Int = -1,
    avgHumidity: Double = -1.0,
    numSucessfulMeasurements: Int = 0,
    numFailedMeasurements: Int = 0
) {

  def addMeasurement(humidity: String): SensorAggregatedData =
    if humidity == "NaN" then
      copy(
        numFailedMeasurements = this.numFailedMeasurements + 1
      )
    else
      val humidityNum = humidity.toInt
      copy(
        minHumidity = if humidityNum < this.minHumidity then humidityNum else this.minHumidity,
        maxHumidity = if humidityNum > this.maxHumidity then humidityNum else this.maxHumidity,
        avgHumidity =
          (this.avgHumidity * this.numSucessfulMeasurements + humidityNum) / (this.numSucessfulMeasurements + 1),
        numSucessfulMeasurements = this.numSucessfulMeasurements + 1
      )

  def merge(otherData: SensorAggregatedData): SensorAggregatedData =
    require(this.name == otherData.name, "Can only merge data from the same sensor")
    copy(
      minHumidity = if otherData.minHumidity < this.minHumidity then otherData.minHumidity else this.minHumidity,
      maxHumidity = if otherData.maxHumidity > this.maxHumidity then otherData.maxHumidity else this.maxHumidity,
      avgHumidity = (otherData.avgHumidity * otherData.numSucessfulMeasurements +
        this.avgHumidity * this.numSucessfulMeasurements) /
        (otherData.numSucessfulMeasurements + this.numSucessfulMeasurements),
      numSucessfulMeasurements = otherData.numSucessfulMeasurements + this.numSucessfulMeasurements,
      numFailedMeasurements = otherData.numFailedMeasurements + this.numFailedMeasurements
    )

  override def toString: String = s"$name, $minHumidity, $maxHumidity, $avgHumidity"
}

object SensorAggregatedData {
  extension (humidity: Int) def display: String = if humidity > -1 then humidity.toString else "NaN"

  extension (humidity: Double) def display: String = if humidity > -1 then humidity.toString else "NaN"

}
