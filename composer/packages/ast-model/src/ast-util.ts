"use strict";
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

import * as _ from "lodash";

export class ASTUtil {
    /**
     * Will convert json model in to source
     *
     * @static
     * @param {Object} json AST
     * @returns {String} source
     */
    public static getSource(ast: any): string {
        let source = "";
        let ws = ASTUtil.extractWS(ast);
        ws = _.unionBy(ws, "i");
        ws.forEach((element) => {
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

        pot = _.sortBy(pot, ["i"]);
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
        let source = "";
        let ws = ASTUtil.extractWSLoop(ast);
        ws = _.unionBy(ws, "i");
        ws.forEach((element) => {
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
                childName !== "position" &&
                childName !== "ws" &&
                childName !== "parent"
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
        pot = _.sortBy(pot, ["i"]);
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
        nodeWS.forEach((ws) => {
            ws.i = ws.i + diff;
        });
        // get the new last of node ws
        // tslint:disable-next-line:no-console
        const lastIndex = nodeWS[nodeWS.length - 1].i;
        // get the diff for tree
        let treeDiff = lastIndex - startIndex;
        treeDiff = treeDiff + 1;
        // update rest of the tree
        astWS.forEach((ws) => {
            if (ws.i >= startIndex) {
                ws.i = ws.i + treeDiff;
            }
        });
    }

    /**
     * Get the start position to add the new node in given attachment point of the given node.
     *
     * @param node - parent node where the new node to be added
     * @param {string} attachPoint - attachment point where new node going to be added in the node
     * @param {number} insertBefore - if attachment point is a collection where the node to be added
     * @returns {number} - start position for the new node
     */
    public static getStartPosition(node: any, attachPoint: string, insertBefore = -1): number {
        let startPosition = 0;
        let prevPosition = 0;
        switch (node.kind) {
            case "CompilationUnit":
                // If attach point is given as top level nodes, calculate the start position to merge the node.
                if (node.topLevelNodes != null
                    && attachPoint === "topLevelNodes"
                    && node.topLevelNodes.length > 0) {
                    startPosition = ASTUtil.getPositionToInsertBefore(node.topLevelNodes, insertBefore);
                }
                break;
            case "Function":
            case "Resource":
                const functionWS = node.ws;
                const functionOpeningBrace = ASTUtil.findOpeningBrace(functionWS);

                // TODO: handle calculation for start position.
                prevPosition = functionOpeningBrace;

                if (node.endpointNodes != null) {
                    // If attachment point endpointNodes, calculate the position to add the new node in to endpoint
                    // Else calculate the position of the last whitespace token.
                    if (attachPoint === "endpointNodes") {
                        startPosition = ASTUtil.getCollectionStartPosition(node.endpointNodes, functionOpeningBrace,
                            insertBefore);
                    } else if (node.endpointNodes.length > 0) {
                        const endpointWS = ASTUtil.extractWS(node.endpointNodes[node.endpointNodes.length - 1]);
                        prevPosition = endpointWS[endpointWS.length - 1].i;
                    }
                }

                if (node.workers != null) {
                    if (attachPoint === "workers") {
                        startPosition = ASTUtil.getCollectionStartPosition(node.workers, prevPosition, insertBefore);
                    } else if (node.workers.length > 0) {
                        // tslint:disable-next-line:no-shadowed-variable
                        const workerWS = ASTUtil.extractWS(node.workers[node.workers.length - 1]);
                        prevPosition = workerWS[workerWS.length - 1].i;
                    }
                }

                if (node.body != null && attachPoint === "statements") {
                    startPosition = ASTUtil.getCollectionStartPosition(node.body.statements, prevPosition,
                        insertBefore);
                }
                break;
            case "Worker":
                const workerWS = node.ws;
                prevPosition = ASTUtil.findOpeningBrace(workerWS);
                if (node.body != null && attachPoint === "statements") {
                    startPosition = ASTUtil.getCollectionStartPosition(node.body.statements, prevPosition,
                        insertBefore);
                }

                break;
            case "While":
                const whileWS = node.ws;
                prevPosition = ASTUtil.findOpeningBrace(whileWS);
                if (node.body != null && attachPoint === "statements") {
                    startPosition = ASTUtil.getCollectionStartPosition(node.body.statements, prevPosition,
                        insertBefore);
                }
                break;
            case "If":
                const ifWS = node.ws;
                prevPosition = ASTUtil.findOpeningBrace(ifWS);
                if (node.body != null && attachPoint === "statements") {
                    startPosition = ASTUtil.getCollectionStartPosition(node.body.statements, prevPosition,
                        insertBefore);
                }
                break;
            case "Block":
                // If block is a else block continue.
                // Else find the startPosition of the parent node of the block.
                if (node.isElseBlock) {
                    const elseWS = node.ws;
                    prevPosition = ASTUtil.findOpeningBrace(elseWS);
                    if (node.statements && attachPoint === "statements") {
                        startPosition = ASTUtil.getCollectionStartPosition(node.statements, prevPosition,
                            insertBefore);
                    }
                } else {
                    startPosition = ASTUtil.getStartPosition(node.parent, attachPoint);
                }
                break;
            default:
                // If whitespaces available set the startPosition as the opening brace position.
                if (node.ws) {
                    startPosition = ASTUtil.findOpeningBrace(node.ws) + 1;
                }
                break;
        }
        return startPosition;
    }

    /**
     * Get the start position for new node in a collection.
     *
     * @param {any[]} collection - collection which new node going to be added
     * @param {number} entryPoint - position of a entry point such as `{` and `(`
     * @param {number} insertBefore - target position where node to be added in the collection
     * @returns {number}
     */
    private static getCollectionStartPosition(collection: any[], entryPoint: number, insertBefore: number): number {
        return (collection.length > 0) ? ASTUtil.getPositionToInsertBefore(collection, insertBefore) : (entryPoint + 1);
    }

    /**
     * Get the position to insert the new node in to the given
     * collection considering the position to add the node.
     *
     * @param {any[]} collection - collection where new node to be added
     * @param {number} insertBefore - position where new node to be placed
     * @returns {number} - start position for the new node
     */
    private static getPositionToInsertBefore(collection: any[], insertBefore: number): number {
        let startPosition;
        if (collection.length > 0) {
            if (insertBefore === -1) {
                const statementWS = ASTUtil.extractWS(collection[collection.length - 1]);
                startPosition = statementWS[statementWS.length - 1].i + 1;
            } else {
                const statementWS = ASTUtil.extractWS(collection[insertBefore]);
                startPosition = statementWS[0].i;
            }
        }
        return startPosition;
    }

    /**
     * Find the index position of the opening brace token.
     *
     * @param {any[]} ws - ws collection
     * @returns {number} - start position
     */
    private static findOpeningBrace(ws: any[]): number {
        let index = -1;
        // tslint:disable-next-line:prefer-for-of
        for (let i = 0; i < ws.length; i++) {
            if (ws[i].text === "{") {
                index = ws[i].i;
                break;
            }
        }
        return index;
    }
}
