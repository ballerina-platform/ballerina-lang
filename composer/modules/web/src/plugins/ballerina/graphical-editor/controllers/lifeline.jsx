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
import { Menu } from 'semantic-ui-react';
import PropTypes from 'prop-types';
import WorkerTools from 'plugins/ballerina/tool-palette/item-provider/worker-tools';
import ControllerUtil from 'plugins/ballerina/diagram/views/default/components/controllers/controller-util';
import HoverButton from '../controller-utils/hover-button';

class DefaultCtrl extends React.Component {
    render() {
        const { model } = this.props;
        const { viewState: { bBox } } = model.getBody();
        const items = ControllerUtil.convertToAddItems(WorkerTools, model.getBody());
        const top = bBox.y + bBox.h + 15;
        const left = bBox.x;
        
        return (
            <HoverButton
                style={{
                    top,
                    left,
                }}
            >
                <Menu vertical>
                    {items}
                </Menu>
            </HoverButton>
        );
    }
}

DefaultCtrl.contextTypes = {
    designer: PropTypes.instanceOf(Object),
};

export default {
    defaults: [
        DefaultCtrl,
    ],
    name: 'Lifeline',
};
