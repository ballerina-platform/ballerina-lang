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
import Environment from '../env/environment';
import DefaultNodes from './default-nodes';
import ConnectorHelper from './../env/helpers/connector-helper';


export const WsResources = [
    {
        resourceName: 'onUpgrade',
        fragment: `
        onUpgrade (endpoint ep, http:Request req) {

        }
    `,
    },
    {
        resourceName: 'onOpen',
        fragment: `
        onOpen (endpoint ep) {

        }
    `,
    },
    {
        resourceName: 'onTextMessage',
        fragment: `
        onTextMessage (endpoint conn, http:TextFrame frame) {

        }
    `,
    },
    {
        resourceName: 'onBinaryMessage',
        fragment: `
        onBinaryMessage (endpoint conn, http:BinaryFrame frame) {

        }
    `,
    },
    {
        resourceName: 'onClose',
        fragment: `
        onClose (endpoint conn, http:CloseFrame closeFrame) {

        }
    `,
    },
    {
        resourceName: 'onIdleTimeOut',
        fragment: `
        onIdleTimeout (endpoint ep) {

        }
    `,
    },
    {
        resourceName: 'onPing',
        fragment: `
        onPong (endpoint conn, http:PongFrame frame) {

        }
    `,
    },
    {
        resourceName: 'onPong',
        fragment: `
        onPong (endpoint conn, http:PongFrame frame) {

        }
    `,
    },
];

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

function getPackageDefinition(fullPackageName) {
    let packageDef = null;
    for (const packageDefintion of Environment.getPackages()) {
        if (packageDefintion.getName() === fullPackageName) {
            packageDef = packageDefintion;
        }
    }
    return packageDef;
}

/**
 * Default node factory class.
 * This creates all the default node for each model.
 * This is mostly used on drag and drop.
 * */
class DefaultNodeFactory {

    createImportWithOrg() {
        const importSt = getStaticDefaultNode('createImportWithOrg');
        return importSt;
    }

    createHTTPServiceDef() {
        const service = getStaticDefaultNode('createHTTPServiceDef');
        const endpoint = getStaticDefaultNode('createHTTPEndpointDef');
        const importSt = getStaticDefaultNode('createImportWithOrg');
        return [endpoint, service, importSt];
    }

    createWSServiceDef() {
        const service = getStaticDefaultNode('createWSServiceDef');
        const endpoint = getStaticDefaultNode('createWSEndpointDef');
        const importSt = getStaticDefaultNode('createImportWithOrg');
        return [endpoint, service, importSt];
    }

    createJMSServiceDef() {
        const node = getStaticDefaultNode('createJMSServiceDef');
        node.setFullPackageName('ballerina/net.jms');
        return node;
    }

    createFSServiceDef() {
        const node = getStaticDefaultNode('createFSServiceDef');
        node.setFullPackageName('ballerina/net.fs');
        return node;
    }

    createFTPServiceDef() {
        const node = getStaticDefaultNode('createFTPServiceDef');
        node.setFullPackageName('ballerina/net.ftp');
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
        const worker = getStaticDefaultNode('createWorkerFragment');

        // here we will send the default worker as a meta item.
        worker.meta = getStaticDefaultNode('createDefaultWorkerFragment');
        return worker;
    }

    createAnnotation() {
        return getStaticDefaultNode('createAnnotation');
    }

    createAssignmentStmt() {
        const node = getStaticDefaultNode('createAssignmentStmt');
        return node;
    }

    createBindStmt() {
        return getStaticDefaultNode('createBindStmt');
    }

    createVarDefStmt() {
        const node = getStaticDefaultNode('createVarDefStmt');
        return node;
    }

    createIf() {
        return getStaticDefaultNode('createIf');
    }

    createIfElse() {
        return getStaticDefaultNode('createIfElse');
    }

    createInvocation() {
        return getStaticDefaultNode('createInvocation');
    }

    createWhile() {
        return getStaticDefaultNode('createWhile');
    }

    createForeach() {
        return getStaticDefaultNode('createForeach');
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

    createForkJoin() {
        return getStaticDefaultNode('createForkJoin');
    }

    createXmlns() {
        return getStaticDefaultNode('createXmlns');
    }

    createEnum() {
        return getStaticDefaultNode('createEnum');
    }

    createWSResource(fragment) {
        return getNodeForFragment(FragmentUtils.createServiceResourceFragment(fragment));
    }

    createEnumerator(enumerator) {
        return getNodeForFragment(FragmentUtils.createTopLevelNodeFragment(`
        enum name {
         ${enumerator}
        }
        `));
    }

    createConnectorDeclaration(args) {
        const node = getNodeForFragment(
            FragmentUtils.createStatementFragment('http:HttpClient endpoint1 = create http:HttpClient("",{});'));
        const { connector, packageName, fullPackageName } = args;

        // Iterate through the params
        const parameters = [];
        if (connector.getParams()) {
            connector.getParams().forEach((param) => {
                let defaultValue = Environment.getDefaultValue(param.type);
                if (defaultValue === undefined) {
                    // Check if its a struct or enum
                    const packageDef = getPackageDefinition(fullPackageName);
                    const identifier = param.type.split(':')[1];
                    // TODO: Current package content should handle properly
                    const structs = packageDef ? packageDef.getStructDefinitions() : [];
                    const enums = packageDef ? packageDef.getEnums() : [];
                    const type = ConnectorHelper.getTypeOfParam(identifier, structs, enums);
                    if (type === 'struct') {
                        defaultValue = '{}';
                    } else {
                        defaultValue = 'null';
                    }
                }
                const paramNode = getNodeForFragment(FragmentUtils.createExpressionFragment(defaultValue));
                parameters.push(paramNode.getVariable().getInitialExpression());
            });
            node.getVariable().getInitialExpression().setExpressions(parameters);
        }
        const pkgStr = packageName !== 'Current Package' ? `${packageName}` : '';
        node.getVariable().getTypeNode().getTypeName().setValue(connector.getName());
        node.getVariable().getTypeNode().getPackageAlias().setValue(pkgStr);
        node.getVariable().getInitialExpression().getConnectorType().getPackageAlias()
            .setValue(pkgStr);
        node.getVariable().getInitialExpression().getConnectorType().getTypeName()
            .setValue(connector.getName());
        node.getVariable().getInitialExpression().setFullPackageName(fullPackageName);
        return node;
    }

    createEndpoint(args) {
        const { endpoint, packageName, fullPackageName } = args;
        let endpointPackageAlias = (packageName !== 'Current Package' && packageName !== '' &&
            packageName !== 'builtin') ? (packageName + ':') : '';
        endpointPackageAlias = endpointPackageAlias !== '' ? endpointPackageAlias.split(/[.]+/).pop() : '';

        return getNodeForFragment(FragmentUtils.createEndpointVarDefFragment(`
            endpoint ${endpointPackageAlias + endpoint.getName()} ep {};
        `));
    }

    createConnectorActionInvocationAssignmentStatement(args) {
        let stmtString = '';
        const { functionDef, packageName, fullPackageName, endpoint } = args;

        if (functionDef && functionDef.getReturnParams().length > 0) {
            stmtString = 'var var1 = ';
        }
        stmtString += 'endpoint1->action1();';

        const node = getNodeForFragment(FragmentUtils.createStatementFragment(stmtString));

        if (functionDef && functionDef.getParameters().length > 0) {
            const parameters = functionDef.getParameters().map((param) => {
                let defaultValue = Environment.getDefaultValue(param.type);
                if (defaultValue === undefined) {
                    defaultValue = '{}';
                }
                const paramNode = getNodeForFragment(FragmentUtils.createExpressionFragment(defaultValue));
                return paramNode.getVariable().getInitialExpression();
            });
            node.getExpression().setArgumentExpressions(parameters);
        }

        if (functionDef && functionDef.getReturnParams().length > 0) {
            const varRefNames = functionDef.getReturnParams().map((param, index) => {
                return 'variable' + index + 1;
            });
            const varRefListString = `var ${varRefNames.join(', ')} = function1();`;
            const returnNode = getNodeForFragment(FragmentUtils.createStatementFragment(varRefListString));
            node.setVariables(returnNode.getVariables());
        }

        if (functionDef) {
            const pkgStr = packageName !== 'Current Package' ? packageName.split(/[.]+/).pop() : '';
            node.getExpression().getName().setValue(functionDef.getName());
            node.getExpression().setFullPackageName(fullPackageName);
            node.getExpression().getPackageAlias().setValue(pkgStr);
        }

        if (endpoint) {
            node.getExpression().getExpression().getVariableName().setValue(endpoint);
        }

        node.getExpression().invocationType = 'ACTION';
        return node;
    }

    createFunctionInvocationStatement(args) {
        let stmtString = '';

        const { functionDef, packageName, fullPackageName } = args;

        if (functionDef.getReturnParams().length > 0) {
            stmtString += 'var var1 = ';
        }

        if (functionDef.getReceiverType()) {
            stmtString += 'typeRef.function1();';
        } else {
            stmtString += 'function1();';
        }

        const node = getNodeForFragment(FragmentUtils.createStatementFragment(stmtString));

        if (functionDef.getParameters().length > 0) {
            const parameters = functionDef.getParameters().map((param) => {
                let defaultValue = Environment.getDefaultValue(param.type);
                if (defaultValue === undefined) {
                    defaultValue = '{}';
                }
                const paramNode = getNodeForFragment(FragmentUtils.createExpressionFragment(defaultValue));
                return paramNode.getVariable().getInitialExpression();
            });
            node.getExpression().setArgumentExpressions(parameters);
        }

        const varRefNames = functionDef.getReturnParams().map((param, index) => {
            return 'variable' + index + 1;
        });

        if (varRefNames.length > 0) {
            const varRefListString = `var ${varRefNames.join(', ')} = function1();`;
            const returnNode = getNodeForFragment(FragmentUtils.createStatementFragment(varRefListString));
            node.setVariables(returnNode.getVariables());
        }

        if (functionDef.getReceiverType()) {
            const receiverName = _.toLower(functionDef.getReceiverType()) + 'Ref';
            node.getExpression().getExpression().getVariableName().setValue(receiverName);
        }

        node.getExpression().getName().setValue(functionDef.getName());
        if (packageName && packageName !== 'Current Package' && packageName !== 'builtin') {
            node.getExpression().getPackageAlias().setValue(packageName);
        }
        node.getExpression().setFullPackageName(fullPackageName);

        return node;
    }
}

export default new DefaultNodeFactory();
