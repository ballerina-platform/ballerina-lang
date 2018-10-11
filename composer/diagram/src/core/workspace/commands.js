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
 * Provides command definitions of workspace plugin.
 *
 * @returns {Object[]} command definitions.
 *
 */
export function getCommandDefinitions() {
    return [
        {
            id: COMMANDS.CREATE_NEW_FILE,
            shortcut: {
                default: 'ctrl+alt+n',
            },
        },
        {
            id: COMMANDS.CREATE_PROJECT,
            shortcut: {
                default: 'ctrl+alt+p',
            },
        },
        {
            id: COMMANDS.OPEN_FILE,
            argTypes: {
                filePath: PropTypes.string.isRequired,
                ext: PropTypes.string,
            },
        },
        {
            id: COMMANDS.OPEN_FOLDER,
            argTypes: {
                folderPath: PropTypes.string.isRequired,
            },
        },
        {
            id: COMMANDS.REMOVE_FOLDER,
            argTypes: {
                folderPath: PropTypes.string.isRequired,
            },
        },
        {
            id: COMMANDS.SAVE_FILE,
            shortcut: {
                default: 'ctrl+s',
            },
            argTypes: {
                onSave: PropTypes.func,
            },
        },
        {
            id: COMMANDS.SAVE_FILE_AS,
            shortcut: {
                default: 'ctrl+shift+s',
            },
        },
        {
            id: COMMANDS.SHOW_FILE_OPEN_WIZARD,
            shortcut: {
                default: 'ctrl+o',
            },
        },
        {
            id: COMMANDS.SHOW_FOLDER_OPEN_WIZARD,
            shortcut: {
                default: 'ctrl+shift+o',
            },
        },
        {
            id: COMMANDS.GO_TO_FILE_IN_EXPLORER,
            argTypes: {
                filePath: PropTypes.string.isRequired,
            },
        },
        {
            id: COMMANDS.SHOW_EXTERNAL_LINK,
        },
    ];
}
