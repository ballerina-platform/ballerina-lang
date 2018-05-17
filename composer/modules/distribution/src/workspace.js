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

const {ipcMain, dialog} = require('electron');

const extensions = [
    { name: 'Ballerina files', extensions: ['bal']}, 
    { name: 'XML files ', extensions: ['xml']},
    { name: 'JSON files', extensions: ['json']}, 
    { name: 'TOML files', extensions: ['toml']}, 
    { name: 'Conf files', extensions: ['conf']},
    { name: 'Text files', extensions: ['txt']}, 
    { name: 'YAML files', extensions: ['yml']}, 
    { name: 'Markdown files', extensions: ['md']},
    { name: 'SQL files', extensions: ['sql']}, 
    { name: 'Shell scripts', extensions: ['sh']}, 
    { name: 'Bat scripts', extensions: ['bat']}
];

function setupNativeWizards(mainWindow) {
    ipcMain.on('show-file-open-dialog', function (event) {
        dialog.showOpenDialog(
          mainWindow,
            {
                title: 'Open Ballerina File',
                message: 'choose ballerina file to open',
                filters: extensions,
                properties: ['openFile', 'promptToCreate']
            }, function (file) {
                event.sender.send('file-open-wizard-closed', file)
            }
        );
    });

    ipcMain.on('show-file-save-dialog', function (event) {
        dialog.showSaveDialog(
          mainWindow,
            {
                title: 'Save Ballerina File',
                message: 'select where to save the ballerina file',
                filters: extensions
            }, function (file) {
                event.sender.send('file-save-wizard-closed', file);
            }
        );
    });

    ipcMain.on('show-folder-open-dialog', function (event) {
        dialog.showOpenDialog(
          mainWindow,
            {
                title: 'Open Ballerina Folder',
                message: 'select a ballerina project folder',
                properties: ['openDirectory', 'createDirectory']
            }, function (folders) {
                event.sender.send('folder-open-wizard-closed', folders ? folders[0] : undefined);
            }
        );
    });
}

module.exports = setupNativeWizards;
