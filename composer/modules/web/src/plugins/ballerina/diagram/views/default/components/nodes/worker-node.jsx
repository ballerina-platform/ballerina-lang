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
import { getComponentForNodeArray } from './../../../../diagram-util';
import LifeLine from '../decorators/lifeline';
import ImageUtil from '../../../../image-util';
import FragmentUtils from '../../../../../utils/fragment-utils';
import TreeBuilder from '../../../../../model/tree-builder';
import HoverGroup from 'plugins/ballerina/graphical-editor/controller-utils/hover-group';

/**
 * Worker Node Decorator.
 * @class WorkerNode
 * @extends React.Component
 * */
class WorkerNode extends React.Component {

    /**
     * Constructor for worker node.
     * @param {Object} props - properties for worker node.
     * */
    constructor(props) {
        super(props);
        this.handleSetName = this.handleSetName.bind(this);
    }

    /**
     * Handle setting name for worker node.
     * @param {string} value - changed value for worker identifier
     * */
    handleSetName(value) {
        if (value) {
            value = value.replace(';', '');
            // Parse new worker node with changed identifier value.
            FragmentUtils.parseFragment(FragmentUtils.createWorkerFragment(`worker ${value} {
            }`))
                .then((parsedJson) => {
                    const newNameNode = TreeBuilder.build(parsedJson).getName();
                    newNameNode.clearWS();

                    // Set name for worker node.
                    this.props.model.setName(newNameNode);
                });
        }
    }

    /**
     * Render Function for the assignment statement.
     * @return {XML} React component.
     * */
    render() {
        const model = this.props.model;
        const blockNode = getComponentForNodeArray(model.body, this.context.mode);
        const title = model.name.value;

        // Set worker name editor option.
        const editorOptions = {
            propertyType: 'text',
            key: 'Worker',
            model: this.props.model.getName(),
            setterMethod: this.handleSetName,
        };

        // TODO: We need to add the expression editor
        return (
            <g>
                <LifeLine
                    model={model}
                    title={title}
                    bBox={model.viewState.components.lifeLine}
                    editorOptions={editorOptions}
                    className='worker'
                    icon={ImageUtil.getCodePoint('worker')}
                />
                <HoverGroup model={model} region='actionBox'>
                    <rect
                        x={model.x}
                        y={model.y}
                        width={model.w}
                        height={model.h}
                        className='invisible-rect'
                    />
                </HoverGroup>
                {blockNode}
            </g>
        );
    }
}

WorkerNode.propTypes = {
    model: PropTypes.instanceOf(Object).isRequired,
};

WorkerNode.contextTypes = {
    designer: PropTypes.instanceOf(Object).isRequired,
};

export default WorkerNode;
