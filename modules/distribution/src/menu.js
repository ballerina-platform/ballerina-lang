const {ipcMain, Menu} = require('electron');

function registerMenuLoader() {
    // remove the stock menu
    Menu.setApplicationMenu(Menu.buildFromTemplate([]));

    ipcMain.on('main-menu-loaded', (event, menu) => {
        _addClickHandlers(menu, event.sender);
        Menu.setApplicationMenu(Menu.buildFromTemplate(menu));
    });
}

function _addClickHandlers(menus, webContents) {
    Object.keys(menus).forEach(menuId => {
        const menu = menus[menuId];
        _addClickHandler(menu, webContents);
    });
}

function _addClickHandler(menuItem, webContents) {
    if(menuItem.submenu){
        menuItem.submenu.map(subItem => {
            return _addClickHandler(subItem, webContents);
        });
    }

    if(menuItem.commandId){
        var commandId = menuItem.commandId;
        menuItem.click = () => {
            webContents.send('menu-item-clicked', commandId);
        };
        delete menuItem.commandId;
    }
}

module.exports = registerMenuLoader;
