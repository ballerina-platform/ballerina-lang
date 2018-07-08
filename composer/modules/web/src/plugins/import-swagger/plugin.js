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
import Plugin from 'core/plugin/plugin';
import { CONTRIBUTIONS } from 'core/plugin/constants';
import { read } from 'core/workspace/fs-util';
import { COMMANDS as WORKSPACE_COMMANDS } from 'core/workspace/constants';
import { getServiceDefinition } from 'api-client/api-client';
import log from 'log';
import { getCommandDefinitions } from './commands';
import { getHandlerDefinitions } from './handlers';
import { getMenuDefinitions } from './menus';
import { PLUGIN_ID, DIALOG } from './constants';
import ImportSwaggerDialog from './dialogs/import-swagger-dialog';


/**
 * Help plugin.
 *
 * @class HelpPlugin
 */
class ImportSwaggerPlugin extends Plugin {

    /**
     * @inheritdoc
     */
    getID() {
        return PLUGIN_ID;
    }

    /**
     * Opens a swagger definition file using related ballerina editor.
     * @param {string} filePath Path of the file.
     * @return {Promise} Resolves or reject with error.
     */
    openSwaggerDefinition(filePath) {
        const appContext = this.appContext;
        return new Promise((resolve, reject) => {
            // if not already opened
            read(filePath)
                .then((swaggerFile) => {
                    getServiceDefinition(swaggerFile.content, 'default')
                    .then((serviceDefinition) => {
                        const { command: { dispatch }, editor } = appContext;
                        dispatch(WORKSPACE_COMMANDS.CREATE_NEW_FILE);


                        const balFile = editor.getActiveEditor().file;
                        balFile.setContent(serviceDefinition, true);
                        resolve(balFile);
                    })
                    .catch(error => log.error(error));
                })
                .catch((err) => {
                    reject('Error occurred while importing Swagger definition. Make sure the swagger definition is ' +
                            'valid.');
                });
        });
    }

    /**
     * @inheritdoc
     */
    getContributions() {
        const { COMMANDS, HANDLERS, MENUS, DIALOGS } = CONTRIBUTIONS;
        return {
            [COMMANDS]: getCommandDefinitions(this),
            [HANDLERS]: getHandlerDefinitions(this),
            [MENUS]: getMenuDefinitions(this),
            [DIALOGS]: [
                {
                    id: DIALOG.IMPORT_SWAGGER,
                    component: ImportSwaggerDialog,
                    propsProvider: () => {
                        return {
                            importSwaggerPlugin: this,
                            extensions: ['json', 'yaml', 'yml'],
                        };
                    },
                },
            ],
        };
    }
}

export default ImportSwaggerPlugin;
