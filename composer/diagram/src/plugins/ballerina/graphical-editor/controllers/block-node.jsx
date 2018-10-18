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
import TreeUtil from 'plugins/ballerina/model/tree-util';

class MainRegion extends React.Component {
    render() {
        const { model } = this.props;
        const { viewState: { bBox } } = model;
        const items = ControllerUtil.convertToAddItems(WorkerTools, model);

        let top = 0;
        let left = 0;

        if (TreeUtil.isTransaction(model.parent) || TreeUtil.isTry(model.parent)) {
            top = bBox.y - 10;
            left = bBox.x;
        } else if (TreeUtil.isIf(model.parent)) {
            top = model.parent.viewState.bBox.y + model.parent.viewState.bBox.h - 20;
            left = model.parent.viewState.components['statement-box'].x + model.parent.viewState.components['statement-box'].w;
        } else {
            return null;
        }
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

class ActionBox extends React.Component {
    render() {
        const { model } = this.props;
        const { viewState: { bBox } } = model;
        let top = 0;
        let left = 0;

        if (TreeUtil.isTransaction(model.parent) || TreeUtil.isTry(model.parent)) {
            top = bBox.y - 25;
            left = bBox.x;
        } else {
            return null;
        }
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

MainRegion.contextTypes = {
    designer: PropTypes.instanceOf(Object),
};

export default {
    regions: {
        main: MainRegion,
        actionBox: ActionBox,
    },
    name: 'Block',
};
