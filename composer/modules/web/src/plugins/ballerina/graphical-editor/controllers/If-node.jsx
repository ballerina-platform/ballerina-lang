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
import Toolbox from 'plugins/ballerina/diagram/views/default/components/decorators/action-box';

class MainRegion extends React.Component {
    render() {
        const { model } = this.props;
        const items = ControllerUtil.convertToAddItems(WorkerTools, model.getBody());
        const top = model.viewState.components['statement-box'].y - 15;
        const left = model.viewState.components['statement-box'].x - 15;

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
        const top = model.viewState.components['statement-box'].y - 25;
        const left = model.viewState.components['statement-box'].x - 38;
        const onDelete = () => { model.remove(); };
        const onJumptoCodeLine = () => {
            const { editor } = this.context;
            editor.goToSource(model);
        }
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
    editor: PropTypes.shape({
        isFileOpenedInEditor: PropTypes.func,
        getEditorByID: PropTypes.func,
        setActiveEditor: PropTypes.func,
        getActiveEditor: PropTypes.func,
        getDefaultContent: PropTypes.func,
    }).isRequired,
};

class Else extends React.Component {
    render() {
        const { model } = this.props;
        const items = ControllerUtil.convertToAddItems(WorkerTools, model.getBody());
        const top = model.viewState.components['statement-box'].y - 15;
        const left = model.viewState.components['statement-box'].x + model.viewState.components['statement-box'].w - 15;

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

export default {
    regions: {
        main: MainRegion,
        actionBox: ActionBox,
        else: Else,
    },
    name: 'If',
};
