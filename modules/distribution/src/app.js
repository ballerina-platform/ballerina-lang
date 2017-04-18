const url = require("url");
const path = require("path");
const {app, BrowserWindow, Menu} = require("electron");
const registerMenuLoader = require("./menu.js");

let win;

function createWindow () {
  // Create the browser window.
  win = new BrowserWindow({width: 1024, height: 768, frame: true});

  // maximize the window
  win.maximize();

  registerMenuLoader();

  let windowUrl = url.format({
    pathname: path.join(__dirname, "..", "resources", "composer", "web", "index.html"),
    protocol: "file:",
    slashes: true
  });

  if (process.env.NODE_ENV === 'electron-dev') {
      windowUrl = 'http://localhost:8080'
  }

  win.loadURL(windowUrl);

  if (process.env.NODE_ENV === 'electron-dev') {
      win.webContents.openDevTools();
  }

  return win;
}

module.exports = {
    createWindow,
}
