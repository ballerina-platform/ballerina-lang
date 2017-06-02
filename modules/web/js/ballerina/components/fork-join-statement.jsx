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
import BlockStatementDecorator from './block-statement-decorator';
import CompoundStatementDecorator from './compound-statement-decorator';
import PropTypes from 'prop-types';
import {statement, blockStatement} from './../configs/designer-defaults';
import {getComponentForNodeArray} from './utils';
import SimpleBBox from './../ast/simple-bounding-box';

class ForkJoinStatement extends React.Component {

    render() {
        let model = this.props.model,
            bBox = model.viewState.bBox,
            bodyBBox = model.viewState.components.body;
        const children = getComponentForNodeArray(this.props.model.getChildren());

        const forkBBox = new SimpleBBox(bBox.x, bBox.y + statement.gutter.v, bBox.w, bodyBBox.h
            + blockStatement.heading.height);
        const hiderTop = bBox.y + blockStatement.heading.height + statement.gutter.v + 1;
        return (<CompoundStatementDecorator model={model} bBox={bBox}>
            <BlockStatementDecorator model={model} dropTarget={model} bBox={forkBBox}
                                     undeletable={true}
                                     title={'Fork'} draggable={ForkJoinStatement.isWorker}>
                <line x1={bBox.getCenterX()} y1={hiderTop} x2={bBox.getCenterX()}
                      y2={bBox.getBottom()}
                      className="life-line-hider"/>
                {children}
            </BlockStatementDecorator>
        </CompoundStatementDecorator>);
    }

    static isWorker(dropTarget, nodeBeingDragged) {
        const factory = dropTarget.getFactory();
        return factory.isWorkerDeclaration(nodeBeingDragged);
    }
}

ForkJoinStatement.propTypes = {
    bBox: PropTypes.shape({
        x: PropTypes.number.isRequired,
        y: PropTypes.number.isRequired,
        w: PropTypes.number.isRequired,
        h: PropTypes.number.isRequired,
    })
};


export default ForkJoinStatement;
