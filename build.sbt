lazy val configVersion = "1.3.2"
lazy val catsVersion = "1.6.0"
lazy val playVersion = "2.7.2"

lazy val root = (project in file(".")).
  settings(
    name              := "doclib-common",
    organization := "io.mdcatapult.doclib",
    version           := "0.0.1",
    scalaVersion      := "2.12.8",
    scalacOptions     ++= Seq("-Ypartial-unification"),
    resolvers         ++= Seq("MDC Nexus" at "http://nexus.mdcatapult.io/repository/maven-releases/"),
    credentials       += {
      val nexusPassword = sys.env.get("NEXUS_PASSWORD")
      if ( nexusPassword.nonEmpty ) {
        Credentials("Sonatype Nexus Repository Manager", "nexus.mdcatapult.io", "gitlab", nexusPassword.get)
      } else {
        Credentials(Path.userHome / ".sbt" / ".credentials")
      }
    },
    libraryDependencies ++= Seq(
      "org.scalatest" % "scalatest_2.12"              % "3.0.5" % "test",
      "com.typesafe.play" %% "play-json"              % playVersion,
      "com.typesafe" % "config"                       % configVersion,
      "org.typelevel" %% "cats-macros"                % catsVersion,
      "org.typelevel" %% "cats-kernel"                % catsVersion,
      "org.typelevel" %% "cats-core"                  % catsVersion,
      "io.lemonlabs" %% "scala-uri"                   % "1.4.5",
      "io.mdcatapult.klein" %% "queue"                % "0.0.3",
      "io.mdcatapult.klein" %% "mongo"                % "0.0.3",
    )
  )
