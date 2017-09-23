import _ from 'lodash';
import { listFiles } from 'api-client/api-client';
import { COMMANDS as WORKSPACE_CMDS } from './../../workspace/constants';
import { EDIT_TYPES } from './TreeNode';

/**
 * Creates the context menu items for given node type
 * @param {Object} node root, folder or file node
 * @param {Object} command Command API
 */
export function getContextMenuItems(node, command, onNodeUpdate = () => {}) {
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
            const createNewFileNode = (targetNode) => {
                const tempNode = {
                    id: 'temp/file.bal',
                    type: 'file',
                    enableEdit: true,
                    editType: EDIT_TYPES.NEW,
                    label: '',
                };
                targetNode.children.splice(0, 0, tempNode);
                targetNode.collapsed = false;
                onNodeUpdate(targetNode);
            };
            // if node children are not loaded yet
            if (_.isBoolean(node.children)) {
                // empty folder
                if (!node.children) {
                    node.children = [];
                    createNewFileNode(node);
                } else {
                    // folder contains files that are not loaded yet
                    node.loading = true;
                    onNodeUpdate(node);
                    listFiles(node.id, ['.bal'])
                        .then((data) => {
                            node.loading = false;
                            node.children = _.isEmpty(data) ? [] : data;
                            createNewFileNode(node);
                        });
                }
            } else {
                createNewFileNode(node);
            }
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
            const createNewFileNode = (targetNode) => {
                const tempNode = {
                    id: 'temp/temp',
                    type: 'folder',
                    enableEdit: true,
                    editType: EDIT_TYPES.NEW,
                    label: '',
                };
                targetNode.children.splice(0, 0, tempNode);
                targetNode.collapsed = false;
                onNodeUpdate(targetNode);
            };
            // if node children are not loaded yet
            if (_.isBoolean(node.children)) {
                // empty folder
                if (!node.children) {
                    node.children = [];
                    createNewFileNode(node);
                } else {
                    // folder contains files that are not loaded yet
                    node.loading = true;
                    onNodeUpdate(node);
                    listFiles(node.id, ['.bal'])
                        .then((data) => {
                            node.loading = false;
                            node.children = _.isEmpty(data) ? [] : data;
                            createNewFileNode(node);
                        });
                }
            } else {
                createNewFileNode(node);
            }
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
            node.enableEdit = true;
            node.editType = EDIT_TYPES.RENAME;
            onNodeUpdate(node);
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
