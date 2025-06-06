#!/bin/bash
ps aux | grep '[j]ava.*scout.*jar' | awk '{print $2 " " $11 " " $12 " " $13 " " $14 " " $15}'
ps aux | grep '[j]ava.*scout.*jar' | grep -v grep | awk '{print $2}' | xargs kill -9

ps aux | grep 'scout' | awk '{print $2 " " $11 " " $12 " " $13 " " $14 " " $15}'
ps aux | grep 'scout' | grep -v grep | awk '{print $2}' | xargs kill -9
