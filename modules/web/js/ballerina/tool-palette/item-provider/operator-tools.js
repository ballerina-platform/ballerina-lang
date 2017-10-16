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

import TransformFactory from '../../model/transform-factory';

const operatorStatementCreator = TransformFactory.createOperatorAssignmentStatement

const getSeperator = id => ({
    id,
    name: '',
    seperator: true,
});

const addOpTool = {
    id: 'add',
    name: 'Add',
    icon: 'add',
    title: 'Add',
    factoryArgs: {
        defaultExpression: '0 + 0',
    },
    nodeFactoryMethod: operatorStatementCreator,
};

const subtractOpTool = {
    id: 'subtract',
    name: 'Subtract',
    icon: 'minus',
    title: 'Subtract',
    factoryArgs: {
        defaultExpression: '0 - 0',
    },
    nodeFactoryMethod: operatorStatementCreator,
};

const divideOpTool = {
    id: 'divide',
    name: 'Divide',
    icon: 'divide',
    title: 'Divide',
    factoryArgs: {
        defaultExpression: '0 / 1',
    },
    nodeFactoryMethod: operatorStatementCreator,
};

const multiplyOpTool = {
    id: 'multiply',
    name: 'Multiply',
    icon: 'cancel',
    title: 'Multiply',
    factoryArgs: {
        defaultExpression: '0 * 0',
    },
    nodeFactoryMethod: operatorStatementCreator,
};

const powerOpTool = {
    id: 'power',
    name: 'Power',
    icon: 'power',
    title: 'Power',
    factoryArgs: {
        defaultExpression: '1 ^ 1',
    },
    nodeFactoryMethod: operatorStatementCreator,
};

const modulusOpTool = {
    id: 'modulus',
    name: 'Modulus',
    icon: 'modulus',
    title: 'Modulus',
    factoryArgs: {
        defaultExpression: '1 % 1',
    },
    nodeFactoryMethod: operatorStatementCreator,
};

const lessThanOpTool = {
    id: 'lessThan',
    name: 'Less Than',
    icon: 'less-than',
    title: 'Less Than',
    factoryArgs: {
        defaultExpression: '0 < 0',
    },
    nodeFactoryMethod: operatorStatementCreator,
};

const greaterThanOpTool = {
    id: 'greaterThan',
    name: 'Greater Than',
    icon: 'greater-than',
    title: 'Greater Than',
    factoryArgs: {
        defaultExpression: '0 > 0',
    },
    nodeFactoryMethod: operatorStatementCreator,
};

const lessThanOrEqualOpTool = {
    id: 'lessThanOrEqual',
    name: 'LT Or Equal To',
    icon: 'less-than-equals',
    title: 'LT Or Equal To',
    factoryArgs: {
        defaultExpression: '0 <= 0',
    },
    nodeFactoryMethod: operatorStatementCreator,
};

const greaterThanOrEqualOpTool = {
    id: 'greaterThanOrEqual',
    name: 'GT Or Equal To',
    icon: 'greater-than-equals',
    title: 'GT Or Equal To',
    factoryArgs: {
        defaultExpression: '0 >= 0',
    },
    nodeFactoryMethod: operatorStatementCreator,
};

const equalOpTool = {
    id: 'equal',
    name: 'Equal To',
    icon: 'equals',
    title: 'Equal To',
    factoryArgs: {
        defaultExpression: '0 == 0',
    },
    nodeFactoryMethod: operatorStatementCreator,
};

const notEqualOpTool = {
    id: 'notEqual',
    name: 'Not Equal To',
    icon: 'not-equal',
    title: 'Not Equal To',
    factoryArgs: {
        defaultExpression: '0 != 0',
    },
    nodeFactoryMethod: operatorStatementCreator,
};

const andOpTool = {
    id: 'and',
    name: 'And',
    icon: 'and',
    title: 'And',
    factoryArgs: {
        defaultExpression: 'true && true',
    },
    nodeFactoryMethod: operatorStatementCreator,
};

const orOpTool = {
    id: 'or',
    name: 'Or',
    icon: 'or',
    title: 'Or',
    factoryArgs: {
        defaultExpression: 'true || true',
    },
    nodeFactoryMethod: operatorStatementCreator,
};

export const binaryOpTools = [
    addOpTool,
    subtractOpTool,
    divideOpTool,
    multiplyOpTool,
    powerOpTool,
    modulusOpTool,
    getSeperator('sep-numeric'),
    lessThanOpTool,
    greaterThanOpTool,
    lessThanOrEqualOpTool,
    greaterThanOrEqualOpTool,
    equalOpTool,
    notEqualOpTool,
    getSeperator('sep-conditional'),
    andOpTool,
    orOpTool,
];

const plusOpTool = {
    id: 'plus',
    name: 'Plus',
    icon: 'add',
    title: 'Plus',
    factoryArgs: {
        defaultExpression: '+ (1)',
    },
    nodeFactoryMethod: operatorStatementCreator,
};

const minusOpTool = {
    id: 'minus',
    name: 'Minus',
    icon: 'minus',
    title: 'Minus',
    factoryArgs: {
        defaultExpression: '- (1)',
    },
    nodeFactoryMethod: operatorStatementCreator,
};

const notOpTool = {
    id: 'not',
    name: 'Not',
    icon: 'not',
    title: 'Not',
    factoryArgs: {
        defaultExpression: '! false',
    },
    nodeFactoryMethod: operatorStatementCreator,
};

const lengthOfOpTool = {
    id: 'lengthof',
    name: 'Length Of',
    icon: 'lengthof',
    title: 'Length Of',
    factoryArgs: {
        defaultExpression: 'lengthof a',
    },
    nodeFactoryMethod: operatorStatementCreator,
};

const typeOfOpTool = {
    id: 'typeof',
    name: 'Type Of',
    icon: 'typeof',
    title: 'Type Of',
    factoryArgs: {
        defaultExpression: 'typeof a',
    },
    nodeFactoryMethod: operatorStatementCreator,
};

export const unaryOpTools = [
    plusOpTool,
    minusOpTool,
    notOpTool,
    lengthOfOpTool,
    typeOfOpTool,
];
