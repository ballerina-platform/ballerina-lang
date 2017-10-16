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
import ActiveArbiter from '../decorators/active-arbiter';
import { getComponentForNodeArray } from './../../../../diagram-util';
import LifeLine from '../decorators/lifeline';
import ImageUtil from '../../../../image-util';

/**
 * Worker Node Decorator.
 * */
class WorkerNode extends React.Component {

    constructor(props) {
        super(props);
        this.onDelete = this.onDelete.bind(this);

        // TODO: Set the name
        this.editorOptions = {
            propertyType: 'text',
            key: 'Worker',
            model: this.props.model,
            getterMethod: this.props.model.getSource,
            setterMethod: this.updateExpression,
        };
    }

    /**
     * ToDo Update the edited expression
     */
    updateExpression(value) {
    }

    /**
     * Removes self on delete button click.
     * @returns {void}
     */
    onDelete() {
        this.props.model.remove();
    }

    /**
     * Render Function for the assignment statement.
     * */
    render() {
        const model = this.props.model;
        const expression = model.viewState.expression;
        const blockNode = getComponentForNodeArray(model.body, this.context.mode);
        const title = model.name.value;
        const classes = {
            lineClass: 'worker-life-line',
            polygonClass: 'worker-life-line-polygon',
        };

        // TODO: We need to add the expression editor
        return (
            <g>
                <LifeLine
                    title={title}
                    bBox={model.viewState.components.lifeLine}
                    classes={classes}
                    icon={ImageUtil.getSVGIconString('tool-icons/worker-white')}
                    iconColor='#0380c6'
                    onDelete={this.onDelete}
                />
                {blockNode}
            </g>
        );
    }
}

WorkerNode.propTypes = {
    model: PropTypes.instanceOf(Object).isRequired,
};

WorkerNode.contextTypes = {
    activeArbiter: PropTypes.instanceOf(ActiveArbiter).isRequired,
    designer: PropTypes.instanceOf(Object).isRequired,
};

export default WorkerNode;
