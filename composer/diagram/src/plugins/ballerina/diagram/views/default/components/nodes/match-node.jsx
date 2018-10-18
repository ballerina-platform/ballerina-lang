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
 * @class MatchNode
 * */
class MatchNode extends React.Component {

    /**
     * Constructor for MatchNode class
     * @param {Object} props - properties passed in to the class at creation.
     * */
    constructor(props) {
        super(props);
    }


    render() {
        const model = this.props.model;
        const bBox = model.viewState.bBox;
        const { designer } = this.context;

        const statementRectClass = 'compound-statement-rect';

        const pX1 = bBox.x - bBox.leftMargin;

        const width = bBox.w + bBox.leftMargin;

        const patternClauses = getComponentForNodeArray(this.props.model.patternClauses);
        return (
            <g>
                <rect
                    x={pX1}
                    y={bBox.y}
                    width={width}
                    height={bBox.h}
                    className={statementRectClass}
                    rx='5'
                    ry='5'
                />
                <text
                    x={pX1 + designer.config.compoundStatement.text.padding}
                    y={bBox.y + designer.config.compoundStatement.padding.top}
                    className='statement-title-text-left'
                >
                    match
                </text>
                <text
                    x={bBox.x + designer.config.compoundStatement.text.padding}
                    y={bBox.y + designer.config.compoundStatement.padding.top}
                    className='match-expression'
                >
                    {model.viewState.components.expression.text}
                </text>
                {patternClauses}
            </g>
        );
    }
}

MatchNode.propTypes = {
    model: PropTypes.instanceOf(Object).isRequired,
};

MatchNode.contextTypes = {
    mode: PropTypes.string,
    designer: PropTypes.instanceOf(Object),
};

export default MatchNode;
