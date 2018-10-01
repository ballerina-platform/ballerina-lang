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
import lifeline from './lifeline';
import Toolbox from 'plugins/ballerina/diagram/views/default/components/decorators/action-box';

class ActionBox extends React.Component {
    render() {
        const { model } = this.props;
        const { viewState: { bBox } } = model;

        const top = bBox.y + 20;
        const left = bBox.x + (bBox.w / 2);

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
    config: PropTypes.instanceOf(Object),
    goToSource: PropTypes.func.isRequired,
    designer: PropTypes.instanceOf(Object),
};

export default {
    regions: {
        actionBox: ActionBox,
    },
    name: 'Endpoint',
};
