#!/bin/bash

while getopts :v:o: FLAG; do
  case $FLAG in
    v)
      bal_version=$OPTARG
      ;;
    o)
      org_name=$OPTARG
      ;;
    \?)
      showUsageAndExit
      ;;
  esac
done

if [ ! -z "$org_name" ] && [ "$org_name" != */ ]; then
  org_name="${org_name}/"
fi

if [ -z "$bal_version" ]; then
    image_name="${org_name}ballerina"
else
    image_name="${org_name}ballerina:${bal_version}"
fi

bal_dist="ballerina-${bal_version}"
bal_dist_file="${bal_dist}.zip"

if [ ! -e $bal_dist_file ]; then
  echo "Cannot find Ballerina distribution ${bal_dist_file}. Aborting..."
  exit 1
fi

echo "Building Ballerina Base Docker image $image_name..."
docker build --no-cache=true --build-arg BAL_DIST=$bal_dist -t $image_name .

echo "Cleaning..."
docker images | grep "<none>" | awk '{print $3}' | xargs docker rmi -f > /dev/null 2>&1
docker images | head -n 2
echo "DONE!"
