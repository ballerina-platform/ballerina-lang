/**
 * Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

import React from 'react';
import PropTypes from 'prop-types';
import ArrowDecorator from '../decorators/arrow-decorator';
import TreeUtil from '../../../../../model/tree-util';

/**
 * Endpoint Node Decorator.
 */
class AwaitResponseNode extends React.Component {

    /**
     * Constructor for connector declaration
     * @param {objects} props - properties
     */
    constructor(props) {
        super();
    }


    /**
     * Render Function for the Connector Declaration Decorator
     * */
    render() {
        const { designer } = this.context;
        const viewState = this.props.model.viewState;
        const backwardArrowStart = {};
        const backwardArrowEnd = {};

        if (!TreeUtil.findCompatibleStart(this.props.model)) {
            return <g />;
        }

        backwardArrowStart.x = viewState.components.response.start;
        backwardArrowStart.y = viewState.bBox.y + designer.config.statement.height;
        backwardArrowEnd.x = viewState.bBox.x;
        backwardArrowEnd.y = viewState.bBox.y + designer.config.statement.height;

        return (
            <g>
                <text
                    x={viewState.bBox.x +
                        designer.config.statement.gutter.h}
                    y={viewState.bBox.y + designer.config.statement.height - 5}
                >
                    {viewState.expression}
                </text>
                <ArrowDecorator
                    start={backwardArrowStart}
                    end={backwardArrowEnd}
                    dashed
                />
            </g>
        );
    }
}

AwaitResponseNode.propTypes = {
    model: PropTypes.instanceOf(Object).isRequired,
};

AwaitResponseNode.contextTypes = {
    designer: PropTypes.instanceOf(Object),
};


export default AwaitResponseNode;
