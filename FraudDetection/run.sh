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

printf "${GREEN}Running Storm tests for FraudDetection application\n${NORMAL}"

NTHREADS=32
NSOURCE_MAX=4
for nsource in $(seq 1 $NSOURCE_MAX);
do
    NPRED_MAX=$((NTHREADS-nsource-1))
    for npred in {0..30..2};
    do
        if [ $npred -eq 0 ];
        then
            printf "${BLUE}storm_frauddetection --nsource $nsource --npred 1 --nsink 1 --rate -1\n\n${NORMAL}"

            if [ $nsource -le 2 ];
            then
                storm jar target/FraudDetection-1.0-SNAPSHOT-jar-with-dependencies.jar FraudDetection.FraudDetection data/credit-card.dat $nsource 1 1 > tests/output_60s/main_$nsource-1-1_-1.log
            else
                timeout 10m storm jar target/FraudDetection-1.0-SNAPSHOT-jar-with-dependencies.jar FraudDetection.FraudDetection data/credit-card.dat $nsource 1 1 > tests/output_60s/main_$nsource-1-1_-1.log
            fi
        elif [ $npred -le $NPRED_MAX ];
        then
            printf "${BLUE}storm_frauddetection --nsource $nsource --npred $npred --nsink 1 --rate -1\n\n${NORMAL}"

            if [ $nsource -le 2 ];
            then
                storm jar target/FraudDetection-1.0-SNAPSHOT-jar-with-dependencies.jar FraudDetection.FraudDetection data/credit-card.dat $nsource $npred 1 > tests/output_60s/main_$nsource-$npred-1_-1.log
            else
                timeout 10m storm jar target/FraudDetection-1.0-SNAPSHOT-jar-with-dependencies.jar FraudDetection.FraudDetection data/credit-card.dat $nsource $npred 1 > tests/output_60s/main_$nsource-$npred-1_-1.log
            fi
        fi
    done
done
