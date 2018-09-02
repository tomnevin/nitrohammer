#! /bin/sh
export DATABASE_CONNECTION_FILENAME=/opt/tomcat/databases.xml
export GEOSERVER_DATA_DIR=/opt/geodata
export DATABASE_LOCATOR=test

export CATALINA_OPTS="$CATALINA_OPTS -Dcom.sun.xml.bind.v2.bytecode.ClassTailor.noOptimize=true"
