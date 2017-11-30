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
 */

import Plugin from 'core/plugin/plugin';
import { parseFile } from 'api-client/api-client';
import { CONTRIBUTIONS } from 'core/plugin/constants';
import { REGIONS, COMMANDS as LAYOUT_COMMANDS } from 'core/layout/constants';
import { EVENTS as WORKSPACE_EVENTS } from 'core/workspace/constants';
import SourceEditor from 'plugins/ballerina/views/source-editor';
import { CLASSES } from 'plugins/ballerina/views/constants';
import Document from 'plugins/ballerina/docerina/document.jsx';
import Editor from './views/editor-wrapper';
import { PLUGIN_ID, EDITOR_ID, DOC_VIEW_ID, COMMANDS as COMMAND_IDS, TOOLS as TOOL_IDS,
            DIALOGS as DIALOG_IDS } from './constants';
import OpenProgramDirConfirmDialog from './dialogs/OpenProgramDirConfirmDialog';
import { getLangServerClientInstance } from './langserver/lang-server-client-controller';

/**
 * Plugin for Ballerina Lang
 */
class BallerinaPlugin extends Plugin {


    /**
     * @inheritdoc
     */
    getID() {
        return PLUGIN_ID;
    }

    /**
     * @inheritdoc
     */
    getContributions() {
        const { EDITORS, TOOLS, VIEWS, HANDLERS, DIALOGS } = CONTRIBUTIONS;
        return {
            [EDITORS]: [
                {
                    id: EDITOR_ID,
                    extension: 'bal',
                    component: Editor,
                    customPropsProvider: () => {
                        return {
                            ballerinaPlugin: this,
                        };
                    },
                    previewView: {
                        component: SourceEditor,
                        customPropsProvider: () => {
                            return {
                                parseFailed: false,
                            };
                        },
                    },
                    tabTitleClass: CLASSES.TAB_TITLE.DESIGN_VIEW,
                },
            ],
            [TOOLS]: [
                {
                    id: TOOL_IDS.DEFAULT_VIEWS,
                    group: TOOL_IDS.GROUP,
                    icon: 'default-view',
                    commandID: COMMAND_IDS.DIAGRAM_MODE_CHANGE,
                    commandArgs: { mode: 'default' },
                    isActive: () => {
                        const { editor } = this.appContext;
                        const activeEditor = editor.getActiveEditor();
                        return (activeEditor && activeEditor.file);
                    },
                    description: 'Default View',
                },
                {
                    id: TOOL_IDS.ACTION_VIEW,
                    group: TOOL_IDS.GROUP,
                    icon: 'action-view',
                    commandID: COMMAND_IDS.DIAGRAM_MODE_CHANGE,
                    commandArgs: { mode: 'action' },
                    isActive: () => {
                        const { editor } = this.appContext;
                        const activeEditor = editor.getActiveEditor();
                        return (activeEditor && activeEditor.file);
                    },
                    description: 'Action View',
                },
                {
                    id: TOOL_IDS.COMPACT_VIEW,
                    group: TOOL_IDS.GROUP,
                    icon: 'compact-view',
                    commandID: COMMAND_IDS.DIAGRAM_MODE_CHANGE,
                    commandArgs: { mode: 'compact' },
                    isActive: () => {
                        const { editor } = this.appContext;
                        const activeEditor = editor.getActiveEditor();
                        return (activeEditor && activeEditor.file);
                    },
                    description: 'Compact View',
                },
            ],
            [VIEWS]: [
                {
                    id: DOC_VIEW_ID,
                    component: Document,
                    propsProvider: () => {
                        return {};
                    },
                    region: REGIONS.EDITOR_TABS,
                    regionOptions: {
                        tabTitle: ({ packageName }) => `${packageName} docs`,
                        customTitleClass: CLASSES.TAB_TITLE.DESIGN_VIEW,
                    },
                    displayOnLoad: false,
                },
            ],
            [HANDLERS]: [
                {
                    cmdID: WORKSPACE_EVENTS.FILE_OPENED,
                    handler: ({ file }) => {
                        getLangServerClientInstance()
                            .then((langServerClient) => {
                                langServerClient.documentDidOpenNotification({
                                    uri: file.fullPath,
                                    text: file.content,
                                });
                            });
                        parseFile(file)
                            .then(({ programDirPath = undefined }) => {
                                const { workspace, command: { dispatch } } = this.appContext;
                                if (programDirPath && !workspace.isFilePathOpenedInExplorer(programDirPath)) {
                                    dispatch(LAYOUT_COMMANDS.POPUP_DIALOG, {
                                        id: DIALOG_IDS.OPEN_PROGRAM_DIR_CONFIRM,
                                        additionalProps: {
                                            file,
                                            programDirPath,
                                            onConfirm: () => {
                                                workspace.openFolder(programDirPath);
                                            },
                                        },
                                    });
                                }
                            });
                    },
                },
                {
                    cmdID: WORKSPACE_EVENTS.FILE_UPDATED,
                    handler: ({ file }) => {
                        getLangServerClientInstance()
                            .then((langServerClient) => {
                                langServerClient.documentDidChangeNotification({
                                    uri: file.fullPath,
                                    text: file.content,
                                });
                            });
                    },
                },
                {
                    cmdID: WORKSPACE_EVENTS.FILE_SAVED,
                    handler: ({ file }) => {
                        getLangServerClientInstance()
                            .then((langServerClient) => {
                                langServerClient.documentDidSaveNotification({
                                    uri: file.fullPath,
                                    text: file.content,
                                });
                            });
                    },
                },
                {
                    cmdID: WORKSPACE_EVENTS.FILE_CLOSED,
                    handler: ({ file }) => {
                        getLangServerClientInstance()
                            .then((langServerClient) => {
                                langServerClient.documentDidCloseNotification({
                                    uri: file.fullPath,
                                });
                            });
                    },
                },
            ],
            [DIALOGS]: [
                {
                    id: DIALOG_IDS.OPEN_PROGRAM_DIR_CONFIRM,
                    component: OpenProgramDirConfirmDialog,
                    propsProvider: () => {
                        return {
                            workspaceManager: this,
                        };
                    },
                },
            ],
        };
    }

}

export default BallerinaPlugin;
