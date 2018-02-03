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

const {BrowserWindow} = require('electron');
const registerMenuLoader = require('./menu.js');
const setupNativeWizards = require('./workspace.js');

let win;

function createWindow () {
    // Create the browser window.
    win = new BrowserWindow({width: 1024, height: 768, frame: true});

    // maximize the window
    win.maximize();

    registerMenuLoader();
    setupNativeWizards(win);

    let windowUrl = 'http://localhost:9091';

    if (process.env.NODE_ENV === 'electron-dev') {
        windowUrl = 'http://localhost:8080';
    }

    win.loadURL(windowUrl);

    if (process.env.NODE_ENV === 'electron-dev') {
        win.webContents.openDevTools();
    }

    return win;
}

module.exports = {
    createWindow,
};
