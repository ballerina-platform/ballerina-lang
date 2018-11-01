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

/**
 * Holds operator lattice to be used for ternary, binary and unary operators
 * @class OperatorLattice
 */

import _ from 'lodash';

class OperatorLattice {

    /**
     * Constructor for operator lattice
     * @memberof OperatorLattice
     */
    constructor() {
        this._unaryLattice = [];
        this._binaryLattice = [];
        this._ternaryLattice = [];
        this._unaryOperators = [];
        this._binaryOperators = [];
        this._ternaryOperators = [];
    }

    /**
     * Get unary operators
     * @returns [{string}] unary operators
     * @memberof OperatorLattice
     */
    getUnaryOperators() {
        return this._unaryOperators;
    }

    /**
     * Get binary operators
     * @returns [{string}] binary operators
     * @memberof OperatorLattice
     */
    getBinaryOperators() {
        return this._binaryOperators;
    }

    /**
     * Get ternary operators
     * @returns [{string}] ternary operators
     * @memberof OperatorLattice
     */
    getTernaryOperators() {
        return this._binaryOperators;
    }

    /**
     * Initialize operator lattice from json
     * @param {any} operatorLatticeJson operator lattice
     * @memberof OperatorLattice
     */
    initFromJson(operatorLatticeJson) {
        this._unaryLattice = [];
        this._binaryLattice = [];
        this._ternaryLattice = [];
        this._unaryOperators = [];
        this._binaryOperators = [];
        this._ternaryOperators = [];

        Object.keys(operatorLatticeJson).forEach((operator) => {
            const operatorLatticeList = operatorLatticeJson[operator];
            const unaryLatticeJson = operatorLatticeList.filter(edge => edge.type === 'unary');
            const binaryLatticeJson = operatorLatticeList.filter(edge => edge.type === 'binary');
            const ternaryLatticeJson = operatorLatticeList.filter(edge => edge.type === 'ternary');

            if (unaryLatticeJson.length > 0) {
                this._unaryOperators.push(operator);
            }
            if (binaryLatticeJson.length > 0) {
                this._binaryOperators.push(operator);
            }
            if (ternaryLatticeJson.length > 0) {
                this._ternaryLattice.push(operator);
            }

            unaryLatticeJson.forEach((edge) => {
                if (!edge.visited) {
                    this._unaryLattice[operator] = [];
                    if (!edge.visited) {
                        unaryLatticeJson.forEach((targetEdge) => {
                            if (!targetEdge.visited) {
                                this._unaryLattice[operator].push({
                                    0: targetEdge.rhType,
                                    ret: targetEdge.retType,
                                });
                                targetEdge.visited = true;
                            }
                        });
                    }
                }
            });

            binaryLatticeJson.forEach((edge) => {
                if (!edge.visited) {
                    this._binaryLattice[operator] = [];
                    binaryLatticeJson.forEach((targetEdge) => {
                        if (!targetEdge.visited) {
                            this._binaryLattice[operator].push({
                                0: targetEdge.lhType,
                                1: targetEdge.rhType,
                                ret: targetEdge.retType,
                            });
                            targetEdge.visited = true;
                        }
                    });
                }
            });

            ternaryLatticeJson.forEach((edge) => {
                if (!edge.visited) {
                    this._ternaryLattice[operator] = edge;
                }
            });
        });

        // typeof is a special type that is not a part of the type lattice, hence handled manually
        this._binaryOperators.push('typeof');
    }

    /**
     * Get compatibile types for a given operator's operand
     * @param {string} operator operator kind
     * @param {[string]} operandTypes operand types of the operator
     * @param {string} operandIndex index of the operand to find compatible types
     * @returns {[string]} compatible types
     * @memberof OperatorLattice
     */
    getCompatibleBinaryTypes(operator, operandTypes, operandIndex) {
        if (operator === 'typeof') {
            // typeof is a special binary operator that needs to be handled specially
            // TODO
        }
        const opLattice = this._binaryLattice[operator];
        if (!opLattice) {
            return undefined;
        }

        const checkIndex = _.without([0, 1], operandIndex);

        return opLattice.filter((edge) => {
            if ((operandTypes[checkIndex] === edge[checkIndex]) && (edge.ret === operandTypes.ret)) {
                return true;
            }
            return false;
        }).map((edge) => {
            return edge[operandIndex];
        });
    }

    /**
     * Get operator compatibility for given types
     * @param {string} operator operator
     * @param {string} rhType right hand type
     * @returns {[string]} compatible types
     * @memberof OperatorLattice
     */
    getCompatibleUnaryTypes(operator, rhType) {
        if (rhType) {
            return this._unaryLattice[operator][rhType];
        } else {
            return this._unaryLattice[operator];
        }
    }

    /**
     * Get operator compatibility for given types
     * @param {string} operator operator
     * @param {string} index index of the operand
     * @returns {[string]} compatible types
     * @memberof OperatorLattice
     */
    getCompatibleTernaryTypes(operator, index) {
        if (index === 0) {
            return this._ternaryLattice[operator].conType;
        }
        return this._ternaryLattice[operator].conType;
    }
}

export default new OperatorLattice();
