name := "SudokuGame"
scalaVersion := "3.8.2"


libraryDependencies ++= Seq(
  "org.scalafx" %% "scalafx" % "20.0.0-R31",
  "org.openjfx" % "javafx-base" % "20.0.1",
  "org.openjfx" % "javafx-controls" % "20.0.1",
  "org.openjfx" % "javafx-graphics" % "20.0.1",
  "software.amazon.awssdk" % "dynamodb" % "2.20.125",
  "software.amazon.awssdk" % "netty-nio-client" % "2.20.125",
  "org.mindrot" % "jbcrypt" % "0.4",
  "org.slf4j" % "slf4j-simple" % "2.0.13",
  "com.typesafe" % "config" % "1.4.2",
)
