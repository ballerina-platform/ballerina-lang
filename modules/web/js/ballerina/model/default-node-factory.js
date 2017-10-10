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
import TreeBuilder from './tree-builder';
import TreeUtils from './tree-util';
/**
 * Creates the node instance for given source fragment
 *
 * @param {Fragment} fragment Source Fragment
 */
function getNodeForFragment(fragment) {
    const parsedJson = FragmentUtils.parseFragment(fragment);
    const node = TreeBuilder.build(parsedJson);
    node.clearWS();
    return node;
}

class DefaultNodeFactory {

    createHTTPServiceDef() {
        const node = getNodeForFragment(
            FragmentUtils.createTopLevelNodeFragment(
`    
    service<http> service1 {
        resource echo1 (http:Request req, http:Response res) {

        }           
    }
`
            ));
        node.viewState.showOverlayContainer = true;
        return node;
    }

    createWSServiceDef() {
        const node = getNodeForFragment(
            FragmentUtils.createTopLevelNodeFragment(
`
    service<ws> service1 {
        resource onOpen(ws:Connection conn) {

        }
        resource onTextMessage(ws:Connection conn, ws:TextFrame frame) {

        }
        resource onClose(ws:Connection conn, ws:CloseFrame frame) {

        }
    }
`
            ));
        node.viewState.showOverlayContainer = true;
        return node;
    }

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

    createFunction() {
        return getNodeForFragment(
            FragmentUtils.createTopLevelNodeFragment(`
                function function1(string arg1) {

                }
            `)
        );
    }

    createConnector() {
        return getNodeForFragment(
            FragmentUtils.createTopLevelNodeFragment(`
                connector ClientConnector(string url) {

                }
            `)
        );
    }

    createConnectorAction() {
        return getNodeForFragment(
            FragmentUtils.createConnectorActionFragment(`
                action action1(message msg) (message){

                }
            `)
        );
    }

    createHTTPResource() {
        return getNodeForFragment(
            FragmentUtils.createServiceResourceFragment(`
                resource echo1 (http:Request req, http:Response res) {

                }
            `)
        );
    }

    createWSResource(fragment) {
        return getNodeForFragment(
            FragmentUtils.createServiceResourceFragment(fragment)
        );
    }

    createStruct() {
        return getNodeForFragment(
            FragmentUtils.createTopLevelNodeFragment(`
                public struct Person {
                    string name;
                    int age;
                }
            `)
        );
    }

    createWorker() {
        return getNodeForFragment(
            FragmentUtils.createWorkerFragment(`
                worker worker1 {
                }
            `)
        );
    }

    createAnnotation() {
        return getNodeForFragment(
            FragmentUtils.createTopLevelNodeFragment(`
                public annotation Annotation1 {
                    string attrib1;
                }
            `)
        );
    }

    createAssignmentStmt() {
        const node = getNodeForFragment(FragmentUtils.createStatementFragment('a = b;'));
        // Check if the node is a ConnectorDeclaration
        if (TreeUtils.isConnectorDeclaration(node)) {
            node.viewState.showOverlayContainer = true;
            return node;
        }
        return node;
    }

    createVarDefStmt() {
        const node = getNodeForFragment(FragmentUtils.createStatementFragment('int a = 1;'));
        // Check if the node is a ConnectorDeclaration
        if (TreeUtils.isConnectorDeclaration(node)) {
            node.viewState.showOverlayContainer = true;
            return node;
        }
        return node;
    }

    createIf() {
        return getNodeForFragment(FragmentUtils.createStatementFragment(`
            if (true) {
                
            }
        `));
    }

    createInvocation() {
        return getNodeForFragment(FragmentUtils.createStatementFragment(`
            callFunction(arg1);cc
        `));
    }

    createWhile() {
        return getNodeForFragment(FragmentUtils.createStatementFragment(`
            while(true) {
                
            }
        `));
    }

    createTransform() {
        return getNodeForFragment(FragmentUtils.createStatementFragment(`
            transform {
                
            }
        `));
    }

    createBreak() {
        return getNodeForFragment(FragmentUtils.createStatementFragment(`
            break;
        `));
    }

    createContinue() {
        return getNodeForFragment(FragmentUtils.createStatementFragment(`
            continue;
        `));
    }

    createTry() {
        return getNodeForFragment(FragmentUtils.createStatementFragment(`
            try {

            } catch (errors:Error e) {

            }
        `));
    }

    createThrow() {
        return getNodeForFragment(FragmentUtils.createStatementFragment(`
            throw e;
        `));
    }

    createReturn() {
        return getNodeForFragment(FragmentUtils.createStatementFragment(`
            return m;
        `));
    }

    createWorkerInvocation() {
        return getNodeForFragment(FragmentUtils.createStatementFragment(`
            m -> worker1 ;
        `));
    }

    createWorkerReceive() {
        return getNodeForFragment(FragmentUtils.createStatementFragment(`
            m <- worker1 ;
        `));
    }

    // FIXME
    createTransaction() {
        return getNodeForFragment(FragmentUtils.createStatementFragment(`
            transaction {
                
            }
        `));
    }

    createAbort() {
        return getNodeForFragment(FragmentUtils.createStatementFragment(`
            abort;
        `));
    }

    // FIXME
    createRetry() {
        return getNodeForFragment(FragmentUtils.createStatementFragment(`
            retry m;
        `));
    }

    createForkJoin() {
        return getNodeForFragment(FragmentUtils.createStatementFragment(`
            fork {

            };
        `));
    }

    createXmlQname() {
        return getNodeForFragment(FragmentUtils.createStatementFragment(`
            xmlns "namespace.uri" as xn;
        `));
    }

}

export default new DefaultNodeFactory();
