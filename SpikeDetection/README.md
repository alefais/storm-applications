# Compile and run SpikeDetection

## Compile
From inside the root directory `SpikeDetection/`

`mvn clean install`

## Run
SpikeDetection application can be run passing some arguments (if no command line argument is provided then default values defined in [sd.properties](https://github.com/alefais/storm-applications/blob/master/SpikeDetection/src/main/resources/spikedetection/sd.properties) and [Constants](https://github.com/alefais/storm-applications/tree/master/SpikeDetection/src/main/java/Constants) package are used). <br> The following arguments can be specified:<ul><li>path of the dataset input file containing sensors' measurements (absolute or relative to `SpikeDetection/` directory)</li><li>source parallelism degree</li><li>moving average bolt parallelism degree</li><li>spike detector bolt parallelism degree</li><li>sink parallelism degree</li><li>source generation rate (default -1, generate at the max possible rate)</li><li>topology name (default SpikeDetection)</li><li>execution mode (default local)</li></ul>

### Execution examples:
* Run the application with no arguments (parameters such as the dataset and the parallelism degree of the nodes assume the default values, the source generation rate is the maximum possible, the execution is local): <br> `storm jar target/SpikeDetection-1.0-SNAPSHOT-jar-with-dependencies.jar SpikeDetection.SpikeDetection`

* Run the application explicitly defining the dataset file path and the parallelism degree for all the nodes in the topology (source: 1, bolt1: 4, bolt2: 1, sink: 1) <br> (<b>NB:</b> for each unspecified parameter the default value is used, as in the previous case): <br> `storm jar target/SpikeDetection-1.0-SNAPSHOT-jar-with-dependencies.jar SpikeDetection.SpikeDetection data/sensors.dat 1 4 1 1`

<b>NB:</b> Running the application as `storm jar target/SpikeDetection-1.0-SNAPSHOT-jar-with-dependencies.jar SpikeDetection.SpikeDetection help` visualizes all the parameter options.

<b>NB:</b> The path of the dataset input file, the prediction model parameters and the parallelism degree of the nodes are defined and can be modified in [sd.properties](https://github.com/alefais/storm-applications/blob/master/SpikeDetection/src/main/resources/spikedetection/sd.properties).