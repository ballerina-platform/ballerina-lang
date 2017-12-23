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
 *
 */

import PropTypes from 'prop-types';
import { COMMANDS } from './constants';

/**
 * Provides command definitions of layout manager plugin.
 *
 * @returns {Object[]} command definitions.
 *
 */
export function getCommandDefinitions() {
    return [
        {
            id: COMMANDS.SHOW_VIEW,
            argTypes: {
                id: PropTypes.string.isRequired,
                additionalProps: PropTypes.objectOf(Object),
                options: PropTypes.objectOf(Object),
            },
        },
        {
            id: COMMANDS.HIDE_VIEW,
            argTypes: {
                id: PropTypes.string.isRequired,
            },
        },
        {
            id: COMMANDS.UPDATE_ALL_ACTION_TRIGGERS,
        },
        {
            id: COMMANDS.POPUP_DIALOG,
            argTypes: {
                id: PropTypes.string.isRequired,
            },
        },
        {
            id: COMMANDS.TOGGLE_BOTTOM_PANEL,
            shortcut: {
                default: 'ctrl+]',
            },
        },
        {
            id: COMMANDS.TOGGLE_LEFT_PANEL,
            shortcut: {
                default: 'ctrl+[',
            },
        },
        {
            id: COMMANDS.RE_RENDER_PLUGIN,
            argTypes: {
                id: PropTypes.string.isRequired,
            },
        },
    ];
}
