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
import BlockStatementDecorator from './block-statement-decorator';
import CompoundStatementDecorator from './compound-statement-decorator';
import { statement, blockStatement } from './../configs/designer-defaults';
import { getComponentForNodeArray } from './utils';
import SimpleBBox from './../ast/simple-bounding-box';
import ForkJoinStatementAST from './../ast/statements/fork-join-statement';

/**
 * React UI component to represent the the fork section and contain the timeout
 * and join children of the fork-join language construct.
 */
class ForkJoinStatement extends React.Component {

    /**
     * Is the node a worker. This is used to identify the node under drag, second arg (target) is not used.
     * @param {ASTNode} node element to be tested.
     * @returns {Boolean} true if the node is a worker.
     */
    static isWorker(node) {
        const factory = node.getFactory();
        return factory.isWorkerDeclaration(node);
    }

    /**
     * Override the rendering logic.
     * @returns {XML} rendered component.
     */
    render() {
        const model = this.props.model;
        const bBox = model.viewState.bBox;
        const bodyBBox = model.viewState.components.body;
        const children = getComponentForNodeArray(this.props.model.getChildren());

        const forkBBox = new SimpleBBox(bBox.x, bBox.y + statement.gutter.v, bBox.w, bodyBBox.h
            + blockStatement.heading.height);
        const hiderTop = bBox.y + blockStatement.heading.height + statement.gutter.v + 1;
        return (<CompoundStatementDecorator model={model} bBox={bBox}>
          <line
            x1={bBox.getCenterX()}
            y1={hiderTop - 1}
            x2={bBox.getCenterX()}
            y2={bBox.getBottom()}
            className="life-line-hider"
          />
          <BlockStatementDecorator
            model={model}
            dropTarget={model}
            bBox={forkBBox}
            title={'Fork'}
            draggable={ForkJoinStatement.isWorker}
          >
            {children}
          </BlockStatementDecorator>
        </CompoundStatementDecorator>);
    }
}

ForkJoinStatement.propTypes = {
    model: PropTypes.instanceOf(ForkJoinStatementAST).isRequired,
};


export default ForkJoinStatement;
