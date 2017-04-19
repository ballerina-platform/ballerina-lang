const url = require("url");
const path = require("path");
const {app, BrowserWindow, Menu} = require("electron");

function createErrorWindow(args) {
        // Create the browser window.
        let win = new BrowserWindow({width: 1024, height: 768, frame: true});
        let windowUrl = url.format({
            pathname: path.join(__dirname, "..", "pages", "error.html"),
            protocol: "file:",
            slashes: true
        });
        console.log(new Buffer(args.errorMessage).toString('base64'));
        win.loadURL(windowUrl + '?errorCode=' + args.errorCode
                + '&errorMessage='
                + new Buffer(args.errorMessage).toString('base64'));
        win.webContents.openDevTools();
        return win;
}

module.exports = {createErrorWindow};
