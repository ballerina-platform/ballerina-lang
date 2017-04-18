const {ipcMain, dialog} = require('electron');

function setupNativeWizards() {
      ipcMain.on('show-file-open-dialog', function (event) {
          dialog.showOpenDialog({
              properties: ['openFile']
            }, function (file) {
              if (file) event.sender.send('file-opened', file)
            }
          );
      });

      ipcMain.on('show-folder-open-dialog', function (event) {
          dialog.showOpenDialog({
              properties: ['openFolder']
            }, function (folder) {
              if (folder) event.sender.send('folder-opened', folder)
            }
          );
      });
}

module.exports = setupNativeWizards;
