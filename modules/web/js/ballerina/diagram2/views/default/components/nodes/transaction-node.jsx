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
import DropZone from './../../../../../drag-drop/DropZone';
import './if-node.css';

class TransactionNode extends React.Component {

    constructor(props) {
        super(props);

        this.state = {
            innerDropZoneActivated: false,
            innerDropZoneDropNotAllowed: false,
            innerDropZoneExist: false,
            active: 'hidden',
        };
    }

    render() {
        const model = this.props.model;
        const bBox = model.viewState.bBox;
        const transactionBody = model.transactionBody;
        const failedBody = model.failedBody;
        const abortedBody = model.abortedBody;
        const committedBody = model.committedBody;
        const dropZone = model.viewState.components['drop-zone'];
        const innerDropZoneActivated = this.state.innerDropZoneActivated;
        const innerDropZoneDropNotAllowed = this.state.innerDropZoneDropNotAllowed;
        const dropZoneClassName = ((!innerDropZoneActivated) ? 'inner-drop-zone' : 'inner-drop-zone active')
            + ((innerDropZoneDropNotAllowed) ? ' block' : '');
        const fill = this.state.innerDropZoneExist ? {} : { fill: 'none' };

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
                />

                <CompoundStatementDecorator
                    dropTarget={model}
                    bBox={transactionBody.viewState.bBox}
                    title={'Transaction'}
                    titleWidth={transactionBody.viewState.components.titleWidth.w}
                    model={transactionBody}
                    body={transactionBody}
                />
                {failedBody &&
                    <CompoundStatementDecorator
                        dropTarget={failedBody}
                        bBox={failedBody.viewState.bBox}
                        title={'Failed'}
                        titleWidth={failedBody.viewState.components.titleWidth.w}
                        model={failedBody}
                        body={failedBody}
                    />
                }
                {abortedBody &&
                    <CompoundStatementDecorator
                        bBox={abortedBody.viewState.bBox}
                        title={'Aborted'}
                        titleWidth={abortedBody.viewState.components.titleWidth.w}
                        model={abortedBody}
                        body={abortedBody}
                    />
                }
                {committedBody &&
                    <CompoundStatementDecorator
                        bBox={committedBody.viewState.bBox}
                        title={'Committed'}
                        titleWidth={committedBody.viewState.components.titleWidth.w}
                        model={committedBody}
                        body={committedBody}
                    />
                }
            </g>
        );
    }
}

TransactionNode.propTypes = {
    bBox: PropTypes.shape({
        x: PropTypes.number.isRequired,
        y: PropTypes.number.isRequired,
        w: PropTypes.number.isRequired,
        h: PropTypes.number.isRequired,
    }),
};

TransactionNode.contextTypes = {
    mode: PropTypes.string,
};

export default TransactionNode;
