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
        ws.forEach(element => {
            source += element.ws + element.text;
        });
        return source;
    }

    public static extractWS(node: any): any[] {
        let pot: any[] = [];

        node.accept({
            beginVisit: (item: any) => {
                if (item.ws !== undefined) {
                    pot = pot.concat(item.ws);
                }
            },
            endVisit: (item: any) => {
                // do nothing
            },
        });

        pot = _.sortBy(pot, ['i']);
        return pot;
    }

    /**
     * Will convert json model in to source for testing
     *
     * @static
     * @param {Object} json AST
     * @returns {String} source
     */
    public static getSourceForTesting(ast: any): string {
        let source = '';
        let ws = ASTUtil.extractWSLoop(ast);
        ws = _.unionBy(ws, 'i');
        ws.forEach(element => {
            source += element.ws + element.text;
        });
        return source;
    }

    public static extractWSLoop(json: any): any[] {
        let childName;
        let pot: any[] = [];

        if (json.ws !== undefined) {
            pot = pot.concat(json.ws);
        }
        // with the following loop we will recursivle dive in to the child nodes and extract ws.
        for (childName in json) {
            // if child name is position || whitespace skip convection.
            if (
                childName !== 'position' &&
                childName !== 'ws' &&
                childName !== 'parent'
            ) {
                const child = json[childName];
                if (_.isPlainObject(child)) {
                    pot = pot.concat(ASTUtil.extractWSLoop(child));
                } else if (child instanceof Array) {
                    for (const item of child) {
                        pot = pot.concat(ASTUtil.extractWSLoop(item));
                    }
                }
            }
        }
        pot = _.sortBy(pot, ['i']);
        return pot;
    }

    public static reconcileWS(node: any, attachPoint: any[], tree: any, startIndex = -1): void {
        // get ws of attach node
        let attachWS: any[] = [];
        if (attachPoint.length > 0) {
            attachWS = ASTUtil.extractWS(attachPoint[attachPoint.length - 1]);
        }
        const nodeWS = ASTUtil.extractWS(node);
        const astWS = ASTUtil.extractWS(tree);
        // find the last of attach node

        if (startIndex === -1) {
            // tslint:disable-next-line:prefer-conditional-expression
            if (attachPoint.length > 0) {
                startIndex = attachWS[attachWS.length - 1].i + 1;
            } else {
                startIndex = 0;
            }
        }

        // tslint:disable-next-line:no-console
        const nodeFirstIndex = nodeWS[0].i;
        // get the diff from node to last
        const diff = startIndex - nodeFirstIndex;
        // update node ws
        nodeWS.forEach(ws => {
            ws.i = ws.i + diff;
        });
        // get the new last of node ws
        // tslint:disable-next-line:no-console
        const lastIndex = nodeWS[nodeWS.length - 1].i;
        // get the diff for tree
        let treeDiff = lastIndex - startIndex;
        treeDiff = treeDiff + 1;
        // update rest of the tree
        astWS.forEach(ws => {
            if (ws.i >= startIndex) {
                ws.i = ws.i + treeDiff;
            }
        });
    }
}
