export const COMMANDS = {
    CREATE_NEW_FILE: 'create-new-file',
    OPEN_FILE: 'open-file',
    OPEN_FOLDER: 'open-folder',
    REMOVE_FOLDER: 'remove-folder',
    SAVE_FILE: 'save-file',
    SAVE_FILE_AS: 'save-as-file',
    SHOW_FILE_OPEN_WIZARD: 'show-file-open-wizard',
    SHOW_FOLDER_OPEN_WIZARD: 'show-folder-open-wizard',
};

export const EVENTS = {
    FILE_UPDATED: 'file-updated',
    DIRTY_STATE_CHANGE: 'dirty-state-change',
    CONTENT_MODIFIED: 'content-modified',
};

export const VIEWS = {
    EXPLORER: 'composer.view.workspace.explorer',
    EXPLORER_OPEN_ACTIVTY: 'composer.view.workspace.explorer.open.activity',
};

export const MENUS = {
    FILE_MENU: 'composer.menu.file',
    NEW_FILE: 'composer.menu.workspace.new-file',
    SHOW_FILE_OPEN_WIZARD: 'composer.menu.workspace.show-file-open-wizard',
    SHOW_FOLDER_OPEN_WIZARD: 'composer.menu.workspace.show-folder-open-wizard',
    SAVE_FILE: 'composer.menu.workspace.save-file',
    SAVE_FILE_AS: 'composer.menu.workspace.save-file-as',
};

export const TOOLS = {
    GROUP: 'composer.tool.group.file',
    NEW_FILE: 'composer.tool.workspace.new-file',
    SAVE_FILE: 'composer.tool.workspace.save-file',
    OPEN_FILE: 'composer.tool.workspace.open-file',
};

export const LABELS = {
    NEW_FILE: 'New File',
    SAVE: 'Save',
    SAVE_AS: 'Save As',
    FILE: 'File',
    SHOW_FILE_OPEN_WIZARD: 'Open File',
    SHOW_FOLDER_OPEN_WIZARD: 'Open Program Directory',
};

export const DIALOGS = {
    SAVE_FILE: 'composer.dialog.save-file',
    OPEN_FILE: 'composer.dialog.open-file',
    OPEN_FOLDER: 'composer.dialog.open-folder',
};

export const HISTORY = {
    OPENED_FILES: 'composer.workspace.opened-files',
    OPENED_FOLDERS: 'composer.workspace.opened-folders',
};

export const PLUGIN_ID = 'composer.plugin.workspace.manager';
