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
const url = require('url');
const path = require('path');
const electron = require('electron');
const registerMenuLoader = require('./menu.js');
const setupNativeWizards = require('./workspace.js');


let win;

function createWindow (pageURL, show = true) {

    const { width, height } = electron.screen.getPrimaryDisplay().workAreaSize;
    // Create the browser window.
    win = new electron.BrowserWindow({
        width, 
        height, 
        frame: true,
        icon: path.join(__dirname, '../icons/png/64x64.png'),
        show
    });

    //registerMenuLoader();
    //setupNativeWizards(win);

    win.loadURL(pageURL);

    if (process.env.NODE_ENV === 'electron-dev') {
        win.webContents.openDevTools();
    }

    return win;
}

function createSplashWindow() {
    // Create the browser window.
    win = new electron.BrowserWindow({
        width: 1280, 
        height: 800, 
        frame: false,
        icon: path.join(__dirname, '../icons/png/64x64.png'),
        show: true
    });
    const splashScreenUrl = url.format({
        pathname: path.join(__dirname, '..', 'pages', 'loading.html'),
        protocol: 'file:',
        slashes: true
    });
    win.loadURL(splashScreenUrl);
    return win;
}

module.exports = {
    createWindow,
    createSplashWindow,
};
