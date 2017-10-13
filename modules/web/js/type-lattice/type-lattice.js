/**
 * Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
 * Holds type lattice to be used for conversions and casts
 * @class TypeLattice
 */
class TypeLattice {

    /**
     * Constructor for type lattice
     * @memberof TypeLattice
     */
    constructor() {
        this._typeLattice = [];
    }

    /**
     * Initialize type lattice from json
     * @param {any} typeLatticeJson type lattice
     * @memberof TypeLattice
     */
    initFromJson(typeLatticeJson) {
        let typeEdges = [];

        typeEdges.push(typeLatticeJson.cast.map((edge) => {
            return {
                source: edge.source,
                target: edge.target,
                safe: edge.safe,
                type: edge.implicit ? 'implicit' : 'explicit',
            };
        }));

        typeEdges.push(typeLatticeJson.conversion.map((edge) => {
            return {
                source: edge.source,
                target: edge.target,
                safe: edge.safe,
                type: 'conversion',
            };
        }));

        typeEdges = [...typeEdges[0], ...typeEdges[1]];

        const typeLattice = {};

        typeEdges.forEach((edge) => {
            if (!edge.visited) {
                const source = edge.source;
                typeLattice[source] = {};
                typeEdges.forEach((targetEdge) => {
                    if ((targetEdge.source === source) && (!targetEdge.visited)) {
                        typeLattice[source][targetEdge.target] = {
                            safe: targetEdge.safe,
                            type: targetEdge.type,
                        };
                        targetEdge.visited = true;
                    }
                });
            }
        });
        this._typeLattice = typeLattice;
    }

    /**
     * Get compatibility for given source and target types
     * @param {any} source source type
     * @param {any} target target type
     * @returns {Object} cast or conversion type and safety
     * @memberof TypeLattice
     */
    getCompatibility(source, target) {
        if (this._typeLattice[source]) {
            return this._typeLattice[source][target];
        }
        return undefined;
    }
}

export default new TypeLattice();
