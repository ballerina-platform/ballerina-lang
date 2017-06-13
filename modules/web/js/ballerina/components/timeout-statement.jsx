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
import { timeout } from './../configs/designer-defaults';
import PropTypes from 'prop-types';
import { getComponentForNodeArray } from './utils';

class TimeoutStatement extends React.Component {

    render() {
        let model = this.props.model,
            bBox = model.viewState.bBox;
        const children = getComponentForNodeArray(this.props.model.getChildren());
        const props = this.props;
        const parameterBbox = this.props.model.viewState.components.param;

        this.editorOptions = {
            propertyType: 'text',
            key: 'Timeout interval',
            model: props.model,
            getterMethod: props.model.getExpression,
            setterMethod: props.model.setExpression,
        };
        const parameterEditorOptions = {
            propertyType: 'text',
            key: 'Timeout Parameter',
            value: parameterBbox.text,
            model: props.model,
            getterMethod: props.model.getParameterAsString,
            setterMethod: props.model.setParameterAsString,
        };

        let lifeLineY1;
        let lifeLineY2;
        if (model.children.length > 0) {
            const firstChild = model.children[0].viewState;
            lifeLineY1 = firstChild.bBox.y + firstChild.components['drop-zone'].h;
            const lastChild = model.children[model.children.length - 1].viewState;
            lifeLineY2 = lastChild.bBox.y + lastChild.components['drop-zone'].h;
        }
        return (<CompoundStatementDecorator model={model} bBox={bBox}>
          <BlockStatementDecorator
            model={model} dropTarget={model} bBox={bBox} title={'Timeout'} titleWidth={timeout.title.w}
            parameterBbox={parameterBbox} parameterEditorOptions={parameterEditorOptions}
            expression={{ text: model.getExpression() }} editorOptions={this.editorOptions}
          >
            {model.children.length > 0 &&
            <line
              x1={bBox.getCenterX()} y1={lifeLineY1} x2={bBox.getCenterX()} y2={lifeLineY2}
              className="join-lifeline"
            /> }
            {children}
          </BlockStatementDecorator>
        </CompoundStatementDecorator>);
    }
}

TimeoutStatement.propTypes = {
    bBox: PropTypes.shape({
        x: PropTypes.number.isRequired,
        y: PropTypes.number.isRequired,
        w: PropTypes.number.isRequired,
        h: PropTypes.number.isRequired,
    }),
};


export default TimeoutStatement;
