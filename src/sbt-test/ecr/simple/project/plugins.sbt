addSbtPlugin("com.typesafe.sbt" % "sbt-native-packager" % "1.3.6")

sys.props.get("plugin.version") match {
  case Some(x) => addSbtPlugin("com.mintbeans" % "sbt-ecr" % x)
  case _ => sys.error("""|The system property 'plugin.version' is not defined.
                         |Specify this property using the scriptedLaunchOpts -D.""".stripMargin)
}
