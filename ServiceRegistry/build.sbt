name := """ServiceRegistry"""

organization := "goreactivewithakka"

version := "1.0-SNAPSHOT"

// must be one of:
// 9001 (Registry)
// 9002 (Topics)
// 9003 (Collaboration)
// 9004 (AgilePM)

initialize ~= { _ =>
  System.setProperty( "http.host", "localhost" )
  System.setProperty( "http.port", "9001" )
  System.setProperty( "broadcast.ports", "9990,9991,9992,9993,9994,9995,9996,9997,9998,9999" )
}

lazy val root = (project in file(".")).enablePlugins(PlayScala)

scalaVersion := "2.11.7"

libraryDependencies ++= Seq(
  jdbc,
  cache,
  ws,
  "org.scalatestplus.play" %% "scalatestplus-play" % "1.5.0-RC1" % Test
)

resolvers += "scalaz-bintray" at "http://dl.bintray.com/scalaz/releases"
