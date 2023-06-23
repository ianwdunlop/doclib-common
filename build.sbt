lazy val scala_2_13 = "2.13.3"

lazy val IntegrationTest = config("it") extend Test

lazy val root = (project in file(".")).
  configs(IntegrationTest)
  .settings(
    Defaults.itSettings,
    name := "common",
    organization := "io.mdcatapult.doclib",
    scalaVersion := scala_2_13,
    useCoursier := false,
    crossScalaVersions := scala_2_13 :: Nil,
    scalacOptions ++= Seq(
      "-encoding", "utf-8",
      "-unchecked",
      "-deprecation",
      "-explaintypes",
      "-feature",
      "-Xlint",
      "-Xfatal-warnings",
    ),
    resolvers         ++= Seq(
      "MDC Nexus Releases" at "https://nexus.wopr.inf.mdc/repository/maven-releases/",
      "MDC Nexus Snapshots" at "https://nexus.wopr.inf.mdc/repository/maven-snapshots/"),
    credentials       += {
      sys.env.get("NEXUS_PASSWORD") match {
        case Some(p) =>
          Credentials("Sonatype Nexus Repository Manager", "nexus.wopr.inf.mdc", "gitlab", p)
        case None =>
          Credentials(Path.userHome / ".sbt" / ".credentials")
      }
    },
    libraryDependencies ++= {
      val kleinUtilVersion = "1.2.4"
      val kleinMongoVersion = "2.0.7"
      val kleinQueueVersion = "2.0.0"

      val configVersion = "1.4.1"
      val catsVersion = "2.6.1"
      val playVersion = "2.9.2"
      val tikaVersion = "1.28"
      val betterFilesVersion = "3.9.1"
      val akkaVersion = "2.8.1"
      val prometheusClientVersion = "0.9.0"
      val scalacticVersion = "3.2.10"
      val scalaTestVersion = "3.2.11"
      val scalaMockVersion = "5.2.0"
      val scalaCheckVersion = "1.15.4"
      val scoptVersion = "4.0.1"
      val lemonLabsURIVersion = "3.6.0"

      Seq(
        "io.mdcatapult.klein" %% "queue"                % kleinQueueVersion,
        "io.mdcatapult.klein" %% "mongo"                % kleinMongoVersion,
        "io.mdcatapult.klein" %% "util"                 % kleinUtilVersion,

        "org.scalactic" %% "scalactic"                  % scalacticVersion,
        "org.scalatest" %% "scalatest"                  % scalaTestVersion % "it, test",
        "org.scalamock" %% "scalamock"                  % scalaMockVersion % "it, test",
        "org.scalacheck" %% "scalacheck"                % scalaCheckVersion % Test,
        "com.typesafe.akka" %% "akka-slf4j"             % akkaVersion,
        "com.typesafe.akka" %% "akka-testkit"           % akkaVersion % "it, test",
        "com.typesafe.akka" %% "akka-protobuf-v3"       % akkaVersion,
        "com.typesafe.akka" %% "akka-stream"            % akkaVersion,
        "com.typesafe.play" %% "play-json"              % playVersion,
        "com.typesafe" % "config"                       % configVersion,
        "org.typelevel" %% "cats-kernel"                % catsVersion,
        "org.typelevel" %% "cats-core"                  % catsVersion,
        "io.lemonlabs" %% "scala-uri"                   % lemonLabsURIVersion,
        "com.github.scopt" %% "scopt"                   % scoptVersion,
        "org.apache.tika" % "tika-core"                 % tikaVersion,
        "org.apache.tika" % "tika-parsers"              % tikaVersion,
        "org.apache.tika" % "tika-langdetect"           % tikaVersion,
        "com.github.pathikrit"  %% "better-files"       % betterFilesVersion,
        "io.prometheus" % "simpleclient"                % prometheusClientVersion,
        "io.prometheus" % "simpleclient_hotspot"        % prometheusClientVersion,
        "io.prometheus" % "simpleclient_httpserver"     % prometheusClientVersion
      )
    }
  )
  .settings(
    publishSettings: _*
  )

lazy val publishSettings = Seq(
  publishTo := {
    val version = if (isSnapshot.value) "snapshots" else "releases"
    Some("MDC Maven Repo" at s"https://nexus.wopr.inf.mdc/repository/maven-$version/")
  },
  credentials += Credentials(Path.userHome / ".sbt" / ".credentials")
)
