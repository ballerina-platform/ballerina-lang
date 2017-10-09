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
class OperatorLattice {

    /**
     * Constructor for operator lattice
     * @memberof OperatorLattice
     */
    constructor() {
        this._unaryLattice = [];
        this._binaryLattice = [];
        this._unaryOperators = [];
        this._binaryOperators = [];
    }

    getUnaryOperators() {
        return this._unaryOperators;
    }

    getBinaryOperators() {
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
        this._unaryOperators = [];
        this._binaryOperators = [];

        Object.keys(operatorLatticeJson).forEach((operator) => {
            const operatorLatticeList = operatorLatticeJson[operator];
            const unaryLatticeJson = operatorLatticeList.filter(edge => edge.type === 'unary');
            const binaryLatticeJson = operatorLatticeList.filter(edge => edge.type === 'binary');

            if (unaryLatticeJson.length > 0) {
                this._unaryOperators.push(operator);
            }
            if (binaryLatticeJson.length > 0) {
                this._binaryOperators.push(operator);
            }

            unaryLatticeJson.forEach((edge) => {
                if (!edge.visited) {
                    this._unaryLattice[operator] = {};
                    if (!edge.visited) {
                        unaryLatticeJson.forEach((targetEdge) => {
                            if (!targetEdge.visited) {
                                this._unaryLattice[operator][targetEdge.rhType] = targetEdge.retType;
                                targetEdge.visited = true;
                            }
                        });
                    }
                }
            });
    
            binaryLatticeJson.forEach((edge) => {
                if (!edge.visited) {
                    this._binaryLattice[operator] = {};
                    binaryLatticeJson.forEach((targetEdge) => {
                        if (!targetEdge.visited) {
                            if (!this._binaryLattice[operator][targetEdge.lhType]) {
                                this._binaryLattice[operator][targetEdge.lhType] = {};
                            }
                            this._binaryLattice[operator][targetEdge.lhType][targetEdge.rhType] = targetEdge.retType;
                            targetEdge.visited = true;
                        }
                    });
                }
            });
        });

        // typeof is a special type that is not a part of the type lattice, hence handled manually
        this._binaryOperators.push('typeof');
    }

    /**
     * Get operator compatibility for given types
     * @param {string} operator operator
     * @param {string} lhType left hand type
     * @param {string} rhType right hand type
     * @param {string} retType return type
     * @returns {[string]} compatible types
     * @memberof OperatorLattice
     */
    getCompatibleBinaryTypes(operator, lhType, rhType, retType) {
        if (operator === 'typeof') {
            // typeof is a special binary operator that needs to be handled specially
            // TODO
        }
        if (lhType && rhType && !retType) {
            return this._binaryLattice[operator][lhType][rhType];
        }
        return undefined;
    }

    /**
     * Get operator compatibility for given types
     * @param {string} operator operator
     * @param {string} rhType right hand type
     * @param {string} retType return type
     * @returns {[string]} compatible types
     * @memberof OperatorLattice
     */
    getCompatibleUnaryTypes(operator, rhType, retType) {
        if (rhType && !retType) {
            return this._unaryLattice[operator][rhType];
        }
        return undefined;
    }
}

export default new OperatorLattice();
