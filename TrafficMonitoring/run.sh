#!/usr/bin/env bash

# @author   Alessandra Fais
# @date     July 2019

############################################## create test directories #################################################

if [ ! -d tests ]; then
    mkdir tests
fi
if [ ! -d tests/output_60s ]; then
    mkdir tests/output_60s
fi

#################################################### run tests #########################################################

printf "Running Storm tests for TrafficMonitoring application\n"

NTHREADS=32
NSOURCE_MAX=1
for nsource in $(seq 1 $NSOURCE_MAX);
do
    NMATCH_MAX=$((NTHREADS-3))
    RATE=$((nsource*1000))
    for nmatch in $(seq 1 $NMATCH_MAX);
    do
        printf "storm_trafficmonitoring --nsource 1 --nmatcher $nmatch --ncalculator 1 --nsink 1 --rate $RATE\n\n"

        if [ $nmatch -lt 4 ]
        then
            timeout 30 storm jar target/TrafficMonitoring-1.0-SNAPSHOT-jar-with-dependencies.jar TrafficMonitoring.TrafficMonitoring beijing 1 $nmatch 1 1 $RATE > tests/output_60s/main_1-$nmatch-1-1_$RATE.log
        elif [ $nmatch -lt 10 ]
        then
            timeout 10 storm jar target/TrafficMonitoring-1.0-SNAPSHOT-jar-with-dependencies.jar TrafficMonitoring.TrafficMonitoring beijing 1 $nmatch 1 1 $RATE > tests/output_60s/main_1-$nmatch-1-1_$RATE.log
        else
            timeout 5 storm jar target/TrafficMonitoring-1.0-SNAPSHOT-jar-with-dependencies.jar TrafficMonitoring.TrafficMonitoring beijing 1 $nmatch 1 1 $RATE > tests/output_60s/main_1-$nmatch-1-1_$RATE.log
        fi
    done
done
