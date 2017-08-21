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

import log from 'log';
import { getDimentionVisitor } from './diagram-util.js';

class DimensionVisitor {

    constructor() {
        this.mode = 'default';
    }

    setMode(mode) {
        this.mode = mode;
    }

    canVisit(node) {
        if (getDimentionVisitor(`${node.getType()}DimensionCalculatorVisitor`, this.mode)) {
            const nodeVisitor = new (getDimentionVisitor(`${node.getType()}DimensionCalculatorVisitor`, this.mode))();
            return nodeVisitor.canVisit(node);
        }
        log.debug(`Unable to find Dimension Calculator for : ${node.getType()}`);


        return false;
    }

    visit(node) {
        if (getDimentionVisitor(`${node.getType()}DimensionCalculatorVisitor`, this.mode)) {
            const nodeVisitor = new (getDimentionVisitor(`${node.getType()}DimensionCalculatorVisitor`, this.mode))();
            return nodeVisitor.visit(node);
        }
        return undefined;
    }

    beginVisit(node) {
        if (getDimentionVisitor(`${node.getType()}DimensionCalculatorVisitor`, this.mode)) {
            const nodeVisitor = new (getDimentionVisitor(`${node.getType()}DimensionCalculatorVisitor`, this.mode))();
            return nodeVisitor.beginVisit(node);
        }
        return undefined;
    }

    endVisit(node) {
        if (getDimentionVisitor(`${node.getType()}DimensionCalculatorVisitor`, this.mode)) {
            const nodeVisitor = new (getDimentionVisitor(`${node.getType()}DimensionCalculatorVisitor`, this.mode))();
            return nodeVisitor.endVisit(node);
        }
        log.warn(`Unable to find Dimension Calculator for : ${node.getType()}`);

        return undefined;
    }
}

export default DimensionVisitor;
