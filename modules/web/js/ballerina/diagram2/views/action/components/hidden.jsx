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
import React from 'react';
import _ from 'lodash';
import './hidden.css';

/**
 * Add an mark to hidden elements.
 */
class Hidden extends React.Component {

    /**
     *
     * @param {object} props - Init props.
     * Initialize the statement decorator.
     */
    constructor(props) {
        super();
        this.designer = _.get(props, 'designer');
        this.mode = _.get(props, 'mode');
    }

    /**
     * Override the rendering logic.
     * @returns {XML} rendered component.
     */
    render() {
        const viewState  = this.props.model.viewState;
        const bBox = viewState.bBox;
        const designer = this.props.designer;

        // calculate the bBox for the statement
        const textX = bBox.x + bBox.w/2 - 7;
        const textY = bBox.y + 20;
        return (
            <g>
                <g className="statement-body">
                    <rect x={textX - 30} y={textY - 15} width="80" height="15" style={{ fill: '#FFF', strokeWidth: 0 }} />
                    <text x={textX - 30} y={textY - 3} className="hidden-code">
                        Hidden Code
                    </text>
                </g>
            </g>);
    }

}

export default Hidden;
