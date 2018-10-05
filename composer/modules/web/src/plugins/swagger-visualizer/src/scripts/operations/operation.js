/**
 * Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
 *
 */

import React from 'react';
import PropTypes from 'prop-types';
import { Accordion, Icon } from 'semantic-ui-react';

import OasParameters from '../parameters/parameters';

/* eslint-disable */
class OasOperation extends React.Component {
    render() {
        const {
            path, opType, oasOp, activeIndex, currIndex, handleExpand, onDeleteOperation
        } = this.props;
        return (
            <div className={'operation '  + opType}>
                <Accordion.Title className='op-title ' index={currIndex} onClick={handleExpand}>
                    <span className='op-method'>{opType}</span>
                    <span className='op-summary'>{oasOp.summary}</span>
                    <Icon
                        className='delete-op'
                        onClick={(event) => {
                            event.stopPropagation();
                            onDeleteOperation({
                                operation: opType,
                                path: path,
                                operationObj: oasOp
                            })
                        }}
                        name='trash alternate'
                    />
                </Accordion.Title>
                <Accordion.Content active={activeIndex === currIndex}>
                    <p>{oasOp.description}</p>
                    <div className='op-section'>
                        <p>Parameters</p>
                        <OasParameters paramType='parameter' parameterObj={oasOp.parameters} />
                    </div>
                    <div className='op-section '>
                        <p>Responses</p>
                        <OasParameters paramType='response' parameterObj={oasOp.responses} />
                    </div>
                </Accordion.Content>
            </div>
        );
    }
}

OasOperation.propTypes = {
    path: PropTypes.string.isRequired,
    opType: PropTypes.string.isRequired,
    oasOp: PropTypes.object.isRequired,
    activeIndex: PropTypes.number.isRequired,
    handleExpand: PropTypes.func.isRequired,
    currIndex: PropTypes.number.isRequired,
    onDeleteOperation: PropTypes.func.isRequired,
};

export default OasOperation;
