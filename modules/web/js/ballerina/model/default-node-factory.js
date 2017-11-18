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
import _ from 'lodash';
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
    const parsedJson = _.cloneDeep(DefaultNodes[fragmentName]);
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
