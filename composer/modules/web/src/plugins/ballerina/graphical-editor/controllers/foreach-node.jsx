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
import ControllerUtil from '../controller-utils/controller-util';
import HoverButton from '../controller-utils/hover-button';
import Toolbox from 'plugins/ballerina/diagram/views/default/components/decorators/action-box';

class MainRegion extends React.Component {
    render() {
        const { model } = this.props;
        const { viewState, viewState: { bBox } } = model;
        const items = ControllerUtil.convertToAddItems(WorkerTools, model.getBody());
        const titleH = this.context.designer.config.flowChartControlStatement.heading.height;
        const gapTop = this.context.designer.config.flowChartControlStatement.padding.top;
        const top = viewState.components['statement-box'].y + titleH + gapTop;
        const left = viewState.components['statement-box'].x;
        
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

MainRegion.contextTypes = {
    designer: PropTypes.instanceOf(Object),
};

class ActionBox extends React.Component {
    render() {
        const { model } = this.props;
        const { viewState, viewState: { bBox } } = model;
        const titleH = this.context.designer.config.flowChartControlStatement.heading.height;
        const gapTop = this.context.designer.config.flowChartControlStatement.padding.top;
        const top = viewState.components['statement-box'].y + titleH + gapTop;
        const left = viewState.components['statement-box'].x;
        const onDelete = () => { model.remove(); };
        const onJumptoCodeLine = () => {
            const { goToSource } = this.context;
            goToSource(model);
        };
        return (
            <Toolbox
                onDelete={onDelete}
                onJumptoCodeLine={onJumptoCodeLine}
                show
                style={{
                    top,
                    left,
                }}
            />
        );
    }
}

ActionBox.contextTypes = {
    goToSource: PropTypes.func.isRequired,
    designer: PropTypes.instanceOf(Object),
};

export default {
    regions: {
        main: MainRegion,
        actionBox: ActionBox,
    },
    name: 'Foreach',
};
