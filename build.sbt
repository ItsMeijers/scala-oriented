name := "scala-oriented"

organization := "com.itsmeijers"

version := "0.1-SNAPSHOT"

scalaVersion := "2.11.8"

crossScalaVersions := Seq("2.10.6", "2.11.8")

scalacOptions := Seq(
    "-unchecked"
  , "-deprecation"
  , "-encoding"
  , "utf8"
  , "-feature"
  , "-language:postfixOps"
  , "-language:higherKinds")


resolvers ++= Seq(
    Resolver.mavenLocal
  , Resolver.sonatypeRepo("releases")
  , Resolver.sonatypeRepo("snapshots")
)

libraryDependencies ++= Seq(
  // OrientDB Java API
    "com.orientechnologies"        % "orientdb-graphdb"  % "2.2.7"
  , "com.orientechnologies"        % "orientdb-server"   % "2.2.7" // For embedding OrientDB
  // Scala Libraries
  , "org.scalatest"                %% "scalatest"        % "3.0.0"  % "test"
  , "org.scalacheck"               %% "scalacheck"       % "1.13.2" % "test"
  , "org.typelevel"                %% "cats"             % "0.7.2"
  , "org.spire-math"               %% "kind-projector"   % "0.7.1"
  , "com.milessabin"               %% "si2712fix-plugin" % "1.2.0" cross CrossVersion.full
  , "com.chuusai"                  %% "shapeless"        % "2.3.2"
)

addCompilerPlugin("org.spire-math" %% "kind-projector" % "0.7.1")
addCompilerPlugin("com.milessabin" % "si2712fix-plugin" % "1.2.0" cross CrossVersion.full)

fork := true
