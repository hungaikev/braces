import sbt.Keys.libraryDependencies

name := """braces"""

version := "1.0"

scalaVersion := "2.11.8"

lazy val akkaVersion = "2.4.16"
lazy val akkaHttpVersion = "10.0.3"
lazy val scalaTestVersion = "3.0.0"

libraryDependencies ++= Seq(
  "com.typesafe.akka" %% "akka-stream" % akkaVersion,
  "com.typesafe.akka" %% "akka-http" % akkaHttpVersion,
  "com.typesafe.akka" %% "akka-http-spray-json" % akkaHttpVersion,
  "org.scalatest"     %% "scalatest" % scalaTestVersion % "test",
  "com.typesafe.akka" %% "akka-testkit" % akkaVersion % "test"
)
