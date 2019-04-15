# Compile and run SpikeDetection

## Compile
From inside the root directory `SpikeDetection/`

`mvn clean install`

## Run
SpikeDetection application can be run passing some arguments (if no command line argument is provided then default values defined in `sd.properties` file and `Constants` package are used). <br> The following arguments can be specified:<ul><li>`--filepath` path of the dataset input file containing sensors' measurements (absolute or relative to `SpikeDetection/` directory)</li><li>source parallelism degree</li><li>moving average bolt parallelism degree</li><li>spike detector bolt parallelism degree</li><li>sink parallelism degree</li><li>source generation rate (default -1, generate at the max possible rate)</li><li>topology name (default SpikeDetection)</li><li>execution mode (default local)</li></ul>

### Execution examples:
* No argument is passed (the input file is the one specified as property, all the nodes have parallelism degree equal to 1, the source generation rate is the maximum possible, the execution is local): <br> `storm jar target/SpikeDetection-1.0-SNAPSHOT-jar-with-dependencies.jar SpikeDetection.SpikeDetection`

* The parallelism degree is explicitly defined for all the nodes in the topology and a specific file path is passed (source: 1, bolt1: 2, bolt2: 2, sink: 1) <br> (<b>NB:</b> for each unspecified parameter the default value is used, as in the previous case): <br> `storm jar target/SpikeDetection-1.0-SNAPSHOT-jar-with-dependencies.jar SpikeDetection.SpikeDetection ../data/app/sd/sensors.dat 1 2 2 1`

<b>NB:</b> Running the application as `storm jar target/SpikeDetection-1.0-SNAPSHOT-jar-with-dependencies.jar SpikeDetection.SpikeDetection help` visualizes all the parameter options.