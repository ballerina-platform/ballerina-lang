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
