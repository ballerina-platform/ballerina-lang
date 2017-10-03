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

import NodeFactory from './node-factory';

class DefaultNodeFactory {
    /**
     * Create main function.
     * @param {Object} json - node attributes as a json
     * @return {Node} function node for main function
     * @memberof DefaultNodeFactory
     * */
    createMainFunction(json) {
        const functionNode = NodeFactory.createFunction(json);
        const name = NodeFactory.createIdentifier({});
        const parameter = NodeFactory.createVariable({});
        const parameterName = NodeFactory.createIdentifier({});
        const valueType = NodeFactory.createValueType({});
        const arrayType = NodeFactory.createArrayType({});
        valueType.setTypeKind('string', true);
        arrayType.setElementType(valueType, true);
        parameterName.setValue('args', true);
        parameter.setName(parameterName, true);
        parameter.setTypeNode(valueType);
        name.setValue('main', true);
        functionNode.setName(name);
        functionNode.addParameters(parameter, -1, true);
        return functionNode;
    }
}

export default new DefaultNodeFactory();
