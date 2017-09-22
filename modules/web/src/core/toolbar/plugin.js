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
import log from 'log';
import _ from 'lodash';
import Plugin from './../plugin/plugin';
import { CONTRIBUTIONS } from './../plugin/constants';

import { REGIONS } from './../layout/constants';

import { getCommandDefinitions } from './commands';
import { getHandlerDefinitions } from './handlers';
import { PLUGIN_ID, VIEW_IDS } from './constants';

import ToolBar from './views/ToolBar';

/**
 * ToolBarPlugin is responsible for rendering tool bar.
 *
 * @class ToolBarPlugin
 */
class ToolBarPlugin extends Plugin {

    /**
     * @inheritdoc
     */
    constructor() {
        super();
        this.tools = [];
    }

    /**
     * @inheritdoc
     */
    getID() {
        return PLUGIN_ID;
    }

    /**
     * Add a tool to tool bar.
     *
     * @param {Object} toolDef Tool Definition
     */
    addTool(toolDef) {
        if (!_.isNil(_.find(this.tools, ['id', toolDef.id]))) {
            log.error('Duplicate tool-definition for tool ' + toolDef.id);
        } else {
            this.tools.push(_.cloneDeep(toolDef));
        }
    }


    /**
     * @inheritdoc
     */
    getContributions() {
        const { COMMANDS, HANDLERS, VIEWS } = CONTRIBUTIONS;
        return {
            [COMMANDS]: getCommandDefinitions(this),
            [HANDLERS]: getHandlerDefinitions(this),
            [VIEWS]: [
                {
                    id: VIEW_IDS.TOOL_BAR,
                    component: ToolBar,
                    propsProvider: () => {
                        return {
                            toolBarPlugin: this,
                        };
                    },
                    region: REGIONS.TOOL_AREA,
                    displayOnLoad: true,
                },
            ],
        };
    }
}

export default ToolBarPlugin;
