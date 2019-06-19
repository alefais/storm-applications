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

NSOURCE_MAX=10
for nsource in $(seq 1 $NSOURCE_MAX);
do
    NMATCH_MAX=$((NTHREADS-3))
    RATE=$((nsource*1000))
    for nmatch in $(seq 1 $NMATCH_MAX);
    do
        printf "storm_test --nsource 1 --nmatcher $nmatch --ncalculator 1 --nsink 1 --rate $RATE\n\n"

        storm jar target/TrafficMonitoring-1.0-SNAPSHOT-jar-with-dependencies.jar TrafficMonitoring.TrafficMonitoring beijing 1 $nmatch 1 1 $RATE | tee tests/output_60s/main_1-$nmatch-1-1_$RATE.log
    done
done
