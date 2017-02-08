#!/bin/bash

function showUsageAndExit() {
    echo "Invalid or insufficient options provided."
    echo
    echo "USAGE: ./build.sh -d <ballerina-distribution> -v <image-version> -o <organization-name>"
    echo
    echo "Ex: Create a Ballerina Docker image tagged \"ballerina:latest\" with Ballerina 0.8.0-SNAPSHOT distribution."
    echo "    ./build.sh ballerina-0.8.0-SNAPSHOT.zip"
    echo

    exit
}
while getopts :d:o:v: FLAG; do
  case $FLAG in
    d)
      bal_dist_file=$OPTARG
      ;;
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

if [ -z $bal_dist_file ]; then
    showUsageAndExit
fi

if [ ! -z "$org_name" ] && [ "$org_name" != */ ]; then
  org_name="${org_name}/"
fi

if [ -z "$bal_version" ]; then
    bal_version="latest"
fi

image_name="${org_name}ballerina:${bal_version}"

if [ ! -e $bal_dist_file ]; then
  echo "Cannot find Ballerina distribution ${bal_dist_file}. Aborting..."
  exit 1
fi

echo "Building Ballerina Base Docker image $image_name..."
docker build --no-cache=true --build-arg BAL_DIST=${bal_dist_file:0:-4} -t $image_name .

echo "Cleaning..."
docker images | grep "<none>" | awk '{print $3}' | xargs docker rmi -f > /dev/null 2>&1
docker images | head -n 2
echo "DONE!"
