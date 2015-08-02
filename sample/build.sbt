import android.Keys._

javacOptions ++= Seq("-source", "1.7", "-target", "1.7", "-encoding", "utf-8")

android.Plugin.androidBuild

name := "hello-reactoid-sbt"

scalaVersion := "2.11.7"

proguardCache in Android ++= Seq(
  "org.scaloid"
)

proguardOptions in Android ++= Seq("-dontobfuscate", "-dontoptimize", "-keepattributes Signature", "-printseeds target/seeds.txt", "-printusage target/usage.txt"
  , "-dontwarn scala.collection.**" // required from Scala 2.11.4
  , "-dontwarn rx.ops.**"
  , "-dontwarn org.scaloid.**"
)

libraryDependencies += "org.scaloid" %% "scaloid" % "4.0-RC1"

libraryDependencies += "com.lihaoyi" %% "scalarx" % "0.2.8"

libraryDependencies += "org.scalaz" %% "scalaz-core" % "7.1.1"

scalacOptions in Compile += "-feature"

run <<= run in Android

install <<= install in Android

retrolambdaEnable in Android := false