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
import { COMMANDS, DIALOG } from './constants';
import TreeBuilder from './../ballerina/model/tree-builder';
import FragmentUtils from './../ballerina/utils/fragment-utils';
import DefaultNodeFactory from './../ballerina/model/default-node-factory';
import TreeUtils from './../ballerina/model/tree-util';

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

/**
 * process JSON and generate struct fields
 * @param  {object} parent      parent struct object
 * @param  {object} literalExpr JSON expression
 * @param  {object} topLevelNodes top level nodes
 * @param  {object} removeDefaults remove defaults
 * @return {boolean}             status
 */
function processJSONStruct(parent, literalExpr, topLevelNodes, removeDefaults) {
    let currentValue;
    let success = true;
    parent.typeNode.fields = [];
    literalExpr.keyValuePairs.every((keyValPair) => {
        let currentName;
        let key = keyValPair.key;
        let val = keyValPair.value;
        if (TreeUtils.isLiteral(key)) {
            currentName = key.getValue().replace(/"/g, '');
        } else {
            currentName = key.getVariableName().getValue();
        }
        if (TreeUtils.isRecordLiteralExpr(val)) {
            let struct = DefaultNodeFactory.createStruct(true);
            if (currentName && currentName !== '') {
                struct.getName().setValue(currentName, true);
                struct.setName(struct.getName(), false);
            }

            success = processJSONStruct(struct, val, topLevelNodes, removeDefaults);
            if (success) {
                topLevelNodes.addTopLevelNodes(struct);
            }
            return success;
        } else if (TreeUtils.isLiteral(val)) {
            currentValue = val.getValue();
            let currentType = 'string';
            let refExpr;
            if (isInt(currentValue)) {
                currentType = 'int';
            } else if (isFloat(currentValue)) {
                currentType = 'float';
            } else if (currentValue === 'true' || currentValue === 'false') {
                currentType = 'boolean';
            }
            if (removeDefaults) {
                refExpr = TreeBuilder.build(FragmentUtils.parseFragment(
                    FragmentUtils.createFieldDefinitionListFragment(`${currentType} ${currentName};`)));
            } else {
                refExpr = TreeBuilder.build(FragmentUtils.parseFragment(
                    FragmentUtils
                        .createFieldDefinitionListFragment(`${currentType} ${currentName} = ${currentValue};`)));
            }
            if (!refExpr.error) {
                // Add ; white space for the field in to record.
                parent.ws.splice(parent.ws.length - 2, 0, {ws: "", text: ";"})
                parent.typeNode.fields.push(refExpr);
            } else {
                success = false;
            }
            return success;
        } else {
            success = false;
        }
    });
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
            cmdID: COMMANDS.SHOW_IMPORT_STRUCT_DIALOG,
            handler: () => {
                const topLevelNodes = plugin.appContext.editor.getActiveEditor().getProperty('ast');
                // Create record with available white spaces in default node.
                const structNode = DefaultNodeFactory.createStruct(true);

                const onImport = (json, structName, removeDefaults) => {
                    let success = true;

                    if (structName && structName !== '') {
                        structNode.name.setValue(structName);
                    }
                    if (json === '') {
                        topLevelNodes.addTopLevelNodes(structNode);
                        return success;
                    }

                    const refExpr = TreeBuilder.build(
                        FragmentUtils.parseFragment(FragmentUtils.createExpressionFragment(json))
                    );
                    if (!refExpr.error) {
                        success = processJSONStruct(structNode, refExpr.variable.initialExpression, topLevelNodes, removeDefaults);
                        if (success) {
                            topLevelNodes.addTopLevelNodes(structNode);
                        }
                    } else {
                        success = false;
                    }
                    return success;
                };

                const { command: { dispatch } } = plugin.appContext;
                const id = DIALOG.IMPORT_STRUCT;

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
