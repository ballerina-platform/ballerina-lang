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
import CompoundStatementDecorator from './compound-statement-decorator';
import TreeUtil from './../../../../../model/tree-util';
import IfNodeModel from './../../../../../model/tree/if-node';
import DropZone from './../../../../../drag-drop/DropZone';
import DefaultNodeFactory from './../../../../../model/default-node-factory';
import './if-node.css';

/**
 * component class for rendering If statement.
 * @class IfNode
 * @extends React.Component
 * */
class IfNode extends React.Component {

    /**
     * Constructor for the IfNode class.
     * @param {Object} props - properties passed in to component.
     * */
    constructor(props) {
        super(props);

        this.state = {
            active: 'hidden',
        };
        this.onAddElseClick = this.onAddElseClick.bind(this);
        this.onAddElseIfClick = this.onAddElseIfClick.bind(this);
    }

    /**
     * Add else to the if statement.
     * */
    onAddElseClick() {
        const elseNode = DefaultNodeFactory.createIf().getElseStatement().getElseStatement();
        const parent = this.props.model;
        elseNode.clearWS();
        parent.setElseStatement(elseNode);
    }

    /**
     * Add else if to the if statement.
     * */
    onAddElseIfClick() {
        const elseIfNode = DefaultNodeFactory.createIf().getElseStatement();
        const parent = this.props.model;
        if (parent.elseStatement) {
            elseIfNode.elseStatement = parent.elseStatement;
            parent.elseStatement.parent = elseIfNode;
            parent.ladderParent = true;
        }
        elseIfNode.clearWS();
        parent.ws = null;
        parent.setElseStatement(elseIfNode);
    }

    /**
     * Get the add block button for if and else if.
     * @param {boolean} isElseIfNode - is model else if node.
     * @return {XML} react component.
     * */
    getAddBlockButton(isElseIfNode) {
        const model = this.props.model;
        // Check whether next node is the Else block or not.
        const elseExist = model.elseStatement ? TreeUtil.isBlock(model.elseStatement) : false;
        // check whether this block is a else if block and it is the last else if.
        if (isElseIfNode && this.isLastElseIf(model) && !elseExist) {
            return (
                <g onClick={this.onAddElseClick}>
                    <title>Add Else</title>
                    <rect
                        x={model.viewState.components['statement-box'].x
                        + model.viewState.components['statement-box'].w
                        + model.viewState.bBox.expansionW - 10}
                        y={model.viewState.components['statement-box'].y
                        + model.viewState.components['statement-box'].h - 25}
                        width={20}
                        height={20}
                        rx={10}
                        ry={10}
                        className="add-catch-button"
                    />
                    <text
                        x={model.viewState.components['statement-box'].x
                        + model.viewState.components['statement-box'].w
                        + model.viewState.bBox.expansionW - 4}
                        y={model.viewState.components['statement-box'].y
                        + model.viewState.components['statement-box'].h - 15}
                        width={20}
                        height={20}
                        className="add-catch-button-label"
                    >
                        +
                    </text>
                </g>
            );
        } else if (!isElseIfNode) {
            return (
                <g onClick={this.onAddElseIfClick}>
                    <title>Add a Else If</title>
                    <rect
                        x={model.viewState.components['statement-box'].x
                        + model.viewState.components['statement-box'].w
                        + model.viewState.bBox.expansionW - 10}
                        y={model.viewState.components['statement-box'].y
                        + model.viewState.components['statement-box'].h - 25}
                        width={20}
                        height={20}
                        rx={10}
                        ry={10}
                        className="add-catch-button"
                    />
                    <text
                        x={model.viewState.components['statement-box'].x
                        + model.viewState.components['statement-box'].w
                        + model.viewState.bBox.expansionW - 4}
                        y={model.viewState.components['statement-box'].y
                        + model.viewState.components['statement-box'].h - 15}
                        width={20}
                        height={20}
                        className="add-catch-button-label"
                    >
                        +
                    </text>
                </g>
            );
        }

        return '';
    }

    /**
     * Check whether this is the last else if node.
     * @param {Node} node - else if node.
     * @return {boolean} true if this is the last else if, else false.
     * */
    isLastElseIf(node) {
        const elseStatement = node.elseStatement ? node.elseStatement : false;
        if (TreeUtil.isIf(node.parent) && TreeUtil.isIf(node) && TreeUtil.isIf(elseStatement)) {
            return this.isLastElseIf(elseStatement);
        } else if (TreeUtil.isIf(node.parent)
            && TreeUtil.isIf(node)
            && (TreeUtil.isBlock(elseStatement) || !elseStatement)
            && node.id === this.props.model.id) {
            return true;
        } else {
            return false;
        }
    }

    render() {
        const model = this.props.model;
        const bBox = model.viewState.bBox;
        const expression = {
            text: model.getCondition().getSource(),
        };
        // TODOX Fix the else-if check
        const isElseIfNode = TreeUtil.isIf(model.parent);
        const elseComp = model.elseStatement;
        const title = isElseIfNode ? 'Else If' : 'If';
        const dropZone = model.viewState.components['drop-zone'];
        const editorOptions = {
            propertyType: 'text',
            key: 'If condition',
            model: model.getCondition(),
        };

        return (
            <g>
                {!isElseIfNode &&
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
                </g>
                }
                <CompoundStatementDecorator
                    dropTarget={model}
                    bBox={bBox}
                    title={title}
                    expression={expression}
                    editorOptions={editorOptions}
                    model={model}
                    body={model.body}
                />
                {this.getAddBlockButton(isElseIfNode)}

                {elseComp && TreeUtil.isIf(elseComp) &&
                <IfNode model={elseComp} />
                }

                {elseComp && TreeUtil.isBlock(elseComp) &&
                <CompoundStatementDecorator
                    dropTarget={model}
                    bBox={elseComp.viewState.bBox}
                    title={'Else'}
                    model={elseComp}
                    body={elseComp}
                />
                }
            </g>
        );
    }
}

IfNode.propTypes = {
    model: PropTypes.instanceOf(IfNodeModel).isRequired,
};

IfNode.contextTypes = {
    mode: PropTypes.string,
};

export default IfNode;
