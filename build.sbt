name := "SudokuGame"
scalaVersion := "3.8.2"


libraryDependencies ++= Seq(
  "org.scalafx" %% "scalafx" % "20.0.0-R31",
  "org.openjfx" % "javafx-base" % "20.0.1",
  "org.openjfx" % "javafx-controls" % "20.0.1",
  "org.openjfx" % "javafx-graphics" % "20.0.1",
  "org.mindrot" % "jbcrypt" % "0.4",
  "org.slf4j" % "slf4j-simple" % "2.0.13",
  "com.typesafe" % "config" % "1.4.2",
  "org.scalameta" %% "munit" % "1.0.0" % Test,
  "com.lihaoyi" %% "ujson" % "3.1.3"
)
