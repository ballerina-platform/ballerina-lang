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

/**
 * Finds which nodes are mouse over in the diagram
 * @class HoverItemVisiter
 */
class HoverItemVisiter {
    /**
     * Creates an instance of HoverItemVisiter.
     * @memberof HoverItemVisiter
     */
    constructor() {
        this._hoveredItems = [];
    }
    /**
     * @inheritdoc
     */
    beginVisit(node) {
        if (node.viewState.hovered) {
            this._hoveredItems.push(node);
        }
    }

    getHoveredItems() {
        return this._hoveredItems;
    }

    /**
     * @inheritdoc
     */
    canVisit() {
        return true;
    }

    /**
     * At the end of node visit.
     * @returns {any} Visit output.
     * @memberof HoverItemVisiter
     */
    endVisit() {
        return undefined;
    }
}

export default HoverItemVisiter;
