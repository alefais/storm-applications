# Compile and run Fraud Detection application

## Compile
From inside the root directory `FraudDetection/`

`mvn clean install`

## Run
FraudDetection application can be run passing some arguments (if no command line argument is provided then default values defined in [fd.properties](https://github.com/alefais/storm-applications/blob/master/FraudDetection/src/main/resources/frauddetection/fd.properties) file and [Constants](https://github.com/alefais/storm-applications/tree/master/FraudDetection/src/main/java/Constants) package are used). <br> The following arguments can be specified:<ul><li>path of the dataset input file containing credit card transactions (absolute or relative to `FraudDetection/` directory)</li><li>source parallelism degree</li><li>predictor bolt parallelism degree</li><li>sink parallelism degree</li><li>source generation rate (default -1, generate at the max possible rate)</li><li>topology name (default FraudDetection)</li><li>execution mode (default local)</li></ul>

### Execution examples:
* No argument is passed (all the nodes have parallelism degree equal to 1 - default value -, the source generation rate is the maximum possible, the execution is local, the input data set is the one inside the `FraudDetection/data/` directory): <br> `$STORM_HOME/bin/storm jar target/FraudDetection-1.0-SNAPSHOT-jar-with-dependencies.jar FraudDetection.FraudDetection`

* The parallelism degree is explicitly set for all the nodes in the application graph (source: 1, predictor: 4, sink: 1) and a specific file path is passed: <br> (<b>NB:</b> for each unspecified parameter the default value is used, as in the previous case): <br> `$STORM_HOME/bin/storm jar target/FraudDetection-1.0-SNAPSHOT-jar-with-dependencies.jar FraudDetection.FraudDetection data/credit-card.dat 1 4 1`

<b>NB:</b> Running the application as `$STORM_HOME/bin/storm jar target/FraudDetection-1.0-SNAPSHOT-jar-with-dependencies.jar FraudDetection.FraudDetection help` visualizes all the parameter options.

<b>NB:</b> The path of the dataset input file, the prediction model parameters and the parallelism degree of the nodes are defined and can be modified in the [fd.properties](https://github.com/alefais/storm-applications/blob/master/FraudDetection/src/main/resources/frauddetection/fd.properties) file.