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
import TreeUtil from 'plugins/ballerina/model/tree-util';
import IfNodeModel from 'plugins/ballerina/model/tree/if-node';
import DropZone from 'plugins/ballerina/drag-drop/DropZone';
import DefaultNodeFactory from 'plugins/ballerina/model/default-node-factory';
import IfStatementDecorator from './if-statement-decorator';
import AddCompoundBlock from './add-compound-block';
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
        const elseNode = DefaultNodeFactory.createIfElse().getElseStatement().getElseStatement();
        const lastElseIf = this.props.model.elseStatement
            ? this.getLastElseIf(this.props.model.elseStatement)
            : this.props.model;
        const parent = !lastElseIf ? this.props.model : lastElseIf;
        parent.ladderParent = true;
        elseNode.clearWS();
        parent.setElseStatement(elseNode);
    }

    /**
     * Add else if to the if statement.
     * */
    onAddElseIfClick() {
        const elseIfNode = DefaultNodeFactory.createIfElse().getElseStatement();
        elseIfNode.elseStatement = null;
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
     * Get the last else statement.
     * @param {Node} elseStmt - current node's else statement.
     * */
    getElseStatement(elseStmt) {
        const elseExist = TreeUtil.isBlock(elseStmt);
        if (elseExist) {
            return elseStmt;
        } else {
            return elseStmt.elseStatement ?
                this.getElseStatement(elseStmt.elseStatement)
                : false;
        }
    }

    /**
     * Get the add block button for if and else if.
     * @param {boolean} isElseIfNode - is model else if node.
     * @return {XML} react component.
     * */
    getAddBlockButton(isElseIfNode) {
        const model = this.props.model;
        const blocksToBeAdded = [];
        // Check whether next node is the Else block or not.
        const elseExist = model.elseStatement ? this.getElseStatement(model.elseStatement) : false;
        // check whether this block is a else if block and it is the last else if.
        if (!elseExist) {
            const elseIfBlock = {
                name: 'else if',
                addBlock: this.onAddElseIfClick,
            };

            const elseBlock = {
                name: 'else',
                addBlock: this.onAddElseClick,
            };

            blocksToBeAdded.push(elseIfBlock);
            blocksToBeAdded.push(elseBlock);
        } else {
            const elseIfBlock = {
                name: 'else if',
                addBlock: this.onAddElseIfClick,
            };
            blocksToBeAdded.push(elseIfBlock);
        }

        if (TreeUtil.isBlock(model.parent)) {
            return (
                <AddCompoundBlock
                    blocksToBeAdded={blocksToBeAdded}
                    model={model}
                />
            );
        } else {
            return null;
        }
    }

    /**
     * get the last else if node.
     * @param {Node} node - else if node.
     * @return {Node} true if this is the last else if, else false.
     * */
    getLastElseIf(node) {
        const elseStatement = node.elseStatement ? node.elseStatement : false;
        if (TreeUtil.isIf(node.parent) && TreeUtil.isIf(node) && TreeUtil.isIf(elseStatement)) {
            return this.getLastElseIf(elseStatement);
        } else if (TreeUtil.isIf(node.parent)
            && TreeUtil.isIf(node)
            && (TreeUtil.isBlock(elseStatement) || !elseStatement)) {
            return node;
        } else {
            return null;
        }
    }

    /**
     * Render the component.
     * @return {XML} react component.
     * */
    render() {
        const model = this.props.model;
        const bBox = model.viewState.bBox;
        const expression = {
            text: model.getCondition().getSource(),
        };
        const isElseIfNode = TreeUtil.isIf(model.parent);
        const title = isElseIfNode ? 'else if' : 'if';
        const dropZone = model.viewState.components['drop-zone'];
        const editorOptions = {
            propertyType: 'text',
            key: 'If condition',
            model: model.getCondition(),
        };
        return (
            <g>
                <DropZone
                    model={this.props.model}
                    x={dropZone.x}
                    y={dropZone.y}
                    width={dropZone.w}
                    height={dropZone.h}
                    baseComponent='rect'
                    dropTarget={model.parent}
                    dropBefore={model}
                    renderUponDragStart
                    enableDragBg
                    enableCenterOverlayLine
                />
                <IfStatementDecorator
                    dropTarget={model}
                    bBox={bBox}
                    title={title}
                    expression={expression}
                    editorOptions={editorOptions}
                    model={model}
                />
            </g>
        );
    }
}

IfNode.propTypes = {
    model: PropTypes.instanceOf(IfNodeModel).isRequired,
};

IfNode.contextTypes = {
    mode: PropTypes.string,
    getOverlayContainer: PropTypes.instanceOf(Object).isRequired,
};

export default IfNode;
