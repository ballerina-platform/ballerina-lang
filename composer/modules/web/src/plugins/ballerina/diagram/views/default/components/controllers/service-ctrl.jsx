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
import ServiceMenu from '../../../../../interactions/service-menu';

/**
 * class to render Next statement.
 * @extends React.Component
 * @class ServiceCtrl
 * */
class ServiceCtrl extends React.Component {

    /**
     * Render Function for the Next statement.
     * @return {React.Component} next node react component.
     * */
    render() {
        const node = this.props.model;
        const y = node.viewState.bBox.y + 8;
        const x = node.viewState.bBox.x - 8;

        const w = 20;
        const h = 30;

        const items = ControllerUtil.convertToAddItems(LifelineTools, node);

        return <ServiceMenu bBox={{ x, y, w, h }} model={node} items={items} />;
    }
}

ServiceCtrl.propTypes = {
    model: PropTypes.instanceOf(Object).isRequired,
};

ServiceCtrl.contextTypes = {
    config: PropTypes.instanceOf(Object).isRequired,
    command: PropTypes.shape({
        on: PropTypes.func,
        dispatch: PropTypes.func,
    }).isRequired,
    mode: PropTypes.string,
};

export default ServiceCtrl;
