#!/bin/bash

#find . | grep Ballerina.toml > out.txt
#cd ballerina-lang
while read p; do
  PROJECT_PATH=${p/\/Ballerina.toml/} 
  cd $PROJECT_PATH
  mkdir xxx 
  #echo "git add src"
  #ls
  # move other directories in to src
  for d in */ ; do
    if [ $d != "xxx/" ] && [ $d != "*/" ]; then
      mv $d xxx/$d
    fi
  done
  #rm -r xxx;
  mv xxx src
  cd /home/jo/workspace/ballerina-lang
done < out.txt
