name := """Collaboration"""

organization := "goreactivewithakka"

version := "1.0-SNAPSHOT"

// must be one of:
// 9001 (Registry)
// 9002 (Topics) 		-- listens on 9999
// 9003 (Collaboration)	-- listens on 9998
// 9004 (AgilePM)		-- listens on 9997

initialize ~= { _ =>
  System.setProperty( "http.host", "localhost" )
  System.setProperty( "http.port", "9003" )
  System.setProperty( "broadcast.port", "9998" )
}

lazy val root = (project in file(".")).enablePlugins(PlayScala)

scalaVersion := "2.11.7"

libraryDependencies ++= Seq(
  jdbc,
  cache,
  ws,
  "org.scalatestplus.play" %% "scalatestplus-play" % "1.5.0-RC1" % Test,
  "org.scalatest" %% "scalatest" % "2.1.6" % "test",
  // Scala Async
  "org.scala-lang.modules" % "scala-async_2.11" % "0.9.6-RC2",
  // Akka
  "com.typesafe.akka" %% "akka-actor" % "2.4.3",
  "com.typesafe.akka" %% "akka-testkit" % "2.4.2",
  "com.typesafe.akka" %% "akka-persistence" % "2.4.3",
  "com.typesafe.akka" %% "akka-persistence-query-experimental" % "2.4.3",
  "com.github.dnvriend" %% "akka-persistence-inmemory" % "1.2.12",
  // Gson
  "com.google.code.gson" % "gson" % "2.6.2",
  // GoReactiveWithAkka Common
  "goreactivewithakka" %% "go-reactive-common" % "1.0-SNAPSHOT"
)

resolvers += "scalaz-bintray" at "http://dl.bintray.com/scalaz/releases"
