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
import FragmentUtils from '../utils/fragment-utils';
import TreeBuilder from './tree-builder';
import TreeUtils from './tree-util';
import Environment from '../env/environment';

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

/**
 * Default node factory class.
 * This creates all the default node for each model.
 * This is mostly used on drag and drop.
 * */
class DefaultNodeFactory {

    createHTTPServiceDef() {
        const node = getNodeForFragment(
            FragmentUtils.createTopLevelNodeFragment(
`
    service<http> service1 {
        resource echo1 (http:Request req, http:Response res) {

        }
    }
`,
            ));
        node.viewState.shouldShowConnectorPropertyWindow = true;
        node.setFullPackageName('ballerina.net.http');
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
`,
            ));
        node.viewState.shouldShowConnectorPropertyWindow = true;
        node.setFullPackageName('ballerina.net.ws');
        return node;
    }

    createJMSServiceDef() {
        const node = getNodeForFragment(
            FragmentUtils.createTopLevelNodeFragment(
                `
    service<jms> service1 {
        resource echo1 (jms:JMSMessage request) {

        }
    }
`,
            ));
        node.viewState.shouldShowConnectorPropertyWindow = true;
        node.setFullPackageName('ballerina.net.jms');
        return node;
    }

    createFSServiceDef() {
        const node = getNodeForFragment(
            FragmentUtils.createTopLevelNodeFragment(
                `
    service<fs> service1 {
        resource echo1 (fs:FileSystemEvent m) {

        }
    }
`,
            ));
        node.viewState.shouldShowConnectorPropertyWindow = true;
        node.setFullPackageName('ballerina.net.fs');
        return node;
    }

    createFTPServiceDef() {
        const node = getNodeForFragment(
            FragmentUtils.createTopLevelNodeFragment(
                `
    service<ftp> service1 {
        resource echo1 (ftp:FTPServerEvent m) {

        }
    }
`,
            ));
        node.viewState.shouldShowConnectorPropertyWindow = true;
        node.setFullPackageName('ballerina.net.ftp');
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
            `),
        );
    }

    createFunction() {
        return getNodeForFragment(
            FragmentUtils.createTopLevelNodeFragment(`
                function function1() {

                }
            `),
        );
    }

    createConnector() {
        return getNodeForFragment(
            FragmentUtils.createTopLevelNodeFragment(`
                connector ClientConnector(string url) {
                    action action1(){

                    }
                }
            `),
        );
    }

    createConnectorAction() {
        return getNodeForFragment(
            FragmentUtils.createConnectorActionFragment(`
                action action1(){

                }
            `),
        );
    }

    createHTTPResource() {
        return getNodeForFragment(
            FragmentUtils.createServiceResourceFragment(`
                resource echo1 (http:Request req, http:Response res) {

                }
            `),
        );
    }

    createFSResource() {
        return getNodeForFragment(
            FragmentUtils.createServiceResourceFragment(`
                resource echo1 (fs:FileSystemEvent m) {

                }
            `),
        );
    }

    createFTPResource() {
        return getNodeForFragment(
            FragmentUtils.createServiceResourceFragment(`
                resource echo1 (ftp:FTPServerEvent m) {

                }
            `),
        );
    }

    createJMSResource() {
        return getNodeForFragment(
            FragmentUtils.createServiceResourceFragment(`
                resource echo1 (jms:JMSMessage request) {

                }
            `),
        );
    }

    createWSResource(fragment) {
        return getNodeForFragment(
            FragmentUtils.createServiceResourceFragment(fragment),
        );
    }

    createStruct() {
        return getNodeForFragment(
            FragmentUtils.createTopLevelNodeFragment(`
                struct struct1 {

                }
            `),
        );
    }

    createTransformer() {
        return getNodeForFragment(FragmentUtils.createTopLevelNodeFragment(`
            transformer <Source a, Target b> newTransformer (){

            }
        `));
    }

    createWorker() {
        let worker =  getNodeForFragment(
            FragmentUtils.createWorkerFragment(`
                worker worker1 {
                }
            `),
        );
        // here we will send the default worker as a meta item.
        worker.meta =  getNodeForFragment(
            FragmentUtils.createWorkerFragment(`
                worker default {
                }
            `),
        );
        return worker;
    }

    createAnnotation() {
        return getNodeForFragment(
            FragmentUtils.createTopLevelNodeFragment(`
                annotation annotation1 {
                }
            `),
        );
    }

    createAssignmentStmt() {
        const node = getNodeForFragment(FragmentUtils.createStatementFragment('var a = 1;'));
        // Check if the node is a ConnectorDeclaration
        if (TreeUtils.isEndpointTypeVariableDef(node)) {
            node.viewState.showOverlayContainer = true;
            return node;
        }
        return node;
    }

    createBindStmt() {
        return getNodeForFragment(FragmentUtils.createStatementFragment('bind __connector with __endpoint;'));
    }

    createVarDefStmt() {
        const node = getNodeForFragment(FragmentUtils.createStatementFragment('int a = 1;'));
        // Check if the node is a ConnectorDeclaration
        if (TreeUtils.isEndpointTypeVariableDef(node)) {
            node.viewState.showOverlayContainer = true;
            return node;
        }
        return node;
    }

    createIf() {
        return getNodeForFragment(FragmentUtils.createStatementFragment(`
            if (true) {

            } else {
            
            }
        `));
    }

    createIfElse(){
        return getNodeForFragment(FragmentUtils.createStatementFragment(`
            if (true) {

            } else if (true) {
            
            } else {
            
            }
        `));
    }

    createInvocation() {
        return getNodeForFragment(FragmentUtils.createStatementFragment(`
            invokeFunction(arg1);
        `));
    }

    createWhile() {
        return getNodeForFragment(FragmentUtils.createStatementFragment(`
            while(true) {

            }
        `));
    }

    createBreak() {
        return getNodeForFragment(FragmentUtils.createStatementFragment(`
            break;
        `));
    }

    createNext() {
        return getNodeForFragment(FragmentUtils.createStatementFragment(`
            next;
        `));
    }

    createTry() {
        return getNodeForFragment(FragmentUtils.createStatementFragment(`
            try {

            } catch (error err) {

            } finally {
            
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

            } failed {

            } aborted {
            
            } committed {
            
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
        const tempNode = getNodeForFragment(FragmentUtils.createTransactionFailedFragment(`
            retry 3;
        `));
        return tempNode.failedBody.statements[0];
    }

    createForkJoin() {
        return getNodeForFragment(FragmentUtils.createStatementFragment(`
            fork {
                worker worker1 {
                }
                worker worker2 {
                }
            } join(all)(map results) {
            
            } timeout(100)(map results1) {
            
            }
        `));
    }

    createXmlns() {
        return getNodeForFragment(FragmentUtils.createStatementFragment(`
            xmlns "namespace.uri" as xn;
        `));
    }

    createEnum() {
        return getNodeForFragment(FragmentUtils.createTopLevelNodeFragment(`
            enum name {
                ENUMERATOR
            }
        `))
    }

    createEnumerator(enumerator) {
        return getNodeForFragment(FragmentUtils.createTopLevelNodeFragment(`
        enum name {
         ${enumerator}
        }
        `))
    }

    createConnectorDeclaration(args) {
        const node = getNodeForFragment(FragmentUtils.createStatementFragment('http:HttpClient endpoint1 = create http:HttpClient("",{});'));
        const { connector, packageName, fullPackageName } = args;

        // Iterate through the params
        let parameters = [];
        if (connector.getParams()) {
            const connectorParams = connector.getParams().map((param) => {
                let defaultValue = Environment.getDefaultValue(param.type);
                if (defaultValue === undefined) {
                    defaultValue = '{}';
                }
                const paramNode = getNodeForFragment(FragmentUtils.createExpressionFragment(defaultValue));
                parameters.push(paramNode.getVariable().getInitialExpression());
            });
            node.getVariable().getInitialExpression().setExpressions(parameters);
        }
        const pkgStr = packageName !== 'Current Package' ? `${packageName}` : '';
        node.getVariable().getTypeNode().getTypeName().setValue(connector.getName());
        node.getVariable().getTypeNode().getPackageAlias().setValue(pkgStr);
        node.getVariable().getInitialExpression().getConnectorType().getPackageAlias().setValue(pkgStr);
        node.getVariable().getInitialExpression().getConnectorType().getTypeName().setValue(connector.getName());
        node.getVariable().getInitialExpression().setFullPackageName(fullPackageName);
        node.viewState.showOverlayContainer = true;
        return node;
    }

    createEndpoint(args) {
        const node = getNodeForFragment(FragmentUtils.createEndpointVarDefFragment(`
            endpoint <http:HttpClient> endpoint1 {
                create http:HttpClient("",{});
            }
        `));
        const { connector, packageName, fullPackageName } = args;

        // Iterate through the params
        let parameters = [];
        if (connector.getParams()) {
            const connectorParams = connector.getParams().map((param) => {
                let defaultValue = Environment.getDefaultValue(param.type);
                if (defaultValue === undefined) {
                    defaultValue = '{}';
                }
                const paramNode = getNodeForFragment(FragmentUtils.createExpressionFragment(defaultValue));
                parameters.push(paramNode.getVariable().getInitialExpression());
            });
            node.getVariable().getInitialExpression().setExpressions(parameters);
        }
        const pkgStr = packageName !== 'Current Package' ? `${packageName}` : '';
        node.getVariable().getTypeNode().getConstraint().getTypeName().setValue(connector.getName());
        node.getVariable().getTypeNode().getConstraint().getPackageAlias().setValue(pkgStr);
        node.getVariable().getInitialExpression().getConnectorType().getPackageAlias().setValue(pkgStr);
        node.getVariable().getInitialExpression().getConnectorType().getTypeName().setValue(connector.getName());
        node.getVariable().getInitialExpression().setFullPackageName(fullPackageName);
        node.viewState.showOverlayContainer = true;
        node.getVariable().getInitialExpression().setFullPackageName(fullPackageName);
        return node;
    }

    createConnectorActionInvocationAssignmentStatement(args) {
        let node;
        if (args.action.getReturnParams().length > 0) {
            node = getNodeForFragment(FragmentUtils.createStatementFragment('var var1 = __endpoint1.select("",{});'));
        } else {
            node = getNodeForFragment(FragmentUtils.createStatementFragment(' __endpoint1.close();'));
        }
        const { action, packageName, fullPackageName } = args;
        let parameters = [];
        // Iterate through the params
        if (action.getParameters()) {
            action.getParameters().map((param) => {
                let defaultValue = Environment.getDefaultValue(param.type);
                if (defaultValue === undefined) {
                    defaultValue = '{}';
                }
                const paramNode = getNodeForFragment(FragmentUtils.createExpressionFragment(defaultValue));
                parameters.push(paramNode.getVariable().getInitialExpression());
            });
            node.getExpression().setArgumentExpressions(parameters);
        }
        // Iterate through the return types
        const varRefNames = args.action.getReturnParams().map((param, index) => {
            return '_output' + index + 1;
        });

        if (varRefNames.length > 0) {
            const varRefListString = `var ${varRefNames.join(', ')} = function1();`;
            const returnNode = getNodeForFragment(FragmentUtils.createStatementFragment(varRefListString));
            node.setVariables(returnNode.getVariables());
        }
        node.getExpression().getName().setValue(action.getName());
        node.getExpression().setFullPackageName(fullPackageName);
        node.getExpression().invocationType = 'ACTION';
        node.getExpression().getPackageAlias().setValue(packageName);
        return node;
    }

    createFunctionInvocationStatement(args) {
        let node;
        if (args.functionDef.getReturnParams().length > 0) {
            node = getNodeForFragment(FragmentUtils.createStatementFragment('var var1 = timeRef.addDuration(0,0,0,0,0,0,0);'));
        } else {
            node = getNodeForFragment(FragmentUtils.createStatementFragment('requestRef.addHeader("","");'));
        }
        const { functionDef, packageName, fullPackageName } = args;

        if(functionDef.getReceiverType()){
            const receiverName = _.toLower(functionDef.getReceiverType()) + 'Ref';
            node.getExpression().getExpression().getVariableName().setValue(receiverName);
        }

        // Iterate over the parameters
        if (functionDef.getParameters()) {
           let parameters = [];
           functionDef.getParameters().map((param) => {
               let defaultValue = Environment.getDefaultValue(param.type);
               const paramNode = getNodeForFragment(FragmentUtils.createExpressionFragment(defaultValue));
               parameters.push(paramNode.getVariable().getInitialExpression());
            });
            node.getExpression().setArgumentExpressions(parameters);
        }

        // Iterate over the return types
        const varRefNames = args.functionDef.getReturnParams().map((param, index) => {
            return 'variable' + index + 1;
        });
        if (varRefNames.length > 0) {
            const varRefListString = `var ${varRefNames.join(', ')} = function1();`;
            const returnNode = getNodeForFragment(FragmentUtils.createStatementFragment(varRefListString));
            node.setVariables(returnNode.getVariables());
        }
        node.getExpression().getName().setValue(functionDef.getName());
         if (packageName && packageName !== 'Current Package' && packageName !== 'builtin' ) {
            node.getExpression().getPackageAlias().setValue(packageName);
         }
        node.getExpression().setFullPackageName(fullPackageName);
        return node;
    }
}

export default new DefaultNodeFactory();
