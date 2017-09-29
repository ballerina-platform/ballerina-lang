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
import EventChannel from 'event_channel';
import SimpleBBox from '../view/simple-bounding-box';

/**
 * Base of all tree nodes.
 *
 * @class Node
 */
class Node extends EventChannel {

    constructor() {
        super();
        // Set an id @TODO move to a path base ID.
        this.id = uuid();

        // Following will propergate tree modified event to the top.
        this.on('tree-modified', function (event) {
            if (!_.isNil(this.parent)) {
                this.parent.trigger('tree-modified', event);
            }
        });

        /**
         * View State Object to keep track of the model's view properties
         * @type {{bBox: SimpleBBox, components: {}, dimensionsSynced: boolean}}
         */
        this.viewState = {
            bBox: new SimpleBBox(),
            components: {},
            dimensionsSynced: false,
            hidden: false,
        };
    }

    /**
     *
     * @param {NodeVisitor} visitor
     */
    accept(visitor) {
        visitor.beginVisit(this);
        // eslint-disable-next-line guard-for-in
        for (const childName in this) {
            if (childName !== 'parent' && childName !== 'position' && childName !== 'ws') {
                const child = this[childName];
                if (child instanceof Node && child.kind) {
                    child.accept(visitor);
                } else if (child instanceof Array) {
                    for (let i = 0; i < child.length; i++) {
                        const childItem = child[i];
                        if (childItem instanceof Node && childItem.kind) {
                            childItem.accept(visitor);
                        }
                    }
                }
            }
        }
        visitor.endVisit(this);
    }

    getKind() {
        return this.kind;
    }

    getWS() {
        return this.wS;
    }

    getPosition() {
        return this.position;
    }

    /**
     * Get the source of the current Node.
     * @return {string} source.
     */
    getSource() {
        return Node.getSourceOf(this);
    }

    /**
     * Joins sources of a array of nodes with given delimiters.
     *
     * @private
     * @param {Node[]} arr Nodes to be joined.
     * @param {function(): string} separatorFunc White space generator function.
     * @param {string} separator
     * @param {boolean} suffixLast
     * @return {string}
     */
    static join(arr, separatorFunc, separator, suffixLast = false) {
        let str = '';
        for (let i = 0; i < arr.length; i++) {
            const node = arr[i];
            str += Node.getSourceOf(node);
            if (node.kind === 'Identifier') {
                str += separatorFunc();
            }
            if (separator && (suffixLast || i !== arr.length - 1)) {
                str += separator + separatorFunc();
            }
        }

        return str;
    }

    /**
     *
     * @private
     * @param {Node} node
     * @return {string}
     */
    static getSourceOf(node) {
        if (!node) {
            return '';
        }
        let i = 0;
        const ws = node.ws ? node.ws.map(wsObj => wsObj.ws) : [];

        function w() {
            const wsI = ws[i++];
            return wsI === undefined ? ' ' : wsI;
        }

        switch (node.kind) {

            // auto gen code - start
            case 'Variable':
                if (node.const && node.typeNode && node.name.value &&
                    node.initialExpression) {
                    return 'const' + w() + node.typeNode.getSource() +
                        node.name.value + w() + '=' + w() + node.initialExpression.getSource() +
                        ';' + w();
                }
                if (node.typeNode && node.name.value && node.initialExpression) {
                    return node.typeNode.getSource() + node.name.value +
                        w() + '=' + w() + node.initialExpression.getSource();
                }
                if (node.typeNode && node.name.value) {
                    return node.typeNode.getSource() + node.name.value +
                        w();
                } else {
                    return node.typeNode.getSource();
                }
            case 'ValueType':
                return node.typeKind + w();
            case 'Literal':
                return node.value + w();
            case 'Function':
                if (node.name.value && node.parameters && node.parameters.length &&
                    node.returnParameters && node.returnParameters.length &&
                    node.body) {
                    return 'function' + w() + node.name.value + w() + '(' +
                        w() + Node.join(node.parameters, w, ',') +
                        ')' + w() + '(' + w() + Node.join(node.returnParameters, w, ',') +
                        ')' + w() + '{' + w() + node.body.getSource() +
                        '}' + w();
                }
                if (node.name.value && node.returnParameters &&
                    node.returnParameters.length && node.body) {
                    return 'function' + w() + node.name.value + w() + '(' +
                        w() + ')' + w() + '(' + w() + Node.join(node.returnParameters, w, ',') +
                        ')' + w() + '{' + w() + node.body.getSource() +
                        '}' + w();
                }
                if (node.name.value && node.parameters && node.parameters.length &&
                    node.returnParameters && node.returnParameters.length) {
                    return 'function' + w() + node.name.value + w() + '(' +
                        w() + Node.join(node.parameters, w, ',') +
                        ')' + w() + '(' + w() + Node.join(node.returnParameters, w, ',') +
                        ')' + w() + '{' + w() + '}' + w();
                }
                if (node.name.value && node.parameters && node.parameters.length &&
                    node.body) {
                    return 'function' + w() + node.name.value + w() + '(' +
                        w() + Node.join(node.parameters, w, ',') +
                        ')' + w() + '{' + w() + node.body.getSource() +
                        '}' + w();
                } else {
                    return 'function' + w() + node.name.value + w() + '(' +
                        w() + ')' + w() + '{' + w() + node.body.getSource() +
                        '}' + w();
                }
            case 'Block':
                if (node.statements && node.statements.length) {
                    return Node.join(node.statements, w, '');
                } else {
                    return '';
                }
            case 'Assignment':
                if (node.declaredWithVar && node.variables && node.variables.length &&
                    node.expression) {
                    return 'var' + w() + Node.join(node.variables, w, ',') + '=' + w() +
                        node.expression.getSource() + ';' + w();
                } else {
                    return Node.join(node.variables, w, ',') + '=' + w() +
                        node.expression.getSource() + ';' + w();
                }
            case 'BinaryExpr':
                return node.leftExpression.getSource() + node.operatorKind +
                    w() + node.rightExpression.getSource();
            case 'SimpleVariableRef':
                if (node.packageIdentifier && node.packageIdentifier.value && node.variableName.value) {
                    return node.packageIdentifier.value + w() + ':' + w() +
                        node.variableName.value + w();
                } else {
                    return node.variableName.value + w();
                }
            case 'ExpressionStatement':
                return node.expression.getSource() + ';' + w();
            case 'Comment':
                return node.comment + w();
            case 'Invocation':
                if (node.packageAlias && node.packageAlias.value && node.name.value &&
                    node.argumentExpressions && node.argumentExpressions.length) {
                    return node.packageAlias.value + w() + ':' + w() +
                        node.name.value + w() + '(' + w() + Node.join(node.argumentExpressions, w, ',') +
                        ')' + w();
                } else if (node.name.value && node.argumentExpressions && node.argumentExpressions.length) {
                    return node.name.value + w() + '(' + w() + Node.join(node.argumentExpressions, w, ',') +
                        ')' + w();
                } else {
                    return node.name.value + w() + '(' + w() + ')' + w();
                }
            case 'VariableDef':
                return node.variable.getSource() + ';' + w();
            case 'Return':
                if (node.expressions && node.expressions.length) {
                    return 'return' + w() + Node.join(node.expressions, w, ',') + ';' + w();
                } else {
                    return 'return' + w() + ';' + w();
                }
            case 'UserDefinedType':
                if (node.packageAlias && node.packageAlias.value && node.typeName.value) {
                    return node.packageAlias.value + w() + ':' + w() +
                        node.typeName.value + w();
                }
                if (node.typeName.value) {
                    return node.typeName.value + w();
                } else {
                    return '';
                }
            case 'RecordLiteralExpr':
                if (node.keyValuePairs && node.keyValuePairs.length) {
                    return '{' + w() + Node.join(node.keyValuePairs, w, ',') +
                        '}' + w();
                } else {
                    return '{' + w() + '}' + w();
                }
            case 'BuiltInRefType':
                if (node.typeKind) {
                    return node.typeKind + w();
                } else {
                    return 'message' + w();
                }
            case 'TypeCastExpr':
                return '(' + w() + node.typeNode.getSource() + ')' +
                    w() + node.expression.getSource();
            case 'ArrayLiteralExpr':
                return '[' + w() + Node.join(node.expressions, w, ',') +
                    ']' + w();
            case 'ArrayType':
                return node.elementType.getSource() + '[' + w() + ']' +
                    w();
            case 'RecordLiteralKeyValue':
                return node.key.getSource() + ':' + w() + node.value.getSource();
            case 'Struct':
                return 'struct' + w() + node.name.value + w() + '{' +
                    w() + Node.join(node.fields, w, ';', true) +
                    '}' + w();
            case 'FieldBasedAccessExpr':
                return node.expression.getSource() + '.' + w() + node.fieldName.value +
                    w();
            case 'IndexBasedAccessExpr':
                return node.expression.getSource() + '[' + w() + node.index.getSource() +
                    ']' + w();
            case 'While':
                return 'while' + w() + '(' + w() + node.condition.getSource() +
                    ')' + w() + '{' + w() + node.body.getSource() +
                    '}' + w();
            case 'If':
                if (node.condition && node.body && node.elseStatement) {
                    return 'if' + w() + '(' + w() + node.condition.getSource() +
                        ')' + w() + '{' + w() + node.body.getSource() +
                        '}' + w() + 'else' + w() + node.elseStatement.getSource();
                } else {
                    return 'if' + w() + '(' + w() + node.condition.getSource() +
                        ')' + w() + '{' + w() + node.body.getSource() +
                        '}' + w();
                }
            case 'UnaryExpr':
                return node.operatorKind + w() + node.expression.getSource();
            case 'Connector':
                if (node.name.value && node.variableDefs && node.variableDefs.length &&
                    node.actions && node.actions.length) {
                    return 'connector' + w() + node.name.value + w() +
                        '(' + w() + ')' + w() + '{' + w() + Node.join(node.variableDefs, w, '') +
                        Node.join(node.actions, w, '') + '}' + w();
                } else {
                    return 'connector' + w() + node.name.value + w() +
                        '(' + w() + ')' + w() + '{' + w() + Node.join(node.actions, w, '') +
                        '}' + w();
                }
            case 'Action':
                if (node.name.value && node.parameters && node.parameters.length &&
                    node.returnParameters && node.returnParameters.length &&
                    node.body) {
                    return 'action' + w() + node.name.value + w() + '(' +
                        w() + Node.join(node.parameters, w, ',') +
                        ')' + w() + '(' + w() + Node.join(node.returnParameters, w, ',') +
                        ')' + w() + '{' + w() + node.body.getSource() +
                        '}' + w();
                } else {
                    return 'action' + w() + node.name.value + w() + '(' +
                        w() + Node.join(node.parameters, w, ',') +
                        ')' + w() + '{' + w() + node.body.getSource() +
                        '}' + w();
                }
            case 'Resource':
                if (node.parameters && node.parameters.length &&
                    node.body) {
                    return Node.join(node.parameters, w, ',') + '{' + w() +
                        node.body.getSource() + '}' + w();
                } else {
                    return Node.join(node.parameters, w, ',') + '{' + w() +
                        '}' + w();
                }
            case 'ConnectorInitExpr':
                if (node.connectorType && node.expressions && node.expressions.length) {
                    return 'create' + w() + node.connectorType.getSource() +
                        '(' + w() + Node.join(node.expressions, w, ',') +
                        ')' + w();
                } else {
                    return 'create' + w() + node.connectorType.getSource() +
                        '(' + w() + ')' + w();
                }
            case 'AnnotationAttachment':
                return;
            case 'ConstrainedType':
                return;
            case 'Break':
                return 'break' + w() + ';' + w();
            case 'ForkJoin':
                return 'fork' + w() + '{' + w() + Node.join(node.workers, w, '') +
                    '}' + w() + node.joinResultVar.getSource();
            case 'Worker':
                return '{' + w() + node.body.getSource() + '}' + w();
            case 'Continue':
                return 'continue' + w() + ';' + w();
            case 'Transform':
                if (node.body) {
                    return node.body.getSource();
                } else {
                    return '';
                }
            case 'WorkerSend':
                return Node.join(node.expressions, w, '') + '->' +
                    w() + node.workerName.value + w() + ';' +
                    w();
            case 'WorkerReceive':
                return Node.join(node.expressions, w, '') + '<-' +
                    w() + node.workerName.value + w() + ';' +
                    w();
            case 'Throw':
                return 'throw' + w() + node.expressions.getSource() +
                    ';' + w();
            case 'TypeConversionExpr':
                return node.typeNode.getSource() + '<' + w() + '>' +
                    w() + node.expression.getSource();
            case 'XmlQname':
                if (node.localname) {
                    return '</' + w() + node.localname + w() + '>' + w();
                } else {
                    return '';
                }
            case 'XmlTextLiteral':
                return Node.join(node.textFragments, w, '');
            case 'XmlElementLiteral':
                if (node.attributes && node.attributes.length &&
                    node.content && node.content.length && node.endTagName) {
                    return '<' + w() + Node.join(node.attributes, w, '') +
                        '>' + w() + Node.join(node.content, w, '') +
                        node.endTagName.getSource();
                } else {
                    return '<' + w() + Node.join(node.attributes, w, '') +
                        '/>' + w();
                }
            case 'XmlAttribute':
                if (node.name && node.value) {
                    return node.name.getSource() + node.value.getSource() +
                        '=' + w();
                }
                if (node.value) {
                    return node.value.getSource() + '=' + w();
                } else {
                    return '=' + w();
                }
            case 'XmlQuotedString':
                if (node.textFragments && node.textFragments.length) {
                    return Node.join(node.textFragments, w, '');
                } else {
                    return '';
                }
            case 'XmlPiLiteral':
                if (node.target && node.dataTextFragments && node.dataTextFragments.length) {
                    return node.target.getSource() + Node.join(node.dataTextFragments, w, '');
                }
                if (node.dataTextFragments && node.dataTextFragments.length) {
                    return Node.join(node.dataTextFragments, w, '');
                } else {
                    return node.target.getSource();
                }
            case 'XmlCommentLiteral':
                if (node.textFragments && node.textFragments.length) {
                    return Node.join(node.textFragments, w, '');
                } else {
                    return '';
                }
            // auto gen code - end
            case 'Identifier':
                return node.value;
            case 'CompilationUnit':
                return w() + node.topLevelNodes.map(Node.getSourceOf).join('');
            case 'PackageDeclaration':
                return 'package' + w() + Node.join(node.packageName, w, '.') + ';' + w();
            case 'Import':
                return 'import' + w() + Node.join(node.packageName, w, '.') + ';' + w();
            default:
                return '';
        }
    }

    [Symbol.iterator]() {
        const children = [];
        for (const key of Object.keys(this)) {
            const prop = this[key];
            if (prop instanceof Node && key !== 'parent') {
                children.push([key, prop]);
            } else if (_.isArray(prop) && prop.length > 0 && prop[0] instanceof Node) {
                let i = 0;
                for (const propI of prop) {
                    children.push([key + '[' + i++ + ']', propI]);
                }
            }
        }
        let nextIndex = 0;

        return {
            next() {
                return nextIndex < children.length ?
                    { value: children[nextIndex++], done: false } :
                    { done: true };
            },
        };
    }

    entries() {
        const props = [];
        for (const key of _.pull(Object.keys(this), 'ws', 'viewState', 'id', '_events', 'parent', 'position', 'kind')) {
            const prop = this[key];
            if (_.isArray(prop)) {
                if (prop.length > 0 && !(prop[0] instanceof Node)) {
                    props.push([key, prop]);
                }
            } else if (!(prop instanceof Node)) {
                props.push([key, prop]);
            }
        }

        return props;
    }


    getID() {
        return this.id;
    }

    /**
     * Get the current file
     * @returns {object} - current file
     */
    getFile() {
        return this.file;
    }

    /**
     * Set the current file
     * @param {object} file - current file
     */
    setFile(file) {
        this.file = file;
    }

    /**
     * Get root of the node which is the compilation unit
     * @returns {Node} root node
     * @memberof Node
     */
    getRoot() {
        if (this.kind === 'CompilationUnit' || !this.parent) {
            return this;
        } else {
            return this.parent.getRoot();
        }
    }

    /**
     * Checks if an string is valid as an identifier.
     * @param {string} identifier - The string value.
     * @return {boolean} - True if valid, else false.
     */
    static isValidIdentifier(identifier) {
        if (_.isUndefined(identifier)) {
            return false;
        } else if (/^[a-zA-Z_$][a-zA-Z0-9_]*$/.test(identifier)) {
            return true;
        }
        return false;
    }

    /**
     * Checks if a string is valid as a type.
     * @param {string} type - The string value.
     * @return {boolean} - True if valid, else false.
     */
    static isValidType(type) {
        if (_.isUndefined(type)) {
            return false;
        } else if (/^[a-zA-Z0-9:_]*\s*[[*\s\]<*\s*>a-zA-Z0-9_]*$/.test(type)) {
            return true;
        }
        return false;
    }
}


const uuid = function () {
    function s4() {
        return Math.floor((1 + Math.random()) * 0x10000)
            .toString(16)
            .substring(1);
    }
    return s4() + s4() + '-' + s4() + '-' + s4() + '-' +
        s4() + '-' + s4() + s4() + s4();
};

export default Node;
