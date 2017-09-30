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
import CompoundStatementDecorator from './compound-statement-decorator';
import { getComponentForNodeArray } from './../../../../diagram-util';
import ASTFactory from '../../../../../ast/ast-factory';
import TreeUtil from './../../../../../model/tree-util';
import './if-node.css';

class IfNode extends React.Component {

    constructor(props) {
        super(props);
        this.editorOptions = {
            propertyType: 'text',
            key: 'If condition',
            model: props.model,
            getterMethod: props.model.getConditionString,
            setterMethod: props.model.setConditionFromString,
        };
        this.onAddElseClick = this.onAddElseClick.bind(this);
    }

    onAddElseClick() {
        const parent = this.props.model.parent;
        if (parent.getElseStatement()) {
            const condition = ASTFactory.createBasicLiteralExpression({
                basicLiteralType: 'boolean',
                basicLiteralValue: true,
            });
            const newElseIfStatement = ASTFactory.createElseIfStatement({
                condition,
            });
            const thisNodeIndex = this.props.model.parent.getIndexOfChild(this.props.model);
            this.props.model.parent.addElseIfStatement(newElseIfStatement, thisNodeIndex + 1);
        } else {
            const newElseStatement = ASTFactory.createElseStatement();
            this.props.model.parent.addElseStatement(newElseStatement);
        }
    }

    render() {
        const model = this.props.model;
        const bBox = model.viewState.bBox;
        const expression = model.viewState.components.expression;
        const isElseIfNode = TreeUtil.isIf(model.parent);
        const elseComp = model.elseStatement;
        const title = isElseIfNode ? 'Else If' : 'If';

        return (
            <g>
                <CompoundStatementDecorator
                    dropTarget={model}
                    bBox={bBox}
                    title={title}
                    expression={expression}
                    editorOptions={this.editorOptions}
                    model={model.parent}
                />

                {elseComp && TreeUtil.isIf(elseComp) &&
                    <IfNode model={elseComp} />
                }

                {elseComp && TreeUtil.isBlock(elseComp) &&
                    <CompoundStatementDecorator
                        dropTarget={model}
                        bBox={elseComp.viewState.bBox}
                        title={'Else'}
                        model={elseComp}
                    />
                }
            </g>
        );
    }
}

IfNode.propTypes = {
    bBox: PropTypes.shape({
        x: PropTypes.number.isRequired,
        y: PropTypes.number.isRequired,
        w: PropTypes.number.isRequired,
        h: PropTypes.number.isRequired,
    }),
};

IfNode.contextTypes = {
    mode: PropTypes.string,
};

export default IfNode;
