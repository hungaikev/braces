import sbt.Keys.libraryDependencies

name := """braces"""

version := "1.0"

scalaVersion := "2.11.8"

lazy val akkaVersion = "2.4.17"
lazy val akkaHttpVersion = "10.0.4"
lazy val scalaTestVersion = "3.0.0"

libraryDependencies ++= Seq(
  "com.typesafe.akka" %% "akka-stream"           % akkaVersion,
  
  "com.typesafe.akka" %% "akka-cluster"          % akkaVersion,
  "com.typesafe.akka" %% "akka-cluster-sharding" % akkaVersion,
  "com.typesafe.akka" %% "akka-cluster-tools"    % akkaVersion,
  
  "com.typesafe.akka"        %% "akka-persistence" % akkaVersion,
  "org.iq80.leveldb"          % "leveldb"          % "0.7",
  "org.fusesource.leveldbjni" % "leveldbjni-all"   % "1.8",
  
  "com.typesafe.akka" %% "akka-http"            % akkaHttpVersion,
  "com.typesafe.akka" %% "akka-http-spray-json" % akkaHttpVersion,
  
  "org.scalatest"     %% "scalatest"    % scalaTestVersion % "test",
  "com.typesafe.akka" %% "akka-testkit" % akkaVersion      % "test"
)
