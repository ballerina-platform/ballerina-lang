import DefaultASTFactory from '../ast/default-ast-factory';

const operatorStatementCreator = DefaultASTFactory.createTransformAssignmentOperatorStatement

const getSeperator = id => ({
    id,
    name: '',
    seperator: true,
});

const addOpTool = {
    id: 'add',
    name: 'Add',
    cssClass: 'icon fw fw-add',
    title: 'Add',
    meta: {
        defaultExpression: '0 + 0',
    },
    nodeFactoryMethod: operatorStatementCreator,
};

const subtractOpTool = {
    id: 'subtract',
    name: 'Subtract',
    cssClass: 'icon fw fw-minus',
    title: 'Subtract',
    meta: {
        defaultExpression: '0 - 0',
    },
    nodeFactoryMethod: operatorStatementCreator,
};

const devideOpTool = {
    id: 'devide',
    name: 'Devide',
    cssClass: 'icon fw fw-devide',
    title: 'Devide',
    meta: {
        defaultExpression: '0 / 1',
    },
    nodeFactoryMethod: operatorStatementCreator,
};

const multiplyOpTool = {
    id: 'multiply',
    name: 'Multiply',
    cssClass: 'icon fw fw-multiply',
    title: 'Multiply',
    meta: {
        defaultExpression: '0 * 0',
    },
    nodeFactoryMethod: operatorStatementCreator,
};

const powerOpTool = {
    id: 'power',
    name: 'Power',
    cssClass: 'icon fw fw-power',
    title: 'Power',
    meta: {
        defaultExpression: '1 ^ 1',
    },
    nodeFactoryMethod: operatorStatementCreator,
};

const lessThanOpTool = {
    id: 'lessThan',
    name: 'Less Than',
    cssClass: 'icon fw fw-lessThan',
    title: 'Less Than',
    meta: {
        defaultExpression: '0 < 0',
    },
    nodeFactoryMethod: operatorStatementCreator,
};

const greaterThanOpTool = {
    id: 'greaterThan',
    name: 'Greater Than',
    cssClass: 'icon fw fw-greaterThan',
    title: 'Greater Than',
    meta: {
        defaultExpression: '0 > 0',
    },
    nodeFactoryMethod: operatorStatementCreator,
};

const lessThanOrEqualOpTool = {
    id: 'lessThanOrEqual',
    name: 'LT Or Equal To',
    cssClass: 'icon fw fw-lessThanOrEqual',
    title: 'LT Or Equal To',
    meta: {
        defaultExpression: '0 <= 0',
    },
    nodeFactoryMethod: operatorStatementCreator,
};

const greaterThanOrEqualOpTool = {
    id: 'greaterThanOrEqual',
    name: 'GT Or Equal To',
    cssClass: 'icon fw fw-greaterThanOrEqual',
    title: 'GT Or Equal To',
    meta: {
        defaultExpression: '0 >= 0',
    },
    nodeFactoryMethod: operatorStatementCreator,
};

const equalOpTool = {
    id: 'equal',
    name: 'Equal To',
    cssClass: 'icon fw fw-equal',
    title: 'Equal To',
    meta: {
        defaultExpression: '0 == 0',
    },
    nodeFactoryMethod: operatorStatementCreator,
};

const notEqualOpTool = {
    id: 'notEqual',
    name: 'Not Equal To',
    cssClass: 'icon fw fw-notEqual',
    title: 'Not Equal To',
    meta: {
        defaultExpression: '0 != 0',
    },
    nodeFactoryMethod: operatorStatementCreator,
};

const andOpTool = {
    id: 'and',
    name: 'And',
    cssClass: 'icon fw fw-and',
    title: 'And',
    meta: {
        defaultExpression: 'true && true',
    },
    nodeFactoryMethod: operatorStatementCreator,
};

const orOpTool = {
    id: 'or',
    name: 'Or',
    cssClass: 'icon fw fw-or',
    title: 'Or',
    meta: {
        defaultExpression: 'true || true',
    },
    nodeFactoryMethod: operatorStatementCreator,
};

export const binaryOpTools = [
    addOpTool,
    subtractOpTool,
    devideOpTool,
    multiplyOpTool,
    getSeperator('sep-numeric'),
    powerOpTool,
    getSeperator('sep-power'),
    lessThanOpTool,
    greaterThanOpTool,
    lessThanOrEqualOpTool,
    greaterThanOrEqualOpTool,
    equalOpTool,
    notEqualOpTool,
    getSeperator('sep-boolean'),
    andOpTool,
    orOpTool,
];

const plusOpTool = {
    id: 'plus',
    name: 'Plus',
    cssClass: 'icon fw fw-add',
    title: 'Plus',
    meta: {
        defaultExpression: '+ 1',
    },
    nodeFactoryMethod: operatorStatementCreator,
};

const minusOpTool = {
    id: 'minus',
    name: 'Minus',
    cssClass: 'icon fw fw-minus',
    title: 'Minus',
    meta: {
        defaultExpression: '- 1',
    },
    nodeFactoryMethod: operatorStatementCreator,
};

const notOpTool = {
    id: 'not',
    name: 'Not',
    cssClass: 'icon fw fw-not',
    title: 'Not',
    meta: {
        defaultExpression: '! false',
    },
    nodeFactoryMethod: operatorStatementCreator,
};

const lengthOfOpTool = {
    id: 'lengthof',
    name: 'Length Of',
    cssClass: 'icon fw fw-lengthof',
    title: 'Length Of',
    meta: {
        defaultExpression: 'lengthof a',
    },
    nodeFactoryMethod: operatorStatementCreator,
};

const typeOfOpTool = {
    id: 'typeof',
    name: 'Type Of',
    cssClass: 'icon fw fw-typeof',
    title: 'Type Of',
    meta: {
        defaultExpression: 'typeof a',
    },
    nodeFactoryMethod: operatorStatementCreator,
};

export const unaryOpTools = [
    plusOpTool,
    minusOpTool,
    getSeperator('sep-plus-minus'),
    notOpTool,
    getSeperator('sep-not'),
    lengthOfOpTool,
    typeOfOpTool,
];
