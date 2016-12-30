import sbt.addCompilerPlugin

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
  version := "0.1.0",
  organization := "com.itsmeijers",
  scalaVersion := "2.11.8",
  version := "0.1.3-SNAPSHOT",
  crossScalaVersions := Seq("2.10.6", "2.11.8"),
  scalacOptions ++= scalacOpts,
  resolvers ++= Seq(Resolver.mavenLocal, Resolver.sonatypeRepo("releases"), Resolver.sonatypeRepo("snapshots")),
  addCompilerPlugin("org.spire-math" %% "kind-projector" % "0.7.1"),
  addCompilerPlugin("com.milessabin" % "si2712fix-plugin" % "1.2.0" cross CrossVersion.full)
)

lazy val doNotPublishArtifact = Seq(
  publishArtifact := false,
  publishArtifact in (Compile, packageDoc) := false,
  publishArtifact in (Compile, packageSrc) := false,
  publishArtifact in (Compile, packageBin) := false
)


val core = project.in(file("core"))
    .settings(commonSettings)
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
  .settings(doNotPublishArtifact)

//separate test module, where you can test all the submodules and only have to make a dependency mess once :-)
val test = project.in(file("test"))
    .dependsOn(testDomain)
    .settings(commonSettings)
    .settings(doNotPublishArtifact)
    .settings(
      fork := true,
      libraryDependencies ++= Seq(
        "com.orientechnologies"        % "orientdb-server"   % orientVersion,
        "org.scalatest"                %% "scalatest"        % "3.0.0",
        "org.scalacheck"               %% "scalacheck"       % "1.13.4",
        "com.github.alexarchambault"   %% "scalacheck-shapeless_1.13" % "1.1.4"
      )
    )

