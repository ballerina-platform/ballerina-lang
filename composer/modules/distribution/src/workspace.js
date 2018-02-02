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

function setupNativeWizards(mainWindow) {
    ipcMain.on('show-file-open-dialog', function (event) {
        dialog.showOpenDialog(
          mainWindow,
            {
                title: 'Open Ballerina File',
                filters: [
                    {name: 'Ballerina Files (*.bal) ', extensions: ['bal']},
                ],
                properties: ['openFile', 'promptToCreate']
            }, function (file) {
                if (file) event.sender.send('file-opened', file);
            }
        );
    });

    ipcMain.on('show-file-save-dialog', function (event) {
        dialog.showSaveDialog(
          mainWindow,
            {
                title: 'Save Ballerina File',
                filters: [
                    {name: 'Ballerina Files (*.bal) ', extensions: ['bal']},
                ]
            }, function (file) {
                if (file) event.sender.send('file-save-path-selected', file);
            }
        );
    });

    ipcMain.on('show-folder-open-dialog', function (event) {
        dialog.showOpenDialog(
          mainWindow,
            {
                title: 'Open Ballerina Folder',
                properties: ['openDirectory', 'createDirectory']
            }, function (folder) {
                if (folder) event.sender.send('folder-opened', folder);
            }
        );
    });
}

module.exports = setupNativeWizards;
