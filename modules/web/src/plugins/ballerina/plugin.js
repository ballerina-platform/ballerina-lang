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
import { CONTRIBUTIONS } from 'core/plugin/constants';
import SourceEditor from 'ballerina/views/source-editor';
import { CLASSES } from 'ballerina/views/constants';
import Editor from './editor/Editor';
import { PLUGIN_ID, EDITOR_ID, COMMANDS as COMMAND_IDS, TOOLS as TOOL_IDS } from './constants';

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
        const { EDITORS, TOOLS } = CONTRIBUTIONS;
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
                },
            ],
        };
    }

}

export default BallerinaPlugin;
