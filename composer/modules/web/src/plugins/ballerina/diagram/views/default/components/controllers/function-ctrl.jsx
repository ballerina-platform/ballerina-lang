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
 */
import React from 'react';
import PropTypes from 'prop-types';
import ControllerUtil from './controller-util';
import LifelineTools from '../../../../../tool-palette/item-provider/lifeline-tools';
import TreeUtil from './../../../../../model/tree-util';
import LifelineButton from '../../../../../interactions/lifeline-button';

/**
 * class to render Next statement.
 * @extends React.Component
 * @class FunctionCtrl
 * */
class FunctionCtrl extends React.Component {

    /**
     * Render Function for the Next statement.
     * @return {React.Component} next node react component.
     * */
    render() {
        if (!this.props.model.body) {
            return null;
        }
        const node = this.props.model;
        const y = node.viewState.components.defaultWorker.y - 20;
        let x = node.viewState.components.defaultWorker.x + node.viewState.components.defaultWorker.w +
            this.context.config.lifeLine.gutter.h;

        if (node.lambda) {
            return null;
        }

        if (node.workers.length > 0) {
            x = node.workers[node.workers.length - 1].viewState.bBox.x +
                node.workers[node.workers.length - 1].viewState.bBox.w +
                this.context.config.lifeLine.gutter.h;
        }

        if (node.viewState.collapsed) {
            return null;
        }
        // Set the size of the connector declarations
        const statements = node.body.statements;
        if (statements instanceof Array) {
            statements.forEach((statement) => {
                if (TreeUtil.isEndpointTypeVariableDef(statement)) {
                    x = statement.viewState.bBox.w + statement.viewState.bBox.x + this.context.config.lifeLine.gutter.h;
                }
            });
        }

        if (TreeUtil.isInitFunction(node)) {
            return <span />;
        }

        const w = 50;
        const h = 50;

        const items = ControllerUtil.convertToAddItems(LifelineTools, node);

        return <LifelineButton bBox={{ x, y, w, h }} model={node} items={items} />;
    }
}

FunctionCtrl.propTypes = {
    model: PropTypes.instanceOf(Object).isRequired,
};

FunctionCtrl.contextTypes = {
    config: PropTypes.instanceOf(Object).isRequired,
    command: PropTypes.shape({
        on: PropTypes.func,
        dispatch: PropTypes.func,
    }).isRequired,
    mode: PropTypes.string,
};

export default FunctionCtrl;
