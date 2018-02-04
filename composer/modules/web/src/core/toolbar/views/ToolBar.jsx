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

import React from 'react';
import _ from 'lodash';
import PropTypes from 'prop-types';
import View from './../../view/view';
import { VIEW_IDS } from './../constants';
import Tool from './../components/Tool';
import './tool-bar.scss';

/**
 * ToolBar
 */
class ToolBar extends View {

    /**
     * @inheritdoc
     */
    getID() {
        return VIEW_IDS.APP_MENU;
    }

    /**
     * @inheritdoc
     */
    render() {
        const { tools, appContext: { command: { dispatch } } } = this.props.toolBarPlugin;
        const toolsByGroup = {};
        tools.forEach((tool) => {
            if (_.isNil(toolsByGroup[tool.group])) {
                toolsByGroup[tool.group] = [];
            }
            toolsByGroup[tool.group].push(tool);
            tool.shortCutLabel = this.props.getLabelForCommand(tool.commandID);
        });
        const toolGroupComponents = _.toPairs(toolsByGroup).map((pair) => {
            return (
                <div className="tool-bar-group unselectable-content" key={pair[0]}>
                    {pair[1].map((tool) => {
                        return (
                            <Tool tool={tool} dispatch={dispatch} key={tool.id} />
                        );
                    })}
                </div>
            );
        });
        return (
            <div className="tool-bar">
                {toolGroupComponents}
            </div>
        );
    }
}

ToolBar.propTypes = {
    toolBarPlugin: PropTypes.objectOf(Object).isRequired,
    getLabelForCommand: PropTypes.func.isRequired,
};

export default ToolBar;
