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
const path = require('path');
const { workspace } = require('vscode');
const glob = require('glob');

const libPath = '/bre/lib/*';
const jrePath = '/bre/lib/jre*';
const composerlibPath = '/lib/resources/composer/services/*';
const main = 'org.ballerinalang.langserver.launchers.stdio.Main';
const log = require('./logger');


function getClassPath() {
    const customClassPath = workspace.getConfiguration('ballerina').get('classpath');
    const sdkPath = workspace.getConfiguration('ballerina').get('home');
    const jarPath = path.join(__dirname, 'server-build', 'language-server-stdio-launcher.jar');
	// in windows class path seperated by ';'
	const sep = process.platform === 'win32' ? ';' : ':';
    let classpath = path.join(sdkPath, composerlibPath) + sep + path.join(sdkPath, libPath) + sep + jarPath;

    if (customClassPath) {
        classpath =  customClassPath + sep + classpath;
    }
    return classpath;
}

function getExcecutable() {
    let excecutable = 'java';
    const { JAVA_HOME } = process.env;
    const sdkPath = workspace.getConfiguration('ballerina').get('home');
    const files = glob.sync(path.join(sdkPath, jrePath));

    if (files[0]) {
        log(`Using java from ballerina.home: ${files[0]}`);
        excecutable = path.join(files[0], 'bin', 'java');
    } else if (JAVA_HOME) {
        log(`Using java from JAVA_HOME: ${JAVA_HOME}`);
        excecutable = path.join(JAVA_HOME, 'bin', 'java');
    }
    return excecutable;
}

function getServerOptions() {
    const args = ['-cp', getClassPath()];
    if (process.env.LSDEBUG === "true") {
        log('LSDEBUG is set to "true". Services will run on debug mode');
        args.push('-agentlib:jdwp=transport=dt_socket,server=y,suspend=y,address=5005,quiet=y');
    }
    const balHomePath = workspace.getConfiguration('ballerina').get('home');
    const balHomeSysProp = `-Dballerina.home=${balHomePath}`;

    const balDebugLog = workspace.getConfiguration('ballerina').get('debugLog');
    const balDebugLogSysProp = `-Dballerina.debugLog=${balDebugLog}`;

    return {
        command: getExcecutable(),
        args: [balHomeSysProp, balDebugLogSysProp, ...args, main],
        options: {
        }
    }
}

module.exports = {
    getServerOptions,
}