package ch.hibernator.luxoft.sensorparser

/** Result of the directory processing
  * @param numFiles
  *   number of files processed
  * @param sensorData
  *   aggregated data for each sensor
  */
case class ProcessingResult(numFiles: Int, sensorData: Seq[SensorAggregatedData]) {

  /** Sorts sensors by average humidity, descending
    * @return
    *   processing result with sorted sensor data
    */
  def sortSensors: ProcessingResult = copy(sensorData = sensorData.sortBy(_.avgHumidity).reverse)

  /** Returns total number of measurements
    * @return
    *   total number of measurements
    */
  def numProcessedMeasurements: Int = sensorData.map(_.totalMeasurements).sum

  /** Returns number of failed measurements
    * @return
    *   number of failed measurements
    */
  def numFailedMeasurements: Int = sensorData.map(_.numFailedMeasurements).sum

  override def toString: String =
    s"""Number of processed files: $numFiles
       |Number of processed measurements: $numProcessedMeasurements
       |Number of failed measurements: $numFailedMeasurements
       |
       |Sensors with highest avg humidity:
       |
       |sensor-id,min,avg,max
       |
       |""".stripMargin + s"${sensorData.map(_.toString).mkString("\n")}"
}
