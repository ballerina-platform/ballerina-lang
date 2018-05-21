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
const { app, ipcMain, systemPreferences } = require('electron');
const path = require('path');
const os = require('os');
const process = require('process');
const fs = require('fs-extra');
const log = require('log');
const { spawn, exec } = require('child_process');
const { createWindow, createSplashWindow } = require('./app');
const { createErrorWindow } = require('./error-window');
const { ErrorCodes } = require('./error-codes');
const registerMenuLoader = require('./menu.js');

// Disable unwanted menus in mac OSX
if (process.platform === 'darwin') {
    systemPreferences.setUserDefault('NSDisabledDictationMenuItem', 'boolean', true);
    systemPreferences.setUserDefault('NSDisabledCharacterPaletteMenuItem', 'boolean', true);
}

let win,
    splashWin,
    serverProcess,
    logger = new log('info'),
    appDir = app.getAppPath(),
    logsDir = path.join(os.homedir(),'.composer', 'logs'),
    balHome = path.join(appDir, 'resources', 'ballerina-platform')
                    .replace('app.asar', 'app.asar.unpacked'),
    composerHome = path.join(balHome, 'lib', 'resources', 'composer')
    composerPublicPath = path.join(composerHome, 'web', 'public'),
    pageURL = `file://${composerPublicPath}/index.html`;

let  openFilePath = '';

function createLogger(){
    fs.ensureDirSync(logsDir);
    let accessError = fs.accessSync(logsDir, fs.W_OK);
    if(accessError) {
        logger.error('cannot write to log folder.');
    } else {
        logger = new log('info', fs.createWriteStream(path.join(logsDir, 'app.log')));
    }
}

function startServer(){
    let executable = 'java',
        args = [],
        errorWin,
        options = {},
        classpath = '';
    if (process.platform === 'win32') {
        options.windowsHide = true;
    } else {
        options.detached = true;
    }
    classpath = path.join(composerHome, 'services', '*');
    classpath = classpath  + path.delimiter + path.join(balHome, 'bre', 'lib', '*');
    // common args
    args.push('-classpath')
    args.push(classpath);
    args.push('-Dballerina.home=' + balHome);
    args.push('-Dcomposer.public.path=' + composerPublicPath);
    args.push('-Dopen.browser=false');
    args.push('org.ballerinalang.composer.server.launcher.ServerLauncher');

    logger.info('Starting composer from ' + balHome);

    // starting backend server
    serverProcess = spawn(executable, args, options);

    logger.info('Verifying whether the backend server is started successfully');
    const sucessMsgPrefix = 'Composer started successfully at ';
    serverProcess.stdout.on('data', function(data) {
        // IMPORTANT: Wait till backend server is started to create window
        if (data.includes(sucessMsgPrefix)) {
            pageURL = (data + '').substring(sucessMsgPrefix.length - 1);
            logger.info('Backend server is properly started at ' + pageURL + ', starting composer UI');
            win = createWindow(pageURL, false);
            ipcMain.once('composer-rendered', () => {
                if (splashWin) {
                    splashWin.destroy();
                }
                win.show();
                if (openFilePath) {
                    win.webContents.send('open-file', openFilePath);
                    openFilePath = undefined;
                }
            });
            win.on('closed', (evt) => {
                win = null;
            });
            win.on('close', (evt) => {
                if (process.platform === 'darwin' && !app.quitting) {
                    evt.preventDefault();
                    win.hide();
                }
            });
        } else {
            logger.info('Server Log: ' + data);
        }
    });

    serverProcess.stderr.on('data', function(data){
        let errorWindowLoaded = false,
            logsBuffer = [];
        if (!errorWin) {
            if (splashWin) {
                splashWin.destroy();
            }
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

if (process.platform == 'win32' && process.argv.length >= 2) {
    openFilePath = process.argv[1];
}

app.on('open-file', (evt, filePath) => {
    if (win) {
        if (win.isMinimized()){
            win.restore();
        }
        win.show();
        win.focus();
        win.webContents.send('open-file', filePath);
    } else {
        openFilePath = filePath;
    }
    evt.preventDefault();
});

// This method will be called when Electron has finished
// initialization and is ready to create browser windows.
// Some APIs can only be used after this event occurs.
app.on('ready', () => {
    registerMenuLoader();
    splashWin = createSplashWindow(false);
    splashWin.once('ready-to-show', () => {
        splashWin.show();
    });
    splashWin.on('closed', () => {
        splashWin = null;
    });
    createLogger();
    logger.info('verifying availability of java runtime in path');
    checkJava((error, message) => {
        if (!error) {
            logger.info('Starting composer backend services');
            startServer();
        } else {
            logger.error('Failed verifying availability of java runtime in path');
            logger.error('Error: ' + message);
            splashWin.destroy();
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
});

// kill server before quitting
app.on('before-quit', () => {
    app.quitting = true;
    logger.info('Quitting composer app');
    if (serverProcess !== undefined) {
        logger.info('kill server process with pid ' + serverProcess.pid);
        if (process.platform !== 'win32') {
            // important - pass - before pid to kill all child processes spawned by server
            process.kill(-serverProcess.pid);
        } else {
            exec('taskkill /PID ' + serverProcess.pid + ' /T /F', function (error) {
                if(error !== null) {
                    logger.error('Error while killing the server: ' + error);
                }
            });
        }
    }
});

app.on('activate', () => {
    // On macOS it's common to re-create a window in the app when the
    // dock icon is clicked and there are no other windows open.
    if (win === null) {
        win = createWindow(pageURL);
        win.on('closed', () => {
            win = null;
        });
    } else {
        win.show();
    }
});

const isSecondInstance = app.makeSingleInstance((argv, workingDirectory) => {
    // Someone tried to run a second instance, we should focus our window.
    if (win) {
        if (win.isMinimized()){
            win.restore();
        }
        win.focus();
        if (process.platform == 'win32' && argv.length >= 2) {
            win.webContents.send('open-file', argv[1]);
        }
    } else {
        if (process.platform == 'win32' && argv.length >= 2) {
            openFilePath = argv[1];
        }
    }
  });
  
if (isSecondInstance) {
    app.quit();
}