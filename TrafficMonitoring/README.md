# Compile and run Traffic Monitoring application

## Compile
From inside the root directory `TrafficMonitoring/`

`mvn clean install`

## Run
<b>TrafficMonitoring</b> application can be run passing some arguments (if no command line argument is provided then default values defined in [tm.properties](https://github.com/alefais/storm-applications/blob/master/TrafficMonitoring/src/main/resources/trafficmonitoring/tm.properties) file and [Constants](https://github.com/alefais/storm-applications/tree/master/TrafficMonitoring/src/main/java/Constants) package are used). <br> The following arguments can be specified:<ul><li>city to monitor (this value can be `beijing` or `dublin`)</li><li>source parallelism degree</li><li>map matcher bolt parallelism degree</li><li>speed calculator bolt parallelism degree</li><li>sink parallelism degree</li><li>source generation rate (default -1, generate at the max possible rate)</li><li>topology name (default TrafficMonitoring)</li><li>execution mode (default local)</li></ul>

### Execution examples:
* No argument is passed (the monitored city is Beijing, all the nodes have parallelism degree equal to 1, the source generation rate is the one defined in [BaseConstants.java](https://github.com/alefais/storm-applications/tree/master/TrafficMonitoring/src/main/java/Constants/BaseConstants.java), the execution is local and the input data set is the ESRI shapefile contained in the `TrafficMonitoring/data/` directory and relative to Beijing city): <br> `$STORM_HOME/bin/storm jar target/TrafficMonitoring-1.0-SNAPSHOT-jar-with-dependencies.jar TrafficMonitoring.TrafficMonitoring`

* The parallelism degree is explicitly set for all the nodes in the application graph (source: 1, map matcher: 2, speed calculator: 1, sink: 1), the data set used as input is the ESRI shapefile contained in the `TrafficMonitoring/data/` directory and relative to Beijing city and the source generation rate is limited to 1000 tuples/second <br> (<b>NB:</b> for each unspecified parameter the default value - specified either as a constant or as a property - is used, as in the previous case): <br> `$STORM_HOME/bin/storm jar target/TrafficMonitoring-1.0-SNAPSHOT-jar-with-dependencies.jar TrafficMonitoring.TrafficMonitoring beijing 1 2 1 1 1000`

<b>NB:</b> Running the application as `$STORM_HOME/bin/storm jar target/TrafficMonitoring-1.0-SNAPSHOT-jar-with-dependencies.jar TrafficMonitoring.TrafficMonitoring help` visualizes all the parameter options.

<b>NB:</b> The default values of the input parameters can be changed by modifying the content of the [tm.properties](https://github.com/alefais/storm-applications/blob/master/TrafficMonitoring/src/main/resources/trafficmonitoring/tm.properties) file.