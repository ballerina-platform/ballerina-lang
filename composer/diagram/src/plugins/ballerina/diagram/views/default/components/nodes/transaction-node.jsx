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
import TransactionStatementDecorator from './transaction-statement-decorator';
import DropZone from './../../../../../drag-drop/DropZone';
import DefaultNodeFactory from '../../../../../model/default-node-factory';
import './if-node.css';

/**
 * Class for transaction statement view
 * @extends React.Component
 * @class TransactionNode
 * */
class TransactionNode extends React.Component {

    /**
     * Constructor for Transaction node.
     * @param {Object} props - properties for creating transaction node.
     * */
    constructor(props) {
        super(props);

        this.state = {
            innerDropZoneExist: false,
            active: 'hidden',
        };

        this.addFailedBody = this.addFailedBody.bind(this);
    }

    /**
     * Add failed body to transaction statement.
     * */
    addFailedBody() {
        const parent = this.props.model;
        const newTransactionBlock = DefaultNodeFactory.createTransaction();
        const newFailedBlock = newTransactionBlock.failedBody;
        newFailedBlock.clearWS();
        parent.setFailedBody(newFailedBlock, false, 'Add Failed Body');
    }

    /**
     * Render the transaction node.
     * @return {XML} react component for transaction.
     * */
    render() {
        const model = this.props.model;
        const bBox = model.viewState.bBox;
        const dropZone = model.viewState.components['drop-zone'];
        return (
            <g>
                <DropZone
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
                <TransactionStatementDecorator
                    dropTarget={model}
                    bBox={bBox}
                    model={model}
                    body={model.body}
                />
            </g>
        );
    }
}

TransactionNode.propTypes = {
    model: PropTypes.shape().isRequired,
};

TransactionNode.contextTypes = {
    mode: PropTypes.string,
};

export default TransactionNode;
