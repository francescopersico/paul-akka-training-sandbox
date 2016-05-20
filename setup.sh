#!/bin/bash -x

cd tmp

# install sbt from http://www.scala-sbt.org/0.13/docs/Installing-sbt-on-Linux.html
wget https://dl.bintray.com/sbt/debian/sbt-0.13.11.deb
sudo dpkg -i sbt-0.13.11.deb
sudo apt-get install -f 

# install activator from https://www.lightbend.com/activator/download
wget https://downloads.typesafe.com/typesafe-activator/1.3.10/typesafe-activator-1.3.10-minimal.zip
unzip typesafe-activator-1.3.10-minimal.zip

echo "export PATH=$PATH:$(pwd)/activator-1.3.10-minimal/bin" >> ~/.profile
source ~/.profile