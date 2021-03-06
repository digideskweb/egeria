#!/bin/bash

#  SPDX-License-Identifier: Apache-2.0
#  Copyright Contributors to the ODPi Egeria project. 

# Script to configure Atlas for use with egeria - see below for variables
# used. Primarily intended as part of automated kubernetes/docker setup,
# but can also be used interactively if required.

printf "\nConfiguring Atlas for Egeria VDC\n\n" 

fail=0

if [ "null" = "$ATLAS_ENDPOINT" ] || [ -z $ATLAS_ENDPOINT ]
then
	echo "ATLAS_ENDPOINT must be supplied. This is URL of the Atlas server that will be configured. An example is 'http://atlas.acme.org:9000'"
	fail=1
fi

if [ "null" = "$KAFKA_ENDPOINT" ] || [ -z $KAFKA_ENDPOINT ]
then
	echo "KAFKA_ENDPOINT must be supplied. This is the address:port of the Kafka Broker. An example is 'kafka.example.org:9092'"
	fail=1
fi

if [ "null" = "$EGERIA_USER" ] || [ -z $EGERIA_USER ]
then
	echo "EGERIA_USER must be supplied. This is user name for the egeria api in Atlas. An example is 'admin'"
	fail=1
fi

if [ "null" = "$EGERIA_SERVER" ] || [ -z $EGERIA_SERVER ]
then
	echo "EGERIA_SERVER must be supplied. This is the server name for the egeria api in Atlas. An example is 'myserver'"
	fail=1
fi

if [ "null" = "$EGERIA_COHORT" ] || [ -z $EGERIA_COHORT ]
then
	echo "EGERIA_COHORT must be supplied. This is the name of the cohort. An example is 'coco'"
	fail=1
fi

if [ $fail -eq 1 ]
then
	exit 1
fi


echo "Atlas server URL  : ${ATLAS_ENDPOINT}"
echo "Kafka broker      : ${KAFKA_ENDPOINT}"
echo "Egeria user name  : ${EGERIA_USER}"
echo "Egeria server name: ${EGERIA_SERVER}"
echo "Egeria Cohort     : ${EGERIA_COHORT}"

echo "Checking KAFKA is up"

n=0
   until [ $n -ge 100 ]
   do
      kafkacat -b ${KAFKA_ENDPOINT} -L  && break  # substitute your command here
      n=$[$n+1]
      sleep 30
   done

echo "Checking ATLAS is up"

loop=100
retrytimeout=10
delay=30

# - In a standard environment, return code 1 indicates service is running. However in a k8s environment the 
# service appears to respond even before the backend is up. Thus a better test is to force an error.
# So we will do a post, expecting a 401 back when things are ok (full authentication is required)
while [ $loop -gt 0 ]
do
    http --check-status --ignore-stdin --timeout=${retrytimeout} HEAD ${ATLAS_ENDPOINT} &> /dev/null
    rc=$?
    if [ $rc -eq 4 ]
    then
        echo 'OK!'
	break
    else
        # timeout or otherwise not ready - let's keep trying
        let loop=$loop-1
        echo ".. not yet up. waiting ${delay}s. ${loop} attempts remaining"
        sleep ${delay}
    fi
done

if [ $loop -le 0 ]
then
	echo "Atlas server was unavailable. Abandoning configuration. Check endpoint URL"
	exit 1
fi


# Issue requests against atlas
http --verbose --ignore-stdin \
	--check-status \
	--auth admin:admin \
	POST ${ATLAS_ENDPOINT}/egeria/open-metadata/admin-services/users/${EGERIA_USER}/servers/${EGERIA_SERVER}/event-bus   \
	producer:='{"bootstrap.servers":"'${KAFKA_ENDPOINT}'"}' \
        consumer:='{"bootstrap.servers":"'${KAFKA_ENDPOINT}'"}' 
rc=$?
if [ $rc -ne 0 ]
then
	printf "\nAtlas setup Failed configuring event bus\n" 
	exit 1
fi


# Repository type configuration

http --verbose --ignore-stdin \
	--check-status \
        --auth admin:admin \
        POST ${ATLAS_ENDPOINT}/egeria/open-metadata/admin-services/users/${EGERIA_USER}/servers/${EGERIA_SERVER}/atlas-repository   
rc=$?
if [ $rc -ne 0 ]
then
        printf "\nAtlas setup Failed configuring repository type\n"
        exit 1
fi

# Server root configuration
http --verbose --ignore-stdin \
	--check-status \
        --auth admin:admin \
        POST ${ATLAS_ENDPOINT}/egeria/open-metadata/admin-services/users/${EGERIA_USER}/servers/${EGERIA_SERVER}/server-url-root?url=${ATLAS_ENDPOINT}/egeria
rc=$?
if [ $rc -ne 0 ]
then
        printf "\nAtlas setup Failed configuring root\n"
        exit 1
fi

# Cohort configuration
http --verbose --ignore-stdin \
	--check-status \
        --auth admin:admin \
        POST ${ATLAS_ENDPOINT}/egeria/open-metadata/admin-services/users/${EGERIA_USER}/servers/${EGERIA_SERVER}/cohorts/${EGERIA_COHORT}
rc=$?
if [ $rc -ne 0 ]
then
        printf "\nAtlas setup Failed configuring cohort\n"
        exit 1
fi

# Activation - note 5 minute timeout
echo "Configuration complete -- now activating (may take a few minutes)"
http --verbose --ignore-stdin \
	--check-status \
        --auth admin:admin \
	--timeout 900 \
        POST ${ATLAS_ENDPOINT}/egeria/open-metadata/admin-services/users/${EGERIA_USER}/servers/${EGERIA_SERVER}/instance
rc=$?
if [ $rc -ne 0 ]
then
        printf "\nAtlas setup Failed starting instance\n"
        exit 1
fi
printf "\nAtlas setup completed\n"

echo "WARNING - Now going into sleep loop - to maintain health successful pod status"
while [ true ]
do
	sleep 100000000
done
exit 0
