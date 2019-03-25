# Compile and run FraudDetection

## Compile
From inside the root directory `FraudDetection/`

`mvn clean install`

## Run
Run passing the input dataset and the parallelism degree for all nodes

`storm jar target/FraudDetection-1.0-SNAPSHOT-jar-with-dependencies.jar FraudDetection.FraudDetection data/app/fd/credit-card.dat 1 4 1`
