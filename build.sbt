ThisBuild / version := "0.1.0-SNAPSHOT"

ThisBuild / scalaVersion := "2.13.8"

lazy val root = (project in file("."))
  .settings(
    name := "tg-standup-bot"
  )

libraryDependencies ++= {
  val canoe = "0.5.1"
  val zio = "1.0.14"
  val pureconfig = "0.17.1"
  val zioInterop = "2.5.1.0"
  val scraper = "2.2.1"

  Seq(
    "org.augustjune" %% "canoe" % canoe,
    "dev.zio" %% "zio" % zio,
    "dev.zio" %% "zio-streams" % zio,
//    "dev.zio"  %% "zio-macros" % zio,
    "com.github.pureconfig" %% "pureconfig" % pureconfig,
    ("dev.zio" %% "zio-interop-cats" % zioInterop).excludeAll(ExclusionRule("dev.zio")),
    "net.ruippeixotog" %% "scala-scraper" % scraper
  )
}


