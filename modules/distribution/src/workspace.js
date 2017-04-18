const {ipcMain, dialog} = require('electron');

function setupNativeWizards() {
      ipcMain.on('show-file-open-dialog', function (event) {
          dialog.showOpenDialog({
              title: 'Open Ballerina File',
              filters: [
                  {name: 'Ballerina Files (*.bal) ', extensions: ['bal']},
              ],
              properties: ['openFile', 'promptToCreate']
            }, function (file) {
              if (file) event.sender.send('file-opened', file)
            }
          );
      });

      ipcMain.on('show-folder-open-dialog', function (event) {
          dialog.showOpenDialog({
                title: 'Open Ballerina Folder',
                properties: ['openDirectory', 'createDirectory']
            }, function (folder) {
              if (folder) event.sender.send('folder-opened', folder)
            }
          );
      });
}

module.exports = setupNativeWizards;
