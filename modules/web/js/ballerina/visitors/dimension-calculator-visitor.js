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
import ASTVisitor from './ast-visitor';
import Visitors from './dimension-calc/components';

class DimensionCalculatorVisitor {

    canVisit(node) {
        if(Visitors[node.getType() + 'DimensionCalcVisitor']) {
            var nodeVisitor = new Visitors[node.getType() + 'DimensionCalcVisitor']();
            return nodeVisitor['canVisit' + node.getType() + 'DimensionCalc'](node);
        }

        return undefined;
    }

    visit(node) {
        if (Visitors[node.getType() + 'DimensionCalcVisitor']) {
            var nodeVisitor = new Visitors[node.getType() + 'DimensionCalcVisitor']();
            return nodeVisitor['visit' + node.getType() + 'DimensionCalc'](node);
        }
        return undefined;
    }

    beginVisit(node) {
        if (Visitors[node.getType() + 'DimensionCalcVisitor']) {
            var nodeVisitor = new Visitors[node.getType() + 'DimensionCalcVisitor']();
            return nodeVisitor['beginVisit' + node.getType() + 'DimensionCalc'](node);
        }
        return undefined;
    }

    endVisit(node) {
        if(Visitors[node.getType() + 'DimensionCalcVisitor']) {
            var nodeVisitor = new Visitors[node.getType() + 'DimensionCalcVisitor']();
            return nodeVisitor['endVisit' + node.getType() + 'DimensionCalc'](node);
        }
        return undefined;
    }
}

export default DimensionCalculatorVisitor;
