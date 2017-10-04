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
import FragmentUtils from './../utils/fragment-utils';
import NodeFactory from './node-factory';
import TreeBuilder from './tree-builder';

/**
 * Creates the node instance for given source fragment
 *
 * @param {Fragment} fragment Source Fragment
 */
function getNodeForFragment(fragment) {
    const parsedJson = FragmentUtils.parseFragment(fragment);
    return TreeBuilder.build(parsedJson);
}

class DefaultNodeFactory {
    /**
     * Create main function
     * @return {Node} function node for main function
     * @memberof DefaultNodeFactory
     * */
    createMainFunction() {
        return getNodeForFragment(
            FragmentUtils.createTopLevelNodeFragment(`
                function main(string[] args) {

                }
            `)
        );
    }

    createHTTPServiceDef() {
        return getNodeForFragment(
            FragmentUtils.createTopLevelNodeFragment(`
                service<http> service1 {

                }
            `)
        );
    }

    createWSServiceDef() {
        return getNodeForFragment(
            FragmentUtils.createTopLevelNodeFragment(`
                service<ws> service1 {
                    
                }
            `)
        );
    }

    createAssignmentStmt() {
        return getNodeForFragment(FragmentUtils.createStatementFragment('a = b;'));
    }

    createVarDefStmt() {
        return getNodeForFragment(FragmentUtils.createStatementFragment('int a = 1;'));
    }

}

export default new DefaultNodeFactory();
