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
import DefaultNodeFactory from './../../../../../model/default-node-factory';
import FragmentUtils from './../../../../../utils/fragment-utils';
import TreeBuilder from './../../../../../model/tree-builder';
import './try-node.css';

/**
 * Class for catch node.
 * @extends React.Component
 * @class CatchNode
 * */
class CatchNode extends React.Component {

    constructor(props) {
        super(props);
        this.onAddFinallyClick = this.onAddFinallyClick.bind(this);
        this.setCatchCondition = this.setCatchCondition.bind(this);
        this.getCatchCondition = this.getCatchCondition.bind(this);

        this.editorOptions = {
            propertyType: 'text',
            key: 'Catch condition',
            model: props.model,
            getterMethod: this.getCatchCondition,
            setterMethod: this.setCatchCondition,
        };
    }

    /**
     * Add finally block to the try catch statement.
     * */
    onAddFinallyClick() {
        const model = this.props.model;
        const parent = model.parent;
        // If no finally blocks available create a final body.
        if (!parent.getFinallyBody()) {
            const finallyBlock = DefaultNodeFactory.createTry().getFinallyBody();
            parent.setFinallyBody(finallyBlock);
        }
    }

    /**
     * Set catch condition.
     * @param {String} newCondition - new condition to be applied to catch block.
     * */
    setCatchCondition(newCondition) {
        const fragmentJson = FragmentUtils.createArgumentParameterFragment(newCondition);
        const parsedJson = FragmentUtils.parseFragment(fragmentJson);
        const newNode = TreeBuilder.build(parsedJson, this.props.model.parent, this.props.model.parent.kind);
        newNode.clearWS();
        this.props.model.setParameter(newNode);
    }

    /**
     * Get catch condition
     * @return {string} parameter source.
     * */
    getCatchCondition() {
        return this.props.model.getParameter().getSource();
    }

    render() {
        const model = this.props.model;
        const bBox = model.viewState.bBox;
        const expression = model.viewState.components.expression;

        // check whether catch block is the final catch block to attach the add finally block button.
        const isFinalCatchBlock = (model.parent.getIndexOfCatchBlocks(model) ===
            (model.parent.getCatchBlocks().length - 1));

        const disableDelete = isFinalCatchBlock && (model.parent.getCatchBlocks().length === 1)
            && !model.parent.finallyBody;

        return (
            <g>
                <CompoundStatementDecorator
                    bBox={bBox}
                    title={'Catch'}
                    expression={expression}
                    editorOptions={this.editorOptions}
                    model={model}
                    body={model.body}
                    disableButtons={{ delete: disableDelete }}
                />

                {!model.parent.getFinallyBody() && isFinalCatchBlock &&
                <g onClick={this.onAddFinallyClick}>
                    <title>Add Finally</title>
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
                }
            </g>
        );
    }
}

CatchNode.propTypes = {
    model: PropTypes.shape({
        getParameter: PropTypes.func.isRequired,
        setParameter: PropTypes.func.isRequired,
        parent: PropTypes.shape().isRequired,
    }).isRequired,
};

CatchNode.contextTypes = {
    mode: PropTypes.string,
};

export default CatchNode;
