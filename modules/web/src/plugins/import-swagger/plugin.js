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
import _ from 'lodash';
import Plugin from 'core/plugin/plugin';
import { CONTRIBUTIONS } from 'core/plugin/constants';
import { read } from 'core/workspace/fs-util';
import SwaggerParser from 'ballerina/swagger-parser/swagger-parser';
import DefaultNodeFactory from 'ballerina/model/default-node-factory';
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
         * Opens a file using related editor
         *
         * @param {String} filePath Path of the file.
         * @param {String} type type of the file.
         * @return {Promise} Resolves or reject with error.
         */
    openSwaggerDefinition(filePath, type = 'bal') {
        return new Promise((resolve, reject) => {
            // if not already opened
            read(filePath)
                .then((file) => {
                    // TODO: Implement
                    // Create a new ast root with the following service node.
                    const serviceNode = DefaultNodeFactory.createHTTPServiceDef();
                    const parser = new SwaggerParser(file.content);
                    parser.mergeToService(serviceNode);

                    // source gen the create ast root and set it to file object.

                    file.extension = type;
                    this.openedFiles.push(file);
                    const { editor } = this.appContext;
                    editor.open(file);
                    resolve(file);
                })
                .catch((err) => {
                    reject(JSON.stringify(err));
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
