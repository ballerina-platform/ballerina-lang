#!/bin/bash
# Copyright (c) 2017, WSO2 Inc. (http://wso2.com) All Rights Reserved.
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
# http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.

set -e

function showUsageAndExit() {
    echo "Invalid or insufficient options provided."
    echo
    echo "USAGE: ./build.sh -d <ballerina-distribution> -v <image-version> -o <organization-name>"
    echo
    echo "Ex: Create a Ballerina Docker image tagged \"ballerina:latest\" with Ballerina 0.970.0-alpha1-SNAPSHOT distribution."
    echo "    ./build.sh -d ballerina-0.970.0-alpha1-SNAPSHOT.zip"
    echo

    exit
}

while getopts :d:o:v:h FLAG; do
  case $FLAG in
    d)
      bal_dist_file=$OPTARG
      ;;
    v)
      image_version=$OPTARG
      ;;
    o)
      org_name=$OPTARG
      ;;
    h)
      showUsageAndExit
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

if [ -z "$image_version" ]; then
    image_version="latest"
fi

image_name="${org_name}ballerina:${image_version}"

if [ ! -e $bal_dist_file ]; then
  echo "Cannot find Ballerina distribution ${bal_dist_file}. Aborting..."
  exit 1
fi

build_version=grep --max-count=1 '<version>' ../pom.xml | awk -F '>' '{ print $2 }' | awk -F '<' '{ print $1 }'
echo "Building Ballerina Base Docker image $image_name..."
cp $bal_dist_file .
bal_dist_file_name=$(basename $bal_dist_file)

docker build --no-cache=true \
             --build-arg BALLERINA_DIST=${bal_dist_file_name} \
             --build-arg BUILD_DATE=$(date -u +'%Y-%m-%dT%H:%M:%SZ') \
             --build-arg VCS_REF=$(git rev-parse --short HEAD) \
             --build-arg BUILD_VERSION=${build_version} \
             -t $image_name .

echo "Cleaning..."
docker images | grep "<none>" | awk '{print $3}' | xargs docker rmi -f > /dev/null 2>&1
docker images | head -n 2
rm -rf $bal_dist_file_name
echo "DONE!"
