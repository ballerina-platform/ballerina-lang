'use strict';
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
 *
 */

import * as _ from 'lodash';

export class ASTUtil {
    /**
     * Will convert json model in to source
     *
     * @static
     * @param {Object} json AST
     * @returns {String} source
     */
    public static getSource(ast: any): string {
        let source = '';
        let ws = ASTUtil.extractWS(ast);
        ws = _.unionBy(ws, 'i');
        ws = _.sortBy(ws, ['i']);
        ws.forEach(element => {
            source += element.ws + element.text;
        });
        return source;
    }

    public static extractWS(json: any): any[] {
        let childName;
        let pot: any[] = [];

        if (json.ws !== undefined) {
            pot = pot.concat(json.ws);
        }
        // with the following loop we will recursivle dive in to the child nodes and extract ws.
        for (childName in json) {
            // if child name is position || whitespace skip convection.
            if (childName !== 'position' && childName !== 'ws') {
                const child = json[childName];
                if (_.isPlainObject(child)) {
                    pot = pot.concat(ASTUtil.extractWS(child));
                } else if (child instanceof Array) {
                    for (const item of child) {
                        pot = pot.concat(ASTUtil.extractWS(item));
                    }
                }
            }
        }

        return pot;
    }
}
