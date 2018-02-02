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
import { COMMANDS as WORKSPACE_COMMANDS } from 'core/workspace/constants';
import SwaggerParser from 'plugins/ballerina/swagger-parser/swagger-parser';
import DefaultNodeFactory from 'plugins/ballerina/model/default-node-factory';
import NodeFactory from 'plugins/ballerina/model/node-factory';
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
                    const { serviceNode, rootNode } = this.getNewAstSkeleton();
                    let swaggerParser;
                    if (swaggerFile.extension.toLowerCase() === 'json') {
                        swaggerParser = new SwaggerParser(JSON.parse(swaggerFile.content));
                    } else {
                        swaggerParser = new SwaggerParser(swaggerFile.content, true);
                    }
                    swaggerParser.mergeToService(serviceNode);

                    // source gen the create ast root and set it to file object.
                    const { command: { dispatch }, editor } = appContext;
                    dispatch(WORKSPACE_COMMANDS.CREATE_NEW_FILE);

                    const balFile = editor.getActiveEditor().file;
                    balFile.setContent(rootNode.getSource(), true);
                    resolve(balFile);
                })
                .catch((err) => {
                    console.log(err);
                    reject('Error occured while importing Swagger definition. Make sure the swagger definition is ' +
                            'valid.');
                });
        });
    }

    /**
     * Gets an premade AST with a service and has no resouces.
     * @returns {Object} An object with service node and root node.
     */
    getNewAstSkeleton() {
        // TODO: Implement
        // Create a new ast root with the following service node.
        const rootNode = NodeFactory.createCompilationUnit();

        // Adding imports.
        const httpImport = NodeFactory.createImport({
            alias: NodeFactory.createLiteral({
                value: 'http',
            }),
            packageName: [
                NodeFactory.createLiteral({
                    value: 'ballerina',
                }),
                NodeFactory.createLiteral({
                    value: 'net',
                }),
                NodeFactory.createLiteral({
                    value: 'http',
                }),
            ],
        });

        const swaggerImport = NodeFactory.createImport({
            alias: NodeFactory.createLiteral({
                value: 'swagger',
            }),
            packageName: [
                NodeFactory.createLiteral({
                    value: 'ballerina',
                }),
                NodeFactory.createLiteral({
                    value: 'net',
                }),
                NodeFactory.createLiteral({
                    value: 'http',
                }),
                NodeFactory.createLiteral({
                    value: 'swagger',
                }),
            ],
        });

        rootNode.addImport(httpImport);
        rootNode.addImport(swaggerImport);

        const serviceNode = DefaultNodeFactory.createHTTPServiceDef();
        // Remove default resource.
        serviceNode.removeResourcesByIndex(0);

        rootNode.addTopLevelNodes(serviceNode);

        return { serviceNode, rootNode };
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
