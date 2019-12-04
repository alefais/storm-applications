# Compile and run Spike Detection application

## Compile
From inside the root directory `SpikeDetection/`

`mvn clean install`

## Run
SpikeDetection application can be run passing some arguments (if no command line argument is provided then default values defined in [sd.properties](https://github.com/alefais/storm-applications/blob/master/SpikeDetection/src/main/resources/spikedetection/sd.properties) file and [Constants](https://github.com/alefais/storm-applications/tree/master/SpikeDetection/src/main/java/Constants) package are used). <br> The following arguments can be specified:<ul><li>path of the dataset input file containing sensors' measurements (absolute or relative to `SpikeDetection/` directory)</li><li>source parallelism degree</li><li>moving average bolt parallelism degree</li><li>spike detector bolt parallelism degree</li><li>sink parallelism degree</li><li>source generation rate (default -1, generate at the max possible rate)</li><li>topology name (default SpikeDetection)</li><li>execution mode (default local)</li></ul>

### Execution examples:
* No argument is passed (all the nodes have parallelism degree equal to 1, the source generation rate is the maximum possible, the execution is local, the input data set is the one inside the `SpikeDetection/data/` directory): <br> `$STORM_HOME/bin/storm jar target/SpikeDetection-1.0-SNAPSHOT-jar-with-dependencies.jar SpikeDetection.SpikeDetection`

* The parallelism degree is explicitly set for all the nodes in the application graph (source: 1, moving average: 2, spike detector: 2, sink: 1) and the data set file to be used as input is explicitly specified (<b>NB:</b> for each unspecified parameter the default value is used, as the generation rate that will be not limited with the following configuration): <br> `$STORM_HOME/bin/storm jar target/SpikeDetection-1.0-SNAPSHOT-jar-with-dependencies.jar SpikeDetection.SpikeDetection data/sensors.dat 1 2 2 1`

<b>NB:</b> Running the application as `$STORM_HOME/bin/storm jar target/SpikeDetection-1.0-SNAPSHOT-jar-with-dependencies.jar SpikeDetection.SpikeDetection help` visualizes all the parameter options.

<b>NB:</b> The path of the dataset input file, the model parameters and the parallelism degree of the nodes are defined and can be modified in the [sd.properties](https://github.com/alefais/storm-applications/blob/master/SpikeDetection/src/main/resources/spikedetection/sd.properties) file.