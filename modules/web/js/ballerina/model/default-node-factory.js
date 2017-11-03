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
            transformer <Source a, Target b> {

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

    createConnectorDeclaration(args) {
        const { connector, packageName, fullPackageName } = args;

        // Iterate through the params and create the parenthesis with the default param values
        let paramString = '';
        if (connector.getParams()) {
            const connectorParams = connector.getParams().map((param) => {
                let defaultValue = Environment.getDefaultValue(param.type);
                if (defaultValue === undefined) {
                    defaultValue = '{}';
                }
                return defaultValue;
            });
            paramString = connectorParams.join(', ')
        }
        const pkgStr = packageName !== 'Current Package' ? `${packageName}:` : '';
        const connectorInit = `${pkgStr}${connector.getName()} endpoint1
                = create ${pkgStr}${connector.getName()}(${paramString});`;
        const connectorDeclaration = getNodeForFragment(FragmentUtils.createStatementFragment(connectorInit));
        connectorDeclaration.getVariable().getInitialExpression().setFullPackageName(fullPackageName);
        connectorDeclaration.viewState.showOverlayContainer = true;
        return connectorDeclaration;
    }

    createEndpoint(args) {
        const { connector, packageName, fullPackageName } = args;

        // Iterate through the params and create the parenthesis with the default param values
        let paramString = '';
        if (connector.getParams()) {
            const connectorParams = connector.getParams().map((param) => {
                let defaultValue = Environment.getDefaultValue(param.type);
                if (defaultValue === undefined) {
                    defaultValue = '{}';
                }
                return defaultValue;
            });
            paramString = connectorParams.join(', ')
        }
        const pkgStr = packageName !== 'Current Package' ? `${packageName}:` : '';
        const connectorInit = `create ${pkgStr}${connector.getName()}(${paramString});`;
        const constraint = `<${pkgStr}${connector.getName()}>`;

        const endpointSource = `endpoint ${constraint} endpoint1 {
            ${connectorInit}
        }`;
        const nodeForFragment = getNodeForFragment(FragmentUtils.createEndpointVarDefFragment(endpointSource));
        return nodeForFragment;
    }

    createConnectorActionInvocationAssignmentStatement(args) {
        const { action, packageName, fullPackageName } = args;

        let actionInvokeString = `endpoint1.`;
        const actionParams = action.getParameters().map((param) => {
            let defaultValue = Environment.getDefaultValue(param.type);
            if (defaultValue === undefined) {
                defaultValue = '{}';
            }
            return defaultValue;
        });
        const paramString = actionParams.join(', ');

        actionInvokeString = `${actionInvokeString}${action.getName()}(${paramString});`;

        const varRefNames = args.action.getReturnParams().map((param, index) => {
            return '_output' + index + 1;
        });

        if (varRefNames.length > 0) {
            const varRefListString = `var ${varRefNames.join(', ')}`;
            actionInvokeString = `${varRefListString} = ${actionInvokeString}`;
        }
        const invocationNode = getNodeForFragment(FragmentUtils.createStatementFragment(actionInvokeString));
        invocationNode.getExpression().setFullPackageName(fullPackageName);
        invocationNode.getExpression().invocationType = 'ACTION';
        invocationNode.getExpression().getPackageAlias().setValue(packageName);
        return invocationNode;
    }

    createFunctionInvocationStatement(args) {
        const { functionDef, packageName, fullPackageName } = args;

        let functionInvokeString = '';
        if (packageName && packageName !== 'Current Package' && packageName !== "builtin" ) {
            functionInvokeString = `${packageName}:`;
        }
        if(functionDef.getReceiverType()){
            functionInvokeString = _.toLower(functionDef.getReceiverType()) + 'Ref.';
        }
        const functionParams = functionDef.getParameters().map((param) => {
            return Environment.getDefaultValue(param.type) || 'null';
        });
        const paramString = functionParams.join(', ');

        functionInvokeString = `${functionInvokeString}${functionDef.getName()}(${paramString});`;

        const varRefNames = args.functionDef.getReturnParams().map((param, index) => {
            return 'variable' + index + 1;
        });

        if (varRefNames.length > 0) {
            const varRefListString = `var ${varRefNames.join(', ')}`;
            functionInvokeString = `${varRefListString} = ${functionInvokeString}`;
        }
        const invocationNode = getNodeForFragment(FragmentUtils.createStatementFragment(functionInvokeString));
        invocationNode.getExpression().setFullPackageName(fullPackageName);
        return invocationNode;
    }
}

export default new DefaultNodeFactory();
