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
import ASTNode from '../node';

/**
 * Class to represent a statement in ballerina.
 * @constructor
 */
class Statement extends ASTNode {

    /**
     * Get all the connector references in the immediate scope
     * @return {Array} connectorReferences
     */
    getConnectorsInImmediateScope() {
        return this.getParent().getConnectorsInImmediateScope();
    }
    /**
     * get the string from expression editor
     * call fragment parser and get parse tree of the node
     * validate and create children from scratch
     * @throws {Error}
     * @returns {void}
     */
    setStatementFromString() {
        throw new Error('This method should be implemented by sub class');
    }

    /**
     * Traverse the children and generate a string to show up in expression editor
     * @throws {Error}
     * @returns {string} statement string
     */
    getStatementString() {
        throw new Error('This method should be implemented by sub class');
    }
}

export default Statement;
