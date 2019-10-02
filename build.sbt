lazy val logbackVersion = "1.2.3"
lazy val akkaHttpVersion = "10.1.9"
lazy val akkaVersion    = "2.5.25"
lazy val flywayVersion = "6.0.3"
lazy val slickRepoVersion = "1.5.3"
lazy val postgresqlVersion = "42.2.8"
lazy val slickVersion = "3.3.2"
lazy val scalatestVersion = "3.0.5"
lazy val testcontainersScalaVersion = "0.33.0"
lazy val testcontainersVersion = "1.12.2"

lazy val root = (project in file(".")).
  settings(
    inThisBuild(List(
      organization    := "com.syamantm",
      scalaVersion    := "2.12.8"
    )),
    name := "todo-akka",
    libraryDependencies ++= Seq(
      "com.typesafe.akka"  %% "akka-http"            % akkaHttpVersion,
      "com.typesafe.akka"  %% "akka-http-spray-json" % akkaHttpVersion,
      "com.typesafe.akka"  %% "akka-http-xml"        % akkaHttpVersion,
      "com.typesafe.akka"  %% "akka-stream"          % akkaVersion,
      "com.typesafe.akka"  %% "akka-slf4j"           % akkaVersion,
      "com.typesafe.slick" %% "slick"                % slickVersion,
      "com.typesafe.slick" %% "slick-hikaricp"       % slickVersion,
      "com.byteslounge"    %% "slick-repo"           % slickRepoVersion,

      "ch.qos.logback" % "logback-classic" % logbackVersion,
      "org.flywaydb"   % "flyway-core"     % flywayVersion,
      "org.postgresql" % "postgresql"      % postgresqlVersion,

      "com.typesafe.akka" %% "akka-http-testkit"           % akkaHttpVersion            % Test,
      "com.typesafe.akka" %% "akka-testkit"                % akkaVersion                % Test,
      "com.typesafe.akka" %% "akka-stream-testkit"         % akkaVersion                % Test,
      "org.scalatest"     %% "scalatest"                   % scalatestVersion           % Test,
      "com.dimafeng"      %% "testcontainers-scala"        % testcontainersScalaVersion % Test,

      "org.testcontainers" % "postgresql" % testcontainersVersion % Test
    )
  )
