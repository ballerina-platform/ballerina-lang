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
    }

    /**
     * Initialize operator lattice from json
     * @param {any} operatorLatticeJson operator lattice
     * @memberof OperatorLattice
     */
    initFromJson(operatorLatticeJson) {
        const unaryLattice = {};
        const binaryLattice = {};

        const unaryLatticeJson = operatorLatticeJson.filter(edge => edge.type === 'unary');
        const binaryLatticeJson = operatorLatticeJson.filter(edge => edge.type === 'binary');

        unaryLatticeJson.forEach((edge) => {
            if (!edge.visited) {
                const operator = edge.operator;
                unaryLattice[operator] = {};
                if (!edge.visited) {
                    unaryLatticeJson.forEach((targetEdge) => {
                        if ((targetEdge.operator === operator) && (!targetEdge.visited)) {
                            unaryLattice[operator][targetEdge.rhType] = targetEdge.retType;
                            targetEdge.visited = true;
                        }
                    });
                }
            }
        });

        binaryLatticeJson.forEach((edge) => {
            if (!edge.visited) {
                const operator = edge.operator;
                binaryLattice[operator] = {};
                if (!edge.visited) {
                    binaryLatticeJson.forEach((targetEdge) => {
                        if ((targetEdge.operator === operator) && (!targetEdge.visited)) {
                            if (!binaryLattice[operator][targetEdge.lhType]) {
                                binaryLattice[operator][targetEdge.lhType] = {};
                            }
                            binaryLattice[operator][targetEdge.lhType][targetEdge.rhType] = targetEdge.retType;
                            targetEdge.visited = true;
                        }
                    });
                }
            }
        });
        this._unaryLattice = unaryLattice;
        this._binaryLattice = binaryLattice;
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
