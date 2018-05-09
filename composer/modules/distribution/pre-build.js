/**
 * Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 *
 */

const fs = require('fs'),
    glob = require('glob'),
    decompress = require('decompress'),
    path = require('path'),
    webModuleBuild = path.join(__dirname, '..', 'web', 'target',
                  'ballerina-composer-web-0.970.0-beta6-SNAPSHOT.zip'),
    serverBuild = path.join(__dirname, '..', 'server',
                  'distribution', 'target', 'composer-server-distribution-*.jar');

// copy resources from web module
function prepareWebModule() {
    // search for web module build file
    let foundFiles = glob.sync(webModuleBuild);

    if(foundFiles.length !== 1) {
        console.error('Error while searching for web module build file.');
    }
    // extract web module build to resources folder
    decompress(foundFiles[0], __dirname, {
        strip: 1
    });
}

// copy server
function prepareServer() {
    // search for server jar
    let foundFiles = glob.sync(serverBuild);

    if(foundFiles.length !== 1) {
        console.error('Error while searching for server build file.');
    }

    fs.createReadStream(foundFiles[0]).pipe(fs.createWriteStream(path.join(__dirname, 'composer-server-distribution.jar')));
}

prepareWebModule();
prepareServer();
