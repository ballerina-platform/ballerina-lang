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
import DefaultNodes from './default-nodes';

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

function getStaticDefaultNode(fragmentName) {
    const parsedJson = DefaultNodes[fragmentName]
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
        const node = getStaticDefaultNode('createHTTPServiceDef');
        node.viewState.shouldShowConnectorPropertyWindow = true;
        node.setFullPackageName('ballerina.net.http');
        return node;
    }

    createWSServiceDef() {
        const node = getStaticDefaultNode('createWSServiceDef')
        node.viewState.shouldShowConnectorPropertyWindow = true;
        node.setFullPackageName('ballerina.net.ws');
        return node;
    }

    createJMSServiceDef() {
        const node = getStaticDefaultNode('createJMSServiceDef')
        node.viewState.shouldShowConnectorPropertyWindow = true;
        node.setFullPackageName('ballerina.net.jms');
        return node;
    }

    createFSServiceDef() {
        const node = getStaticDefaultNode('createFSServiceDef')
        node.viewState.shouldShowConnectorPropertyWindow = true;
        node.setFullPackageName('ballerina.net.fs');
        return node;
    }

    createFTPServiceDef() {
        const node = getStaticDefaultNode('createFSServiceDef')
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
        return getStaticDefaultNode('createMainFunction');
    }

    createFunction() {
        return getStaticDefaultNode('createFunction');
    }

    createConnector() {
        return getStaticDefaultNode('createConnector');
    }

    createConnectorAction() {
        return getStaticDefaultNode('createConnectorAction');
    }

    createHTTPResource() {
        return getStaticDefaultNode('createHTTPResource');
    }

    createFSResource() {
        return getStaticDefaultNode('createFSResource');
    }

    createFTPResource() {
        return getStaticDefaultNode('createFTPResource');
    }

    createJMSResource() {
        return getStaticDefaultNode('createJMSResource');
    }

    createStruct() {
        return getStaticDefaultNode('createStruct');
    }

    createTransformer() {
        return getStaticDefaultNode('createTransformer');
    }

    createWorker() {
        let worker = getStaticDefaultNode('createWorkerFragment');

        // here we will send the default worker as a meta item.
        worker.meta =  getStaticDefaultNode('createDefaultWorkerFragment');
        return worker;
    }

    createAnnotation() {
        return getStaticDefaultNode('createAnnotation');
    }

    createAssignmentStmt() {
        const node = getStaticDefaultNode('createAssignmentStmt');
        // Check if the node is a ConnectorDeclaration
        if (TreeUtils.isEndpointTypeVariableDef(node)) {
            node.viewState.showOverlayContainer = true;
            return node;
        }
        return node;
    }

    createBindStmt() {
        return getStaticDefaultNode('createBindStmt');
    }

    createVarDefStmt() {
        const node = getStaticDefaultNode('createVarDefStmt');
        // Check if the node is a ConnectorDeclaration
        if (TreeUtils.isEndpointTypeVariableDef(node)) {
            node.viewState.showOverlayContainer = true;
            return node;
        }
        return node;
    }

    createIf() {
        return getStaticDefaultNode('createIf');
    }

    createIfElse(){
        return getStaticDefaultNode('createIfElse');
    }

    createInvocation() {
        return getStaticDefaultNode('createInvocation');
    }

    createWhile() {
        return getStaticDefaultNode('createWhile');
    }

    createBreak() {
        return getStaticDefaultNode('createBreak');
    }

    createNext() {
        return getStaticDefaultNode('createNext');
    }

    createTry() {
        return getStaticDefaultNode('createTry');
    }

    createThrow() {
        return getStaticDefaultNode('createThrow');
    }

    createReturn() {
        return getStaticDefaultNode('createReturn');
    }

    createWorkerInvocation() {
        return getStaticDefaultNode('createWorkerInvocation');
    }

    createWorkerReceive() {
        return getStaticDefaultNode('createWorkerReceive');
    }

    // FIXME
    createTransaction() {
        return getStaticDefaultNode('createTransaction');
    }

    createAbort() {
        return getStaticDefaultNode('createAbort');
    }

    // FIXME
    createRetry() {
        const tempNode = getStaticDefaultNode('createRetry');
        return tempNode.failedBody.statements[0];
    }

    createForkJoin() {
        return getStaticDefaultNode('createForkJoin');
    }

    createXmlns() {
        return getStaticDefaultNode('createXmlns');
    }

    createEnum() {
        return getStaticDefaultNode('createEnum')
    }

    createWSResource(fragment) {
        return getNodeForFragment(
            FragmentUtils.createServiceResourceFragment(fragment),
        );
    }

    createEnumerator(enumerator) {
        return getNodeForFragment(FragmentUtils.createTopLevelNodeFragment(`
        enum name {
         ${enumerator}
        }
        `))
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
        nodeForFragment.viewState.showOverlayContainer = true;
        nodeForFragment.getVariable().getInitialExpression().setFullPackageName(fullPackageName);
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
