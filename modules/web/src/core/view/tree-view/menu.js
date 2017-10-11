import _ from 'lodash';
import log from 'log';
import copy from 'copy-to-clipboard';
import { listFiles } from 'api-client/api-client';
import { COMMANDS as WORKSPACE_CMDS, DIALOGS as WORKSPACE_DIALOGS } from './../../workspace/constants';
import { COMMANDS as LAYOUT_COMMANDS } from './../../layout/constants';
import { EDIT_TYPES } from './TreeNode';

import { remove } from './../../workspace/fs-util';

/**
 * Creates the context menu items for given node type
 * @param {Object} node root, folder or file node
 * @param {Object} parentNode
 * @param {Object} command Command API
 * @param {function} onNodeUpdate
 * @param {function} onNodeRefresh
 */
export function getContextMenuItems(node, parentNode, command, onNodeUpdate = () => {}, onNodeRefresh = () => {}) {
    const menu = [];
    const { dispatch } = command;
    const { type } = node;

    const menuDivider = {
        divider: true,
    };

    const copyPathMenu = {
        icon: '',
        label: 'Copy Path',
        handler: () => {
            copy(node.id);
        },
        isActive: () => {
            return true;
        },
        children: [],
    };

    const renameMenu = {
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
    };

    const deleteMenu = {
        icon: 'delete',
        label: 'Delete',
        handler: () => {
            dispatch(LAYOUT_COMMANDS.POPUP_DIALOG, {
                id: WORKSPACE_DIALOGS.DELETE_FILE_CONFIRM,
                additionalProps: {
                    filePath: node.id,
                    onConfirm: () => {
                        remove(node.id)
                            .then(() => {
                                onNodeRefresh(parentNode);
                            })
                            .catch((err) => {
                                log.error(err);
                            });
                    },
                },
            });
        },
        isActive: () => {
            return true;
        },
        children: [],
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
                    parent: targetNode.id,
                };
                // first expand the tree if in collapsed state
                if (node.collapsed) {
                    node.collapsed = false;
                    onNodeUpdate(node);
                }
                targetNode.children.splice(0, 0, tempNode);
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
                    parent: targetNode.id,
                };
                // first expand the tree if in collapsed state
                if (node.collapsed) {
                    node.collapsed = false;
                    onNodeUpdate(node);
                }
                targetNode.children.splice(0, 0, tempNode);
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
            menu.push(menuDivider);
            menu.push(copyPathMenu);
            break;
        }
        case 'folder': {
            menu.push(newFileMenu);
            menu.push(newFolderMenu);
            menu.push(menuDivider);
            menu.push(copyPathMenu);
            menu.push(menuDivider);
            menu.push(renameMenu);
            menu.push(deleteMenu);
            break;
        }
        case 'file': {
            menu.push({
                icon: '',
                label: 'Open In Editor',
                handler: () => {
                    dispatch(WORKSPACE_CMDS.OPEN_FILE, { filePath: node.id });
                },
                isActive: () => {
                    return true;
                },
                children: [],
            });
            menu.push(menuDivider);
            menu.push(copyPathMenu);
            menu.push(menuDivider);
            menu.push(renameMenu);
            menu.push(deleteMenu);
            break;
        }
        default:
    }
    return menu;
}
