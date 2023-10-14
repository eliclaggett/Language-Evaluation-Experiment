#!/bin/bash
parent_path=$( cd "$(dirname "${BASH_SOURCE[0]}")" ; pwd -P )
cd "$parent_path"

if [ ! -f .env ]
then
    export $(cat ../.env | xargs)
fi

ssh -i ./server.pem -tt $SERVER_SSH << HERE
 bash
 cd $EXPERIMENT_DIR
 ./cleanup.sh
 exit
 exit
HERE

echo "Breadboard stopped!"