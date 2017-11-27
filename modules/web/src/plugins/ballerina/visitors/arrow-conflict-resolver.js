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
import _ from 'lodash';
import SimpleBBox from 'plugins/ballerina/model/view/simple-bounding-box';
import TreeUtils from 'plugins/ballerina/model/tree-util';

/**
 * This will vist the tree and resolve any conflicts of arrows with boxes.
 *
 * @class ArrowConflictResolver
 */
class ArrowConflictResolver {

    /**
     * Creates an instance of ArrowConflictResolver.
     * @memberof ArrowConflictResolver
     */
    constructor() {
        this.startGrid();
        this.startWorker();
        this.hasConflicts = true;
    }

    /**
     * Start a new grid, this will be used when you find a new function or resource.
     *
     * @memberof ArrowConflictResolver
     */
    startGrid() {
        this.arrows = [];
        this.workerList = ['default'];
    }

    /**
     * Reset the context when worker visit starts.
     *
     * @memberof ArrowConflictResolver
     */
    startWorker() {
        this.offSet = 0;
        this.connectors = [];
    }

    canVisit() {
        return true;
    }

    visit(node) {
        return undefined;
    }

    beginVisit(node) {
        // init a new list for function.
        // If a function, resource, acction discoved reset the grid lines.
        if (TreeUtils.isFunction(node) ||
            TreeUtils.isResource(node) ||
            TreeUtils.isConnector(node)) {
            this.startGrid();
        }
        // lets collect the connector declarations for this worker.
        if (TreeUtils.isConnectorDeclaration(node)) {
            this.connectors.push(node.getIdentifire());
        }
        // if a worker is found init a new active line
        // TODO : Is worker declaration
        if (TreeUtils.isWorker(node)) {
            this.startWorker();
            this.workerList.push(node.getWorkerName());
        }
        // If the send/receive is the ending box for the arrow reset opaque and arrow.
        if (TreeUtils.isWorkerSend(node) ||
            TreeUtils.isWorkerReceive(node)) {
            if (_.includes(this.workerList, node.getWorkerName())) {
                node.viewState.components['statement-box'].arrow = false;
                node.viewState.components['statement-box'].setOpaque(false);
            }
        }
        // run conflict resolution logic.
        this.resolveConflicts(node);
    }

    endVisit(node) {
        return undefined;
    }

    resolveConflicts(node) {
        node.viewState.offSet = 0;
        const components = node.viewState.components;
        if (components !== undefined) {
            _.forEach(components, (box) => {
                // ditect if there is a conflict for opaque elements.
                if (box instanceof SimpleBBox && box.getOpaque()) {
                    this.arrows.forEach((arrow) => {
                        const boxStart = box.y + this.offSet;
                        const boxEnd = box.y + box.h + this.offSet;
                        const arrowStart = arrow.y;
                        const arrowEnd = arrow.y + arrow.h;
                        if (!(arrowStart >= boxEnd || arrowEnd <= boxStart)) {
                            // If we ditect a conflict we will set an offSet to the view which will -
                            // contribute to the height of the elements drop zone.
                            const increase = arrowEnd - boxStart;
                            node.viewState.offSet = increase + node.viewState.offSet;
                            this.offSet += increase;
                            this.hasConflicts = true;
                        }
                    });
                }
                // Collect if the element is an arrow.
                if (box.arrow) {
                    box.y += node.viewState.offSet;
                    this.arrows.push(box);
                }
            });
        }
    }

    removeArrow(node) {
        this.arrows = this.arrows.filter((arrow) => {
            if ((node.viewState.components['statement-box'].y + this.offSet) === arrow.y) {
                return false;
            }
            return true;
        }, this);
    }

    /**
     * Return true if there are conflicts.
     *
     * @returns {boolean} Returns true if there are conflicts.
     * @memberof ArrowConflictResolver
     */
    hasConflicts() {
        return this.hasConflicts;
    }
}

export default ArrowConflictResolver;
