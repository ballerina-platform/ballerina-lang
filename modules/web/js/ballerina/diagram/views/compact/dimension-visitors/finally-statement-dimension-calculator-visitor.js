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
 * Dimension visitor class for finally statement.
 *
 * @class FinallyStatementDimensionCalculatorVisitor
 * */
class FinallyStatementDimensionCalculatorVisitor {

    /**
     * Constructor for Committed statement dimensions
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
     * @memberOf FinallyStatementDimensionCalculatorVisitor
     * */
    canVisit() {
        return true;
    }

    /**
     * begin visiting the visitor.
     *
     * @memberOf FinallyStatementDimensionCalculatorVisitor
     * */
    beginVisit() {
    }

    /**
     * visit the visitor.
     *
     * @memberOf FinallyStatementDimensionCalculatorVisitor
     * */
    visit() {
    }

    /**
     * visit the visitor at the end.
     *
     * @param {ASTNode} node - Finally statement node.
     *
     * @memberOf FinallyStatementDimensionCalculatorVisitor
     * */
    endVisit(node) {
        this.sizingUtil.populateCompoundStatementChild(node);
    }
}

export default FinallyStatementDimensionCalculatorVisitor;
