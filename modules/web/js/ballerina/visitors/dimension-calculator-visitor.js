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

import Visitors from './dimension-calculator/components';
import log from 'log';

class DimensionCalculatorVisitor {

    canVisit(node) {
        if (Visitors[`${node.getType()}DimensionCalculatorVisitor`]) {
            const nodeVisitor = new Visitors[`${node.getType()}DimensionCalculatorVisitor`]();
            return nodeVisitor.canVisit(node);
        }
        log.debug(`Unable to find Dimension Calculator for : ${node.getType()}`);


        return false;
    }

    visit(node) {
        if (Visitors[`${node.getType()}DimensionCalculatorVisitor`]) {
            const nodeVisitor = new Visitors[`${node.getType()}DimensionCalculatorVisitor`]();
            return nodeVisitor.visit(node);
        }
        return undefined;
    }

    beginVisit(node) {
        if (Visitors[`${node.getType()}DimensionCalculatorVisitor`]) {
            const nodeVisitor = new Visitors[`${node.getType()}DimensionCalculatorVisitor`]();
            return nodeVisitor.beginVisit(node);
        }
        return undefined;
    }

    endVisit(node) {
        if (Visitors[`${node.getType()}DimensionCalculatorVisitor`]) {
            const nodeVisitor = new Visitors[`${node.getType()}DimensionCalculatorVisitor`]();
            return nodeVisitor.endVisit(node);
        }
        log.warn(`Unable to find Dimension Calculator for : ${node.getType()}`);

        return undefined;
    }
}

export default DimensionCalculatorVisitor;
