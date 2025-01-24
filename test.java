#!/bin/bash
echo "Stopping Tomcat..."
<TOMCAT_HOME>/bin/shutdown.sh

echo "Killing remaining Java processes..."
ps -ef | grep java | grep -v grep | awk '{print $2}' | xargs kill -9

echo "Clearing temporary files..."
rm -rf <TOMCAT_HOME>/work/*
rm -rf <TOMCAT_HOME>/temp/*

echo "Starting Tomcat..."
<TOMCAT_HOME>/bin/startup.sh