lazy val Scala212 = "2.12.8"
lazy val Scala211 = "2.11.12"
lazy val Scala210 = "2.10.7"

lazy val configVersion = "1.3.2"
lazy val catsVersion = "2.0.0"
lazy val playVersion = "2.7.2"
lazy val tikaVersion = "1.21"

lazy val root = (project in file(".")).
  settings(
    name              := "common",
    organization := "io.mdcatapult.doclib",
    version           := "0.0.17-SNAPSHOT",
    scalaVersion      := "2.12.8",
    crossScalaVersions  := Scala212 :: Scala211 :: Scala210 :: Nil,
    scalacOptions     ++= Seq("-Ypartial-unification"),
    resolvers         ++= Seq("MDC Nexus Releases" at "http://nexus.mdcatapult.io/repository/maven-releases/", "MDC Nexus Snapshots" at "http://nexus.mdcatapult.io/repository/maven-snapshots/"),
    credentials       += {
      val nexusPassword = sys.env.get("NEXUS_PASSWORD")
      if ( nexusPassword.nonEmpty ) {
        Credentials("Sonatype Nexus Repository Manager", "nexus.mdcatapult.io", "gitlab", nexusPassword.get)
      } else {
        Credentials(Path.userHome / ".sbt" / ".credentials")
      }
    },
    libraryDependencies ++= Seq(
      "org.scalactic" %% "scalactic"                  % "3.0.5",
      "org.scalatest" %% "scalatest"                  % "3.0.5" % "test",
      "org.scalamock" %% "scalamock"                  % "4.3.0" % Test,
      "com.typesafe.play" %% "play-json"              % playVersion,
      "com.typesafe" % "config"                       % configVersion,
      "org.typelevel" %% "cats-macros"                % catsVersion,
      "org.typelevel" %% "cats-kernel"                % catsVersion,
      "org.typelevel" %% "cats-core"                  % catsVersion,
      "io.lemonlabs" %% "scala-uri"                   % "1.4.5",
      "io.mdcatapult.klein" %% "queue"                % "0.0.9",
      "io.mdcatapult.klein" %% "mongo"                % "0.0.3",
      "commons-io" % "commons-io"                     % "2.6",
      "org.apache.commons" % "commons-compress"       % "1.18",
      "org.apache.tika" % "tika-core"                 % tikaVersion,
      "org.apache.tika" % "tika-parsers"              % tikaVersion,
      "org.apache.tika" % "tika-langdetect"           % tikaVersion,
      "org.apache.pdfbox" % "jbig2-imageio"           % "3.0.2",
      "com.github.jai-imageio" % "jai-imageio-jpeg2000" % "1.3.0",
      "org.xerial" % "sqlite-jdbc"                      % "3.25.2"
    )
  ).
  settings(
    publishSettings: _*
  )

lazy val publishSettings = Seq(
  publishTo := {
    if (isSnapshot.value)
      Some("MDC Maven Repo" at "https://nexus.mdcatapult.io/repository/maven-snapshots/")
    else
      Some("MDC Maven Repo" at "https://nexus.mdcatapult.io/repository/maven-releases/")
  },
  credentials += Credentials(Path.userHome / ".sbt" / ".credentials")
)
