lazy val scala_2_13 = "2.13.1"
lazy val scala_2_12 = "2.12.10"

lazy val configVersion = "1.3.2"
lazy val catsVersion = "2.1.0"
lazy val playVersion = "2.8.1"
lazy val tikaVersion = "1.24"
lazy val betterFilesVersion = "3.8.0"
lazy val akkaVersion = "2.6.4"
lazy val monixVersion = "3.1.0"

lazy val IntegrationTest = config("it") extend Test

lazy val root = (project in file(".")).
  configs(IntegrationTest)
  .settings(
    Defaults.itSettings,
    name              := "common",
    organization := "io.mdcatapult.doclib",
    scalaVersion      := scala_2_13,
    coverageEnabled   := false,
    useCoursier   := false,
    crossScalaVersions  := scala_2_13 :: scala_2_12 :: Nil,
    scalacOptions ++= Seq(
      "-encoding", "utf-8",
      "-unchecked",
      "-deprecation",
      "-explaintypes",
      "-feature",
      "-Xlint",
      // "-Xfatal-warnings",  2.13 compatibility library necessarily brings in unused dependency in Scala 2.13
    ),
    resolvers         ++= Seq(
      "MDC Nexus Releases" at "https://nexus.mdcatapult.io/repository/maven-releases/",
      "MDC Nexus Snapshots" at "https://nexus.mdcatapult.io/repository/maven-snapshots/"),
    credentials       += {
      sys.env.get("NEXUS_PASSWORD") match {
        case Some(p) =>
          Credentials("Sonatype Nexus Repository Manager", "nexus.mdcatapult.io", "gitlab", p)
        case None =>
          Credentials(Path.userHome / ".sbt" / ".credentials")
      }
    },
    libraryDependencies ++= Seq(
      "org.scalactic" %% "scalactic"                  % "3.1.1",
      "org.scalatest" %% "scalatest"                  % "3.1.1" % "it, test",
      "org.scalamock" %% "scalamock"                  % "4.4.0" % "it, test",
      "org.scalacheck" %% "scalacheck"                % "1.14.3" % Test,
      "com.typesafe.akka" %% "akka-testkit"           % akkaVersion % "it, test",
      "com.typesafe.akka" %% "akka-protobuf"          % akkaVersion,
      "com.typesafe.akka" %% "akka-stream"            % akkaVersion,
      "com.typesafe.play" %% "play-json"              % playVersion,
      "com.typesafe" % "config"                       % configVersion,
      "org.typelevel" %% "cats-macros"                % catsVersion,
      "org.typelevel" %% "cats-kernel"                % catsVersion,
      "org.typelevel" %% "cats-core"                  % catsVersion,
      "io.lemonlabs" %% "scala-uri"                   % "2.2.0",
      "io.mdcatapult.klein" %% "queue"                % "0.0.20",
      "io.mdcatapult.klein" %% "mongo"                % "0.0.14",
      "com.github.scopt" %% "scopt"                   % "4.0.0-RC2",
      "org.apache.tika" % "tika-core"                 % tikaVersion,
      "org.apache.tika" % "tika-parsers"              % tikaVersion,
      "org.apache.tika" % "tika-langdetect"           % tikaVersion,
      "com.github.pathikrit"  %% "better-files"       % betterFilesVersion,
      "io.monix" %% "monix"                           % monixVersion,
    )
  )
  .settings(
    publishSettings: _*
  )

lazy val publishSettings = Seq(
  publishTo := {
    val version = if (isSnapshot.value) "snapshots" else "releases"
    Some("MDC Maven Repo" at s"https://nexus.mdcatapult.io/repository/maven-$version/")
  },
  credentials += Credentials(Path.userHome / ".sbt" / ".credentials")
)
