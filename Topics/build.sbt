name := """Topics"""

organization := "goreactivewithakka"

version := "1.0-SNAPSHOT"

// must be one of:
// 9001 (Registry)
// 9002 (Topics) 		-- listens on 9999
// 9003 (Collaboration)	-- listens on 9998
// 9004 (AgilePM)		-- listens on 9997

initialize ~= { _ =>
  System.setProperty( "http.host", "localhost" )
  System.setProperty( "http.port", "9002" )
  System.setProperty( "broadcast.port", "9999" )
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
