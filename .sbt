#!/bin/sh
java -XX:+CMSClassUnloadingEnabled -XX:MaxPermSize=256m -Xmx512M -Xss2M -jar util/sbt-launch.jar "$@"
