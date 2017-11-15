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
                  'ballerina-composer-web-*.zip'),
    serviceBuild = path.join(__dirname, '..', 'services',
                  'workspace-service', 'target', 'workspace-service-*.jar');

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

// copy micro service
function prepareService() {
    // search for service jar
    let foundFiles = glob.sync(serviceBuild);

    if(foundFiles.length !== 1) {
        console.error('Error while searching for service build file.');
    }

    fs.createReadStream(foundFiles[0]).pipe(fs.createWriteStream(path.join(__dirname, 'workspace-service.jar')));
}

prepareWebModule();
prepareService();
