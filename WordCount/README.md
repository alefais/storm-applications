# Compile and run WordCount

## Compile
From inside the root directory `WordCount/`

`mvn clean install`

## Run
WordCount application can be run passing some arguments (if no command line argument is provided then default values defined in [wc.properties](https://github.com/alefais/storm-applications/blob/master/WordCount/src/main/resources/wordcount/wc.properties) and [Constants](https://github.com/alefais/storm-applications/tree/master/WordCount/src/main/java/Constants) package are used). <br> The following arguments can be specified:<ul><li>source type (can be `file` or `generator`)</li><li>path of the dataset input file containing words (absolute or relative to `WordCount/` directory) <br> <b>NB:</b> this parameter is valid only if the source type is `file`</li><li>source parallelism degree</li><li>splitter bolt parallelism degree</li><li>counter bolt parallelism degree</li><li>sink parallelism degree</li><li>source generation rate (default -1, generate at the max possible rate)</li><li>topology name (default WordCount)</li><li>execution mode (default local)</li></ul>

### Execution examples:
* Run the application with no arguments (parameters such as the input dataset and the parallelism degree of the nodes assume the default values, the source generation rate is the maximum possible, the execution is local): <br> `storm jar target/WordCount-1.0-SNAPSHOT-jar-with-dependencies.jar WordCount.WordCount`

* Run the application explicitly defining the dataset file path and the parallelism degree for all the nodes in the topology (source: 1, splitter: 2, counter: 2, sink: 1) <br> (<b>NB:</b> for each unspecified parameter the default value is used, as in the previous case): <br> `storm jar target/WordCount-1.0-SNAPSHOT-jar-with-dependencies.jar WordCount.WordCount file data/book.dat 1 2 2 1`

<b>NB:</b> Running the application as `storm jar target/WordCount-1.0-SNAPSHOT-jar-with-dependencies.jar WordCount.WordCount help` visualizes all the parameter options.

<b>NB:</b> The path of the dataset input file, the prediction model parameters and the parallelism degree of the nodes are defined and can be modified in [wc.properties](https://github.com/alefais/storm-applications/blob/master/WordCount/src/main/resources/wordcount/wc.properties).