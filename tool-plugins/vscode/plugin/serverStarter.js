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

const net = require('net');
const path = require('path');
const { spawn } = require('child_process');
const openport = require('openport');
const { workspace, window } = require('vscode');
const glob = require('glob');

let serverProcess;
const libPath = '/bre/lib/*';
const jrePath = '/bre/lib/jre*';
const composerlibPath = '/lib/resources/composer/services/*';
const main = 'org.ballerinalang.vscode.server.Main';
const log = require('./logger');

let LSService;
let parserService;

function getClassPath() {
    const customClassPath = workspace.getConfiguration('ballerina').get('classpath');
    const sdkPath = workspace.getConfiguration('ballerina').get('home');
    const jarPath = path.join(__dirname, 'server-build', 'plugin-vscode-server.jar');
	// in windows class path seperated by ';'
	const sep = process.platform === 'win32' ? ';' : ':';
    let classpath = path.join(sdkPath, composerlibPath) + sep + path.join(sdkPath, libPath) + sep + jarPath;

    if (customClassPath) {
        classpath =  customClassPath + sep + classpath;
    }
    return classpath;
}

function getExcecutable() {
    var glob = require("glob")

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

function startServices() {
    if (LSService) {
        // services already started
        return;
    }

    let onParserStarted;
    let onParserError;
    let onLSStarted;
    let onLSError;

    LSService = new Promise((res, rej) => {
        onLSStarted = res;
        onLSError = rej;
    })

    parserService = new Promise((res, rej) => {
        onParserStarted = res;
        onParserError = rej;
    })

    openport.find({count: 2}, (err, [LSPort, parserPort]) => {
        let server = net.createServer(stream => {
            // Server is closed so that no more connections will be accepted
            server.close();
            onLSStarted({ reader: stream, writer: stream });
            log(`Ballerina Language Server connected on port: ${LSPort}`);
        });
        server.on('error', onLSError);
        server.listen(LSPort, () => {
            log(`Listening for Ballerina Language Server on: ${LSPort}`);
            server.removeListener('error', onLSError);

            const args = ['-cp', getClassPath()]

            if (process.env.LSDEBUG === "true") {
                log('LSDEBUG is set to "true". Services will run on debug mode');
                args.push('-agentlib:jdwp=transport=dt_socket,server=y,suspend=y,address=5005,quiet=y');
            }

            const balHomePath = workspace.getConfiguration('ballerina').get('home');
            const balHomeSysProp = `-Dballerina.home=${balHomePath}`;

            log(`Starting parser service on: ${parserPort}`);

            serverProcess = spawn(getExcecutable(), [balHomeSysProp, ...args, main, LSPort, parserPort]);

            serverProcess.on('error', (e) => {
                log(`Could not start services ${e}\n`, true);
                onParserError(e);
                onLSError(e);
            });

            let parserStarted = false;
            let aggregatedStdout = '';

            serverProcess.stdout.on('data', (data) => {
                log(`${data}`);

                if (parserStarted) {
                    return;
                }

                aggregatedStdout = `${aggregatedStdout}${data}`;
                if (aggregatedStdout.indexOf('Parser started successfully') > -1) {
                    log(`Parser started successfully on port: ${parserPort}`);
                    onParserStarted({port: parserPort});
                    parserStarted = true;
                }
            });
            serverProcess.stderr.on('data', (data) => {
                log(`${data}`);
            });
        });
    });
}

function getLSService() {
    if (!LSService) {
        startServices();
    }

    return LSService;
}

function getParserService() {
    if (!parserService) {
        startServices();
    }

    return parserService;
}

module.exports = {
    getLSService,
    getParserService,
    startServices,
}