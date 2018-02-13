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
import { LayoutConstants } from '@ballerina-lang/composer-core';
import { COMMANDS, DIALOG } from './constants';

/**
 * Provides command handler definitions of debugger plugin.
 * @param {plugin} plugin plugin instance
 * @returns {Object[]} command handler definitions.
 *
 */
export function getHandlerDefinitions(plugin) {
    return [
        {
            cmdID: COMMANDS.SHOW_EXPORT_DIAGRAM_DIALOG,
            handler: () => {
                const { command: { dispatch } } = plugin.appContext;
                const id = DIALOG.EXPORT_DIAGRAM;
                dispatch(LayoutConstants.COMMANDS.POPUP_DIALOG, { id });
            },
        },
    ];
}
