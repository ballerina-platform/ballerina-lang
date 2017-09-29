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

import NodeFactory from '../../../../../model/node-factory';

class TransformFactory {
    static createSimpleVarRef(variableName) {

    }

    static createVariableDef(name, type, value) {
        const variableDef = NodeFactory.createVariableDef({});
        const variable = NodeFactory.createVariable({});
        const initialExpression = NodeFactory.createLiteral({});
        initialExpression.setValue(value);
        variable.setInitialExpression(initialExpression);
        const typeNode = NodeFactory.createValueType({});
        typeNode.setTypeKind(type);
        const identifierNode = NodeFactory.createIdentifier({});
        identifierNode.setValue(name);
        variable.setName(identifierNode);
        variable.setTypeNode(typeNode);
        variableDef.setVariable(variable);

        // TODO: replace with fragment parser

        // variableDef.setStatementFromString('string ' + varName + ' = ""');
        return variableDef;
    }

}

export default TransformFactory;
