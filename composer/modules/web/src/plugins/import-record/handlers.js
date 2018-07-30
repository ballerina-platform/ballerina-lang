
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

import { COMMANDS as LAYOUT_COMMANDS } from 'core/layout/constants';
import GenerateSchema from 'generate-schema';
import { COMMANDS, DIALOG } from './constants';
import TreeBuilder from './../ballerina/model/tree-builder';
import FragmentUtils from './../ballerina/utils/fragment-utils';
import DefaultNodeFactory from './../ballerina/model/default-node-factory';

/**
 * Check given value is an Integer
 * @param  {Number} val value to check
 * @return {Boolean}  is Intger
 */
function isInt(val) {
    return !isNaN(val) && Number(val).toString().length === (Number.parseInt(Number(val), 10).toString().length);
}

/**
 * Check given value is an Integer
 * @param  {Number} val value to check
 * @return {Boolean}  is Intger
 */
function isFloat(val) {
    return !isNaN(val) && !this.isInt(Number(val)) && val.toString().length > 0;
}

function jsonPathToValue(jsonData, path) {
    path = path.replace('/','').split('/');
    for (var i = 0, n = path.length; i < n; ++i) {
        var key = path[i];
        if (key in jsonData) {
            if (jsonData[key] !== null) {
                jsonData = jsonData[key];
            } else {
                return null;
            }
        } else if (Array.isArray(jsonData)) {
            jsonData.forEach((arrKey) => {
                if (key in arrKey) {
                    jsonData = arrKey[key];
                }
            })
        } else {
            return key;
        }
    }
    return jsonData;
}  

function processJSONSchema(schema, topLevelNodes, rootRecord, schemaPath = '', rootExpr, useDefaults = false) {
    let success = true;
    if (typeof schema === 'object' && !Array.isArray(schema)) {
        for (const key in schema) {
            const schemaObj = schema[key];
            if (key === 'properties' && typeof schemaObj === 'object') {
                for (const objProp in schemaObj) {
                    const curObj = schemaObj[objProp];
                    if (curObj.type === 'object' || curObj.type === 'array') {
                        const anonExpr = TreeBuilder.build(FragmentUtils.parseFragment(FragmentUtils.createAnonRecordFragment(`record { } ${objProp};`)));
                        const anonExprObj = anonExpr.topLevelNodes[anonExpr.topLevelNodes.length - 1].body.statements[0];
                        if (!anonExpr.error) {
                            anonExpr.topLevelNodes[anonExpr.topLevelNodes.length - 1].body.statements[0].parent = rootRecord.typeNode;
                            if (rootRecord.kind === 'RecordType') {
                                rootRecord.fields.push(anonExprObj);
                            } else {
                                rootRecord.typeNode.fields.push(anonExprObj);
                            }
                        }
                        success = processJSONSchema(schemaObj[objProp], topLevelNodes, 
                            anonExprObj.variable.typeNode.anonType, schemaPath + '/' + objProp, rootExpr, useDefaults);
                    } else {
                        let refExpr;
                        const value = jsonPathToValue(rootExpr, schemaPath + '/' + objProp);
                        let currentType = 'string';
                        if (isInt(value)) {
                            currentType = 'int';
                        } else if (isFloat(value)) {
                            currentType = 'float';
                        } else if (value === 'true' || value === 'false') {
                            currentType = 'boolean';
                        }
                        if (useDefaults) {
                            refExpr = TreeBuilder.build(FragmentUtils.parseFragment(FragmentUtils
                                .createFieldDefinitionListFragment(`${currentType} ${objProp};`)));
                        } else {
                            if (currentType === 'string') {
                                refExpr = TreeBuilder.build(FragmentUtils.parseFragment(FragmentUtils
                                    .createFieldDefinitionListFragment(`${currentType} ${objProp} = "${value}";`)));
                            } else {
                                refExpr = TreeBuilder.build(FragmentUtils.parseFragment(FragmentUtils
                                    .createFieldDefinitionListFragment(`${currentType} ${objProp} = ${value};`)));
                            }
                        }
                        if (!refExpr.error) {
                            // Add ; white space for the field in to record.
                            if (rootRecord.kind === 'RecordType') {
                                refExpr.parent = rootRecord;
                                rootRecord.fields.push(refExpr);
                            } else {
                                rootRecord.ws.splice(rootRecord.ws.length - 2, 0, { ws: '', text: ';' });
                                refExpr.parent = rootRecord.typeNode;
                                rootRecord.typeNode.fields.push(refExpr);
                            }
                        } else {
                            success = false;
                        }
                    }
                }
            } else if (key === 'items') {
                processJSONSchema(schemaObj, topLevelNodes, rootRecord, schemaPath, rootExpr, useDefaults);
            }
        }
    }
    return success;
}

/**
 * Provides command handler definitions of debugger plugin.
 * @param {plugin} plugin plugin instance
 * @returns {Object[]} command handler definitions.
 *
 */
export function getHandlerDefinitions(plugin) {
    return [
        {
            cmdID: COMMANDS.SHOW_IMPORT_RECORD_DIALOG,
            handler: () => {
                const topLevelNodes = plugin.appContext.editor.getActiveEditor().getProperty('ast');
                // Create record with available white spaces in default node.
                const RecordNode = DefaultNodeFactory.createRecord(true);

                const onImport = (json, recordName, removeDefaults) => {
                    let success = true;

                    if (recordName && recordName !== '') {
                        RecordNode.name.setValue(recordName);
                    }

                    if (json === '') {
                        topLevelNodes.addTopLevelNodes(RecordNode);
                        return success;
                    }

                    const schema = GenerateSchema.json('json', JSON.parse(json));
                    RecordNode.typeNode.fields = [];
                    success = processJSONSchema(schema, topLevelNodes, RecordNode, '', JSON.parse(json), removeDefaults);

                    if (success) {
                        topLevelNodes.addTopLevelNodes(RecordNode);
                    }

                    return success;
                };

                const { command: { dispatch } } = plugin.appContext;
                const id = DIALOG.IMPORT_RECORD;

                dispatch(LAYOUT_COMMANDS.POPUP_DIALOG, {
                    id,
                    additionalProps: {
                        onImport,
                    },
                });
            },
        },
    ];
}
