#!/bin/bash

################################################################################
# Filename: deploy.command
# Author: Elijah Claggett
# Description: Deploys this Breadboard experiment along with its associated files
#
# Usage:
#   ./deployment/deploy.command
#
################################################################################

parent_path=$( cd "$(dirname "${BASH_SOURCE[0]}")" ; pwd -P )
cd "$parent_path"

# Load dotenv
if [ ! -f .env ]
then
    export $(cat ../.env | xargs)
fi

cd "../frontend"
npm run build

cd "../"
echo $SERVER_SSH

sftp -b - -i ./deployment/server.pem $SERVER_SSH <<EOF
	put -r breadboard/generated/breadboard-v2.4 /home/ubuntu/eli/breadboard/generated
	put .env /home/ubuntu/eli/.env
	put texts.json /home/ubuntu/eli/texts.json
	put bot/convo_evaluator.py /home/ubuntu/eli/bot/convo_evaluator.py
	exit
EOF