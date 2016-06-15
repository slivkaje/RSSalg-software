# RSSalg-software

A tool for application of co-training on single-view datasets

## Getting Started

These instructions will get you a copy of the project up and running on your local machine for development and testing purposes.

### Prerequisities

In order to run RSSalg-software you need to have the following things installed

```
Java v1.7 or above
```
To install Java go to http://www.oracle.com/technetwork/indexes/downloads/index.html#java

### Running

In order to run RSS-alg software you can either load the source code from the "src" folder into your favorite IDE, or run the executable distribution, located in the "dist" folder, by typing the following command in the terminal:
 
```
java -jar RSSalg.jar <properties_folder> <experiment_properties>
```
where 
1. <properties_folder> is the folder containing the following property files: 
	* data.properties - parameters describing the dataset used in the experiment
	* cv.properties - parameters for setting the X-fold-cross validation experiment
	* co-training.properties - parameters of the underlying co-training algorithm
	* GA.properties - properties of the genetic algorithm used in RSSalg threshold optimization
2. <experiment_properties> is the property file containing the desired experiment settings

## Author

* **Jelena Slivka** - [GitHub](https://github.com/slivkaje)

## License

This project is licensed under the GNU General Public License.