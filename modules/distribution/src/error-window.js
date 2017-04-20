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
