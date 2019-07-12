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

RED=$(tput setaf 1)
GREEN=$(tput setaf 2)
BLUE=$(tput setaf 4)
MAGENTA=$(tput setaf 5)
CYAN=$(tput setaf 6)
NORMAL=$(tput sgr0)

printf "${GREEN}Running Storm tests for TrafficMonitoring application\n${NORMAL}"

NTHREADS=32
NSOURCE_MAX=1
for nsource in $(seq 1 $NSOURCE_MAX);
do
    NMATCH_MAX=$((NTHREADS-3))
    RATE=$((nsource*1000))
    for nmatch in {0..29..2};
    do
        if [ $nmatch -eq 0 ];
        then
            printf "${BLUE}storm_trafficmonitoring --nsource 1 --nmatcher 1 --ncalculator 1 --nsink 1 --rate $RATE\n\n${NORMAL}"

            timeout 30m storm jar target/TrafficMonitoring-1.0-SNAPSHOT-jar-with-dependencies.jar TrafficMonitoring.TrafficMonitoring beijing 1 1 1 1 $RATE > tests/output_60s/main_1-1-1-1_$RATE.log

        elif [ $nmatch -le $NMATCH_MAX ];
        then
            printf "${BLUE}storm_trafficmonitoring --nsource 1 --nmatcher $nmatch --ncalculator 1 --nsink 1 --rate $RATE\n\n${NORMAL}"

            if [ $nmatch -le 4 ];
            then
                timeout 30m storm jar target/TrafficMonitoring-1.0-SNAPSHOT-jar-with-dependencies.jar TrafficMonitoring.TrafficMonitoring beijing 1 $nmatch 1 1 $RATE > tests/output_60s/main_1-$nmatch-1-1_$RATE.log
            else
                timeout 10m storm jar target/TrafficMonitoring-1.0-SNAPSHOT-jar-with-dependencies.jar TrafficMonitoring.TrafficMonitoring beijing 1 $nmatch 1 1 $RATE > tests/output_60s/main_1-$nmatch-1-1_$RATE.log
            fi
        else
            printf "${BLUE}storm_trafficmonitoring --nsource 1 --nmatcher 29 --ncalculator 1 --nsink 1 --rate $RATE\n\n${NORMAL}"

            timeout 10m storm jar target/TrafficMonitoring-1.0-SNAPSHOT-jar-with-dependencies.jar TrafficMonitoring.TrafficMonitoring beijing 1 29 1 1 $RATE > tests/output_60s/main_1-29-1-1_$RATE.log
        fi
    done
done
