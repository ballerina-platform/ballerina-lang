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
const { app } = require('electron');
const path = require('path');
const process = require('process');
const fs = require('fs');
const log = require('log');
const { spawn } = require('child_process');
const { createWindow } = require('./app');
const { createErrorWindow } = require('./error-window');
const { ErrorCodes } = require('./error-codes');

let win,
    serverProcess,
    logger = new log('info'),
    appDir = app.getAppPath(),
    logsDir = path.join(appDir, '..', '..', 'logs');

function createLogger(){
    if (!fs.existsSync(logsDir)){
        fs.mkdirSync(logsDir);
    }
    let accessError = fs.accessSync(logsDir, fs.W_OK);
    if(accessError) {
        logger.error('cannot write to log folder.');
    } else {
        logger = new log('info', fs.createWriteStream(path.join(logsDir, 'app.log')));
    }
}

function startServer(){
    let errorWin;
    const composerExec = path.join(appDir, 'resources', 'ballerina-tools', 'bin', 'composer')
                    .replace('app.asar', 'app.asar.unpacked');
    logger.info('Starting composer exec at ' + composerExec);
    serverProcess = spawn('sh', 
                        [
                            composerExec,
                            '--openInBrowser',
                            'false'
                        ]
                    );
    logger.info('Verifying whether the backend server is started successfully');
    serverProcess.stdout.on('data', function(data) {
        // IMPORTANT: Wait till backend server is started to create window
        if (data.includes('Composer started successfully')) {
            logger.info('Backend server is properly started, starting composer UI');
            if (win === undefined) {
                win = createWindow();
                win.on('closed', () => {
                    win = null;
                });
            }
        }
    });

    serverProcess.stderr.on('data', function(data){
        let errorWindowLoaded = false,
            logsBuffer = [];
        if (!errorWin) {
            errorWin = createErrorWindow({errorCode: ErrorCodes.SERVICE_FAILED,
                errorMessage: data});
        }
        errorWin.webContents.on('did-finish-load', () => {
            errorWindowLoaded = true;
            while (logsBuffer.length > 0) {
                errorWin.webContents.send('error-log', logsBuffer.pop());
            }
        });
        if(!errorWindowLoaded) {
            logsBuffer.push(data);
        } else {
            errorWin.webContents.send('error-log', data);
        }
        logger.error('Failed to start backend server: ' + data);
    });

    serverProcess.on('close', function(code){
        logger.info('Server was shutdown. Exit code: ' + code);
    });
}

function checkJava(callback) {
    let callbackInvoked = false,
        javaCheck = spawn('java', ['-version']);
    javaCheck.on('error', function(err){
        if(!callbackInvoked) {
            callbackInvoked = true;
            callback(true, JSON.stringify(err));
        }
    });
    javaCheck.stderr.on('data', function(data) {
        if(!callbackInvoked) {
            callbackInvoked = true;
            callback(false, data);
        }
    });
}

// This method will be called when Electron has finished
// initialization and is ready to create browser windows.
// Some APIs can only be used after this event occurs.
app.on('ready', () => {
    createLogger();
    logger.info('verifying availability of java runtime in path');
    checkJava((error, message) => {
        if (!error) {
            logger.info('Starting composer backend services');
            startServer();
        } else {
            logger.error('Failed verifying availability of java runtime in path');
            logger.error('Error: ' + message);
            let errorWin = createErrorWindow({errorCode: ErrorCodes.JAVA_NOT_FOUND,
                errorMessage: message});
        }
    });
});

// Quit when all windows are closed.
app.on('window-all-closed', () => {
  // On macOS it is common for applications and their menu bar
  // to stay active until the user quits explicitly with Cmd + Q
    if (process.platform !== 'darwin') {
        app.quit();
    }
    if(serverProcess !== undefined) {
        serverProcess.kill();
    }
});

app.on('activate', () => {
    // On macOS it's common to re-create a window in the app when the
    // dock icon is clicked and there are no other windows open.
    if (win === null) {
        createWindow();
    }
});
