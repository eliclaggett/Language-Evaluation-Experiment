#!/bin/bash

################################################################################
# Filename: stop.command
# Author: Elijah Claggett
# Description: Stops the Breadboard experiment on the server
#
# Usage:
#   ./deployment/stop.command
#
################################################################################

parent_path=$( cd "$(dirname "${BASH_SOURCE[0]}")" ; pwd -P )
cd "$parent_path"

if [ ! -f .env ]
then
    export $(cat ../.env | xargs)
fi

ssh -i ./server.pem -tt $SERVER_SSH << HERE
 bash
 cd $PROD_EXPERIMENT_DIR
 ./stop.sh
 exit
 exit
HERE

echo "Breadboard stopped!"