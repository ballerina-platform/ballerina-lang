#!/bin/bash

# $1 - Property file
# $2 - associative array
# How to call
# declare -A somearray
# read_property_file testplan-props.properties somearray
function read_property_file() {
    local testplan_properties=$1
    # Read configuration into an associative array
    # IFS is the 'internal field separator'. In this case, your file uses '='
    local -n configArray=$2
    IFS="="
    while read -r key value
    do
         configArray[$key]=$value
    done < ${testplan_properties}
    unset IFS
}

# $1 file path
function write_to_properties_file() {
    local properties_file_path=$1
    local -n properties_array=$2

    # Keys are accessed through exclamation point
    for i in ${!properties_array[@]}
    do
      echo ${i}=${properties_array[$i]} >> ${properties_file_path}
    done
}

function install_ballerina() {
    local ballerina_version=$1
    wget https://product-dist.ballerina.io/downloads/${ballerina_version}/ballerina-linux-installer-x64-${ballerina_version}.deb
    sudo dpkg -i ballerina-linux-installer-x64-${ballerina_version}.deb --quiet

    ballerina version
}

function download_and_extract_mysql_connector() {
    wget https://dev.mysql.com/get/Downloads/Connector-J/mysql-connector-java-5.1.47.tar.gz --quiet

    tar -xzf mysql-connector-java-5.1.47.tar.gz --directory ./
}
