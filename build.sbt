name := """braces"""

version := "1.0"

scalaVersion := "2.11.8"

lazy val akkaVersion = "2.4.16"
lazy val akkaHttpVersion = "10.0.3"

libraryDependencies ++= Seq(
  // akka streaming
  "com.typesafe.akka"      %% "akka-stream"              % akkaVersion,
  "com.typesafe.akka"      %% "akka-http"                % akkaHttpVersion,
  "com.typesafe.akka"      %% "akka-stream-kafka"        % "0.13",
  // machine learning from h2o
  "ai.h2o"                  % "h2o-genmodel"             % "3.10.2.2",
  "org.scala-lang.modules" %% "scala-parser-combinators" % "1.0.4"
)
