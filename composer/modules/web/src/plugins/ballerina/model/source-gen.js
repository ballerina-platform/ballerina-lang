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
 *
 */
import _ from 'lodash';

let join;
const tab = '    ';

function times(n, f) {
    let s = '';
    for (let i = 0; i < n; i++) {
        s += f();
    }
    return s;
}

export default function getSourceOf(node, pretty = false, l = 0, replaceLambda) {
    if (!node) {
        return '';
    }
    let i = 0;
    const ws = node.ws ? node.ws.map(wsObj => wsObj.ws) : [];
    const shouldIndent = pretty || !ws.length;

    /**
     * White space generator function,
     * @param {string?} defaultWS
     * @return {string}
     */
    function w(defaultWS) {
        const wsI = ws[i++];
        // Check if whitespces have comments
        const hasComment = (wsI !== undefined) && wsI.trim().length > 0;
        if (hasComment || (!shouldIndent && wsI !== undefined)) {
            return wsI;
        }
        return defaultWS || '';
    }

    function a(afterWS) {
        if (shouldIndent) {
            return afterWS || '';
        }
        return '';
    }

    /* eslint-disable no-unused-vars */
    const b = a;

    function indent() {
        ++l;
        return '';
    }

    function outdent() {
        --l;
        if (shouldIndent) {
            if (node.documentationText) {
                const indent = _.last(node.documentationText.split('\n'));
                if (indent === _.repeat(tab, l)) {
                    // if documentation text already contains the correct dent
                    return '';
                }
            }
            return '\n' + _.repeat(tab, l);
        }
        return '';
    }

    function dent() {
        if (shouldIndent) {
            return '\n' + _.repeat(tab, l);
        }
        return '';
    }
    /* eslint-enable no-unused-vars */

    if (replaceLambda && node.kind === 'Lambda') {
        return '$ function LAMBDA $';
    }
    // if this is a primitive value, return value directly
    if (Object(node) !== node) {
        return node;
    }

    switch (node.kind) {
        case 'CompilationUnit':
            return join(node.topLevelNodes, pretty, replaceLambda, l, w) + w();
        case 'ArrayType':
            if (node.parent.rest) {
                return getSourceOf(node.elementType, pretty, l, replaceLambda);
            } else {
                return getSourceOf(node.elementType, pretty, l, replaceLambda) +
                times(node.dimensions, () => w() + '[' + w() + ']');
            }

        /* eslint-disable max-len */
        // auto gen start

        case 'PackageDeclaration':
            return dent() + w() + 'package' + a(' ')
                 + join(node.packageName, pretty, replaceLambda, l, w, '', '.') + w() + ';';
        case 'Import':
            if (node.isInternal) {
                return '';
            } else if (node.userDefinedAlias && node.orgName.valueWithBar
                         && node.packageName && node.alias.valueWithBar) {
                return dent() + w() + 'import' + a(' ') + w()
                 + node.orgName.valueWithBar + w() + '/'
                 + join(node.packageName, pretty, replaceLambda, l, w, '', '.') + w(' ') + 'as' + w(' ') + node.alias.valueWithBar
                 + w() + ';';
            } else if (node.userDefinedAlias && node.packageName
                         && node.alias.valueWithBar) {
                return dent() + w() + 'import' + a(' ')
                 + join(node.packageName, pretty, replaceLambda, l, w, '', '.') + w(' ') + 'as' + w(' ')
                 + node.alias.valueWithBar + w() + ';';
            } else if (node.orgName.valueWithBar && node.packageName) {
                return dent() + w() + 'import' + a(' ') + w()
                 + node.orgName.valueWithBar + w() + '/'
                 + join(node.packageName, pretty, replaceLambda, l, w, '', '.') + w() + ';';
            } else {
                return dent() + w() + 'import' + a(' ')
                 + join(node.packageName, pretty, replaceLambda, l, w, '', '.') + w() + ';';
            }
        case 'Identifier':
            return w() + node.valueWithBar;
        case 'Abort':
            return dent() + w() + 'abort' + w() + ';';
        case 'Action':
            if (node.annotationAttachments && node.documentationAttachments
                         && node.deprecatedAttachments && node.name.valueWithBar
                         && node.parameters && node.returnParameters && node.returnParameters.length
                         && node.body && node.workers) {
                return join(node.annotationAttachments, pretty, replaceLambda, l, w, '')
                 + join(node.documentationAttachments, pretty, replaceLambda, l, w, '')
                 + join(node.deprecatedAttachments, pretty, replaceLambda, l, w, '') + dent() + w() + 'action' + w(' ')
                 + node.name.valueWithBar + w(' ') + '('
                 + join(node.parameters, pretty, replaceLambda, l, w, '', ',') + w() + ')' + w(' ') + '('
                 + join(node.returnParameters, pretty, replaceLambda, l, w, '', ',') + w() + ')'
                 + w(' ') + '{' + indent()
                 + getSourceOf(node.body, pretty, l, replaceLambda) + join(node.workers, pretty, replaceLambda, l, w, '')
                 + outdent() + w() + '}';
            } else {
                return join(node.annotationAttachments, pretty, replaceLambda, l, w, '')
                 + join(node.documentationAttachments, pretty, replaceLambda, l, w, '')
                 + join(node.deprecatedAttachments, pretty, replaceLambda, l, w, '') + dent() + w() + 'action' + w(' ')
                 + node.name.valueWithBar + w(' ') + '('
                 + join(node.parameters, pretty, replaceLambda, l, w, '', ',') + w() + ')' + w(' ') + '{' + indent()
                 + getSourceOf(node.body, pretty, l, replaceLambda)
                 + join(node.workers, pretty, replaceLambda, l, w, '') + outdent() + w() + '}';
            }
        case 'Annotation':
            return dent()
                 + join(node.annotationAttachments, pretty, replaceLambda, l, w, '') + w() + 'annotation' + w(' ') + '<'
                 + join(node.attachmentPoints, pretty, replaceLambda, l, w, '', ',') + w() + '>'
                 + w(' ') + node.name.valueWithBar + a(' ') + b(' ')
                 + getSourceOf(node.typeNode, pretty, l, replaceLambda) + w() + ';';
        case 'AnnotationAttachment':
            if (node.builtin && node.annotationName.valueWithBar
                         && node.expression) {
                return w() + '@' + w() + node.annotationName.valueWithBar + b(' ')
                 + getSourceOf(node.expression, pretty, l, replaceLambda);
            } else if (node.packageAlias.valueWithBar
                         && node.annotationName.valueWithBar && node.expression) {
                return w() + '@' + w() + node.packageAlias.valueWithBar + w() + ':'
                 + w() + node.annotationName.valueWithBar + b(' ')
                 + getSourceOf(node.expression, pretty, l, replaceLambda);
            } else {
                return w() + '@' + w() + node.annotationName.valueWithBar + b(' ')
                 + getSourceOf(node.expression, pretty, l, replaceLambda);
            }
        case 'ArrayLiteralExpr':
            return w() + '['
                 + join(node.expressions, pretty, replaceLambda, l, w, '', ',') + w() + ']';
        case 'Assignment':
            return dent() + (node.declaredWithVar ? w() + 'var' + a(' ') : '')
                 + getSourceOf(node.variable, pretty, l, replaceLambda) + w(' ') + '='
                 + a(' ') + getSourceOf(node.expression, pretty, l, replaceLambda)
                 + w() + ';';
        case 'AwaitExpr':
            return w() + 'await' + a(' ')
                 + getSourceOf(node.expression, pretty, l, replaceLambda);
        case 'BinaryExpr':
            if (node.inTemplateLiteral && node.leftExpression
                         && node.operatorKind && node.rightExpression) {
                return w() + '{{'
                 + getSourceOf(node.leftExpression, pretty, l, replaceLambda) + w(' ') + node.operatorKind + a(' ')
                 + getSourceOf(node.rightExpression, pretty, l, replaceLambda) + w() + '}}';
            } else {
                return getSourceOf(node.leftExpression, pretty, l, replaceLambda)
                 + w(' ') + node.operatorKind + a(' ')
                 + getSourceOf(node.rightExpression, pretty, l, replaceLambda);
            }
        case 'Bind':
            return dent() + w() + 'bind' + b(' ')
                 + getSourceOf(node.expression, pretty, l, replaceLambda) + w(' ') + 'with' + b(' ')
                 + getSourceOf(node.variable, pretty, l, replaceLambda) + w() + ';';
        case 'Block':
            return join(node.statements, pretty, replaceLambda, l, w, '');
        case 'Break':
            return dent() + w() + 'break' + w() + ';';
        case 'BracedTupleExpr':
            return w() + '('
                 + join(node.expressions, pretty, replaceLambda, l, w, '', ',') + w() + ')';
        case 'BuiltInRefType':
            return w() + node.typeKind;
        case 'Catch':
            return dent() + w() + 'catch' + w() + '('
                 + getSourceOf(node.parameter, pretty, l, replaceLambda) + w() + ')' + w(' ') + '{' + indent()
                 + getSourceOf(node.body, pretty, l, replaceLambda) + outdent()
                 + w() + '}';
        case 'CheckExpr':
            return w() + 'check' + a(' ')
                 + getSourceOf(node.expression, pretty, l, replaceLambda);
        case 'Comment':
            return dent() + w() + node.comment;
        case 'Connector':
            if (node.annotationAttachments && node.documentationAttachments
                         && node.deprecatedAttachments && node.name.valueWithBar
                         && node.parameters && node.variableDefs && node.actions) {
                return join(node.annotationAttachments, pretty, replaceLambda, l, w, '')
                 + join(node.documentationAttachments, pretty, replaceLambda, l, w, '')
                 + join(node.deprecatedAttachments, pretty, replaceLambda, l, w, '') + dent() + (node.public ? w() + 'public' + a(' ') : '')
                 + w() + 'connector' + w(' ') + node.name.valueWithBar + w(' ')
                 + '('
                 + join(node.parameters, pretty, replaceLambda, l, w, '', ',') + w() + ')' + w(' ') + '{' + indent()
                 + join(node.variableDefs, pretty, replaceLambda, l, w, '')
                 + join(node.actions, pretty, replaceLambda, l, w, '') + outdent() + w() + '}';
            } else {
                return join(node.annotationAttachments, pretty, replaceLambda, l, w, '')
                 + join(node.documentationAttachments, pretty, replaceLambda, l, w, '')
                 + join(node.deprecatedAttachments, pretty, replaceLambda, l, w, '') + dent() + (node.public ? w() + 'public' + a(' ') : '')
                 + w() + 'connector' + w(' ') + node.name.valueWithBar + w(' ')
                 + '('
                 + join(node.parameters, pretty, replaceLambda, l, w, '', ',') + w() + ')' + w(' ') + '{' + indent()
                 + join(node.actions, pretty, replaceLambda, l, w, '') + outdent() + w() + '}';
            }
        case 'ConnectorInitExpr':
            if (node.connectorType && node.expressions) {
                return dent() + w() + 'create' + b(' ')
                 + getSourceOf(node.connectorType, pretty, l, replaceLambda) + w() + '('
                 + join(node.expressions, pretty, replaceLambda, l, w, '', ',') + w() + ')';
            } else {
                return dent() + w() + 'create' + b(' ')
                 + getSourceOf(node.connectorType, pretty, l, replaceLambda) + w() + '(' + w() + ')';
            }
        case 'ConstrainedType':
            return getSourceOf(node.type, pretty, l, replaceLambda) + w() + '<'
                 + getSourceOf(node.constraint, pretty, l, replaceLambda) + w() + '>';
        case 'Documentation':
            return dent() + w() + 'documentation' + a(' ') + w() + '{' + indent()
                 + w() + node.documentationText
                 + join(node.attributes, pretty, replaceLambda, l, w, '') + outdent() + w() + '}';
        case 'DocumentationAttribute':
            return w() + node.paramType + w() + '{{' + w()
                 + node.documentationField.valueWithBar + w() + '}}' + a(' ') + w()
                 + node.documentationText;
        case 'Deprecated':
            return dent() + w() + 'deprecated' + a(' ') + w() + '{' + indent() + w()
                 + node.documentationText + outdent() + w() + '}';
        case 'Endpoint':
            return dent()
                 + join(node.annotationAttachments, pretty, replaceLambda, l, w, '') + w() + 'endpoint' + a(' ')
                 + getSourceOf(node.endPointType, pretty, l, replaceLambda) + w(' ') + node.name.valueWithBar
                 + b(' ')
                 + getSourceOf(node.configurationExpression, pretty, l, replaceLambda) + w() + ';';
        case 'EndpointType':
            return w() + '<'
                 + getSourceOf(node.constraint, pretty, l, replaceLambda) + w() + '>';
        case 'ExpressionStatement':
            return dent() + getSourceOf(node.expression, pretty, l, replaceLambda)
                 + w() + ';';
        case 'FieldBasedAccessExpr':
            return getSourceOf(node.expression, pretty, l, replaceLambda) + w()
                 + '.' + w() + node.fieldName.valueWithBar;
        case 'Foreach':
            return dent() + w() + 'foreach' + a(' ')
                 + join(node.variables, pretty, replaceLambda, l, w, ' ', ',') + w(' ') + 'in' + b(' ')
                 + getSourceOf(node.collection, pretty, l, replaceLambda) + w(' ') + '{'
                 + indent() + getSourceOf(node.body, pretty, l, replaceLambda)
                 + outdent() + w() + '}';
        case 'ForkJoin':
            if (node.workers && node.joinType && node.joinCount >= 0
                         && node.joinedWorkerIdentifiers && node.joinResultVar && node.joinBody
                         && node.timeOutExpression && node.timeOutVariable && node.timeoutBody) {
                return dent() + dent() + dent() + w() + 'fork' + w(' ') + '{' + indent()
                 + join(node.workers, pretty, replaceLambda, l, w, '')
                 + outdent() + w() + '}' + w() + 'join' + w() + '(' + w() + node.joinType
                 + w(' ') + node.joinCount
                 + join(node.joinedWorkerIdentifiers, pretty, replaceLambda, l, w, ' ', ',') + w() + ')' + w() + '('
                 + getSourceOf(node.joinResultVar, pretty, l, replaceLambda) + w() + ')'
                 + w(' ') + '{' + indent()
                 + getSourceOf(node.joinBody, pretty, l, replaceLambda) + outdent() + w() + '}' + w() + 'timeout' + w()
                 + '('
                 + getSourceOf(node.timeOutExpression, pretty, l, replaceLambda) + w() + ')' + w() + '('
                 + getSourceOf(node.timeOutVariable, pretty, l, replaceLambda) + w() + ')' + w(' ') + '{' + indent()
                 + getSourceOf(node.timeoutBody, pretty, l, replaceLambda)
                 + outdent() + w() + '}';
            } else if (node.workers && node.joinType && node.joinCount >= 0
                         && node.joinedWorkerIdentifiers && node.joinResultVar && node.joinBody) {
                return dent() + dent() + w() + 'fork' + w(' ') + '{' + indent()
                 + join(node.workers, pretty, replaceLambda, l, w, '') + outdent() + w()
                 + '}' + w() + 'join' + w() + '(' + w() + node.joinType + w(' ')
                 + node.joinCount
                 + join(node.joinedWorkerIdentifiers, pretty, replaceLambda, l, w, ' ', ',') + w() + ')' + w() + '('
                 + getSourceOf(node.joinResultVar, pretty, l, replaceLambda) + w() + ')' + w(' ')
                 + '{' + indent()
                 + getSourceOf(node.joinBody, pretty, l, replaceLambda) + outdent() + w() + '}';
            } else if (node.workers && node.joinType && node.joinCount >= 0
                         && node.joinedWorkerIdentifiers && node.joinResultVar) {
                return dent() + dent() + w() + 'fork' + w(' ') + '{' + indent()
                 + join(node.workers, pretty, replaceLambda, l, w, '') + outdent() + w()
                 + '}' + w() + 'join' + w() + '(' + w() + node.joinType + w(' ')
                 + node.joinCount
                 + join(node.joinedWorkerIdentifiers, pretty, replaceLambda, l, w, ' ', ',') + w() + ')' + w() + '('
                 + getSourceOf(node.joinResultVar, pretty, l, replaceLambda) + w() + ')' + w(' ')
                 + '{' + indent() + outdent() + w() + '}';
            } else if (node.workers && node.joinType && node.joinedWorkerIdentifiers
                         && node.joinResultVar && node.joinBody && node.timeOutExpression
                         && node.timeOutVariable && node.timeoutBody) {
                return dent() + dent() + dent() + w() + 'fork' + w(' ') + '{' + indent()
                 + join(node.workers, pretty, replaceLambda, l, w, '')
                 + outdent() + w() + '}' + w() + 'join' + w() + '(' + w() + node.joinType
                 + join(node.joinedWorkerIdentifiers, pretty, replaceLambda, l, w, ' ', ',') + w() + ')' + w() + '('
                 + getSourceOf(node.joinResultVar, pretty, l, replaceLambda) + w() + ')' + w(' ') + '{' + indent()
                 + getSourceOf(node.joinBody, pretty, l, replaceLambda)
                 + outdent() + w() + '}' + w() + 'timeout' + w() + '('
                 + getSourceOf(node.timeOutExpression, pretty, l, replaceLambda) + w() + ')' + w()
                 + '(' + getSourceOf(node.timeOutVariable, pretty, l, replaceLambda)
                 + w() + ')' + w(' ') + '{' + indent()
                 + getSourceOf(node.timeoutBody, pretty, l, replaceLambda) + outdent() + w() + '}';
            } else if (node.workers && node.joinType && node.joinedWorkerIdentifiers
                         && node.joinResultVar && node.joinBody) {
                return dent() + dent() + w() + 'fork' + w(' ') + '{' + indent()
                 + join(node.workers, pretty, replaceLambda, l, w, '') + outdent() + w()
                 + '}' + w() + 'join' + w() + '(' + w() + node.joinType
                 + join(node.joinedWorkerIdentifiers, pretty, replaceLambda, l, w, ' ', ',')
                 + w() + ')' + w() + '('
                 + getSourceOf(node.joinResultVar, pretty, l, replaceLambda) + w() + ')' + w(' ') + '{' + indent()
                 + getSourceOf(node.joinBody, pretty, l, replaceLambda) + outdent() + w()
                 + '}';
            } else {
                return dent() + dent() + w() + 'fork' + w(' ') + '{' + indent()
                 + join(node.workers, pretty, replaceLambda, l, w, '') + outdent() + w()
                 + '}' + w() + 'join' + w() + '(' + w() + node.joinType
                 + join(node.joinedWorkerIdentifiers, pretty, replaceLambda, l, w, ' ', ',')
                 + w() + ')' + w() + '('
                 + getSourceOf(node.joinResultVar, pretty, l, replaceLambda) + w() + ')' + w(' ') + '{' + indent()
                 + outdent() + w() + '}';
            }
        case 'Function':
            if (node.defaultConstructor) {
                return '';
            } else if (node.isConstructor && node.annotationAttachments
                         && node.documentationAttachments && node.deprecatedAttachments
                         && node.name.valueWithBar && node.parameters && node.endpointNodes && node.body
                         && node.workers) {
                return dent()
                 + join(node.annotationAttachments, pretty, replaceLambda, l, w, '')
                 + join(node.documentationAttachments, pretty, replaceLambda, l, w, '')
                 + join(node.deprecatedAttachments, pretty, replaceLambda, l, w, '') + w(' ') + node.name.valueWithBar + w() + '('
                 + join(node.parameters, pretty, replaceLambda, l, w, '', ',')
                 + w() + ')' + a(' ') + w() + '{' + indent()
                 + join(node.endpointNodes, pretty, replaceLambda, l, w, '')
                 + getSourceOf(node.body, pretty, l, replaceLambda)
                 + join(node.workers, pretty, replaceLambda, l, w, '') + outdent() + w() + '}';
            } else if (node.lambda && node.annotationAttachments
                         && node.documentationAttachments && node.deprecatedAttachments && node.parameters
                         && node.restParameters && node.returnParameters
                         && node.returnParameters.length && node.endpointNodes && node.body && node.workers) {
                return dent()
                 + join(node.annotationAttachments, pretty, replaceLambda, l, w, '')
                 + join(node.documentationAttachments, pretty, replaceLambda, l, w, '')
                 + join(node.deprecatedAttachments, pretty, replaceLambda, l, w, '') + w() + 'function' + w() + '('
                 + join(node.parameters, pretty, replaceLambda, l, w, '', ',')
                 + getSourceOf(node.restParameters, pretty, l, replaceLambda) + w() + ')' + a(' ')
                 + w() + '('
                 + join(node.returnParameters, pretty, replaceLambda, l, w, '', ',') + w() + ')' + a(' ') + w() + '{' + indent()
                 + join(node.endpointNodes, pretty, replaceLambda, l, w, '')
                 + getSourceOf(node.body, pretty, l, replaceLambda)
                 + join(node.workers, pretty, replaceLambda, l, w, '') + outdent() + w() + '}';
            } else if (node.lambda && node.annotationAttachments
                         && node.documentationAttachments && node.deprecatedAttachments && node.parameters
                         && node.restParameters && node.endpointNodes && node.body
                         && node.workers) {
                return dent()
                 + join(node.annotationAttachments, pretty, replaceLambda, l, w, '')
                 + join(node.documentationAttachments, pretty, replaceLambda, l, w, '')
                 + join(node.deprecatedAttachments, pretty, replaceLambda, l, w, '') + w() + 'function' + w() + '('
                 + join(node.parameters, pretty, replaceLambda, l, w, '', ',')
                 + getSourceOf(node.restParameters, pretty, l, replaceLambda) + w() + ')' + a(' ')
                 + w() + '{' + indent()
                 + join(node.endpointNodes, pretty, replaceLambda, l, w, '')
                 + getSourceOf(node.body, pretty, l, replaceLambda) + join(node.workers, pretty, replaceLambda, l, w, '')
                 + outdent() + w() + '}';
            } else if (node.noVisibleReceiver && node.annotationAttachments
                         && node.documentationAttachments && node.deprecatedAttachments
                         && node.name.valueWithBar && node.parameters && node.hasReturns
                         && node.returnTypeNode && node.endpointNodes && node.body && node.workers) {
                return dent()
                 + join(node.annotationAttachments, pretty, replaceLambda, l, w, '')
                 + join(node.documentationAttachments, pretty, replaceLambda, l, w, '')
                 + join(node.deprecatedAttachments, pretty, replaceLambda, l, w, '') + (node.public ? w() + 'public' + a(' ') : '')
                 + w() + 'function' + w(' ') + node.name.valueWithBar + w() + '('
                 + join(node.parameters, pretty, replaceLambda, l, w, '', ',')
                 + w() + ')' + a(' ') + w() + 'returns' + a(' ')
                 + getSourceOf(node.returnTypeNode, pretty, l, replaceLambda) + w() + '{' + indent()
                 + join(node.endpointNodes, pretty, replaceLambda, l, w, '')
                 + getSourceOf(node.body, pretty, l, replaceLambda)
                 + join(node.workers, pretty, replaceLambda, l, w, '') + outdent() + w() + '}';
            } else if (node.noVisibleReceiver && node.annotationAttachments
                         && node.documentationAttachments && node.deprecatedAttachments
                         && node.name.valueWithBar && node.parameters && node.endpointNodes && node.body
                         && node.workers) {
                return dent()
                 + join(node.annotationAttachments, pretty, replaceLambda, l, w, '')
                 + join(node.documentationAttachments, pretty, replaceLambda, l, w, '')
                 + join(node.deprecatedAttachments, pretty, replaceLambda, l, w, '') + (node.public ? w() + 'public' + a(' ') : '')
                 + w() + 'function' + w(' ') + node.name.valueWithBar + w() + '('
                 + join(node.parameters, pretty, replaceLambda, l, w, '', ',')
                 + w() + ')' + a(' ') + w() + '{' + indent()
                 + join(node.endpointNodes, pretty, replaceLambda, l, w, '')
                 + getSourceOf(node.body, pretty, l, replaceLambda)
                 + join(node.workers, pretty, replaceLambda, l, w, '') + outdent() + w() + '}';
            } else if (node.hasReturns && node.annotationAttachments
                         && node.documentationAttachments && node.deprecatedAttachments && node.receiver
                         && node.name.valueWithBar && node.parameters && node.restParameters
                         && node.returnTypeNode && node.endpointNodes && node.body
                         && node.workers) {
                return dent()
                 + join(node.annotationAttachments, pretty, replaceLambda, l, w, '')
                 + join(node.documentationAttachments, pretty, replaceLambda, l, w, '')
                 + join(node.deprecatedAttachments, pretty, replaceLambda, l, w, '') + (node.public ? w() + 'public' + a(' ') : '')
                 + w() + 'function' + w() + '<'
                 + getSourceOf(node.receiver, pretty, l, replaceLambda) + w() + '>' + w(' ')
                 + node.name.valueWithBar + w() + '('
                 + join(node.parameters, pretty, replaceLambda, l, w, '', ',')
                 + getSourceOf(node.restParameters, pretty, l, replaceLambda) + w() + ')' + a(' ') + w() + 'returns' + a(' ')
                 + getSourceOf(node.returnTypeNode, pretty, l, replaceLambda) + w() + '{'
                 + indent()
                 + join(node.endpointNodes, pretty, replaceLambda, l, w, '') + getSourceOf(node.body, pretty, l, replaceLambda)
                 + join(node.workers, pretty, replaceLambda, l, w, '') + outdent() + w()
                 + '}';
            } else if (node.hasReturns && node.annotationAttachments
                         && node.documentationAttachments && node.deprecatedAttachments
                         && node.name.valueWithBar && node.parameters && node.restParameters
                         && node.returnTypeNode && node.endpointNodes && node.body && node.workers) {
                return dent()
                 + join(node.annotationAttachments, pretty, replaceLambda, l, w, '')
                 + join(node.documentationAttachments, pretty, replaceLambda, l, w, '')
                 + join(node.deprecatedAttachments, pretty, replaceLambda, l, w, '') + (node.public ? w() + 'public' + a(' ') : '')
                 + w() + 'function' + w(' ') + node.name.valueWithBar + w() + '('
                 + join(node.parameters, pretty, replaceLambda, l, w, '', ',')
                 + getSourceOf(node.restParameters, pretty, l, replaceLambda) + w()
                 + ')' + a(' ') + w() + 'returns' + a(' ')
                 + getSourceOf(node.returnTypeNode, pretty, l, replaceLambda) + w() + '{' + indent()
                 + join(node.endpointNodes, pretty, replaceLambda, l, w, '')
                 + getSourceOf(node.body, pretty, l, replaceLambda)
                 + join(node.workers, pretty, replaceLambda, l, w, '') + outdent() + w() + '}';
            } else if (node.annotationAttachments && node.documentationAttachments
                         && node.deprecatedAttachments && node.receiver
                         && node.name.valueWithBar && node.parameters && node.restParameters
                         && node.endpointNodes && node.body && node.workers) {
                return join(node.annotationAttachments, pretty, replaceLambda, l, w, '')
                 + join(node.documentationAttachments, pretty, replaceLambda, l, w, '')
                 + join(node.deprecatedAttachments, pretty, replaceLambda, l, w, '') + dent() + (node.public ? w() + 'public' + a(' ') : '')
                 + w() + 'function' + w() + '<'
                 + getSourceOf(node.receiver, pretty, l, replaceLambda) + w() + '>' + w(' ')
                 + node.name.valueWithBar + w() + '('
                 + join(node.parameters, pretty, replaceLambda, l, w, '', ',')
                 + getSourceOf(node.restParameters, pretty, l, replaceLambda) + w() + ')' + a(' ') + w() + '{' + indent()
                 + join(node.endpointNodes, pretty, replaceLambda, l, w, '')
                 + getSourceOf(node.body, pretty, l, replaceLambda)
                 + join(node.workers, pretty, replaceLambda, l, w, '') + outdent() + w() + '}';
            } else {
                return join(node.annotationAttachments, pretty, replaceLambda, l, w, '')
                 + join(node.documentationAttachments, pretty, replaceLambda, l, w, '')
                 + join(node.deprecatedAttachments, pretty, replaceLambda, l, w, '') + dent() + (node.public ? w() + 'public' + a(' ') : '')
                 + w() + 'function' + w(' ') + node.name.valueWithBar + w() + '('
                 + join(node.parameters, pretty, replaceLambda, l, w, '', ',')
                 + getSourceOf(node.restParameters, pretty, l, replaceLambda) + w()
                 + ')' + a(' ') + w() + '{' + indent()
                 + join(node.endpointNodes, pretty, replaceLambda, l, w, '')
                 + getSourceOf(node.body, pretty, l, replaceLambda)
                 + join(node.workers, pretty, replaceLambda, l, w, '') + outdent() + w() + '}';
            }
        case 'FunctionType':
            if (node.paramTypeNode && node.returnParamTypeNode
                         && node.returnParamTypeNode.length) {
                return w() + 'function' + w() + '('
                 + join(node.paramTypeNode, pretty, replaceLambda, l, w, '', ',') + w() + ')'
                 + (node.returnKeywordExists ? w() + 'returns' : '') + w() + '('
                 + join(node.returnParamTypeNode, pretty, replaceLambda, l, w, '') + w() + ')';
            } else {
                return w() + 'function' + w() + '('
                 + join(node.paramTypeNode, pretty, replaceLambda, l, w, '', ',') + w() + ')';
            }
        case 'If':
            if (node.ladderParent && node.condition && node.body
                         && node.elseStatement) {
                return (node.parent.kind === 'If' ? '' : dent()) + w() + 'if' + w(' ')
                 + '(' + getSourceOf(node.condition, pretty, l, replaceLambda)
                 + w() + ')' + a(' ') + w() + '{' + indent()
                 + getSourceOf(node.body, pretty, l, replaceLambda) + outdent() + w() + '}' + w(' ')
                 + 'else' + a(' ')
                 + getSourceOf(node.elseStatement, pretty, l, replaceLambda);
            } else if (node.condition && node.body && node.elseStatement) {
                return (node.parent.kind === 'If' ? '' : dent())
                 + (node.parent.kind === 'If' ? '' : dent()) + w() + 'if' + w(' ') + '('
                 + getSourceOf(node.condition, pretty, l, replaceLambda) + w() + ')' + a(' ')
                 + w() + '{' + indent()
                 + getSourceOf(node.body, pretty, l, replaceLambda) + outdent() + w() + '}' + w(' ') + 'else' + a(' ') + w()
                 + '{' + indent()
                 + getSourceOf(node.elseStatement, pretty, l, replaceLambda) + outdent() + w() + '}';
            } else {
                return (node.parent.kind === 'If' ? '' : dent()) + w() + 'if' + w(' ')
                 + '(' + getSourceOf(node.condition, pretty, l, replaceLambda)
                 + w() + ')' + a(' ') + w() + '{' + indent()
                 + getSourceOf(node.body, pretty, l, replaceLambda) + outdent() + w() + '}';
            }
        case 'IndexBasedAccessExpr':
            return getSourceOf(node.expression, pretty, l, replaceLambda) + w()
                 + '[' + getSourceOf(node.index, pretty, l, replaceLambda) + w()
                 + ']';
        case 'Invocation':
            if (node.actionInvocation && node.expression
                         && node.name.valueWithBar && node.argumentExpressions) {
                return (node.async ? w() + 'async' + a(' ') : '')
                 + getSourceOf(node.expression, pretty, l, replaceLambda) + w() + '->' + w()
                 + node.name.valueWithBar + w() + '('
                 + join(node.argumentExpressions, pretty, replaceLambda, l, w, '', ',') + w() + ')';
            } else if (node.expression && node.name.valueWithBar
                         && node.argumentExpressions) {
                return getSourceOf(node.expression, pretty, l, replaceLambda) + w()
                 + '.' + (node.async ? w() + 'async' + a(' ') : '') + w()
                 + node.name.valueWithBar + w() + '('
                 + join(node.argumentExpressions, pretty, replaceLambda, l, w, '', ',') + w() + ')';
            } else if (node.packageAlias.valueWithBar && node.name.valueWithBar
                         && node.argumentExpressions) {
                return w() + node.packageAlias.valueWithBar + w() + ':'
                 + (node.async ? w() + 'async' + a(' ') : '') + w() + node.name.valueWithBar + w()
                 + '('
                 + join(node.argumentExpressions, pretty, replaceLambda, l, w, '', ',') + w() + ')';
            } else {
                return (node.async ? w() + 'async' + a(' ') : '') + w()
                 + node.name.valueWithBar + w() + '('
                 + join(node.argumentExpressions, pretty, replaceLambda, l, w, '', ',') + w() + ')';
            }
        case 'Lambda':
            return getSourceOf(node.functionNode, pretty, l, replaceLambda);
        case 'Literal':
            if (node.inTemplateLiteral && node.unescapedValue) {
                return w() + node.unescapedValue;
            } else if (node.inTemplateLiteral) {
                return '';
            } else {
                return w() + node.value;
            }
        case 'Match':
            return dent() + w() + 'match' + a(' ')
                 + getSourceOf(node.expression, pretty, l, replaceLambda) + w(' ') + '{' + indent()
                 + join(node.patternClauses, pretty, replaceLambda, l, w, '') + outdent() + w()
                 + '}';
        case 'MatchPatternClause':
            if (node.withoutCurlies && node.variableNode && node.statement) {
                return getSourceOf(node.variableNode, pretty, l, replaceLambda) + w(' ')
                 + '=>' + a(' ')
                 + getSourceOf(node.statement, pretty, l, replaceLambda);
            } else {
                return dent() + getSourceOf(node.variableNode, pretty, l, replaceLambda)
                 + w(' ') + '=>' + a(' ') + w() + '{' + indent()
                 + getSourceOf(node.statement, pretty, l, replaceLambda) + outdent() + w() + '}';
            }
        case 'MatchExpression':
            return dent() + w() + node.expr + a(' ') + w() + 'but' + a(' ') + w()
                 + '{' + indent()
                 + join(node.patternClauses, pretty, replaceLambda, l, w, '') + outdent() + w() + '}';
        case 'MatchExpressionPatternClause':
            return getSourceOf(node.variable, pretty, l, replaceLambda) + w() + '=>'
                 + a(' ')
                 + getSourceOf(node.statement, pretty, l, replaceLambda);
        case 'NamedArgsExpr':
            return w() + node.name.valueWithBar + w(' ') + '=' + a(' ')
                 + getSourceOf(node.expression, pretty, l, replaceLambda);
        case 'Next':
            return dent() + w() + 'next' + w() + ';';
        case 'Object':
            if (node.noFieldsAvailable && node.annotationAttachments
                         && node.documentationAttachments && node.deprecatedAttachments
                         && node.name.valueWithBar && node.initFunction && node.functions) {
                return dent()
                 + join(node.annotationAttachments, pretty, replaceLambda, l, w, '')
                 + join(node.documentationAttachments, pretty, replaceLambda, l, w, '')
                 + join(node.deprecatedAttachments, pretty, replaceLambda, l, w, '') + w() + 'type' + a(' ') + w()
                 + node.name.valueWithBar + w(' ') + 'object' + w() + '{' + indent()
                 + getSourceOf(node.initFunction, pretty, l, replaceLambda)
                 + join(node.functions, pretty, replaceLambda, l, w, '') + w() + '};';
            } else if (node.noPrivateFieldsAvailable && node.annotationAttachments
                         && node.documentationAttachments && node.deprecatedAttachments
                         && node.name.valueWithBar && node.publicFields && node.initFunction
                         && node.functions) {
                return dent() + dent()
                 + join(node.annotationAttachments, pretty, replaceLambda, l, w, '')
                 + join(node.documentationAttachments, pretty, replaceLambda, l, w, '')
                 + join(node.deprecatedAttachments, pretty, replaceLambda, l, w, '') + w() + 'type' + a(' ') + w()
                 + node.name.valueWithBar + w(' ') + 'object' + w() + '{' + indent()
                 + w(' ') + 'public' + w() + '{' + indent()
                 + join(node.publicFields, pretty, replaceLambda, l, w, '', ';', true) + outdent() + w()
                 + '}' + getSourceOf(node.initFunction, pretty, l, replaceLambda)
                 + join(node.functions, pretty, replaceLambda, l, w, '') + w()
                 + '};';
            } else if (node.noPublicFieldAvailable && node.annotationAttachments
                         && node.documentationAttachments && node.deprecatedAttachments
                         && node.name.valueWithBar && node.privateFields && node.initFunction
                         && node.functions) {
                return dent() + dent()
                 + join(node.annotationAttachments, pretty, replaceLambda, l, w, '')
                 + join(node.documentationAttachments, pretty, replaceLambda, l, w, '')
                 + join(node.deprecatedAttachments, pretty, replaceLambda, l, w, '') + w() + 'type' + a(' ') + w()
                 + node.name.valueWithBar + w(' ') + 'object' + w() + '{' + indent()
                 + w(' ') + 'private' + w() + '{' + indent()
                 + join(node.privateFields, pretty, replaceLambda, l, w, '', ';', true) + outdent() + w()
                 + '}' + getSourceOf(node.initFunction, pretty, l, replaceLambda)
                 + join(node.functions, pretty, replaceLambda, l, w, '') + w()
                 + '};';
            } else {
                return join(node.annotationAttachments, pretty, replaceLambda, l, w, '')
                 + join(node.documentationAttachments, pretty, replaceLambda, l, w, '')
                 + join(node.deprecatedAttachments, pretty, replaceLambda, l, w, '') + dent() + dent() + dent() + w() + 'type' + a(' ')
                 + w() + node.name.valueWithBar + w(' ') + 'object' + w() + '{'
                 + indent() + w(' ') + 'public' + w() + '{' + indent()
                 + join(node.publicFields, pretty, replaceLambda, l, w, '', ';', true) + outdent()
                 + w() + '}' + w(' ') + 'private' + w() + '{' + indent()
                 + join(node.privateFields, pretty, replaceLambda, l, w, '', ';', true)
                 + outdent() + w() + '}'
                 + getSourceOf(node.initFunction, pretty, l, replaceLambda)
                 + join(node.functions, pretty, replaceLambda, l, w, '') + w() + '};';
            }
        case 'Record':
            return join(node.annotationAttachments, pretty, replaceLambda, l, w, '')
                 + join(node.documentationAttachments, pretty, replaceLambda, l, w, '')
                 + join(node.deprecatedAttachments, pretty, replaceLambda, l, w, '') + dent() + w() + 'type' + w() + node.name.valueWithBar
                 + w() + '{' + indent()
                 + join(node.fields, pretty, replaceLambda, l, w, '', ';', true) + w() + '};';
        case 'RecordLiteralExpr':
            if (node.keyValuePairs) {
                return w() + '{'
                 + join(node.keyValuePairs, pretty, replaceLambda, l, w, '', ',') + w() + '}';
            } else {
                return w() + '{' + w() + '}';
            }
        case 'RecordLiteralKeyValue':
            return getSourceOf(node.key, pretty, l, replaceLambda) + w() + ':'
                 + getSourceOf(node.value, pretty, l, replaceLambda);
        case 'Resource':
            return join(node.annotationAttachments, pretty, replaceLambda, l, w, '')
                 + join(node.documentationAttachments, pretty, replaceLambda, l, w, '')
                 + join(node.deprecatedAttachments, pretty, replaceLambda, l, w, '') + dent() + w() + node.name.valueWithBar + a(' ') + w()
                 + '('
                 + join(node.parameters, pretty, replaceLambda, l, w, '', ',') + w() + ')' + w(' ') + '{' + indent()
                 + getSourceOf(node.body, pretty, l, replaceLambda)
                 + join(node.workers, pretty, replaceLambda, l, w, '') + outdent() + w() + '}';
        case 'Return':
            if (node.noExpressionAvailable) {
                return dent() + w() + 'return' + a(' ') + w() + ';';
            } else if (node.emptyBrackets) {
                return w() + 'return' + a(' ') + w() + '();';
            } else {
                return dent() + w() + 'return' + a(' ')
                 + getSourceOf(node.expression, pretty, l, replaceLambda) + w() + ';';
            }
        case 'Service':
            if (node.isServiceTypeUnavailable && node.annotationAttachments
                         && node.documentationAttachments && node.deprecatedAttachments
                         && node.name.valueWithBar && node.boundEndpoints && node.variables
                         && node.resources) {
                return dent()
                 + join(node.annotationAttachments, pretty, replaceLambda, l, w, '')
                 + join(node.documentationAttachments, pretty, replaceLambda, l, w, '')
                 + join(node.deprecatedAttachments, pretty, replaceLambda, l, w, '') + w() + 'service' + a(' ') + w(' ')
                 + node.name.valueWithBar + a(' ') + w() + 'bind' + a(' ')
                 + join(node.boundEndpoints, pretty, replaceLambda, l, w, '', ',') + w(' ') + '{'
                 + indent() + join(node.variables, pretty, replaceLambda, l, w, '')
                 + join(node.resources, pretty, replaceLambda, l, w, '')
                 + outdent() + w() + '}';
            } else {
                return join(node.annotationAttachments, pretty, replaceLambda, l, w, '')
                 + join(node.documentationAttachments, pretty, replaceLambda, l, w, '')
                 + join(node.deprecatedAttachments, pretty, replaceLambda, l, w, '') + dent() + w() + 'service' + a(' ') + w() + '<'
                 + getSourceOf(node.serviceTypeStruct, pretty, l, replaceLambda) + w()
                 + '>' + w(' ') + node.name.valueWithBar + a(' ') + w() + 'bind'
                 + a(' ')
                 + join(node.boundEndpoints, pretty, replaceLambda, l, w, '', ',') + w(' ') + '{' + indent()
                 + join(node.variables, pretty, replaceLambda, l, w, '')
                 + join(node.resources, pretty, replaceLambda, l, w, '') + outdent() + w() + '}';
            }
        case 'SimpleVariableRef':
            if (node.inTemplateLiteral && node.packageAlias.valueWithBar
                         && node.variableName.valueWithBar) {
                return w() + '{{' + w() + node.packageAlias.valueWithBar + w() + ':'
                 + w() + node.variableName.valueWithBar + w() + '}}';
            } else if (node.inTemplateLiteral && node.variableName.valueWithBar) {
                return w() + '{{' + w() + node.variableName.valueWithBar + w() + '}}';
            } else if (node.packageAlias.valueWithBar && node.variableName.valueWithBar) {
                return w() + node.packageAlias.valueWithBar + w() + ':' + w()
                 + node.variableName.valueWithBar;
            } else {
                return w() + node.variableName.valueWithBar;
            }
        case 'StringTemplateLiteral':
            return w() + 'string\u0020`'
                 + join(node.expressions, pretty, replaceLambda, l, w, '') + w() + '`';
        case 'TernaryExpr':
            return getSourceOf(node.condition, pretty, l, replaceLambda) + w() + '?'
                 + getSourceOf(node.thenExpression, pretty, l, replaceLambda)
                 + w() + ':'
                 + getSourceOf(node.elseExpression, pretty, l, replaceLambda);
        case 'Throw':
            return dent() + w() + 'throw' + b(' ')
                 + getSourceOf(node.expressions, pretty, l, replaceLambda) + w() + ';';
        case 'Transaction':
            if (node.retryCount && node.onCommitFunction && node.onAbortFunction
                         && node.transactionBody && node.onRetryBody) {
                return dent() + dent() + w() + 'transaction' + a(' ') + w() + 'with'
                 + a(' ') + w() + 'retries' + w() + '='
                 + getSourceOf(node.retryCount, pretty, l, replaceLambda) + w() + ',' + a(' ') + w()
                 + 'oncommit' + w() + '='
                 + getSourceOf(node.onCommitFunction, pretty, l, replaceLambda) + w() + ',' + a(' ') + w() + 'onabort' + w() + '='
                 + getSourceOf(node.onAbortFunction, pretty, l, replaceLambda)
                 + w() + '{' + indent()
                 + getSourceOf(node.transactionBody, pretty, l, replaceLambda) + outdent() + w() + '}' + w() + 'onretry'
                 + a(' ') + w() + '{' + indent()
                 + getSourceOf(node.onRetryBody, pretty, l, replaceLambda) + outdent() + w() + '}';
            } else if (node.retryCount && node.onCommitFunction && node.transactionBody
                         && node.onRetryBody) {
                return dent() + dent() + w() + 'transaction' + a(' ') + w() + 'with'
                 + a(' ') + w() + 'retries' + w() + '='
                 + getSourceOf(node.retryCount, pretty, l, replaceLambda) + w() + ',' + a(' ') + w()
                 + 'oncommit' + w() + '='
                 + getSourceOf(node.onCommitFunction, pretty, l, replaceLambda) + w() + ',' + a(' ') + w() + '{' + indent()
                 + getSourceOf(node.transactionBody, pretty, l, replaceLambda) + outdent()
                 + w() + '}' + w() + 'onretry' + a(' ') + w() + '{' + indent()
                 + getSourceOf(node.onRetryBody, pretty, l, replaceLambda)
                 + outdent() + w() + '}';
            } else if (node.onCommitFunction && node.onAbortFunction
                         && node.transactionBody && node.onRetryBody) {
                return dent() + dent() + w() + 'transaction' + a(' ') + w() + 'with'
                 + a(' ') + w() + 'oncommit' + w() + '='
                 + getSourceOf(node.onCommitFunction, pretty, l, replaceLambda) + w() + ',' + a(' ') + w()
                 + 'onabort' + w() + '='
                 + getSourceOf(node.onAbortFunction, pretty, l, replaceLambda) + w() + '{' + indent()
                 + getSourceOf(node.transactionBody, pretty, l, replaceLambda) + outdent() + w() + '}'
                 + w() + 'onretry' + a(' ') + w() + '{' + indent()
                 + getSourceOf(node.onRetryBody, pretty, l, replaceLambda) + outdent() + w() + '}';
            } else if (node.retryCount && node.onAbortFunction && node.transactionBody
                         && node.onRetryBody) {
                return dent() + dent() + w() + 'transaction' + a(' ') + w() + 'with'
                 + a(' ') + w() + 'retries' + w() + '='
                 + getSourceOf(node.retryCount, pretty, l, replaceLambda) + w() + ',' + a(' ') + w()
                 + 'onabort' + w() + '='
                 + getSourceOf(node.onAbortFunction, pretty, l, replaceLambda) + w() + '{' + indent()
                 + getSourceOf(node.transactionBody, pretty, l, replaceLambda) + outdent() + w() + '}' + w()
                 + 'onretry' + a(' ') + w() + '{' + indent()
                 + getSourceOf(node.onRetryBody, pretty, l, replaceLambda) + outdent() + w() + '}';
            } else if (node.retryCount && node.transactionBody && node.onRetryBody) {
                return dent() + dent() + w() + 'transaction' + a(' ') + w() + 'with'
                 + a(' ') + w() + 'retries' + w() + '='
                 + getSourceOf(node.retryCount, pretty, l, replaceLambda) + w() + ',' + a(' ') + w() + '{'
                 + indent()
                 + getSourceOf(node.transactionBody, pretty, l, replaceLambda) + outdent() + w() + '}' + w() + 'onretry' + a(' ') + w()
                 + '{' + indent()
                 + getSourceOf(node.onRetryBody, pretty, l, replaceLambda) + outdent() + w() + '}';
            } else if (node.onCommitFunction && node.transactionBody
                         && node.onRetryBody) {
                return dent() + dent() + w() + 'transaction' + a(' ') + w() + 'with'
                 + a(' ') + w() + 'oncommit' + w() + '='
                 + getSourceOf(node.onCommitFunction, pretty, l, replaceLambda) + w() + ',' + a(' ') + w()
                 + '{' + indent()
                 + getSourceOf(node.transactionBody, pretty, l, replaceLambda) + outdent() + w() + '}' + w() + 'onretry' + a(' ')
                 + w() + '{' + indent()
                 + getSourceOf(node.onRetryBody, pretty, l, replaceLambda) + outdent() + w() + '}';
            } else if (node.onAbortFunction && node.transactionBody && node.onRetryBody) {
                return dent() + dent() + w() + 'transaction' + a(' ') + w() + 'with'
                 + a(' ') + w() + 'onabort' + w() + '='
                 + getSourceOf(node.onAbortFunction, pretty, l, replaceLambda) + w() + '{' + indent()
                 + getSourceOf(node.transactionBody, pretty, l, replaceLambda) + outdent()
                 + w() + '}' + w() + 'onretry' + a(' ') + w() + '{' + indent()
                 + getSourceOf(node.onRetryBody, pretty, l, replaceLambda)
                 + outdent() + w() + '}';
            } else if (node.transactionBody && node.onRetryBody) {
                return dent() + dent() + w() + 'transaction' + a(' ') + w() + '{'
                 + indent()
                 + getSourceOf(node.transactionBody, pretty, l, replaceLambda) + outdent() + w() + '}' + w() + 'onretry' + a(' ') + w() + '{'
                 + indent()
                 + getSourceOf(node.onRetryBody, pretty, l, replaceLambda) + outdent() + w() + '}';
            } else if (node.retryCount && node.onCommitFunction && node.onAbortFunction
                         && node.transactionBody) {
                return dent() + w() + 'transaction' + a(' ') + w() + 'with' + a(' ')
                 + w() + 'retries' + w() + '='
                 + getSourceOf(node.retryCount, pretty, l, replaceLambda) + w() + ',' + a(' ') + w() + 'oncommit' + w()
                 + '='
                 + getSourceOf(node.onCommitFunction, pretty, l, replaceLambda) + w() + ',' + a(' ') + w() + 'onabort' + w() + '='
                 + getSourceOf(node.onAbortFunction, pretty, l, replaceLambda) + w() + '{'
                 + indent()
                 + getSourceOf(node.transactionBody, pretty, l, replaceLambda) + outdent() + w() + '}';
            } else if (node.retryCount && node.onCommitFunction && node.transactionBody) {
                return dent() + w() + 'transaction' + a(' ') + w() + 'with' + a(' ')
                 + w() + 'retries' + w() + '='
                 + getSourceOf(node.retryCount, pretty, l, replaceLambda) + w() + ',' + a(' ') + w() + 'oncommit' + w()
                 + '='
                 + getSourceOf(node.onCommitFunction, pretty, l, replaceLambda) + w() + ',' + a(' ') + w() + '{' + indent()
                 + getSourceOf(node.transactionBody, pretty, l, replaceLambda) + outdent() + w()
                 + '}';
            } else if (node.retryCount && node.onAbortFunction && node.transactionBody) {
                return dent() + w() + 'transaction' + a(' ') + w() + 'with' + a(' ')
                 + w() + 'retries' + w() + '='
                 + getSourceOf(node.retryCount, pretty, l, replaceLambda) + w() + ',' + a(' ') + w() + 'onabort' + w()
                 + '='
                 + getSourceOf(node.onAbortFunction, pretty, l, replaceLambda) + w() + '{' + indent()
                 + getSourceOf(node.transactionBody, pretty, l, replaceLambda) + outdent() + w() + '}';
            } else if (node.onCommitFunction && node.onAbortFunction
                         && node.transactionBody) {
                return dent() + w() + 'transaction' + a(' ') + w() + 'with' + a(' ')
                 + w() + 'oncommit' + w() + '='
                 + getSourceOf(node.onCommitFunction, pretty, l, replaceLambda) + w() + ',' + a(' ') + w() + 'onabort'
                 + w() + '='
                 + getSourceOf(node.onAbortFunction, pretty, l, replaceLambda) + w() + '{' + indent()
                 + getSourceOf(node.transactionBody, pretty, l, replaceLambda) + outdent() + w() + '}';
            } else if (node.retryCount && node.transactionBody) {
                return dent() + w() + 'transaction' + a(' ') + w() + 'with' + a(' ')
                 + w() + 'retries' + w() + '='
                 + getSourceOf(node.retryCount, pretty, l, replaceLambda) + w() + ',' + a(' ') + w() + '{' + indent()
                 + getSourceOf(node.transactionBody, pretty, l, replaceLambda)
                 + outdent() + w() + '}';
            } else if (node.onCommitFunction && node.transactionBody) {
                return dent() + w() + 'transaction' + a(' ') + w() + 'with' + a(' ')
                 + w() + 'oncommit' + w() + '='
                 + getSourceOf(node.onCommitFunction, pretty, l, replaceLambda) + w() + ',' + a(' ') + w() + '{'
                 + indent()
                 + getSourceOf(node.transactionBody, pretty, l, replaceLambda) + outdent() + w() + '}';
            } else if (node.onAbortFunction && node.transactionBody) {
                return dent() + w() + 'transaction' + a(' ') + w() + 'with' + a(' ')
                 + w() + 'onabort' + w() + '='
                 + getSourceOf(node.onAbortFunction, pretty, l, replaceLambda) + w() + '{' + indent()
                 + getSourceOf(node.transactionBody, pretty, l, replaceLambda) + outdent() + w()
                 + '}';
            } else {
                return dent() + w() + 'transaction' + a(' ') + w() + '{' + indent()
                 + getSourceOf(node.transactionBody, pretty, l, replaceLambda)
                 + outdent() + w() + '}';
            }
        case 'Transform':
            return dent() + w() + 'transform' + w(' ') + '{' + indent()
                 + getSourceOf(node.body, pretty, l, replaceLambda) + outdent() + w() + '}';
        case 'Transformer':
            if (node.source && node.returnParameters
                         && node.returnParameters.length && node.name.valueWithBar && node.parameters && node.body) {
                return dent() + (node.public ? w() + 'public' + a(' ') : '') + w()
                 + 'transformer' + w() + '<'
                 + getSourceOf(node.source, pretty, l, replaceLambda) + w() + ','
                 + join(node.returnParameters, pretty, replaceLambda, l, w, '', ',') + w() + '>' + w()
                 + node.name.valueWithBar + w() + '('
                 + join(node.parameters, pretty, replaceLambda, l, w, '', ',') + w() + ')' + w() + '{' + indent()
                 + getSourceOf(node.body, pretty, l, replaceLambda) + outdent() + w() + '}';
            } else if (node.source && node.returnParameters
                         && node.returnParameters.length && node.name.valueWithBar && node.body) {
                return dent() + (node.public ? w() + 'public' + a(' ') : '') + w()
                 + 'transformer' + w() + '<'
                 + getSourceOf(node.source, pretty, l, replaceLambda) + w() + ','
                 + join(node.returnParameters, pretty, replaceLambda, l, w, '', ',') + w() + '>' + w()
                 + node.name.valueWithBar + w() + '{' + indent()
                 + getSourceOf(node.body, pretty, l, replaceLambda) + outdent() + w() + '}';
            } else {
                return dent() + (node.public ? w() + 'public' + a(' ') : '') + w()
                 + 'transformer' + w() + '<'
                 + getSourceOf(node.source, pretty, l, replaceLambda) + w() + ','
                 + join(node.returnParameters, pretty, replaceLambda, l, w, '', ',') + w() + '>' + w() + '{' + indent()
                 + getSourceOf(node.body, pretty, l, replaceLambda) + outdent() + w()
                 + '}';
            }
        case 'Try':
            if (node.body && node.catchBlocks && node.finallyBody) {
                return dent() + dent() + w() + 'try' + w(' ') + '{' + indent()
                 + getSourceOf(node.body, pretty, l, replaceLambda) + outdent() + w() + '}'
                 + join(node.catchBlocks, pretty, replaceLambda, l, w, '') + w()
                 + 'finally' + w(' ') + '{' + indent()
                 + getSourceOf(node.finallyBody, pretty, l, replaceLambda) + outdent() + w() + '}';
            } else {
                return dent() + w() + 'try' + w(' ') + '{' + indent()
                 + getSourceOf(node.body, pretty, l, replaceLambda) + outdent() + w() + '}'
                 + join(node.catchBlocks, pretty, replaceLambda, l, w, '');
            }
        case 'TupleDestructure':
            if (node.declaredWithVar && node.variableRefs
                         && node.variableRefs.length) {
                return w() + 'var' + w() + '('
                 + join(node.variableRefs, pretty, replaceLambda, l, w, '', ',') + w() + ')' + w() + '=' + w()
                 + '<expression.source>;';
            } else {
                return w() + '('
                 + join(node.variableRefs, pretty, replaceLambda, l, w, '', ',') + w() + ')' + w() + '=' + w() + '<expression.source>;';
            }
        case 'TupleTypeNode':
            return w() + '('
                 + join(node.memberTypeNodes, pretty, replaceLambda, l, w, '', ',') + w() + ')';
        case 'TypeCastExpr':
            return w() + '(' + getSourceOf(node.typeNode, pretty, l, replaceLambda)
                 + w() + ')'
                 + getSourceOf(node.expression, pretty, l, replaceLambda);
        case 'TypeConversionExpr':
            if (node.typeNode && node.transformerInvocation && node.expression) {
                return w() + '<' + getSourceOf(node.typeNode, pretty, l, replaceLambda)
                 + w() + ','
                 + getSourceOf(node.transformerInvocation, pretty, l, replaceLambda) + w() + '>' + a(' ')
                 + getSourceOf(node.expression, pretty, l, replaceLambda);
            } else {
                return w() + '<' + getSourceOf(node.typeNode, pretty, l, replaceLambda)
                 + w() + '>' + a(' ')
                 + getSourceOf(node.expression, pretty, l, replaceLambda);
            }
        case 'TypeofExpression':
            return w() + 'typeof' + b(' ')
                 + getSourceOf(node.typeNode, pretty, l, replaceLambda);
        case 'TypeInitExpr':
            if (node.noExpressionAvailable) {
                return w() + 'new';
            } else if (node.noTypeAttached && node.expressions) {
                return w() + 'new' + w(' ') + '('
                 + join(node.expressions, pretty, replaceLambda, l, w, '', ',') + w() + ')';
            } else {
                return w() + 'new' + b(' ')
                 + getSourceOf(node.typeName, pretty, l, replaceLambda) + w(' ') + '('
                 + join(node.expressions, pretty, replaceLambda, l, w, '', ',') + w() + ')';
            }
        case 'UnaryExpr':
            return w() + node.operatorKind + b(' ')
                 + getSourceOf(node.expression, pretty, l, replaceLambda);
        case 'UnionTypeNode':
            if (node.withParantheses && node.memberTypeNodes) {
                return w() + '('
                 + join(node.memberTypeNodes, pretty, replaceLambda, l, w, '', '|') + w() + ')';
            } else {
                return join(node.memberTypeNodes, pretty, replaceLambda, l, w, '', '|');
            }
        case 'UserDefinedType':
            if (node.anonStruct) {
                return getSourceOf(node.anonStruct, pretty, l, replaceLambda);
            } else if (node.packageAlias.valueWithBar && node.typeName.valueWithBar) {
                return w() + node.packageAlias.valueWithBar + w() + ':' + w()
                 + node.typeName.valueWithBar;
            } else {
                return w() + node.typeName.valueWithBar;
            }
        case 'ValueType':
            if (node.withParantheses && node.typeKind) {
                return w() + '(' + w() + node.typeKind + w() + ')';
            } else {
                return w() + node.typeKind;
            }
        case 'Variable':
            if (node.endpoint && node.typeNode && node.name.valueWithBar
                         && node.initialExpression) {
                return dent() + dent() + w() + 'endpoint'
                 + getSourceOf(node.typeNode, pretty, l, replaceLambda) + w(' ') + node.name.valueWithBar + w()
                 + '{' + indent()
                 + getSourceOf(node.initialExpression, pretty, l, replaceLambda) + w() + ';' + outdent() + w() + '}';
            } else if (node.endpoint && node.typeNode && node.name.valueWithBar) {
                return dent() + w() + 'endpoint'
                 + getSourceOf(node.typeNode, pretty, l, replaceLambda) + w(' ') + node.name.valueWithBar + w() + '{'
                 + indent() + outdent() + w() + '}';
            } else if (node.serviceEndpoint && node.name.valueWithBar) {
                return w() + 'endpoint' + w(' ') + node.name.valueWithBar;
            } else if (node.global && node.annotationAttachments
                         && node.documentationAttachments && node.deprecatedAttachments && node.safeAssignment
                         && node.typeNode && node.name.valueWithBar
                         && node.initialExpression) {
                return dent()
                 + join(node.annotationAttachments, pretty, replaceLambda, l, w, '')
                 + join(node.documentationAttachments, pretty, replaceLambda, l, w, '')
                 + join(node.deprecatedAttachments, pretty, replaceLambda, l, w, '') + (node.public ? w() + 'public' + a(' ') : '')
                 + (node.const ? w() + 'const' + a(' ') : '')
                 + getSourceOf(node.typeNode, pretty, l, replaceLambda) + w(' ')
                 + node.name.valueWithBar + w() + '=?'
                 + getSourceOf(node.initialExpression, pretty, l, replaceLambda) + w() + ';';
            } else if (node.global && node.annotationAttachments
                         && node.documentationAttachments && node.deprecatedAttachments && node.typeNode
                         && node.name.valueWithBar && node.initialExpression) {
                return dent()
                 + join(node.annotationAttachments, pretty, replaceLambda, l, w, '')
                 + join(node.documentationAttachments, pretty, replaceLambda, l, w, '')
                 + join(node.deprecatedAttachments, pretty, replaceLambda, l, w, '') + (node.public ? w() + 'public' + a(' ') : '')
                 + (node.const ? w() + 'const' + a(' ') : '')
                 + getSourceOf(node.typeNode, pretty, l, replaceLambda) + w(' ')
                 + node.name.valueWithBar + w(' ') + '=' + a(' ')
                 + getSourceOf(node.initialExpression, pretty, l, replaceLambda) + w() + ';';
            } else if (node.global && node.annotationAttachments
                         && node.documentationAttachments && node.deprecatedAttachments && node.typeNode
                         && node.name.valueWithBar) {
                return dent()
                 + join(node.annotationAttachments, pretty, replaceLambda, l, w, '')
                 + join(node.documentationAttachments, pretty, replaceLambda, l, w, '')
                 + join(node.deprecatedAttachments, pretty, replaceLambda, l, w, '')
                 + getSourceOf(node.typeNode, pretty, l, replaceLambda) + w(' ') + node.name.valueWithBar + w() + ';';
            } else if (node.safeAssignment && node.typeNode && node.name.valueWithBar
                         && node.initialExpression) {
                return getSourceOf(node.typeNode, pretty, l, replaceLambda) + w(' ')
                 + node.name.valueWithBar + w() + '=?'
                 + getSourceOf(node.initialExpression, pretty, l, replaceLambda);
            } else if (node.typeNode && node.name.valueWithBar
                         && node.initialExpression) {
                return getSourceOf(node.typeNode, pretty, l, replaceLambda) + w(' ')
                 + node.name.valueWithBar + w(' ') + '=' + a(' ')
                 + getSourceOf(node.initialExpression, pretty, l, replaceLambda);
            } else if (node.annotationAttachments && node.documentationAttachments
                         && node.deprecatedAttachments && node.typeNode
                         && node.name.valueWithBar) {
                return join(node.annotationAttachments, pretty, replaceLambda, l, w, '')
                 + join(node.documentationAttachments, pretty, replaceLambda, l, w, '')
                 + join(node.deprecatedAttachments, pretty, replaceLambda, l, w, '') + getSourceOf(node.typeNode, pretty, l, replaceLambda)
                 + (node.rest ? w() + '...' : '') + w(' ')
                 + node.name.valueWithBar;
            } else {
                return getSourceOf(node.typeNode, pretty, l, replaceLambda);
            }
        case 'VariableDef':
            if (node.endpoint && node.variable) {
                return getSourceOf(node.variable, pretty, l, replaceLambda);
            } else {
                return dent() + getSourceOf(node.variable, pretty, l, replaceLambda)
                 + w() + ';';
            }
        case 'While':
            return dent() + w() + 'while' + w(' ') + '('
                 + getSourceOf(node.condition, pretty, l, replaceLambda) + w() + ')' + w(' ') + '{'
                 + indent() + getSourceOf(node.body, pretty, l, replaceLambda) + outdent()
                 + w() + '}';
        case 'Worker':
            return dent() + w() + 'worker' + w(' ') + node.name.valueWithBar
                 + w(' ') + '{' + indent()
                 + getSourceOf(node.body, pretty, l, replaceLambda) + outdent() + w() + '}';
        case 'WorkerReceive':
            return dent() + getSourceOf(node.expression, pretty, l, replaceLambda)
                 + w() + '<-' + w() + node.workerName.valueWithBar + w() + ';';
        case 'WorkerSend':
            if (node.forkJoinedSend && node.expression) {
                return dent() + getSourceOf(node.expression, pretty, l, replaceLambda)
                 + w() + '->' + w() + 'fork' + w() + ';';
            } else {
                return dent() + getSourceOf(node.expression, pretty, l, replaceLambda)
                 + w() + '->' + w() + node.workerName.valueWithBar + w() + ';';
            }
        case 'XmlAttribute':
            return getSourceOf(node.name, pretty, l, replaceLambda) + w() + '='
                 + getSourceOf(node.value, pretty, l, replaceLambda);
        case 'XmlAttributeAccessExpr':
            if (node.expression && node.index) {
                return getSourceOf(node.expression, pretty, l, replaceLambda) + w()
                 + '@' + w() + '[' + getSourceOf(node.index, pretty, l, replaceLambda)
                 + w() + ']';
            } else {
                return getSourceOf(node.expression, pretty, l, replaceLambda) + w()
                 + '@';
            }
        case 'XmlCommentLiteral':
            if (node.root && node.textFragments) {
                return w() + 'xml`' + w() + '<!--'
                 + join(node.textFragments, pretty, replaceLambda, l, w, '') + w() + '-->' + w() + '`';
            } else {
                return w() + '<!--'
                 + join(node.textFragments, pretty, replaceLambda, l, w, '') + w() + '-->';
            }
        case 'XmlElementLiteral':
            if (node.root && node.startTagName && node.attributes && node.content
                         && node.endTagName) {
                return w() + 'xml`' + w() + '<'
                 + getSourceOf(node.startTagName, pretty, l, replaceLambda)
                 + join(node.attributes, pretty, replaceLambda, l, w, '') + w() + '>'
                 + join(node.content, pretty, replaceLambda, l, w, '') + w() + '</'
                 + getSourceOf(node.endTagName, pretty, l, replaceLambda) + w() + '>' + w() + '`';
            } else if (node.startTagName && node.attributes && node.content
                         && node.endTagName) {
                return w() + '<'
                 + getSourceOf(node.startTagName, pretty, l, replaceLambda) + join(node.attributes, pretty, replaceLambda, l, w, '')
                 + w() + '>' + join(node.content, pretty, replaceLambda, l, w, '')
                 + w() + '</'
                 + getSourceOf(node.endTagName, pretty, l, replaceLambda) + w() + '>';
            } else if (node.root && node.startTagName && node.attributes) {
                return w() + 'xml`' + w() + '<'
                 + getSourceOf(node.startTagName, pretty, l, replaceLambda)
                 + join(node.attributes, pretty, replaceLambda, l, w, '') + w() + '/>`';
            } else {
                return w() + '<'
                 + getSourceOf(node.startTagName, pretty, l, replaceLambda) + join(node.attributes, pretty, replaceLambda, l, w, '')
                 + w() + '/>';
            }
        case 'XmlPiLiteral':
            if (node.target && node.dataTextFragments) {
                return getSourceOf(node.target, pretty, l, replaceLambda)
                 + join(node.dataTextFragments, pretty, replaceLambda, l, w, '');
            } else if (node.dataTextFragments) {
                return join(node.dataTextFragments, pretty, replaceLambda, l, w, '');
            } else {
                return getSourceOf(node.target, pretty, l, replaceLambda);
            }
        case 'XmlQname':
            if (node.prefix.valueWithBar && node.localname.valueWithBar) {
                return w() + node.prefix.valueWithBar + w() + ':' + w()
                 + node.localname.valueWithBar;
            } else {
                return w() + node.localname.valueWithBar;
            }
        case 'XmlQuotedString':
            return join(node.textFragments, pretty, replaceLambda, l, w, '');
        case 'XmlTextLiteral':
            return join(node.textFragments, pretty, replaceLambda, l, w, '');
        case 'Xmlns':
            if (node.namespaceURI && node.prefix.valueWithBar) {
                return dent() + w() + 'xmlns' + b(' ')
                 + getSourceOf(node.namespaceURI, pretty, l, replaceLambda) + w(' ') + 'as' + w(' ')
                 + node.prefix.valueWithBar + w() + ';';
            } else if (node.namespaceURI) {
                return dent() + w() + 'xmlns' + b(' ')
                 + getSourceOf(node.namespaceURI, pretty, l, replaceLambda) + w() + ';';
            } else {
                return getSourceOf(node.namespaceDeclaration, pretty, l, replaceLambda);
            }

        // auto gen end
        /* eslint-enable max-len */

        default:
            console.error('no source gen for ' + node.kind);
            return '';

    }
}

/**
 * Joins sources of a array of nodes with given delimiters.
 *
 * @private
 * @param {Node[]} arr Nodes to be joined.
 * @param {boolean} pretty
 * @param {number} l indent level.
 * @param {function(number): string} wsFunc White space generator function.
 * @param defaultWS
 * @param {string} separator
 * @param {boolean} suffixLast
 * @param {boolean} replaceLambda
 * @return {string}
 */
join = function (arr, pretty, replaceLambda, l, wsFunc, defaultWS, separator, suffixLast = false) {
    let str = '';
    for (let i = 0; i < arr.length; i++) {
        const node = arr[i];
        if (node.kind === 'Identifier') {
            str += wsFunc(defaultWS);
        }
        str += getSourceOf(node, pretty, l, replaceLambda);
        if (separator && (suffixLast || i !== arr.length - 1)) {
            str += wsFunc(defaultWS) + separator;
        }
    }

    return str;
};

