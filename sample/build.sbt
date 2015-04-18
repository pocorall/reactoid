import android.Keys._

javacOptions ++= Seq("-source", "1.7", "-target", "1.7")

android.Plugin.androidBuild

name := "hello-reactoid-sbt"

scalaVersion := "2.11.6"

proguardCache in Android ++= Seq(
  ProguardCache("org.scaloid") % "org.scaloid"
)

proguardOptions in Android ++= Seq("-dontobfuscate", "-dontoptimize", "-keepattributes Signature", "-printseeds target/seeds.txt", "-printusage target/usage.txt"
  , "-dontwarn scala.collection.**" // required from Scala 2.11.4
  , "-dontwarn rx.ops.**"
)

libraryDependencies += "org.scaloid" %% "scaloid" % "4.0-RC1"

libraryDependencies += "com.lihaoyi" %% "scalarx" % "0.2.8"

libraryDependencies += "org.scalaz" %% "scalaz-core" % "7.1.1"

scalacOptions in Compile += "-feature"

run <<= run in Android

install <<= install in Android

retrolambdaEnable in Android := false