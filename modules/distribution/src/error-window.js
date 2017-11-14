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
const {BrowserWindow} = require('electron');

function createErrorWindow(args) {
    // Create the browser window.
    let win = new BrowserWindow({width: 600, height: 500,
        resizable: false, frame: true});
    let windowUrl = url.format({
        pathname: path.join(__dirname, '..', 'pages', 'error.html'),
        protocol: 'file:',
        slashes: true
    });
    win.loadURL(windowUrl + '?errorCode=' + args.errorCode
            + '&errorMessage='
            + new Buffer(args.errorMessage).toString('base64'));
    return win;
}

module.exports = {createErrorWindow};
