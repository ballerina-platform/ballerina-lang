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
import { getComponentForNodeArray } from './../../../../diagram-util';
/**
 * Class for match node
 * @extends React.Component
 * @class MatchPatternClauseNode
 * */
class MatchPatternClauseNode extends React.Component {

    /**
     * Constructor for MatchPatternClauseNode class
     * @param {Object} props - properties passed in to the class at creation.
     * */
    constructor(props) {
        super(props);
    }


    render() {
        const model = this.props.model;
        const bBox = model.viewState.bBox;
        const parentBBox = model.parent.viewState.bBox;

        const x1 = parentBBox.x - model.parent.viewState.components['left-margin'].w;
        const y1 = bBox.y;
        const x2 = x1 + parentBBox.w;
        const y2 = bBox.y;

        const statement = getComponentForNodeArray(this.props.model.statement);

        return (
            <g>
                <text
                    x={x1}
                    y={y1}
                    className='match-text'
                >
                    true
                </text>
                <line x1={x1} y1={y1} x2={x2} y2={y2} className='compound-statement-rect' />
                {statement}
            </g>
        );
    }
}

MatchPatternClauseNode.propTypes = {
    model: PropTypes.instanceOf(Object).isRequired,
};

MatchPatternClauseNode.contextTypes = {
    mode: PropTypes.string,
};

export default MatchPatternClauseNode;
