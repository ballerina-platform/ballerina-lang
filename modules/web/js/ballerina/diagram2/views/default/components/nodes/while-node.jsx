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
import PropTypes from 'prop-types';
import _ from 'lodash';
import WhileNodeModel from 'ballerina/model/tree/while-node';
import DropZone from 'ballerina/drag-drop/DropZone';
import FragmentUtils from 'ballerina/utils/fragment-utils';
import TreeBuilder from 'ballerina/model/tree-builder';
import CompoundStatementDecorator from './compound-statement-decorator';
import './try-node.css';

class WhileNode extends React.Component {

    constructor(props) {
        super(props);
        this.setWhileCondition = this.setWhileCondition.bind(this);
        this.getWhileCondition = this.getWhileCondition.bind(this);

        this.editorOptions = {
            propertyType: 'text',
            key: 'While condition',
            model: props.model,
            getterMethod: this.getWhileCondition,
            setterMethod: this.setWhileCondition,
        };
    }

  /**
   * Set while condition.
   * @param {String} newCondition - new condition to be applied to while block.
   * */
    setWhileCondition(newCondition) {
        if (!newCondition) {
            return;
        }
        newCondition = _.trimEnd(newCondition, ';');
        const fragmentJson = FragmentUtils.createExpressionFragment(newCondition);
        const parsedJson = FragmentUtils.parseFragment(fragmentJson);
        if (!parsedJson.error) {
            const newNode = TreeBuilder.build(parsedJson);
            newNode.clearWS();
            this.props.model.setCondition(newNode.getVariable().getInitialExpression());
        }
    }

  /**
   * Get while condition
   * @return {string} condition source.
   * */
    getWhileCondition() {
        return this.props.model.getCondition().getSource();
    }
    render() {
        const model = this.props.model;
        const bBox = model.viewState.bBox;
        const expression = model.viewState.components.expression;
        const dropZone = model.viewState.components['drop-zone'];
        return (
            <g>
                <DropZone
                    x={dropZone.x}
                    y={dropZone.y}
                    width={dropZone.w}
                    height={dropZone.h}
                    baseComponent="rect"
                    dropTarget={model.parent}
                    dropBefore={model}
                    renderUponDragStart
                    enableDragBg
                    enableCenterOverlayLine
                />
                <CompoundStatementDecorator
                    dropTarget={model}
                    bBox={bBox}
                    title={'While'}
                    expression={expression}
                    editorOptions={this.editorOptions}
                    model={model}
                    body={model.body}
                />
            </g>
        );
    }
}

WhileNode.propTypes = {
    model: PropTypes.instanceOf(WhileNodeModel).isRequired,
    bBox: PropTypes.shape({
        x: PropTypes.number.isRequired,
        y: PropTypes.number.isRequired,
        w: PropTypes.number.isRequired,
        h: PropTypes.number.isRequired,
    }),
};

WhileNode.contextTypes = {
    mode: PropTypes.string,
};

export default WhileNode;
