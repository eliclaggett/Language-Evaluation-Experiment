#!/bin/bash

################################################################################
# Filename: start.command
# Author: Elijah Claggett
# Description: Starts the Breadboard experiment on the server
#
# Usage:
#   ./deployment/start.command
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
 nohup ./start.sh &
 sleep 1
 tail -f nohup.out
HERE