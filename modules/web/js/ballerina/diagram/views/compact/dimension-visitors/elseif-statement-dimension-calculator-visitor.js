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

import SizingUtil from './../sizing-util';

/**
 * Dimension visitor class for else if statement.
 *
 * @class ElseIfStatementDimensionCalculatorVisitor
 * */
class ElseIfStatementDimensionCalculatorVisitor {

    /**
     * Constructor for Else If statement dimensions
     * @param {object} options - options
     */
    constructor(options) {
        this.sizingUtil = new SizingUtil(options);
    }

    /**
     * can visit the visitor.
     *
     * @return {boolean} true.
     *
     * @memberOf ElseIfStatementDimensionCalculatorVisitor
     * */
    canVisit() {
        return true;
    }

    /**
     * begin visiting the visitor.
     *
     * @memberOf ElseIfStatementDimensionCalculatorVisitor
     * */
    beginVisit() {
    }

    /**
     * visit the visitor.
     *
     * @memberOf ElseIfStatementDimensionCalculatorVisitor
     * */
    visit() {
    }

    /**
     * visit the visitor at the end.
     *
     * @param {ASTNode} node - ElseIf statement node.
     *
     * @memberOf ElseIfStatementDimensionCalculatorVisitor
     * */
    endVisit(node) {
        this.sizingUtil.populateCompoundStatementChild(node, node.getConditionString());
    }
}

export default ElseIfStatementDimensionCalculatorVisitor;
