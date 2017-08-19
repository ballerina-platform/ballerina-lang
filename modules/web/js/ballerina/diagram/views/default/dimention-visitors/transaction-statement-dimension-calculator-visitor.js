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
import { util } from './../sizing-utils';

/**
 * Dimension visitor class for Transaction Statement.
 *
 * @class TransactionStatementDimensionCalculatorVisitor
 * */
class TransactionStatementDimensionCalculatorVisitor {

    /**
     * can visit the visitor.
     *
     * @return {boolean} true.
     *
     * @memberOf TransactionStatementDimensionCalculatorVisitor
     * */
    canVisit() {
        return true;
    }

    /**
     * begin visiting the visitor.
     *
     * @memberOf TransactionStatementDimensionCalculatorVisitor
     * */
    beginVisit() {
    }

    /**
     * visit the visitor.
     *
     * @memberOf TransactionStatementDimensionCalculatorVisitor
     * */
    visit() {
    }

    /**
     * visit the visitor at the end.
     *
     * @param {ASTNode} node - Transaction Statement node.
     *
     * @memberOf TransactionStatementDimensionCalculatorVisitor
     * */
    endVisit(node) {
        util.populateCompoundStatementChild(node);

        // / Calculate the title width and height as to the keyword width.
        const viewState = node.getViewState();
        viewState.titleWidth = util.getTextWidth('Transaction').w;
    }
}

export default TransactionStatementDimensionCalculatorVisitor;
