'use strict';
/**
 * Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

// You can import and use all API from the 'vscode' module
// as well as import your extension to test it

// Ballerina tools distribution will be copied to following location by maven
import * as fs from 'fs';

const TEST_RESOURCES = __dirname + '/../../target/test/';
const PLATFORM_PREFIX = /ballerina-tools-/;


function findBallerinaDistribution(){
    const directories = fs.readdirSync(TEST_RESOURCES);
    if(directories.length !== 1){
        throw new Error("Unable to find ballerina distribution in test resources.");
    }
    return directories[0];
}

export function getBallerinaHome(): string {
    const path = TEST_RESOURCES + findBallerinaDistribution();
    return fs.realpathSync(path);
}

export function getBallerinaVersion() {
    return findBallerinaDistribution().replace(PLATFORM_PREFIX, '').replace('\n','').trim();
}