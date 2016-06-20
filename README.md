# RSSalg-software

**A tool for co-training application on single-view datasets.**
RSSalg software is intended for easy and flexible experimenting with both RSSalg and co-training.

Co-training is a major semi-supervised learning technique. It is applicable on the datasets whose features can be divided in two separate feature subsets called views. Ideally, this feature division is dictated by two independent sources of information describing the data. 

There are many real-world problems where the labeled data is scarce and thus would benefit from co-training application, but we lack the knowledge of the a natural way to separate the features in two views.

Random Split Statistic algorithm (RSSalg) is a co-training based algorithm that can be applied on single-view datasets. For a detailed explanation of RSSalg, please refer to:

[J. Slivka, A. Kovačević and Z. Konjović: "Combining CoTraining
with Ensemble Learning for Application on Single-View Natural Language Datasets", Acta Polytechnica Hungarica, 10.2, 133-152, 2013](http://uni-obuda.hu/journal/Slivka_Kovacevic_Konjovic_40.pdf)

## Getting Started

These instructions will get you a copy of the project up and running on your local machine for development and testing purposes. 

The illustrative example and detailed instructions of running RSSalg software is located in /docs/IllustrativeExample.pdf

### Prerequisities

In order to run RSSalg-software you need to have the following things installed

```
Java v1.7 or above
```
To install Java go to http://www.oracle.com/technetwork/indexes/downloads/index.html#java

### Running

In order to run RSS-alg software you can run the executable distribution, located in the "dist" folder, by typing the following command in the terminal:
 
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

Optionally, you load the source code from the "src" folder into your favorite IDE. Note that RSSalg software implementation depends on Weka (version 3.4 or above). Thus, you will need to add weka.jar library into your project (weka.jar can be dowloaded from http://www.cs.waikato.ac.nz/ml/weka/downloading.html). The starting point for running RSSalg software is the class `StartExperiment`, located inside `src/application`.

## Author

* **Jelena Slivka** - [GitHub](https://github.com/slivkaje)

## License

This project is licensed under the GNU General Public License.
