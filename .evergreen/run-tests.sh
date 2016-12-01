#!/bin/bash

set -o xtrace   # Write all commands first to stderr
set -o errexit  # Exit the script with error if any of the commands fail

# Supported/used environment variables:
#       MONGODB_URI             Set the suggested connection MONGODB_URI (including credentials and topology info)
#       TOPOLOGY                Allows you to modify variables and the MONGODB_URI based on test topology
#                               Supported values: "server", "replica_set", "sharded_cluster"
#       SCALA_VERSION           Set the version of Scala to be used.


MONGODB_URI=${MONGODB_URI:-}
TOPOLOGY=${TOPOLOGY:-server}
JAVA_HOME="/opt/java/jdk8"

############################################
#            Main Program                  #
############################################

echo "Running tests for Scala $SCALA_VERSION for $TOPOLOGY and connecting to $MONGODB_URI"

./sbt -java-home $JAVA_HOME version
./sbt -java-home $JAVA_HOME ++${SCALA_VERSION} test -Dorg.mongodb.test.uri=${MONGODB_URI}
