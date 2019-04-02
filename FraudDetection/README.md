# Compile and run FraudDetection

## Compile
From inside the root directory `FraudDetection/`

`mvn clean install`

## Run
In order to correctly run FraudDetection app you need to pass the input file path as mandatory argument.
Optional arguments are:
- source parallelism degree (default 1)
- bolt parallelism degree (default 1)
- sink parallelism degree (default 1)
- source generation rate (default -1, generate at the max possible rate)
- topology name (default FraudDetection)
- execution mode (default local)

### Execution example:
The parallelism degree is set for all the nodes in the topology (source: 1, bolt: 4, sink: 1).

`storm jar target/FraudDetection-1.0-SNAPSHOT-jar-with-dependencies.jar FraudDetection.FraudDetection data/app/fd/credit-card.dat 1 4 1`
