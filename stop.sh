#!/bin/bash
kill -9 `cat breadboard/RUNNING_PID`
kill -9 `cat running_proclist.txt`
rm running_proclist.txt nohup.out log* breadboard/RUNNING_PID
