# Compile and run SpikeDetection

## Compile
From inside the root directory `SpikeDetection/`

`mvn clean install`

## Run
In order to correctly run SpikeDetection app you need to pass the input file path as mandatory argument.<br>
Optional arguments are:
- source parallelism degree (default 1)
- bolt1 parallelism degree (default 1)
- bolt2 parallelism degree (default 1)
- sink parallelism degree (default 1)
- source generation rate (default -1, generate at the max possible rate)
- topology name (default FraudDetection)
- execution mode (default local)

### Execution example:
The parallelism degree is set for all the nodes in the topology (source: 1, bolt1: 2, bolt2: 2, sink: 1).

`storm jar target/SpikeDetection-1.0-SNAPSHOT-jar-with-dependencies.jar SpikeDetection.SpikeDetection data/app/sd/sensors.dat 1 2 2 1`
