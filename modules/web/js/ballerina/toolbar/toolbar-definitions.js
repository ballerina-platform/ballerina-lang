/**
 * Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
 */

const createNewFileTool = {
    id: 'newFile',
    name: 'New',
    cssClass: 'fw fw-blank-document fw-lg',
    title: 'New Ballerina File',
    cssClassOnDiv: '',
    children: {},
    command: {
        id: 'create-new-tab',
    },
};

const openFileTool = {
    id: 'openFile',
    name: 'Open',
    cssClass: 'fw fw-folder-open fw-lg',
    title: 'Open Folder',
    cssClassOnDiv: '',
    children: {},
    command: {
        id: 'show-folder-open-dialog',
    },
};

const saveFileTool = {
    id: 'saveFile',
    name: 'Save',
    cssClass: 'fw fw-save fw-lg',
    title: 'Save File',
    cssClassOnDiv: 'section-separator',
    children: {},
    command: {
        id: 'save',
    },
};

const undoTool = {
    id: 'undo',
    name: 'Undo',
    cssClass: 'fw fw-undo fw-lg',
    title: 'Undo',
    cssClassOnDiv: '',
    children: {},
    command: {
        id: 'undo',
    },
};

const redoTool = {
    id: 'redo',
    name: 'Redo',
    cssClass: 'fw fw-redo fw-lg',
    title: 'Redo',
    cssClassOnDiv: 'section-separator',
    children: {},
    command: {
        id: 'redo',
    },
};

const startTool = {
    id: 'start',
    name: 'Start',
    cssClass: 'fw fw-start fw-lg',
    title: 'Run',
    cssClassOnDiv: '',
    getChildrenIcon: 'fw fw-sort-down more',
    children: {
        application: {
            id: 'startApplication',
            name: 'Application',
        },
        service: {
            id: 'startService',
            name: 'Service',
        },
    },
};

const debugTool = {
    id: 'debug',
    name: 'Debug',
    cssClass: 'fw fw-bug fw-lg',
    title: 'Debugger',
    cssClassOnDiv: 'section-separator',
    getChildrenIcon: 'fw fw-sort-down more',
    children: {
        application: {
            id: 'debugApplication',
            name: 'Application',
        },
        service: {
            id: 'debugService',
            name: 'Service',
        },
        remoteDebug: {
            id: 'remoteDebug',
            name: 'Debug Remotely',
        },
    },
    toolbarShortcuts: [
        {
            id: 'StepOver',
            shortcuts: {
                mac: {
                    key: 'alt+o',
                    label: 'alt+o',
                },
                other: {
                    key: 'alt+o',
                    label: 'alt+o',
                },
            },
        },
        {
            id: 'Resume',
            shortcuts: {
                mac: {
                    key: 'alt+r',
                    label: 'alt+r',
                },
                other: {
                    key: 'alt+r',
                    label: 'alt+r',
                },
            },
        },
        {
            id: 'StepIn',
            shortcuts: {
                mac: {
                    key: 'alt+i',
                    label: 'alt+i',
                },
                other: {
                    key: 'alt+i',
                    label: 'alt+i',
                },
            },
        },
        {
            id: 'StepOut',
            shortcuts: {
                mac: {
                    key: 'alt+u',
                    label: 'alt+u',
                },
                other: {
                    key: 'alt+u',
                    label: 'alt+u',
                },
            },
        },
        {
            id: 'Stop',
            shortcuts: {
                mac: {
                    key: 'alt+p',
                    label: 'alt+p',
                },
                other: {
                    key: 'alt+p',
                    label: 'alt+p',
                },
            },
        },
    ],
};


const defaultView = {
    id: 'defaultView',
    name: 'Default View',
    iconImage: 'images/default-view.svg',
    title: 'Default View',
    cssClassOnDiv: '',
    children: {},
    command: {
        id: 'diagram-mode-change',
    },
};

const actionView = {
    id: 'actionView',
    name: 'Action View',
    iconImage: 'images/action-view.svg',
    title: 'Action View',
    cssClassOnDiv: '',
    children: {},
    command: {
        id: 'diagram-mode-change',
    },
};

const compactView = {
    id: 'compactView',
    name: 'Compact View',
    iconImage: 'images/compact-view.svg',
    title: 'Compact View',
    cssClassOnDiv: '',
    children: {},
    command: {
        id: 'diagram-mode-change',
    },
};

const toolgroupArray = [
    createNewFileTool, openFileTool, saveFileTool, undoTool, redoTool,
    startTool, debugTool, defaultView, compactView, actionView];

export default toolgroupArray;
