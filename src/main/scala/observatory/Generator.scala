package observatory

import java.nio.file.{Files, Path, Paths}

import com.sksamuel.scrimage.Image

object Generator {
  val tempColors = Seq(
    (60d, Color(255, 255, 255)),
    (32d, Color(255, 0, 0)),
    (12d, Color(255, 255, 0)),
    (0d, Color(0, 255, 255)),
    (-15d, Color(0, 0, 255)),
    (-27d, Color(255, 0, 255)),
    (-50d, Color(33, 0, 107)),
    (-60d, Color(0, 0, 0))
  )

  val devColors = Seq(
    (7d, Color(0, 0, 0)),
    (4d, Color(255, 0, 0)),
    (2d, Color(255, 255, 0)),
    (0d, Color(255, 255, 255)),
    (-2d, Color(0, 255, 255)),
    (-7d, Color(0, 0, 255))
  )

  val years = 1975 to 1975

  val tempTilesStoragePath = "C:\\Users\\mprushin\\Desktop\\Scala\\observatory\\target\\temperatures\\"
  val devTilesStoragePath = "C:\\Users\\mprushin\\Desktop\\Scala\\observatory\\target\\deviations\\"

  def generateTempImageImpl(year: Int, zoom: Int, x: Int, y: Int, data: Iterable[(Location, Double)]): Unit = {
    val image = Interaction.tile(data, tempColors, zoom, x, y)
    val stringPath = tempTilesStoragePath + "%d/%d/%d-%d.png".format(year, zoom, x, y)
    writeImage(image, stringPath)
  }

  def generateDevImageImpl(year: Int, zoom: Int, x: Int, y: Int, data: Iterable[(Location, Double)]): Unit = {
    val image = Interaction.tile(data, devColors, zoom, x, y)
    val stringPath = devTilesStoragePath + "%d/%d/%d-%d.png".format(year, zoom, x, y)
    writeImage(image, stringPath)
  }

  def writeImage(image: Image, path: String): Path = {
    val parentDir = Paths.get(path).getParent
    if (!Files.exists(parentDir)) Files.createDirectories(parentDir)
    image.output(path)
  }

  def main(args: Array[String]) {

    val resultsMap = Extraction.sc.parallelize(years).map(year=>(year, Extraction.locateTemperatures(year, "/stations.csv", "/%d.csv".format(year))))
      .map(pair=>(pair._1, Extraction.locationYearlyAverageRecords(pair._2).toSeq))

    Interaction.generateTiles[Seq[(Location, Double)]](resultsMap.collect(), generateTempImageImpl)
  }
}