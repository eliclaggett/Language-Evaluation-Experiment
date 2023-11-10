#!/bin/bash
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