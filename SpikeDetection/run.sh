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

printf "${GREEN}Running Storm tests for SpikeDetection application\n${NORMAL}"

NTHREADS=32
NSOURCE_MAX=4
for nsource in $(seq 1 $NSOURCE_MAX);
do
    NAVG_MAX=$((NTHREADS-nsource-2))
    for navg in {0..29..2};
    do
        if [ $navg -eq 0 ];
        then
            printf "${BLUE}storm_spikedetection --nsource $nsource --naverage 1 --ndetector 1 --nsink 1 --rate -1\n\n${NORMAL}"

            if [ $nsource -le 2 ];
            then
                storm jar target/SpikeDetection-1.0-SNAPSHOT-jar-with-dependencies.jar SpikeDetection.SpikeDetection data/sensors.dat $nsource 1 1 1 > tests/output_60s/main_$nsource-1-1-1_-1.log
            else
                timeout 10m storm jar target/SpikeDetection-1.0-SNAPSHOT-jar-with-dependencies.jar SpikeDetection.SpikeDetection data/sensors.dat $nsource 1 1 1 > tests/output_60s/main_$nsource-1-1-1_-1.log
            fi
        elif [ $navg -le $NAVG_MAX ];
        then
            printf "${BLUE}storm_spikedetection --nsource $nsource --naverage $navg --ndetector 1 --nsink 1 --rate -1\n\n${NORMAL}"

            if [ $nsource -le 2 ];
            then
                storm jar target/SpikeDetection-1.0-SNAPSHOT-jar-with-dependencies.jar SpikeDetection.SpikeDetection data/sensors.dat $nsource $navg 1 1 > tests/output_60s/main_$nsource-$navg-1-1_-1.log
            else
                timeout 10m storm jar target/SpikeDetection-1.0-SNAPSHOT-jar-with-dependencies.jar SpikeDetection.SpikeDetection data/sensors.dat $nsource $navg 1 1 > tests/output_60s/main_$nsource-$navg-1-1_-1.log
            fi

            for ndet in {2..8..2};
            do
                if [ $navg -le $((NTHREADS-nsource-ndet-1)) ];
                then
                    printf "${BLUE}storm_spikedetection --nsource $nsource --naverage $navg --ndetector $ndet --nsink 1 --rate -1\n\n${NORMAL}"

                    if [ $nsource -le 2 ];
                    then
                        storm jar target/SpikeDetection-1.0-SNAPSHOT-jar-with-dependencies.jar SpikeDetection.SpikeDetection data/sensors.dat $nsource $navg $ndet 1 > tests/output_60s/main_$nsource-$navg-$ndet-1_-1.log
                    else
                        timeout 10m storm jar target/SpikeDetection-1.0-SNAPSHOT-jar-with-dependencies.jar SpikeDetection.SpikeDetection data/sensors.dat $nsource $navg $ndet 1 > tests/output_60s/main_$nsource-$navg-$ndet-1_-1.log
                    fi
                fi
            done
        fi
    done
done