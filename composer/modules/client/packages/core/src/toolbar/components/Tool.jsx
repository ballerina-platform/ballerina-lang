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
import cn from 'classnames';
import PropTypes from 'prop-types';
import './tool.scss';

/**
 * ToolBar
 */
class Tool extends React.Component {

    /**
     * @inheritdoc
     */
    render() {
        const { tool: { icon, commandID, commandArgs = {}, isActive = () => true, isVisible = () => true,
                        description, shortCutLabel },
                dispatch } = this.props;

        if (!isVisible()) {
            return null;
        }
        return (
            <div
                className={cn('tool-bar-tool', { active: isActive(), visible: isVisible() })}
                onClick={() => {
                    if (isActive() && isVisible()) {
                        dispatch(commandID, commandArgs);
                    }
                }}
                title={`${description} ${`${shortCutLabel ? `(${shortCutLabel})` : ''}`}`}
            >
                <i className={`fw fw-${icon}`} />
            </div>
        );
    }
}

Tool.propTypes = {
    dispatch: PropTypes.func.isRequired,
    tool: PropTypes.shape({
        id: PropTypes.string.isRequired,
        icon: PropTypes.string.isRequired,
        commandID: PropTypes.string.isRequired,
        commandArgs: PropTypes.objectOf(Object),
        isActive: PropTypes.func,
        description: PropTypes.string,
    }).isRequired,
};

export default Tool;
