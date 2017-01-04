import sbt.addCompilerPlugin


val libVersion = "0.1.4-SNAPSHOT"
val orientVersion = "2.2.14"
val catsVersion = "0.7.2"
val enumeratumVersion = "1.5.4"

val scalacOpts = Seq(
    "-unchecked"
  , "-deprecation"
  , "-encoding"
  , "utf8"
  , "-feature"
  , "-language:postfixOps"
  , "-language:higherKinds")


val commonSettings = Seq(
  organization := "com.itsmeijers",
  scalaVersion := "2.11.8",
  version := libVersion,
  scalacOptions ++= scalacOpts,
  javaOptions in Test ++= Seq("-Xmx512m","-XX:MaxDirectMemorySize=512m"),
  resolvers ++= Seq(Resolver.mavenLocal, Resolver.sonatypeRepo("releases"), Resolver.sonatypeRepo("snapshots")),
  addCompilerPlugin("org.spire-math" %% "kind-projector" % "0.7.1"),
  addCompilerPlugin("com.milessabin" % "si2712fix-plugin" % "1.2.0" cross CrossVersion.full)
)

lazy val doPublish = Seq(
  credentials += Credentials(Path.userHome / ".ivy2" / ".credentials"),
  publishTo := {
    if (isSnapshot.value) {
      Some("snapshots" at "http://artifactory.lunatech.com/artifactory/snapshots-public")
    } else {
      Some("releases" at "https://oss.sonatype.org/service/local/staging/deploy/maven2")
    }
  }
)

lazy val doNotPublish = Seq(
  publish := (),
  publishLocal := (),
  publishArtifact := false
)


val core = project.in(file("core"))
    .settings(commonSettings)
    .settings(doPublish)
    .settings(
      name := "scala-oriented-core",
      libraryDependencies ++= Seq(
        "com.orientechnologies"        % "orientdb-graphdb"  % orientVersion,
        "org.typelevel"                %% "cats"             % catsVersion,
        "com.chuusai"                  %% "shapeless"        % "2.3.2"
      )
    )

val enumeratum = project.in(file("enumeratum"))
  .dependsOn(core)
  .settings(commonSettings)
  .settings(doPublish)
  .settings(
    name := "scala-oriented-enumeratum",
    libraryDependencies ++= Seq(
      "com.beachape" %% "enumeratum" % enumeratumVersion
    )
  )


//test domain is there, so shapeless derivation works
val testDomain = project.in(file("test-domain"))
  .dependsOn(enumeratum)
  .settings(commonSettings)
  .settings(doNotPublish)

//separate test module, where you can test all the submodules and only have to make a dependency mess once :-)
val test = project.in(file("test"))
    .dependsOn(testDomain)
    .settings(commonSettings)
    .settings(doNotPublish)
    .settings(
      fork := true,
      testForkedParallel := false,
      parallelExecution := false,
      libraryDependencies ++= Seq(
        "com.orientechnologies"        % "orientdb-server"   % orientVersion,
        "org.scalatest"                %% "scalatest"        % "3.0.1",
        "org.scalacheck"               %% "scalacheck"       % "1.13.4",
        "com.github.alexarchambault"   %% "scalacheck-shapeless_1.13" % "1.1.4"
      )
    )


lazy val root = project.in(file("."))
  .settings(name := "root")
  .settings(commonSettings)
  .settings(doNotPublish)
  .aggregate(core, enumeratum)
