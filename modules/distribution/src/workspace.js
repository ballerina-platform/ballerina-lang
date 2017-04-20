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
