# Compile and run FraudDetection

## Compile
From inside the root directory `FraudDetection/`

`mvn clean install`

## Run
FraudDetection application can be run passing some arguments (if no command line argument is provided then default values defined in `fd.properties` file and `Constants` package are used). <br> Optional arguments are:<ul><li>source parallelism degree</li><li>predictor bolt parallelism degree</li><li>sink parallelism degree</li><li>source generation rate (default -1, generate at the max possible rate)</li><li>topology name (default FraudDetection)</li><li>execution mode (default local)</li></ul>

### Execution examples:
* No argument is passed (all the nodes have parallelism degree equal to 1, the source generation rate is the maximum possible, the execution is local): <br> `storm jar target/FraudDetection-1.0-SNAPSHOT-jar-with-dependencies.jar FraudDetection.FraudDetection`

* The parallelism degree is esplicitly defined for all the nodes in the topology and a specific file path is passed (source: 1, bolt: 4, sink: 1) <br> (<b>NB:</b> for each unspecified parameter the default value is used, as in the previous case): <br> `storm jar target/FraudDetection-1.0-SNAPSHOT-jar-with-dependencies.jar FraudDetection.FraudDetection data/app/fd/credit-card.dat 1 4 1`
