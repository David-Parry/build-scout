#!/bin/bash
ps aux | grep '[j]ava.*scout.*jar' | awk '{print $2 " " $11 " " $12 " " $13 " " $14 " " $15}'
ps aux | grep '[j]ava.*scout.*jar' | grep -v grep | awk '{print $2}' | xargs kill -9
