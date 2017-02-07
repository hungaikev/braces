name := """braces"""

version := "1.0"

scalaVersion := "2.11.8"

lazy val akkaVersion = "2.4.16"
lazy val akkaHttpVersion = "10.0.3"

libraryDependencies ++= Seq(
  "com.typesafe.akka" %% "akka-stream" % akkaVersion,
  "com.typesafe.akka" %% "akka-http" % akkaHttpVersion
)
