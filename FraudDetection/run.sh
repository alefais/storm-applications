#!/usr/bin/env bash

# @author   Alessandra Fais
# @date     18/06/2019

############################################## create test directories #################################################

if [ ! -d tests ]; then
    mkdir tests
fi
if [ ! -d tests/output_60s ]; then
    mkdir tests/output_60s
fi

#################################################### run tests #########################################################

printf "Running Storm tests with rate -1\n"

NCORES=16
NTHREADS=32

NSOURCE_MAX=5
for nsource in $(seq 1 $NSOURCE_MAX);
do
    NPRED_MAX=$((NTHREADS-nsource-1))
    for npred in $(seq 1 $NPRED_MAX);
    do
        printf "storm_test --nsource $nsource --npred $npred --nsink 1 --rate -1\n\n"

        storm jar target/FraudDetection-1.0-SNAPSHOT-jar-with-dependencies.jar FraudDetection.FraudDetection $nsource $npred 1 | tee tests/output_60s/main_$nsource-$npred-1_-1.log
    done
done
