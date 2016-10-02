name := "scala-oriented"

version := "0.1"

scalaVersion := "2.11.8"

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
  , "com.projectseptember"         %% "freek"            % "0.6.0"
  , "org.spire-math"               %% "kind-projector"   % "0.7.1"
  , "com.milessabin"               %% "si2712fix-plugin" % "1.2.0" cross CrossVersion.full
)

addCompilerPlugin("org.spire-math" %% "kind-projector" % "0.7.1")
addCompilerPlugin("com.milessabin" % "si2712fix-plugin" % "1.2.0" cross CrossVersion.full)

fork := true

// Increase maximum JVM memory allocation
javaOptions in run ++= Seq(
  "-XX:MaxDirectMemorySize=16384m"
)