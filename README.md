# _RSSalg-software_

**A tool for co-training application on single-view datasets.**
_RSSalg software_ is intended for easy and flexible experimenting with both RSSalg and co-training.

Co-training is a major semi-supervised learning technique. It is applicable on the datasets whose features can be divided in two separate feature subsets called views. Ideally, this feature division is dictated by two independent sources of information describing the data. 

There are many real-world problems where the labeled data is scarce and thus would benefit from co-training application, but we lack the knowledge of the a natural way to separate the features in two views.

Random Split Statistic algorithm (RSSalg) is a co-training based algorithm that can be applied on single-view datasets. For a detailed explanation of RSSalg, please refer to:

[J. Slivka, A. Kovačević and Z. Konjović: "Combining CoTraining
with Ensemble Learning for Application on Single-View Natural Language Datasets", Acta Polytechnica Hungarica, 10.2, 133-152, 2013](http://uni-obuda.hu/journal/Slivka_Kovacevic_Konjovic_40.pdf)

## Getting Started

These instructions will get you a copy of the project up and running on your local machine for development and testing purposes. 

The illustrative example and detailed instructions of running _RSSalg software_ is located in /docs/IllustrativeExample.pdf

### Prerequisities

In order to run _RSSalg software_ you need to have the following things installed

```
Java v1.7 or above
```
To install Java go to http://www.oracle.com/technetwork/indexes/downloads/index.html#java

### Building the project

The easiest way to build and run the project is by using Apache Ant (http://ant.apache.org/). You will find the Apache Ant script named "build.xml" in the root of the project.
After installing Ant, open the terminal and navigate to the root of the project. Type the command ```ant```. This will compile and package the code in an executable located in /dist/jar/RSSalg.jar

Optionally, you load the source code from the ```src``` folder into your favorite IDE. Note that _RSSalg software_ implementation depends on Weka (version 3.7.0). Thus, you will need to add ```/lib/weka.jar``` library into your project. The starting point for running _RSSalg software_ is the class `StartExperiment`, located inside `src/application`.

### **Running the project by using the built executable**

Open the terminal and navigate to the executable RSSalg.jar.
 
1. Running RSSalg software in console:
	```
	java -jar RSSalg.jar <properties_folder> <experiment_properties>
	```
	where 
	* `properties_folder` is the folder containing the following property files (needed for execution of all experiments): 
		* data.properties - parameters describing the dataset used in the experiment
		* cv.properties - parameters for setting the X-fold-cross validation experiment
		* co-training.properties - parameters of the underlying co-training algorithm
		* GA.properties - properties of the genetic algorithm used in RSSalg threshold optimization
	* `experiment_properties` is the property file containing the containing the desired settings for concrete experiment that should be run

2. Running RSSalg software as swing application
	```
	java -jar RSSalg.jar
	```

### **Running the project by using Ant**

Open the terminal and navigate to the root of the project (containing build.xml): 
	
* You can run _RSSalg software_ in GUI mode by typing the command ```ant run_GUI```

* You can run _RSSalg software_ in console mode by typing the command ```ant run_console```. In this case you need to modify the arguments of target run_console located in build.xml to correspond to the adequate `properties_folder` and `experiment_properties`.
	
## Author

* **Jelena Slivka** - [GitHub](https://github.com/slivkaje)

## License

This project is licensed under the GNU General Public License.
