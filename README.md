# ClustnSee 3

This is the Maven project for building ClustnSee app for Cytoscape 3

## Requirements

First, you need to install the following tools to build and install ClustnSee app:

  * [Git](https://github.com/git-guides/install-git)
  * [JDK](https://www.oracle.com/java/technologies/downloads/)
  * [Maven](https://maven.apache.org/download.cgi)
    * binary zip archive for Windows
    * binary tar.gz archive for Linux and OS X
  * [Cytoscape](https://cytoscape.org/download.html)

## Building ClustnSee app :
```
git clone https://github.com/fafa13/ClustnSee-3.git
cd ClustnSee-3
mvn clean install -U
```
## Installing the app :
```
cp ./target/tagc-clustnsee-3.0.0.jar $HOME/CytoscapeConfiguration/3/apps/installed/
```
