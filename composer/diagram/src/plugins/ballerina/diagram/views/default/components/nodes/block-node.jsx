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
import './block-node.css';
import BlockNodeModel from '../../../../../model/tree/block-node';
import { getComponentForNodeArray } from './../../../../diagram-util';
import TreeUtil from '../../../../../model/tree-util';

/**
 * Class to represent block node in tree.
 */
class BlockNode extends React.Component {

    /**
     * @inheritdoc
     */
    render() {
        const model = this.props.model;
        const withoutConnectors = model.statements
            .filter((element) => { return !TreeUtil.isEndpointTypeVariableDef(element); });
        const statements = getComponentForNodeArray(withoutConnectors, this.context.mode);
        return (
            <g className='block-node'>
                {statements}
            </g>
        );
    }
}

BlockNode.propTypes = {
    model: PropTypes.instanceOf(BlockNodeModel).isRequired,
};

BlockNode.contextTypes = {
    mode: PropTypes.string,
};

export default BlockNode;
