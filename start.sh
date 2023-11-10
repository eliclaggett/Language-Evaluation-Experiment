#!/bin/bash
set -a            
source .env
set +a

echo $DEPLOYMENT;

export PYENV_ROOT="$HOME/.pyenv"
command -v pyenv >/dev/null || export PATH="$PYENV_ROOT/bin:$pth"
eval "$(pyenv init -)"

if which pyenv-virtualenv-init > /dev/null; then eval "$(pyenv virtualenv-init -)"; fi

pth=$(pwd)

cd "$pth/breadboard" && { ./breadboard.sh $> "$pth/log_breadboard.log" & }
pid1=$!
echo "Breadboard running on $pid1";


venv=$PROD_VENV
if [ "$DEPLOYMENT" == "dev" ]
then
    cd "$pth/frontend" && { npm run serve &> "$pth/log_npm.log" & }
    pid2=$!
    echo "NPM running on $pid2";
    venv=$VENV
fi

cd "$pth/bot" && { pyenv activate $venv; python3 -u convo_evaluator.py $> "$pth/log_python.log" & }
pid3=$!

echo "Python running on $pid3";

cd $pth;

if [ "$DEPLOYMENT" == "prod" ]
then
sudo service nginx start;
echo "Nginx running";
fi

function cleanup()
{
    echo -e "\nKilling breadboard" $(cat $pth/breadboard/RUNNING_PID);
    kill $(cat $pth/breadboard/RUNNING_PID);
    
    if [ "$DEPLOYMENT" == "dev" ]
    then
        kill -9 $pid1 $pid2 $pid3;
        echo "Killing $pid1 $pid2 $pid3";
    else
        kill -9 $pid1 $pid3;
        echo "Killing $pid1 $pid3";
        sudo service nginx stop;
        echo "Nginx stopped";
    fi
    rm log_* running_proclist.txt
}

if [ "$DEPLOYMENT" == "dev" ]
    then
        echo "$$ $pid1 $pid2 $pid3" > running_proclist.txt
    else
        echo "$$ $pid1 $pid3" > running_proclist.txt
fi

trap cleanup SIGINT
sleep 30;
echo "Ready! Open Breadboard at https://$SERVER_URL"
wait