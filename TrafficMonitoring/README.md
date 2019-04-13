# Compile and run TrafficMonitoring

## Compile
From inside the root directory `TrafficMonitoring/`

`mvn clean install`

## Run
<b>TrafficMonitoring</b> application can be run passing some arguments (if no command line argument is provided then default values defined in `tm.properties` file and `Constants` package are used). <br> Optional arguments are:<ul><li>city (you can choose `beijing` or `dublin`)</li><li>source parallelism degree</li><li>map matching bolt parallelism degree</li><li>speed calculator bolt parallelism degree</li><li>sink parallelism degree</li><li>source generation rate (default -1, generate at the max possible rate)</li><li>topology name (default TrafficMonitoring)</li><li>execution mode (default local)</li></ul>

### Execution examples:
* No argument is passed (the monitored city is Beijing, all the nodes have parallelism degree equal to 1, the source generation rate is the one defined in `BaseConstants.java`, the execution is local): <br> `storm jar target/TrafficMonitoring-1.0-SNAPSHOT-jar-with-dependencies.jar TrafficMonitoring.TrafficMonitoring`

* The parallelism degree is explicitly defined for all the nodes in the topology and we specify we want to analyze data coming from Beijing city taxi traces, setting the parallelism degree for all the nodes in the topology (source: 1, bolt1: 2, bolt2: 1, sink: 1) and the tuples' generation rate in the spout to 1000 tuples/second <br> (<b>NB:</b> for each unspecified parameter the default value - specified either as a constant or as a property - is used, as in the previous case): <br> `storm jar target/TrafficMonitoring-1.0-SNAPSHOT-jar-with-dependencies.jar TrafficMonitoring.TrafficMonitoring beijing 1 2 1 1 1000`

<b>NB:</b> Running the application as `storm jar target/TrafficMonitoring-1.0-SNAPSHOT-jar-with-dependencies.jar TrafficMonitoring.TrafficMonitoring help` visualizes all the parameter options.