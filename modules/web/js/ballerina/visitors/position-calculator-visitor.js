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

import Visitors from './position-calc/components';

class PositionCalculatorVisitor {

    canVisit(node) {
        if(Visitors[node.getType() + 'PositionCalcVisitor']) {
            var nodeVisitor = new Visitors[node.getType() + 'PositionCalcVisitor']();
            return nodeVisitor['canVisit'](node);
        }else{
            console.log("Unable to find position visitor for : " + node.getType());
        }

        return undefined;
    }

    visit(node) {
        if (Visitors[node.getType() + 'PositionCalcVisitor']) {
            var nodeVisitor = new Visitors[node.getType() + 'PositionCalcVisitor']();
            return nodeVisitor['visit'](node);
        }
        return undefined;
    }

    beginVisit(node) {
        if (Visitors[node.getType() + 'PositionCalcVisitor']) {
            var nodeVisitor = new Visitors[node.getType() + 'PositionCalcVisitor']();
            return nodeVisitor['beginVisit'](node);
        }
        return undefined;
    }

    endVisit(node) {
        if(Visitors[node.getType() + 'PositionCalcVisitor']) {
            var nodeVisitor = new Visitors[node.getType() + 'PositionCalcVisitor']();
            return nodeVisitor['endVisit'](node);
        }
        return undefined;
    }
}

export default PositionCalculatorVisitor;
