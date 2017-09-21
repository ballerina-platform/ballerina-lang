import { COMMANDS as WORKSPACE_CMDS } from './../../workspace/constants';

/**
 * Creates the context menu items for given node type
 * @param {Object} node root, folder or file node
 * @param {Object} command Command API
 */
export function getContextMenuItems(node, command) {
    const menu = [];
    const { dispatch } = command;
    const { type } = node;

    const menuDivider = {
        divider: true,
    };

    const newFileMenu = {
        icon: '',
        label: 'New File',
        handler: () => {
            const tempNode = {
                id: 'temp/file.bal',
                type: 'file',
                label: 'ssssssssssssssss',

            };
        },
        isActive: () => {
            return true;
        },
        children: [],
    };

    const newFolderMenu = {
        icon: '',
        label: 'New Folder',
        handler: () => {
        },
        isActive: () => {
            return true;
        },
        children: [],
    };

    switch (type) {
        case 'root': {
            menu.push({
                icon: '',
                label: 'Remove Program Directory',
                handler: () => {
                    dispatch(WORKSPACE_CMDS.REMOVE_FOLDER, { folderPath: node.id });
                },
                isActive: () => {
                    return true;
                },
                children: [],
            });
            menu.push(menuDivider);
            menu.push(newFileMenu);
            menu.push(newFolderMenu);
            break;
        }
        case 'folder': {
            menu.push(newFileMenu);
            menu.push(newFolderMenu);
            break;
        }
        case 'file': {
            menu.push({
                icon: '',
                label: 'Open In Editor',
                handler: () => {
                },
                isActive: () => {
                    return true;
                },
                children: [],
            });
            break;
        }
        default:
    }
    menu.push(menuDivider);
    menu.push({
        icon: '',
        label: 'Rename',
        handler: () => {
        },
        isActive: () => {
            return true;
        },
        children: [],
    });
    menu.push(menuDivider);
    menu.push({
        icon: 'delete',
        label: 'Delete',
        handler: () => {
        },
        isActive: () => {
            return true;
        },
        children: [],
    });
    return menu;
}
