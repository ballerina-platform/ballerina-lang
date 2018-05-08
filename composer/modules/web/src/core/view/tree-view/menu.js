/**
 * Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 *
 */

import _ from 'lodash';
import log from 'log';
import copy from 'copy-to-clipboard';
import { listFiles } from 'api-client/api-client';
import { COMMANDS as WORKSPACE_CMDS, DIALOGS as WORKSPACE_DIALOGS } from './../../workspace/constants';
import { COMMANDS as LAYOUT_COMMANDS } from './../../layout/constants';
import { DIALOGS as EDITOR_DIALOGS } from './../../editor/constants';
import { EDIT_TYPES } from './TreeNode';

import { remove } from './../../workspace/fs-util';

/**
 * Creates the context menu items for given node type
 * @param {Object} node root, folder or file node
 * @param {Object} parentNode
 * @param {Object} command Command API
 * @param {function} onNodeUpdate
 * @param {function} onNodeRefresh
 * @param {Object} reactContext
 */
export function getContextMenuItems(node, parentNode, command,
        onNodeUpdate = () => {}, onNodeRefresh = () => {}, reactContext) {
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
            const { editor } = reactContext;
            const prepareNodeForRename = () => {
                node.enableEdit = true;
                node.editType = EDIT_TYPES.RENAME;
                onNodeUpdate(node);
            };
            if (editor.isFileOpenedInEditor(node.id)) {
                const targetEditor = editor.getEditorByID(node.id);
                if (targetEditor.isDirty) {
                    let canContinueRename = false;
                    let saveBeforeRename = false;
                    dispatch(LAYOUT_COMMANDS.POPUP_DIALOG, {
                        id: EDITOR_DIALOGS.DIRTY_CLOSE_CONFIRM,
                        additionalProps: {
                            file: targetEditor.file,
                            onConfirm: () => {
                                canContinueRename = true;
                            },
                            onSave: () => {
                                canContinueRename = true;
                                saveBeforeRename = true;
                            },
                            onAfterHide: () => {
                                if (canContinueRename) {
                                    if (saveBeforeRename) {
                                        dispatch(WORKSPACE_CMDS.SAVE_FILE, {
                                            file: targetEditor.file,
                                            onSaveSuccess: prepareNodeForRename,
                                        });
                                    } else {
                                        prepareNodeForRename();
                                    }
                                }
                            },
                        },
                    });
                } else {
                    prepareNodeForRename();
                }
            } else {
                prepareNodeForRename();
            }
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
            const { editor } = reactContext;
            const deleteFile = () => {
                remove(node.id)
                    .then(() => {
                        onNodeRefresh(parentNode);
                    })
                    .catch((err) => {
                        log.error(err);
                    });
            };
            if (editor.isFileOpenedInEditor(node.id)) {
                const targetEditor = editor.getEditorByID(node.id);
                dispatch(LAYOUT_COMMANDS.POPUP_DIALOG, {
                    id: EDITOR_DIALOGS.OPENED_FILE_DELETE_CONFIRM,
                    additionalProps: {
                        file: targetEditor.file,
                        onConfirm: () => {
                            editor.closeEditor(targetEditor);
                            deleteFile();
                        },
                    },
                });
            } else {
                dispatch(LAYOUT_COMMANDS.POPUP_DIALOG, {
                    id: WORKSPACE_DIALOGS.DELETE_FILE_CONFIRM,
                    additionalProps: {
                        isFolder: node.type === 'folder',
                        target: node.label,
                        onConfirm: deleteFile,
                    },
                });
            }
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
                label: 'Remove Project Directory',
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
