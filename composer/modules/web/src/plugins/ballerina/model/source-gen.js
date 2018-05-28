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
            } else if (node.builtin && node.annotationName.valueWithBar) {
                return w() + '@' + w() + node.annotationName.valueWithBar;
            } else if (node.packageAlias.valueWithBar
                         && node.annotationName.valueWithBar && node.expression) {
                return w() + '@' + w() + node.packageAlias.valueWithBar + w() + ':'
                 + w() + node.annotationName.valueWithBar + b(' ')
                 + getSourceOf(node.expression, pretty, l, replaceLambda);
            } else if (node.packageAlias.valueWithBar
                         && node.annotationName.valueWithBar) {
                return w() + '@' + w() + node.packageAlias.valueWithBar + w() + ':'
                 + w() + node.annotationName.valueWithBar;
            } else if (node.annotationName.valueWithBar && node.expression) {
                return w() + '@' + w() + node.annotationName.valueWithBar + b(' ')
                 + getSourceOf(node.expression, pretty, l, replaceLambda);
            } else {
                return w() + '@' + w() + node.annotationName.valueWithBar;
            }
        case 'ArrayLiteralExpr':
            return w() + '['
                 + join(node.expressions, pretty, replaceLambda, l, w, '', ',') + w() + ']';
        case 'ArrayType':
            if (node.isRestParam && node.grouped && node.elementType) {
                return w() + '('
                 + getSourceOf(node.elementType, pretty, l, replaceLambda) + w() + ')';
            } else if (node.isRestParam && node.elementType) {
                return getSourceOf(node.elementType, pretty, l, replaceLambda);
            } else if (node.grouped && node.elementType && node.dimensionAsString) {
                return w() + '('
                 + getSourceOf(node.elementType, pretty, l, replaceLambda) + w() + node.dimensionAsString + w() + ')';
            } else {
                return getSourceOf(node.elementType, pretty, l, replaceLambda) + w()
                 + node.dimensionAsString;
            }
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
        case 'CompoundAssignment':
            return dent() + getSourceOf(node.variable, pretty, l, replaceLambda)
                 + w() + '+=' + a(' ')
                 + getSourceOf(node.expression, pretty, l, replaceLambda) + w() + ';';
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
        case 'Done':
            return dent() + w() + 'done' + w() + ';';
        case 'ElvisExpr':
            return getSourceOf(node.leftExpression, pretty, l, replaceLambda) + w()
                 + '?:'
                 + getSourceOf(node.rightExpression, pretty, l, replaceLambda);
        case 'Endpoint':
            if (node.skipSourceGen) {
                return '';
            } else if (node.isConfigAssignment && node.annotationAttachments
                         && node.endPointType && node.name.valueWithBar
                         && node.configurationExpression) {
                return dent()
                 + join(node.annotationAttachments, pretty, replaceLambda, l, w, '') + w() + 'endpoint' + a(' ')
                 + getSourceOf(node.endPointType, pretty, l, replaceLambda) + w(' ') + node.name.valueWithBar
                 + w() + '=' + b(' ')
                 + getSourceOf(node.configurationExpression, pretty, l, replaceLambda) + w() + ';';
            } else {
                return dent()
                 + join(node.annotationAttachments, pretty, replaceLambda, l, w, '') + w() + 'endpoint' + a(' ')
                 + getSourceOf(node.endPointType, pretty, l, replaceLambda) + w(' ') + node.name.valueWithBar
                 + b(' ')
                 + getSourceOf(node.configurationExpression, pretty, l, replaceLambda) + w() + ';';
            }
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
            if (node.withParantheses && node.variables && node.collection
                         && node.body) {
                return dent() + w() + 'foreach' + a(' ') + w() + '('
                 + join(node.variables, pretty, replaceLambda, l, w, ' ', ',') + w(' ') + 'in'
                 + b(' ') + getSourceOf(node.collection, pretty, l, replaceLambda) + w()
                 + ')' + w(' ') + '{' + indent()
                 + getSourceOf(node.body, pretty, l, replaceLambda) + outdent() + w() + '}';
            } else {
                return dent() + w() + 'foreach' + a(' ')
                 + join(node.variables, pretty, replaceLambda, l, w, ' ', ',') + w(' ') + 'in' + b(' ')
                 + getSourceOf(node.collection, pretty, l, replaceLambda) + w(' ') + '{'
                 + indent() + getSourceOf(node.body, pretty, l, replaceLambda)
                 + outdent() + w() + '}';
            }
        case 'Forever':
            return dent() + w() + 'forever' + w() + '{' + indent()
                 + join(node.streamingQueryStatements, pretty, replaceLambda, l, w, '') + outdent()
                 + w() + '}';
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
                         && node.name.valueWithBar && node.allParams && node.restParameters
                         && node.endpointNodes && node.body && node.workers) {
                return dent()
                 + join(node.annotationAttachments, pretty, replaceLambda, l, w, '')
                 + join(node.documentationAttachments, pretty, replaceLambda, l, w, '')
                 + join(node.deprecatedAttachments, pretty, replaceLambda, l, w, '') + (node.public ? w() + 'public' + a(' ') : '')
                 + w(' ') + node.name.valueWithBar + w() + '('
                 + join(node.allParams, pretty, replaceLambda, l, w, '', ',')
                 + (node.hasRestParams ? w() + ',' : '')
                 + getSourceOf(node.restParameters, pretty, l, replaceLambda) + w() + ')' + a(' ') + w() + '{' + indent()
                 + join(node.endpointNodes, pretty, replaceLambda, l, w, '')
                 + getSourceOf(node.body, pretty, l, replaceLambda)
                 + join(node.workers, pretty, replaceLambda, l, w, '') + outdent() + w() + '}';
            } else if (node.isConstructor && node.annotationAttachments
                         && node.documentationAttachments && node.deprecatedAttachments
                         && node.name.valueWithBar && node.allParams && node.endpointNodes && node.body
                         && node.workers) {
                return dent()
                 + join(node.annotationAttachments, pretty, replaceLambda, l, w, '')
                 + join(node.documentationAttachments, pretty, replaceLambda, l, w, '')
                 + join(node.deprecatedAttachments, pretty, replaceLambda, l, w, '') + (node.public ? w() + 'public' + a(' ') : '')
                 + w(' ') + node.name.valueWithBar + w() + '('
                 + join(node.allParams, pretty, replaceLambda, l, w, '', ',') + w() + ')' + a(' ')
                 + w() + '{' + indent()
                 + join(node.endpointNodes, pretty, replaceLambda, l, w, '')
                 + getSourceOf(node.body, pretty, l, replaceLambda) + join(node.workers, pretty, replaceLambda, l, w, '')
                 + outdent() + w() + '}';
            } else if (node.interface && node.annotationAttachments
                         && node.documentationAttachments && node.deprecatedAttachments
                         && node.name.valueWithBar && node.allParams && node.restParameters && node.hasReturns
                         && node.returnTypeAnnotationAttachments && node.returnTypeNode) {
                return dent()
                 + join(node.annotationAttachments, pretty, replaceLambda, l, w, '')
                 + join(node.documentationAttachments, pretty, replaceLambda, l, w, '')
                 + join(node.deprecatedAttachments, pretty, replaceLambda, l, w, '') + (node.public ? w() + 'public' + a(' ') : '')
                 + w() + 'function' + w(' ') + node.name.valueWithBar + w() + '('
                 + join(node.allParams, pretty, replaceLambda, l, w, '', ',')
                 + (node.hasRestParams ? w() + ',' : '')
                 + getSourceOf(node.restParameters, pretty, l, replaceLambda) + w() + ')' + a(' ') + w()
                 + 'returns' + a(' ')
                 + join(node.returnTypeAnnotationAttachments, pretty, replaceLambda, l, w, '')
                 + getSourceOf(node.returnTypeNode, pretty, l, replaceLambda) + w() + ';';
            } else if (node.interface && node.annotationAttachments
                         && node.documentationAttachments && node.deprecatedAttachments
                         && node.name.valueWithBar && node.allParams && node.restParameters) {
                return dent()
                 + join(node.annotationAttachments, pretty, replaceLambda, l, w, '')
                 + join(node.documentationAttachments, pretty, replaceLambda, l, w, '')
                 + join(node.deprecatedAttachments, pretty, replaceLambda, l, w, '') + (node.public ? w() + 'public' + a(' ') : '')
                 + w() + 'function' + w(' ') + node.name.valueWithBar + w() + '('
                 + join(node.allParams, pretty, replaceLambda, l, w, '', ',')
                 + (node.hasRestParams ? w() + ',' : '')
                 + getSourceOf(node.restParameters, pretty, l, replaceLambda) + w() + ')' + a(' ') + w()
                 + ';';
            } else if (node.interface && node.annotationAttachments
                         && node.documentationAttachments && node.deprecatedAttachments
                         && node.name.valueWithBar && node.allParams && node.hasReturns
                         && node.returnTypeAnnotationAttachments && node.returnTypeNode) {
                return dent()
                 + join(node.annotationAttachments, pretty, replaceLambda, l, w, '')
                 + join(node.documentationAttachments, pretty, replaceLambda, l, w, '')
                 + join(node.deprecatedAttachments, pretty, replaceLambda, l, w, '') + (node.public ? w() + 'public' + a(' ') : '')
                 + w() + 'function' + w(' ') + node.name.valueWithBar + w() + '('
                 + join(node.allParams, pretty, replaceLambda, l, w, '', ',')
                 + w() + ')' + a(' ') + w() + 'returns' + a(' ')
                 + join(node.returnTypeAnnotationAttachments, pretty, replaceLambda, l, w, '')
                 + getSourceOf(node.returnTypeNode, pretty, l, replaceLambda) + w()
                 + ';';
            } else if (node.interface && node.annotationAttachments
                         && node.documentationAttachments && node.deprecatedAttachments
                         && node.name.valueWithBar && node.allParams) {
                return dent()
                 + join(node.annotationAttachments, pretty, replaceLambda, l, w, '')
                 + join(node.documentationAttachments, pretty, replaceLambda, l, w, '')
                 + join(node.deprecatedAttachments, pretty, replaceLambda, l, w, '') + (node.public ? w() + 'public' + a(' ') : '')
                 + w() + 'function' + w(' ') + node.name.valueWithBar + w() + '('
                 + join(node.allParams, pretty, replaceLambda, l, w, '', ',')
                 + w() + ')' + a(' ') + w() + ';';
            } else if (node.lambda && node.annotationAttachments
                         && node.documentationAttachments && node.deprecatedAttachments && node.isStreamAction
                         && node.allParams && node.restParameters && node.endpointNodes
                         && node.body && node.workers) {
                return dent()
                 + join(node.annotationAttachments, pretty, replaceLambda, l, w, '')
                 + join(node.documentationAttachments, pretty, replaceLambda, l, w, '')
                 + join(node.deprecatedAttachments, pretty, replaceLambda, l, w, '') + w() + '=>' + w() + '('
                 + join(node.allParams, pretty, replaceLambda, l, w, '', ',')
                 + (node.hasRestParams ? w() + ',' : '')
                 + getSourceOf(node.restParameters, pretty, l, replaceLambda) + w() + ')' + a(' ') + w() + '{' + indent()
                 + join(node.endpointNodes, pretty, replaceLambda, l, w, '')
                 + getSourceOf(node.body, pretty, l, replaceLambda)
                 + join(node.workers, pretty, replaceLambda, l, w, '') + outdent() + w() + '}';
            } else if (node.lambda && node.annotationAttachments
                         && node.documentationAttachments && node.deprecatedAttachments && node.isStreamAction
                         && node.allParams && node.endpointNodes && node.body
                         && node.workers) {
                return dent()
                 + join(node.annotationAttachments, pretty, replaceLambda, l, w, '')
                 + join(node.documentationAttachments, pretty, replaceLambda, l, w, '')
                 + join(node.deprecatedAttachments, pretty, replaceLambda, l, w, '') + w() + '=>' + w() + '('
                 + join(node.allParams, pretty, replaceLambda, l, w, '', ',') + w() + ')' + a(' ')
                 + w() + '{' + indent()
                 + join(node.endpointNodes, pretty, replaceLambda, l, w, '') + getSourceOf(node.body, pretty, l, replaceLambda)
                 + join(node.workers, pretty, replaceLambda, l, w, '') + outdent()
                 + w() + '}';
            } else if (node.lambda && node.annotationAttachments
                         && node.documentationAttachments && node.deprecatedAttachments && node.allParams
                         && node.restParameters && node.hasReturns && node.returnTypeNode
                         && node.endpointNodes && node.body && node.workers) {
                return dent()
                 + join(node.annotationAttachments, pretty, replaceLambda, l, w, '')
                 + join(node.documentationAttachments, pretty, replaceLambda, l, w, '')
                 + join(node.deprecatedAttachments, pretty, replaceLambda, l, w, '') + w() + '('
                 + join(node.allParams, pretty, replaceLambda, l, w, '', ',') + (node.hasRestParams ? w()
                 + ',' : '') + getSourceOf(node.restParameters, pretty, l, replaceLambda)
                 + w() + ')' + a(' ') + w() + '=>'
                 + getSourceOf(node.returnTypeNode, pretty, l, replaceLambda) + w() + '{' + indent()
                 + join(node.endpointNodes, pretty, replaceLambda, l, w, '')
                 + getSourceOf(node.body, pretty, l, replaceLambda)
                 + join(node.workers, pretty, replaceLambda, l, w, '') + outdent() + w() + '}';
            } else if (node.lambda && node.annotationAttachments
                         && node.documentationAttachments && node.deprecatedAttachments && node.allParams
                         && node.hasReturns && node.returnTypeNode && node.endpointNodes
                         && node.body && node.workers) {
                return dent()
                 + join(node.annotationAttachments, pretty, replaceLambda, l, w, '')
                 + join(node.documentationAttachments, pretty, replaceLambda, l, w, '')
                 + join(node.deprecatedAttachments, pretty, replaceLambda, l, w, '') + w() + '('
                 + join(node.allParams, pretty, replaceLambda, l, w, '', ',') + w() + ')' + a(' ') + w() + '=>'
                 + getSourceOf(node.returnTypeNode, pretty, l, replaceLambda) + w()
                 + '{' + indent()
                 + join(node.endpointNodes, pretty, replaceLambda, l, w, '') + getSourceOf(node.body, pretty, l, replaceLambda)
                 + join(node.workers, pretty, replaceLambda, l, w, '') + outdent()
                 + w() + '}';
            } else if (node.lambda && node.annotationAttachments
                         && node.documentationAttachments && node.deprecatedAttachments && node.allParams
                         && node.restParameters && node.endpointNodes && node.body
                         && node.workers) {
                return dent()
                 + join(node.annotationAttachments, pretty, replaceLambda, l, w, '')
                 + join(node.documentationAttachments, pretty, replaceLambda, l, w, '')
                 + join(node.deprecatedAttachments, pretty, replaceLambda, l, w, '') + w() + '('
                 + join(node.allParams, pretty, replaceLambda, l, w, '', ',') + (node.hasRestParams ? w()
                 + ',' : '') + getSourceOf(node.restParameters, pretty, l, replaceLambda)
                 + w() + ')' + a(' ') + w() + '=>' + w() + '(' + w() + ')' + a(' ')
                 + w() + '{' + indent()
                 + join(node.endpointNodes, pretty, replaceLambda, l, w, '')
                 + getSourceOf(node.body, pretty, l, replaceLambda) + join(node.workers, pretty, replaceLambda, l, w, '')
                 + outdent() + w() + '}';
            } else if (node.lambda && node.annotationAttachments
                         && node.documentationAttachments && node.deprecatedAttachments && node.allParams
                         && node.endpointNodes && node.body && node.workers) {
                return dent()
                 + join(node.annotationAttachments, pretty, replaceLambda, l, w, '')
                 + join(node.documentationAttachments, pretty, replaceLambda, l, w, '')
                 + join(node.deprecatedAttachments, pretty, replaceLambda, l, w, '') + w() + '('
                 + join(node.allParams, pretty, replaceLambda, l, w, '', ',') + w() + ')' + a(' ') + w() + '=>'
                 + w() + '(' + w() + ')' + a(' ') + w() + '{' + indent()
                 + join(node.endpointNodes, pretty, replaceLambda, l, w, '')
                 + getSourceOf(node.body, pretty, l, replaceLambda)
                 + join(node.workers, pretty, replaceLambda, l, w, '') + outdent() + w() + '}';
            } else if (node.noVisibleReceiver && node.annotationAttachments
                         && node.documentationAttachments && node.deprecatedAttachments
                         && node.name.valueWithBar && node.allParams && node.restParameters
                         && node.hasReturns && node.returnTypeAnnotationAttachments
                         && node.returnTypeNode && node.endpointNodes && node.body && node.workers) {
                return dent()
                 + join(node.annotationAttachments, pretty, replaceLambda, l, w, '')
                 + join(node.documentationAttachments, pretty, replaceLambda, l, w, '')
                 + join(node.deprecatedAttachments, pretty, replaceLambda, l, w, '') + (node.public ? w() + 'public' + a(' ') : '')
                 + w() + 'function' + w(' ') + node.name.valueWithBar + w() + '('
                 + join(node.allParams, pretty, replaceLambda, l, w, '', ',')
                 + (node.hasRestParams ? w() + ',' : '')
                 + getSourceOf(node.restParameters, pretty, l, replaceLambda) + w() + ')' + a(' ') + w()
                 + 'returns' + a(' ')
                 + join(node.returnTypeAnnotationAttachments, pretty, replaceLambda, l, w, '')
                 + getSourceOf(node.returnTypeNode, pretty, l, replaceLambda) + w() + '{' + indent()
                 + join(node.endpointNodes, pretty, replaceLambda, l, w, '')
                 + getSourceOf(node.body, pretty, l, replaceLambda)
                 + join(node.workers, pretty, replaceLambda, l, w, '') + outdent() + w() + '}';
            } else if (node.noVisibleReceiver && node.annotationAttachments
                         && node.documentationAttachments && node.deprecatedAttachments
                         && node.name.valueWithBar && node.allParams && node.hasReturns
                         && node.returnTypeAnnotationAttachments && node.returnTypeNode
                         && node.endpointNodes && node.body && node.workers) {
                return dent()
                 + join(node.annotationAttachments, pretty, replaceLambda, l, w, '')
                 + join(node.documentationAttachments, pretty, replaceLambda, l, w, '')
                 + join(node.deprecatedAttachments, pretty, replaceLambda, l, w, '') + (node.public ? w() + 'public' + a(' ') : '')
                 + w() + 'function' + w(' ') + node.name.valueWithBar + w() + '('
                 + join(node.allParams, pretty, replaceLambda, l, w, '', ',')
                 + w() + ')' + a(' ') + w() + 'returns' + a(' ')
                 + join(node.returnTypeAnnotationAttachments, pretty, replaceLambda, l, w, '')
                 + getSourceOf(node.returnTypeNode, pretty, l, replaceLambda) + w() + '{'
                 + indent()
                 + join(node.endpointNodes, pretty, replaceLambda, l, w, '') + getSourceOf(node.body, pretty, l, replaceLambda)
                 + join(node.workers, pretty, replaceLambda, l, w, '') + outdent() + w()
                 + '}';
            } else if (node.noVisibleReceiver && node.annotationAttachments
                         && node.documentationAttachments && node.deprecatedAttachments
                         && node.name.valueWithBar && node.allParams && node.restParameters
                         && node.endpointNodes && node.body && node.workers) {
                return dent()
                 + join(node.annotationAttachments, pretty, replaceLambda, l, w, '')
                 + join(node.documentationAttachments, pretty, replaceLambda, l, w, '')
                 + join(node.deprecatedAttachments, pretty, replaceLambda, l, w, '') + (node.public ? w() + 'public' + a(' ') : '')
                 + w() + 'function' + w(' ') + node.name.valueWithBar + w() + '('
                 + join(node.allParams, pretty, replaceLambda, l, w, '', ',')
                 + (node.hasRestParams ? w() + ',' : '')
                 + getSourceOf(node.restParameters, pretty, l, replaceLambda) + w() + ')' + a(' ') + w() + '{'
                 + indent()
                 + join(node.endpointNodes, pretty, replaceLambda, l, w, '') + getSourceOf(node.body, pretty, l, replaceLambda)
                 + join(node.workers, pretty, replaceLambda, l, w, '') + outdent() + w()
                 + '}';
            } else if (node.noVisibleReceiver && node.annotationAttachments
                         && node.documentationAttachments && node.deprecatedAttachments
                         && node.name.valueWithBar && node.allParams && node.endpointNodes && node.body
                         && node.workers) {
                return dent()
                 + join(node.annotationAttachments, pretty, replaceLambda, l, w, '')
                 + join(node.documentationAttachments, pretty, replaceLambda, l, w, '')
                 + join(node.deprecatedAttachments, pretty, replaceLambda, l, w, '') + (node.public ? w() + 'public' + a(' ') : '')
                 + w() + 'function' + w(' ') + node.name.valueWithBar + w() + '('
                 + join(node.allParams, pretty, replaceLambda, l, w, '', ',')
                 + w() + ')' + a(' ') + w() + '{' + indent()
                 + join(node.endpointNodes, pretty, replaceLambda, l, w, '')
                 + getSourceOf(node.body, pretty, l, replaceLambda)
                 + join(node.workers, pretty, replaceLambda, l, w, '') + outdent() + w() + '}';
            } else if (node.objectOuterFunction && node.annotationAttachments
                         && node.documentationAttachments && node.deprecatedAttachments
                         && node.objectOuterFunctionTypeName.valueWithBar && node.name.valueWithBar
                         && node.allParams && node.restParameters && node.hasReturns
                         && node.returnTypeAnnotationAttachments && node.returnTypeNode
                         && node.endpointNodes && node.body && node.workers) {
                return dent()
                 + join(node.annotationAttachments, pretty, replaceLambda, l, w, '')
                 + join(node.documentationAttachments, pretty, replaceLambda, l, w, '')
                 + join(node.deprecatedAttachments, pretty, replaceLambda, l, w, '') + (node.public ? w() + 'public' + a(' ') : '')
                 + w() + 'function' + w()
                 + node.objectOuterFunctionTypeName.valueWithBar + w() + '::' + w(' ') + node.name.valueWithBar + w()
                 + '(' + join(node.allParams, pretty, replaceLambda, l, w, '', ',')
                 + (node.hasRestParams ? w() + ',' : '')
                 + getSourceOf(node.restParameters, pretty, l, replaceLambda) + w() + ')' + a(' ') + w()
                 + 'returns' + a(' ')
                 + join(node.returnTypeAnnotationAttachments, pretty, replaceLambda, l, w, '')
                 + getSourceOf(node.returnTypeNode, pretty, l, replaceLambda) + w() + '{' + indent()
                 + join(node.endpointNodes, pretty, replaceLambda, l, w, '')
                 + getSourceOf(node.body, pretty, l, replaceLambda)
                 + join(node.workers, pretty, replaceLambda, l, w, '') + outdent() + w() + '}';
            } else if (node.objectOuterFunction && node.annotationAttachments
                         && node.documentationAttachments && node.deprecatedAttachments
                         && node.objectOuterFunctionTypeName.valueWithBar && node.name.valueWithBar
                         && node.allParams && node.hasReturns
                         && node.returnTypeAnnotationAttachments && node.returnTypeNode && node.endpointNodes
                         && node.body && node.workers) {
                return dent()
                 + join(node.annotationAttachments, pretty, replaceLambda, l, w, '')
                 + join(node.documentationAttachments, pretty, replaceLambda, l, w, '')
                 + join(node.deprecatedAttachments, pretty, replaceLambda, l, w, '') + (node.public ? w() + 'public' + a(' ') : '')
                 + w() + 'function' + w()
                 + node.objectOuterFunctionTypeName.valueWithBar + w() + '::' + w(' ') + node.name.valueWithBar + w()
                 + '(' + join(node.allParams, pretty, replaceLambda, l, w, '', ',')
                 + w() + ')' + a(' ') + w() + 'returns' + a(' ')
                 + join(node.returnTypeAnnotationAttachments, pretty, replaceLambda, l, w, '')
                 + getSourceOf(node.returnTypeNode, pretty, l, replaceLambda) + w()
                 + '{' + indent()
                 + join(node.endpointNodes, pretty, replaceLambda, l, w, '') + getSourceOf(node.body, pretty, l, replaceLambda)
                 + join(node.workers, pretty, replaceLambda, l, w, '') + outdent()
                 + w() + '}';
            } else if (node.objectOuterFunction && node.annotationAttachments
                         && node.documentationAttachments && node.deprecatedAttachments
                         && node.objectOuterFunctionTypeName.valueWithBar && node.name.valueWithBar
                         && node.allParams && node.restParameters && node.endpointNodes
                         && node.body && node.workers) {
                return dent()
                 + join(node.annotationAttachments, pretty, replaceLambda, l, w, '')
                 + join(node.documentationAttachments, pretty, replaceLambda, l, w, '')
                 + join(node.deprecatedAttachments, pretty, replaceLambda, l, w, '') + (node.public ? w() + 'public' + a(' ') : '')
                 + w() + 'function' + w()
                 + node.objectOuterFunctionTypeName.valueWithBar + w() + '::' + w(' ') + node.name.valueWithBar + w()
                 + '(' + join(node.allParams, pretty, replaceLambda, l, w, '', ',')
                 + (node.hasRestParams ? w() + ',' : '')
                 + getSourceOf(node.restParameters, pretty, l, replaceLambda) + w() + ')' + a(' ') + w()
                 + '{' + indent()
                 + join(node.endpointNodes, pretty, replaceLambda, l, w, '') + getSourceOf(node.body, pretty, l, replaceLambda)
                 + join(node.workers, pretty, replaceLambda, l, w, '') + outdent()
                 + w() + '}';
            } else if (node.objectOuterFunction && node.annotationAttachments
                         && node.documentationAttachments && node.deprecatedAttachments
                         && node.objectOuterFunctionTypeName.valueWithBar && node.name.valueWithBar
                         && node.allParams && node.endpointNodes && node.body
                         && node.workers) {
                return dent()
                 + join(node.annotationAttachments, pretty, replaceLambda, l, w, '')
                 + join(node.documentationAttachments, pretty, replaceLambda, l, w, '')
                 + join(node.deprecatedAttachments, pretty, replaceLambda, l, w, '') + (node.public ? w() + 'public' + a(' ') : '')
                 + w() + 'function' + w()
                 + node.objectOuterFunctionTypeName.valueWithBar + w() + '::' + w(' ') + node.name.valueWithBar + w()
                 + '(' + join(node.allParams, pretty, replaceLambda, l, w, '', ',')
                 + w() + ')' + a(' ') + w() + '{' + indent()
                 + join(node.endpointNodes, pretty, replaceLambda, l, w, '')
                 + getSourceOf(node.body, pretty, l, replaceLambda)
                 + join(node.workers, pretty, replaceLambda, l, w, '') + outdent() + w() + '}';
            } else if (node.hasReturns && node.annotationAttachments
                         && node.documentationAttachments && node.deprecatedAttachments && node.receiver
                         && node.name.valueWithBar && node.allParams && node.restParameters
                         && node.returnTypeAnnotationAttachments && node.returnTypeNode
                         && node.endpointNodes && node.body && node.workers) {
                return dent()
                 + join(node.annotationAttachments, pretty, replaceLambda, l, w, '')
                 + join(node.documentationAttachments, pretty, replaceLambda, l, w, '')
                 + join(node.deprecatedAttachments, pretty, replaceLambda, l, w, '') + (node.public ? w() + 'public' + a(' ') : '')
                 + w() + 'function' + w() + '<'
                 + getSourceOf(node.receiver, pretty, l, replaceLambda) + w() + '>' + w(' ')
                 + node.name.valueWithBar + w() + '('
                 + join(node.allParams, pretty, replaceLambda, l, w, '', ',') + (node.hasRestParams ? w() + ',' : '')
                 + getSourceOf(node.restParameters, pretty, l, replaceLambda) + w() + ')'
                 + a(' ') + w() + 'returns' + a(' ')
                 + join(node.returnTypeAnnotationAttachments, pretty, replaceLambda, l, w, '')
                 + getSourceOf(node.returnTypeNode, pretty, l, replaceLambda) + w() + '{' + indent()
                 + join(node.endpointNodes, pretty, replaceLambda, l, w, '')
                 + getSourceOf(node.body, pretty, l, replaceLambda)
                 + join(node.workers, pretty, replaceLambda, l, w, '') + outdent() + w() + '}';
            } else if (node.hasReturns && node.annotationAttachments
                         && node.documentationAttachments && node.deprecatedAttachments && node.receiver
                         && node.name.valueWithBar && node.allParams
                         && node.returnTypeAnnotationAttachments && node.returnTypeNode && node.endpointNodes
                         && node.body && node.workers) {
                return dent()
                 + join(node.annotationAttachments, pretty, replaceLambda, l, w, '')
                 + join(node.documentationAttachments, pretty, replaceLambda, l, w, '')
                 + join(node.deprecatedAttachments, pretty, replaceLambda, l, w, '') + (node.public ? w() + 'public' + a(' ') : '')
                 + w() + 'function' + w() + '<'
                 + getSourceOf(node.receiver, pretty, l, replaceLambda) + w() + '>' + w(' ')
                 + node.name.valueWithBar + w() + '('
                 + join(node.allParams, pretty, replaceLambda, l, w, '', ',') + w() + ')' + a(' ') + w() + 'returns' + a(' ')
                 + join(node.returnTypeAnnotationAttachments, pretty, replaceLambda, l, w, '')
                 + getSourceOf(node.returnTypeNode, pretty, l, replaceLambda) + w() + '{' + indent()
                 + join(node.endpointNodes, pretty, replaceLambda, l, w, '')
                 + getSourceOf(node.body, pretty, l, replaceLambda) + join(node.workers, pretty, replaceLambda, l, w, '')
                 + outdent() + w() + '}';
            } else if (node.hasReturns && node.annotationAttachments
                         && node.documentationAttachments && node.deprecatedAttachments
                         && node.name.valueWithBar && node.allParams && node.restParameters
                         && node.returnTypeAnnotationAttachments && node.returnTypeNode && node.endpointNodes
                         && node.body && node.workers) {
                return dent()
                 + join(node.annotationAttachments, pretty, replaceLambda, l, w, '')
                 + join(node.documentationAttachments, pretty, replaceLambda, l, w, '')
                 + join(node.deprecatedAttachments, pretty, replaceLambda, l, w, '') + (node.public ? w() + 'public' + a(' ') : '')
                 + w() + 'function' + w(' ') + node.name.valueWithBar + w() + '('
                 + join(node.allParams, pretty, replaceLambda, l, w, '', ',')
                 + (node.hasRestParams ? w() + ',' : '')
                 + getSourceOf(node.restParameters, pretty, l, replaceLambda) + w() + ')' + a(' ') + w()
                 + 'returns' + a(' ')
                 + join(node.returnTypeAnnotationAttachments, pretty, replaceLambda, l, w, '')
                 + getSourceOf(node.returnTypeNode, pretty, l, replaceLambda) + w() + '{' + indent()
                 + join(node.endpointNodes, pretty, replaceLambda, l, w, '')
                 + getSourceOf(node.body, pretty, l, replaceLambda)
                 + join(node.workers, pretty, replaceLambda, l, w, '') + outdent() + w() + '}';
            } else if (node.hasReturns && node.annotationAttachments
                         && node.documentationAttachments && node.deprecatedAttachments
                         && node.name.valueWithBar && node.allParams && node.returnTypeAnnotationAttachments
                         && node.returnTypeNode && node.endpointNodes && node.body
                         && node.workers) {
                return dent()
                 + join(node.annotationAttachments, pretty, replaceLambda, l, w, '')
                 + join(node.documentationAttachments, pretty, replaceLambda, l, w, '')
                 + join(node.deprecatedAttachments, pretty, replaceLambda, l, w, '') + (node.public ? w() + 'public' + a(' ') : '')
                 + w() + 'function' + w(' ') + node.name.valueWithBar + w() + '('
                 + join(node.allParams, pretty, replaceLambda, l, w, '', ',')
                 + w() + ')' + a(' ') + w() + 'returns' + a(' ')
                 + join(node.returnTypeAnnotationAttachments, pretty, replaceLambda, l, w, '')
                 + getSourceOf(node.returnTypeNode, pretty, l, replaceLambda) + w() + '{'
                 + indent()
                 + join(node.endpointNodes, pretty, replaceLambda, l, w, '') + getSourceOf(node.body, pretty, l, replaceLambda)
                 + join(node.workers, pretty, replaceLambda, l, w, '') + outdent() + w()
                 + '}';
            } else if (node.annotationAttachments && node.documentationAttachments
                         && node.deprecatedAttachments && node.receiver
                         && node.name.valueWithBar && node.allParams && node.restParameters && node.endpointNodes
                         && node.body && node.workers) {
                return join(node.annotationAttachments, pretty, replaceLambda, l, w, '')
                 + join(node.documentationAttachments, pretty, replaceLambda, l, w, '')
                 + join(node.deprecatedAttachments, pretty, replaceLambda, l, w, '') + dent() + (node.public ? w() + 'public' + a(' ') : '')
                 + w() + 'function' + w() + '<'
                 + getSourceOf(node.receiver, pretty, l, replaceLambda) + w() + '>' + w(' ')
                 + node.name.valueWithBar + w() + '('
                 + join(node.allParams, pretty, replaceLambda, l, w, '', ',') + (node.hasRestParams ? w() + ',' : '')
                 + getSourceOf(node.restParameters, pretty, l, replaceLambda) + w() + ')'
                 + a(' ') + w() + '{' + indent()
                 + join(node.endpointNodes, pretty, replaceLambda, l, w, '')
                 + getSourceOf(node.body, pretty, l, replaceLambda) + join(node.workers, pretty, replaceLambda, l, w, '')
                 + outdent() + w() + '}';
            } else if (node.annotationAttachments && node.documentationAttachments
                         && node.deprecatedAttachments && node.receiver
                         && node.name.valueWithBar && node.allParams && node.endpointNodes && node.body
                         && node.workers) {
                return join(node.annotationAttachments, pretty, replaceLambda, l, w, '')
                 + join(node.documentationAttachments, pretty, replaceLambda, l, w, '')
                 + join(node.deprecatedAttachments, pretty, replaceLambda, l, w, '') + dent() + (node.public ? w() + 'public' + a(' ') : '')
                 + w() + 'function' + w() + '<'
                 + getSourceOf(node.receiver, pretty, l, replaceLambda) + w() + '>' + w(' ')
                 + node.name.valueWithBar + w() + '('
                 + join(node.allParams, pretty, replaceLambda, l, w, '', ',') + w() + ')' + a(' ') + w() + '{' + indent()
                 + join(node.endpointNodes, pretty, replaceLambda, l, w, '')
                 + getSourceOf(node.body, pretty, l, replaceLambda)
                 + join(node.workers, pretty, replaceLambda, l, w, '') + outdent() + w() + '}';
            } else if (node.annotationAttachments && node.documentationAttachments
                         && node.deprecatedAttachments && node.name.valueWithBar
                         && node.allParams && node.restParameters && node.endpointNodes && node.body
                         && node.workers) {
                return join(node.annotationAttachments, pretty, replaceLambda, l, w, '')
                 + join(node.documentationAttachments, pretty, replaceLambda, l, w, '')
                 + join(node.deprecatedAttachments, pretty, replaceLambda, l, w, '') + dent() + (node.public ? w() + 'public' + a(' ') : '')
                 + w() + 'function' + w(' ') + node.name.valueWithBar + w() + '('
                 + join(node.allParams, pretty, replaceLambda, l, w, '', ',')
                 + (node.hasRestParams ? w() + ',' : '')
                 + getSourceOf(node.restParameters, pretty, l, replaceLambda) + w() + ')' + a(' ') + w() + '{'
                 + indent()
                 + join(node.endpointNodes, pretty, replaceLambda, l, w, '') + getSourceOf(node.body, pretty, l, replaceLambda)
                 + join(node.workers, pretty, replaceLambda, l, w, '') + outdent() + w()
                 + '}';
            } else {
                return join(node.annotationAttachments, pretty, replaceLambda, l, w, '')
                 + join(node.documentationAttachments, pretty, replaceLambda, l, w, '')
                 + join(node.deprecatedAttachments, pretty, replaceLambda, l, w, '') + dent() + (node.public ? w() + 'public' + a(' ') : '')
                 + w() + 'function' + w(' ') + node.name.valueWithBar + w() + '('
                 + join(node.allParams, pretty, replaceLambda, l, w, '', ',')
                 + w() + ')' + a(' ') + w() + '{' + indent()
                 + join(node.endpointNodes, pretty, replaceLambda, l, w, '')
                 + getSourceOf(node.body, pretty, l, replaceLambda)
                 + join(node.workers, pretty, replaceLambda, l, w, '') + outdent() + w() + '}';
            }
        case 'FunctionType':
            if (node.hasReturn && node.withParantheses && node.paramTypeNode
                         && node.returnTypeNode) {
                return w() + '(' + w() + 'function' + w() + '('
                 + join(node.paramTypeNode, pretty, replaceLambda, l, w, '', ',') + w() + ')'
                 + (node.returnKeywordExists ? w() + 'returns' : '')
                 + getSourceOf(node.returnTypeNode, pretty, l, replaceLambda) + w() + ')';
            } else if (node.hasReturn && node.paramTypeNode && node.returnTypeNode) {
                return w() + 'function' + w() + '('
                 + join(node.paramTypeNode, pretty, replaceLambda, l, w, '', ',') + w() + ')'
                 + (node.returnKeywordExists ? w() + 'returns' : '')
                 + getSourceOf(node.returnTypeNode, pretty, l, replaceLambda);
            } else if (node.withParantheses && node.paramTypeNode) {
                return w() + '(' + w() + 'function' + w() + '('
                 + join(node.paramTypeNode, pretty, replaceLambda, l, w, '', ',') + w() + ')' + w() + ')';
            } else {
                return w() + 'function' + w() + '('
                 + join(node.paramTypeNode, pretty, replaceLambda, l, w, '', ',') + w() + ')';
            }
        case 'GroupBy':
            return w() + 'group' + w() + 'by'
                 + join(node.variables, pretty, replaceLambda, l, w, '', ',');
        case 'Having':
            return w() + 'having'
                 + getSourceOf(node.expression, pretty, l, replaceLambda);
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
        case 'IntRangeExpr':
            if (node.isWrappedWithParenthesis && node.startExpression
                         && node.endExpression) {
                return w() + '('
                 + getSourceOf(node.startExpression, pretty, l, replaceLambda) + w() + '..'
                 + getSourceOf(node.endExpression, pretty, l, replaceLambda) + w() + ')';
            } else if (node.isWrappedWithParenthesis && node.startExpression) {
                return w() + '('
                 + getSourceOf(node.startExpression, pretty, l, replaceLambda) + w() + '..' + w() + ')';
            } else if (node.isWrappedWithBracket && node.startExpression
                         && node.endExpression) {
                return w(' ') + '['
                 + getSourceOf(node.startExpression, pretty, l, replaceLambda) + w() + '..'
                 + getSourceOf(node.endExpression, pretty, l, replaceLambda) + w() + ']';
            } else {
                return w(' ') + '['
                 + getSourceOf(node.startExpression, pretty, l, replaceLambda) + w() + '..' + w() + ']';
            }
        case 'Invocation':
            if (node.actionInvocation && node.expression
                         && node.name.valueWithBar && node.argumentExpressions) {
                return (node.async ? w() + 'start' + a(' ') : '')
                 + getSourceOf(node.expression, pretty, l, replaceLambda) + w() + '->' + w()
                 + node.name.valueWithBar + w() + '('
                 + join(node.argumentExpressions, pretty, replaceLambda, l, w, '', ',') + w() + ')';
            } else if (node.expression && node.name.valueWithBar
                         && node.argumentExpressions) {
                return getSourceOf(node.expression, pretty, l, replaceLambda) + w()
                 + '.' + (node.async ? w() + 'start' + a(' ') : '') + w()
                 + node.name.valueWithBar + w() + '('
                 + join(node.argumentExpressions, pretty, replaceLambda, l, w, '', ',') + w() + ')';
            } else if (node.packageAlias.valueWithBar && node.name.valueWithBar
                         && node.argumentExpressions) {
                return w() + node.packageAlias.valueWithBar + w() + ':'
                 + (node.async ? w() + 'start' + a(' ') : '') + w() + node.name.valueWithBar + w()
                 + '('
                 + join(node.argumentExpressions, pretty, replaceLambda, l, w, '', ',') + w() + ')';
            } else {
                return (node.async ? w() + 'start' + a(' ') : '') + w()
                 + node.name.valueWithBar + w() + '('
                 + join(node.argumentExpressions, pretty, replaceLambda, l, w, '', ',') + w() + ')';
            }
        case 'JoinStreamingInput':
            if (node.unidirectionalBeforeJoin && node.streamingInput
                         && node.onExpression) {
                return w() + 'unidirectional' + w() + 'join'
                 + getSourceOf(node.streamingInput, pretty, l, replaceLambda) + w() + 'on'
                 + getSourceOf(node.onExpression, pretty, l, replaceLambda);
            } else if (node.unidirectionalAfterJoin && node.streamingInput
                         && node.onExpression) {
                return w() + 'join' + w() + 'unidirectional'
                 + getSourceOf(node.streamingInput, pretty, l, replaceLambda) + w() + 'on'
                 + getSourceOf(node.onExpression, pretty, l, replaceLambda);
            } else {
                return w() + 'join'
                 + getSourceOf(node.streamingInput, pretty, l, replaceLambda) + w() + 'on'
                 + getSourceOf(node.onExpression, pretty, l, replaceLambda);
            }
        case 'Lambda':
            return getSourceOf(node.functionNode, pretty, l, replaceLambda);
        case 'Limit':
            return w(' ') + 'limit' + w(' ') + node.limitValue;
        case 'Literal':
            if (node.inTemplateLiteral && node.unescapedValue) {
                return w() + node.unescapedValue;
            } else if (node.inTemplateLiteral) {
                return '';
            } else if (node.emptyParantheses) {
                return w(' ') + '(' + w() + ')';
            } else {
                return w() + node.value;
            }
        case 'Lock':
            return dent() + w() + 'lock' + w() + '{' + indent()
                 + getSourceOf(node.body, pretty, l, replaceLambda) + outdent() + w() + '}';
        case 'Match':
            return dent() + w() + 'match' + a(' ')
                 + getSourceOf(node.expression, pretty, l, replaceLambda) + w(' ') + '{' + indent()
                 + join(node.patternClauses, pretty, replaceLambda, l, w, '') + outdent() + w()
                 + '}';
        case 'MatchPatternClause':
            if (node.withCurlies && node.variableNode && node.statement) {
                return dent() + getSourceOf(node.variableNode, pretty, l, replaceLambda)
                 + w(' ') + '=>' + a(' ') + w() + '{' + indent()
                 + getSourceOf(node.statement, pretty, l, replaceLambda) + outdent() + w() + '}';
            } else {
                return getSourceOf(node.variableNode, pretty, l, replaceLambda) + w(' ')
                 + '=>' + a(' ')
                 + getSourceOf(node.statement, pretty, l, replaceLambda);
            }
        case 'MatchExpression':
            return dent() + getSourceOf(node.expression, pretty, l, replaceLambda)
                 + w() + 'but' + a(' ') + w() + '{' + indent()
                 + join(node.patternClauses, pretty, replaceLambda, l, w, '', ',') + outdent() + w()
                 + '}';
        case 'MatchExpressionPatternClause':
            if (node.withCurlies && node.variableNode && node.statement) {
                return dent() + getSourceOf(node.variableNode, pretty, l, replaceLambda)
                 + w() + '=>' + a(' ') + w() + '{' + indent()
                 + getSourceOf(node.statement, pretty, l, replaceLambda) + outdent() + w() + '}';
            } else {
                return getSourceOf(node.variableNode, pretty, l, replaceLambda) + w()
                 + '=>' + a(' ')
                 + getSourceOf(node.statement, pretty, l, replaceLambda);
            }
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
                 + join(node.deprecatedAttachments, pretty, replaceLambda, l, w, '') + (node.public ? w(' ') + 'public' : '') + w()
                 + 'type' + a(' ') + w() + node.name.valueWithBar + w(' ')
                 + 'object' + w() + '{' + indent()
                 + getSourceOf(node.initFunction, pretty, l, replaceLambda)
                 + join(node.functions, pretty, replaceLambda, l, w, '') + w() + '};';
            } else if (node.noPrivateFieldsAvailable && node.publicCommaSeparator
                         && node.annotationAttachments && node.documentationAttachments
                         && node.deprecatedAttachments && node.name.valueWithBar
                         && node.publicFields && node.initFunction && node.functions) {
                return dent() + dent()
                 + join(node.annotationAttachments, pretty, replaceLambda, l, w, '')
                 + join(node.documentationAttachments, pretty, replaceLambda, l, w, '')
                 + join(node.deprecatedAttachments, pretty, replaceLambda, l, w, '') + (node.public ? w(' ')
                 + 'public' : '') + w() + 'type' + a(' ') + w() + node.name.valueWithBar
                 + w(' ') + 'object' + w() + '{' + indent() + w(' ') + 'public' + w()
                 + '{' + indent()
                 + join(node.publicFields, pretty, replaceLambda, l, w, '', ',', true) + outdent() + w() + '}'
                 + getSourceOf(node.initFunction, pretty, l, replaceLambda)
                 + join(node.functions, pretty, replaceLambda, l, w, '') + w() + '};';
            } else if (node.noPrivateFieldsAvailable && node.publicSemicolonSeparator
                         && node.annotationAttachments && node.documentationAttachments
                         && node.deprecatedAttachments && node.name.valueWithBar
                         && node.publicFields && node.initFunction && node.functions) {
                return dent() + dent()
                 + join(node.annotationAttachments, pretty, replaceLambda, l, w, '')
                 + join(node.documentationAttachments, pretty, replaceLambda, l, w, '')
                 + join(node.deprecatedAttachments, pretty, replaceLambda, l, w, '') + (node.public ? w(' ')
                 + 'public' : '') + w() + 'type' + a(' ') + w() + node.name.valueWithBar
                 + w(' ') + 'object' + w() + '{' + indent() + w(' ') + 'public' + w()
                 + '{' + indent()
                 + join(node.publicFields, pretty, replaceLambda, l, w, '', ';', true) + outdent() + w() + '}'
                 + getSourceOf(node.initFunction, pretty, l, replaceLambda)
                 + join(node.functions, pretty, replaceLambda, l, w, '') + w() + '};';
            } else if (node.noPublicFieldAvailable && node.privateCommaSeparator
                         && node.annotationAttachments && node.documentationAttachments
                         && node.deprecatedAttachments && node.name.valueWithBar
                         && node.privateFields && node.initFunction && node.functions) {
                return dent() + dent()
                 + join(node.annotationAttachments, pretty, replaceLambda, l, w, '')
                 + join(node.documentationAttachments, pretty, replaceLambda, l, w, '')
                 + join(node.deprecatedAttachments, pretty, replaceLambda, l, w, '') + (node.public ? w(' ')
                 + 'public' : '') + w() + 'type' + a(' ') + w() + node.name.valueWithBar
                 + w(' ') + 'object' + w() + '{' + indent() + w(' ') + 'private' + w()
                 + '{' + indent()
                 + join(node.privateFields, pretty, replaceLambda, l, w, '', ',', true) + outdent() + w() + '}'
                 + getSourceOf(node.initFunction, pretty, l, replaceLambda)
                 + join(node.functions, pretty, replaceLambda, l, w, '') + w() + '};';
            } else if (node.noPublicFieldAvailable && node.privateSemicolonSeparator
                         && node.annotationAttachments && node.documentationAttachments
                         && node.deprecatedAttachments && node.name.valueWithBar
                         && node.privateFields && node.initFunction && node.functions) {
                return dent() + dent()
                 + join(node.annotationAttachments, pretty, replaceLambda, l, w, '')
                 + join(node.documentationAttachments, pretty, replaceLambda, l, w, '')
                 + join(node.deprecatedAttachments, pretty, replaceLambda, l, w, '') + (node.public ? w(' ')
                 + 'public' : '') + w() + 'type' + a(' ') + w() + node.name.valueWithBar
                 + w(' ') + 'object' + w() + '{' + indent() + w(' ') + 'private' + w()
                 + '{' + indent()
                 + join(node.privateFields, pretty, replaceLambda, l, w, '', ';', true) + outdent() + w() + '}'
                 + getSourceOf(node.initFunction, pretty, l, replaceLambda)
                 + join(node.functions, pretty, replaceLambda, l, w, '') + w() + '};';
            } else if (node.publicCommaSeparator && node.privateCommaSeparator
                         && node.annotationAttachments && node.documentationAttachments
                         && node.deprecatedAttachments && node.name.valueWithBar && node.publicFields
                         && node.privateFields && node.initFunction && node.functions) {
                return dent() + dent() + dent()
                 + join(node.annotationAttachments, pretty, replaceLambda, l, w, '')
                 + join(node.documentationAttachments, pretty, replaceLambda, l, w, '')
                 + join(node.deprecatedAttachments, pretty, replaceLambda, l, w, '') + (node.public ? w(' ')
                 + 'public' : '') + w() + 'type' + a(' ') + w()
                 + node.name.valueWithBar + w(' ') + 'object' + w() + '{' + indent() + w(' ') + 'public'
                 + w() + '{' + indent()
                 + join(node.publicFields, pretty, replaceLambda, l, w, '', ',', true) + outdent() + w() + '}' + w(' ')
                 + 'private' + w() + '{' + indent()
                 + join(node.privateFields, pretty, replaceLambda, l, w, '', ',', true) + outdent() + w() + '}'
                 + getSourceOf(node.initFunction, pretty, l, replaceLambda)
                 + join(node.functions, pretty, replaceLambda, l, w, '') + w() + '};';
            } else if (node.publicSemicolonSeparator && node.privateSemicolonSeparator
                         && node.annotationAttachments && node.documentationAttachments
                         && node.deprecatedAttachments && node.name.valueWithBar
                         && node.publicFields && node.privateFields && node.initFunction
                         && node.functions) {
                return dent() + dent() + dent()
                 + join(node.annotationAttachments, pretty, replaceLambda, l, w, '')
                 + join(node.documentationAttachments, pretty, replaceLambda, l, w, '')
                 + join(node.deprecatedAttachments, pretty, replaceLambda, l, w, '') + (node.public ? w(' ')
                 + 'public' : '') + w() + 'type' + a(' ') + w()
                 + node.name.valueWithBar + w(' ') + 'object' + w() + '{' + indent() + w(' ') + 'public'
                 + w() + '{' + indent()
                 + join(node.publicFields, pretty, replaceLambda, l, w, '', ';', true) + outdent() + w() + '}' + w(' ')
                 + 'private' + w() + '{' + indent()
                 + join(node.privateFields, pretty, replaceLambda, l, w, '', ';', true) + outdent() + w() + '}'
                 + getSourceOf(node.initFunction, pretty, l, replaceLambda)
                 + join(node.functions, pretty, replaceLambda, l, w, '') + w() + '};';
            } else if (node.publicCommaSeparator && node.privateSemicolonSeparator
                         && node.annotationAttachments && node.documentationAttachments
                         && node.deprecatedAttachments && node.name.valueWithBar
                         && node.publicFields && node.privateFields && node.initFunction
                         && node.functions) {
                return dent() + dent() + dent()
                 + join(node.annotationAttachments, pretty, replaceLambda, l, w, '')
                 + join(node.documentationAttachments, pretty, replaceLambda, l, w, '')
                 + join(node.deprecatedAttachments, pretty, replaceLambda, l, w, '') + (node.public ? w(' ')
                 + 'public' : '') + w() + 'type' + a(' ') + w()
                 + node.name.valueWithBar + w(' ') + 'object' + w() + '{' + indent() + w(' ') + 'public'
                 + w() + '{' + indent()
                 + join(node.publicFields, pretty, replaceLambda, l, w, '', ',', true) + outdent() + w() + '}' + w(' ')
                 + 'private' + w() + '{' + indent()
                 + join(node.privateFields, pretty, replaceLambda, l, w, '', ';', true) + outdent() + w() + '}'
                 + getSourceOf(node.initFunction, pretty, l, replaceLambda)
                 + join(node.functions, pretty, replaceLambda, l, w, '') + w() + '};';
            } else if (node.publicSemicolonSeparator && node.privateCommaSeparator
                         && node.annotationAttachments && node.documentationAttachments
                         && node.deprecatedAttachments && node.name.valueWithBar
                         && node.publicFields && node.privateFields && node.initFunction
                         && node.functions) {
                return dent() + dent() + dent()
                 + join(node.annotationAttachments, pretty, replaceLambda, l, w, '')
                 + join(node.documentationAttachments, pretty, replaceLambda, l, w, '')
                 + join(node.deprecatedAttachments, pretty, replaceLambda, l, w, '') + (node.public ? w(' ')
                 + 'public' : '') + w() + 'type' + a(' ') + w()
                 + node.name.valueWithBar + w(' ') + 'object' + w() + '{' + indent() + w(' ') + 'public'
                 + w() + '{' + indent()
                 + join(node.publicFields, pretty, replaceLambda, l, w, '', ';', true) + outdent() + w() + '}' + w(' ')
                 + 'private' + w() + '{' + indent()
                 + join(node.privateFields, pretty, replaceLambda, l, w, '', ',', true) + outdent() + w() + '}'
                 + getSourceOf(node.initFunction, pretty, l, replaceLambda)
                 + join(node.functions, pretty, replaceLambda, l, w, '') + w() + '};';
            } else {
                return join(node.annotationAttachments, pretty, replaceLambda, l, w, '')
                 + join(node.documentationAttachments, pretty, replaceLambda, l, w, '')
                 + join(node.deprecatedAttachments, pretty, replaceLambda, l, w, '') + dent() + dent() + dent() + (node.public ? w(' ')
                 + 'public' : '') + w() + 'type' + a(' ') + w()
                 + node.name.valueWithBar + w(' ') + 'object' + w() + '{' + indent() + w(' ') + 'public'
                 + w() + '{' + indent()
                 + join(node.publicFields, pretty, replaceLambda, l, w, '', ',', true) + outdent() + w() + '}' + w(' ')
                 + 'private' + w() + '{' + indent()
                 + join(node.privateFields, pretty, replaceLambda, l, w, '', ',', true) + outdent() + w() + '}'
                 + getSourceOf(node.initFunction, pretty, l, replaceLambda)
                 + join(node.functions, pretty, replaceLambda, l, w, '') + w() + '};';
            }
        case 'OrderBy':
            return w() + 'order' + w() + 'by'
                 + join(node.variables, pretty, replaceLambda, l, w, '', ',');
        case 'OrderByVariable':
            return getSourceOf(node.variableReference, pretty, l, replaceLambda)
                 + w(' ') + node.orderByType;
        case 'PostIncrement':
            return dent() + getSourceOf(node.variable, pretty, l, replaceLambda)
                 + w() + node.operator + w() + ';';
        case 'Record':
            if (node.separateWithComma && node.annotationAttachments
                         && node.documentationAttachments && node.deprecatedAttachments
                         && node.name.valueWithBar && node.fields) {
                return dent()
                 + join(node.annotationAttachments, pretty, replaceLambda, l, w, '')
                 + join(node.documentationAttachments, pretty, replaceLambda, l, w, '')
                 + join(node.deprecatedAttachments, pretty, replaceLambda, l, w, '') + (node.public ? w() + 'public' : '') + w()
                 + 'type' + a(' ') + w() + node.name.valueWithBar + a(' ') + w()
                 + '{' + indent()
                 + join(node.fields, pretty, replaceLambda, l, w, '', ',', true) + w() + '};';
            } else {
                return dent()
                 + join(node.annotationAttachments, pretty, replaceLambda, l, w, '')
                 + join(node.documentationAttachments, pretty, replaceLambda, l, w, '')
                 + join(node.deprecatedAttachments, pretty, replaceLambda, l, w, '') + (node.public ? w() + 'public' : '') + w()
                 + 'type' + a(' ') + w() + node.name.valueWithBar + a(' ') + w()
                 + '{' + indent()
                 + join(node.fields, pretty, replaceLambda, l, w, '', ';', true) + w() + '};';
            }
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
                 + join(node.endpointNodes, pretty, replaceLambda, l, w, '')
                 + getSourceOf(node.body, pretty, l, replaceLambda)
                 + join(node.workers, pretty, replaceLambda, l, w, '') + outdent() + w() + '}';
        case 'RestArgsExpr':
            return w() + '...'
                 + getSourceOf(node.expression, pretty, l, replaceLambda);
        case 'Retry':
            return dent() + w() + 'retry' + w() + ';';
        case 'Return':
            if (node.noExpressionAvailable) {
                return dent() + w() + 'return' + a(' ') + w() + ';';
            } else if (node.emptyBrackets) {
                return w() + 'return' + a(' ') + w() + '();';
            } else {
                return dent() + w() + 'return' + a(' ')
                 + getSourceOf(node.expression, pretty, l, replaceLambda) + w() + ';';
            }
        case 'SelectClause':
            if (node.selectAll && node.groupBy && node.having) {
                return w() + 'select' + w() + '*'
                 + getSourceOf(node.groupBy, pretty, l, replaceLambda)
                 + getSourceOf(node.having, pretty, l, replaceLambda);
            } else if (node.selectAll && node.groupBy) {
                return w() + 'select' + w() + '*'
                 + getSourceOf(node.groupBy, pretty, l, replaceLambda);
            } else if (node.selectAll && node.having) {
                return w() + 'select' + w() + '*'
                 + getSourceOf(node.having, pretty, l, replaceLambda);
            } else if (node.selectAll) {
                return w() + 'select' + w() + '*';
            } else if (node.selectExpressions && node.groupBy && node.having) {
                return w() + 'select'
                 + join(node.selectExpressions, pretty, replaceLambda, l, w, '', ',')
                 + getSourceOf(node.groupBy, pretty, l, replaceLambda) + getSourceOf(node.having, pretty, l, replaceLambda);
            } else if (node.selectExpressions && node.groupBy) {
                return w() + 'select'
                 + join(node.selectExpressions, pretty, replaceLambda, l, w, '', ',')
                 + getSourceOf(node.groupBy, pretty, l, replaceLambda);
            } else if (node.selectExpressions && node.having) {
                return w() + 'select'
                 + join(node.selectExpressions, pretty, replaceLambda, l, w, '', ',')
                 + getSourceOf(node.having, pretty, l, replaceLambda);
            } else {
                return w() + 'select'
                 + join(node.selectExpressions, pretty, replaceLambda, l, w, '', ',');
            }
        case 'SelectExpression':
            if (node.identifierAvailable && node.expression && node.identifier) {
                return getSourceOf(node.expression, pretty, l, replaceLambda) + w(' ')
                 + 'as' + w(' ') + node.identifier;
            } else {
                return getSourceOf(node.expression, pretty, l, replaceLambda);
            }
        case 'Service':
            if (node.isServiceTypeUnavailable && node.bindNotAvailable
                         && node.annotationAttachments && node.documentationAttachments
                         && node.deprecatedAttachments && node.name.valueWithBar && node.variables
                         && node.resources) {
                return dent()
                 + join(node.annotationAttachments, pretty, replaceLambda, l, w, '')
                 + join(node.documentationAttachments, pretty, replaceLambda, l, w, '')
                 + join(node.deprecatedAttachments, pretty, replaceLambda, l, w, '') + w() + 'service' + a(' ') + w(' ')
                 + node.name.valueWithBar + a(' ') + w(' ') + '{' + indent()
                 + join(node.variables, pretty, replaceLambda, l, w, '')
                 + join(node.resources, pretty, replaceLambda, l, w, '') + outdent() + w() + '}';
            } else if (node.isServiceTypeUnavailable && node.annotationAttachments
                         && node.documentationAttachments && node.deprecatedAttachments
                         && node.name.valueWithBar && node.anonymousEndpointBind
                         && node.boundEndpoints && node.variables && node.resources) {
                return dent()
                 + join(node.annotationAttachments, pretty, replaceLambda, l, w, '')
                 + join(node.documentationAttachments, pretty, replaceLambda, l, w, '')
                 + join(node.deprecatedAttachments, pretty, replaceLambda, l, w, '') + w() + 'service' + a(' ') + w(' ')
                 + node.name.valueWithBar + a(' ') + w() + 'bind' + a(' ')
                 + getSourceOf(node.anonymousEndpointBind, pretty, l, replaceLambda)
                 + join(node.boundEndpoints, pretty, replaceLambda, l, w, '', ',') + w(' ')
                 + '{' + indent()
                 + join(node.variables, pretty, replaceLambda, l, w, '') + join(node.resources, pretty, replaceLambda, l, w, '')
                 + outdent() + w() + '}';
            } else if (node.isServiceTypeUnavailable && node.annotationAttachments
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
            } else if (node.bindNotAvailable && node.annotationAttachments
                         && node.documentationAttachments && node.deprecatedAttachments
                         && node.serviceTypeStruct && node.name.valueWithBar && node.variables
                         && node.resources) {
                return dent()
                 + join(node.annotationAttachments, pretty, replaceLambda, l, w, '')
                 + join(node.documentationAttachments, pretty, replaceLambda, l, w, '')
                 + join(node.deprecatedAttachments, pretty, replaceLambda, l, w, '') + w() + 'service' + a(' ') + w() + '<'
                 + getSourceOf(node.serviceTypeStruct, pretty, l, replaceLambda) + w()
                 + '>' + w(' ') + node.name.valueWithBar + a(' ') + w(' ') + '{'
                 + indent() + join(node.variables, pretty, replaceLambda, l, w, '')
                 + join(node.resources, pretty, replaceLambda, l, w, '')
                 + outdent() + w() + '}';
            } else if (node.annotationAttachments && node.documentationAttachments
                         && node.deprecatedAttachments && node.serviceTypeStruct
                         && node.name.valueWithBar && node.anonymousEndpointBind && node.boundEndpoints
                         && node.variables && node.resources) {
                return join(node.annotationAttachments, pretty, replaceLambda, l, w, '')
                 + join(node.documentationAttachments, pretty, replaceLambda, l, w, '')
                 + join(node.deprecatedAttachments, pretty, replaceLambda, l, w, '') + dent() + w() + 'service' + a(' ') + w() + '<'
                 + getSourceOf(node.serviceTypeStruct, pretty, l, replaceLambda) + w()
                 + '>' + w(' ') + node.name.valueWithBar + a(' ') + w() + 'bind'
                 + a(' ')
                 + getSourceOf(node.anonymousEndpointBind, pretty, l, replaceLambda)
                 + join(node.boundEndpoints, pretty, replaceLambda, l, w, '', ',') + w(' ') + '{' + indent()
                 + join(node.variables, pretty, replaceLambda, l, w, '')
                 + join(node.resources, pretty, replaceLambda, l, w, '') + outdent() + w() + '}';
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
        case 'StreamAction':
            return getSourceOf(node.invokableBody, pretty, l, replaceLambda);
        case 'StreamingInput':
            if (node.windowTraversedAfterWhere && node.streamReference
                         && node.beforeStreamingCondition && node.windowClause
                         && node.afterStreamingCondition && node.aliasAvailable && node.alias) {
                return getSourceOf(node.streamReference, pretty, l, replaceLambda)
                 + getSourceOf(node.beforeStreamingCondition, pretty, l, replaceLambda)
                 + getSourceOf(node.windowClause, pretty, l, replaceLambda)
                 + getSourceOf(node.afterStreamingCondition, pretty, l, replaceLambda)
                 + w() + 'as' + w() + node.alias;
            } else if (node.windowTraversedAfterWhere && node.streamReference
                         && node.beforeStreamingCondition && node.windowClause
                         && node.afterStreamingCondition) {
                return getSourceOf(node.streamReference, pretty, l, replaceLambda)
                 + getSourceOf(node.beforeStreamingCondition, pretty, l, replaceLambda)
                 + getSourceOf(node.windowClause, pretty, l, replaceLambda)
                 + getSourceOf(node.afterStreamingCondition, pretty, l, replaceLambda);
            } else if (node.windowTraversedAfterWhere && node.streamReference
                         && node.windowClause && node.afterStreamingCondition && node.aliasAvailable
                         && node.alias) {
                return getSourceOf(node.streamReference, pretty, l, replaceLambda)
                 + getSourceOf(node.windowClause, pretty, l, replaceLambda)
                 + getSourceOf(node.afterStreamingCondition, pretty, l, replaceLambda) + w()
                 + 'as' + w() + node.alias;
            } else if (node.windowTraversedAfterWhere && node.streamReference
                         && node.windowClause && node.afterStreamingCondition) {
                return getSourceOf(node.streamReference, pretty, l, replaceLambda)
                 + getSourceOf(node.windowClause, pretty, l, replaceLambda)
                 + getSourceOf(node.afterStreamingCondition, pretty, l, replaceLambda);
            } else if (node.windowTraversedAfterWhere && node.streamReference
                         && node.windowClause && node.afterStreamingCondition && node.aliasAvailable
                         && node.alias) {
                return getSourceOf(node.streamReference, pretty, l, replaceLambda)
                 + getSourceOf(node.windowClause, pretty, l, replaceLambda)
                 + getSourceOf(node.afterStreamingCondition, pretty, l, replaceLambda) + w()
                 + 'as' + w() + node.alias;
            } else if (node.windowTraversedAfterWhere && node.streamReference
                         && node.windowClause && node.afterStreamingCondition) {
                return getSourceOf(node.streamReference, pretty, l, replaceLambda)
                 + getSourceOf(node.windowClause, pretty, l, replaceLambda)
                 + getSourceOf(node.afterStreamingCondition, pretty, l, replaceLambda);
            } else if (node.windowTraversedAfterWhere && node.streamReference
                         && node.beforeStreamingCondition && node.windowClause
                         && node.aliasAvailable && node.alias) {
                return getSourceOf(node.streamReference, pretty, l, replaceLambda)
                 + getSourceOf(node.beforeStreamingCondition, pretty, l, replaceLambda)
                 + getSourceOf(node.windowClause, pretty, l, replaceLambda) + w()
                 + 'as' + w() + node.alias;
            } else if (node.windowTraversedAfterWhere && node.streamReference
                         && node.beforeStreamingCondition && node.windowClause) {
                return getSourceOf(node.streamReference, pretty, l, replaceLambda)
                 + getSourceOf(node.beforeStreamingCondition, pretty, l, replaceLambda)
                 + getSourceOf(node.windowClause, pretty, l, replaceLambda);
            } else if (node.streamReference && node.beforeStreamingCondition
                         && node.afterStreamingCondition && node.aliasAvailable && node.alias) {
                return getSourceOf(node.streamReference, pretty, l, replaceLambda)
                 + getSourceOf(node.beforeStreamingCondition, pretty, l, replaceLambda)
                 + getSourceOf(node.afterStreamingCondition, pretty, l, replaceLambda) + w() + 'as' + w() + node.alias;
            } else if (node.streamReference && node.beforeStreamingCondition
                         && node.afterStreamingCondition) {
                return getSourceOf(node.streamReference, pretty, l, replaceLambda)
                 + getSourceOf(node.beforeStreamingCondition, pretty, l, replaceLambda)
                 + getSourceOf(node.afterStreamingCondition, pretty, l, replaceLambda);
            } else if (node.streamReference && node.beforeStreamingCondition
                         && node.aliasAvailable && node.alias) {
                return getSourceOf(node.streamReference, pretty, l, replaceLambda)
                 + getSourceOf(node.beforeStreamingCondition, pretty, l, replaceLambda)
                 + w() + 'as' + w() + node.alias;
            } else if (node.streamReference && node.beforeStreamingCondition) {
                return getSourceOf(node.streamReference, pretty, l, replaceLambda)
                 + getSourceOf(node.beforeStreamingCondition, pretty, l, replaceLambda);
            } else if (node.streamReference && node.afterStreamingCondition
                         && node.aliasAvailable && node.alias) {
                return getSourceOf(node.streamReference, pretty, l, replaceLambda)
                 + getSourceOf(node.afterStreamingCondition, pretty, l, replaceLambda)
                 + w() + 'as' + w() + node.alias;
            } else if (node.streamReference && node.afterStreamingCondition) {
                return getSourceOf(node.streamReference, pretty, l, replaceLambda)
                 + getSourceOf(node.afterStreamingCondition, pretty, l, replaceLambda);
            } else if (node.streamReference && node.aliasAvailable && node.alias) {
                return getSourceOf(node.streamReference, pretty, l, replaceLambda) + w()
                 + 'as' + w() + node.alias;
            } else {
                return getSourceOf(node.streamReference, pretty, l, replaceLambda);
            }
        case 'StreamingQuery':
            if (node.streamingInput && node.joinStreamingInput
                         && node.selectClause && node.orderbyClause && node.streamingAction) {
                return w() + 'from'
                 + getSourceOf(node.streamingInput, pretty, l, replaceLambda)
                 + getSourceOf(node.joinStreamingInput, pretty, l, replaceLambda)
                 + getSourceOf(node.selectClause, pretty, l, replaceLambda) + getSourceOf(node.orderbyClause, pretty, l, replaceLambda)
                 + getSourceOf(node.streamingAction, pretty, l, replaceLambda);
            } else if (node.streamingInput && node.selectClause && node.orderbyClause
                         && node.streamingAction) {
                return w() + 'from'
                 + getSourceOf(node.streamingInput, pretty, l, replaceLambda)
                 + getSourceOf(node.selectClause, pretty, l, replaceLambda) + getSourceOf(node.orderbyClause, pretty, l, replaceLambda)
                 + getSourceOf(node.streamingAction, pretty, l, replaceLambda);
            } else if (node.streamingInput && node.joinStreamingInput
                         && node.selectClause && node.streamingAction) {
                return w() + 'from'
                 + getSourceOf(node.streamingInput, pretty, l, replaceLambda)
                 + getSourceOf(node.joinStreamingInput, pretty, l, replaceLambda)
                 + getSourceOf(node.selectClause, pretty, l, replaceLambda) + getSourceOf(node.streamingAction, pretty, l, replaceLambda);
            } else if (node.streamingInput && node.selectClause && node.streamingAction) {
                return w() + 'from'
                 + getSourceOf(node.streamingInput, pretty, l, replaceLambda)
                 + getSourceOf(node.selectClause, pretty, l, replaceLambda) + getSourceOf(node.streamingAction, pretty, l, replaceLambda);
            } else if (node.streamingInput && node.joinStreamingInput
                         && node.selectClause && node.orderbyClause) {
                return w() + 'from'
                 + getSourceOf(node.streamingInput, pretty, l, replaceLambda)
                 + getSourceOf(node.joinStreamingInput, pretty, l, replaceLambda)
                 + getSourceOf(node.selectClause, pretty, l, replaceLambda) + getSourceOf(node.orderbyClause, pretty, l, replaceLambda);
            } else if (node.streamingInput && node.selectClause && node.orderbyClause) {
                return w() + 'from'
                 + getSourceOf(node.streamingInput, pretty, l, replaceLambda)
                 + getSourceOf(node.selectClause, pretty, l, replaceLambda) + getSourceOf(node.orderbyClause, pretty, l, replaceLambda);
            } else if (node.streamingInput && node.joinStreamingInput
                         && node.selectClause) {
                return w() + 'from'
                 + getSourceOf(node.streamingInput, pretty, l, replaceLambda)
                 + getSourceOf(node.joinStreamingInput, pretty, l, replaceLambda)
                 + getSourceOf(node.selectClause, pretty, l, replaceLambda);
            } else {
                return w() + 'from'
                 + getSourceOf(node.streamingInput, pretty, l, replaceLambda)
                 + getSourceOf(node.selectClause, pretty, l, replaceLambda);
            }
        case 'StringTemplateLiteral':
            return w() + 'string\u0020`'
                 + join(node.expressions, pretty, replaceLambda, l, w, '') + w() + '`';
        case 'Table':
            return w() + 'table'
                 + getSourceOf(node.configurationExpression, pretty, l, replaceLambda);
        case 'TableQueryExpression':
            return getSourceOf(node.tableQuery, pretty, l, replaceLambda);
        case 'TableQuery':
            if (node.streamingInput && node.joinStreamingInput
                         && node.selectClauseNode && node.orderByNode && node.limitClause) {
                return w() + 'from'
                 + getSourceOf(node.streamingInput, pretty, l, replaceLambda)
                 + getSourceOf(node.joinStreamingInput, pretty, l, replaceLambda)
                 + getSourceOf(node.selectClauseNode, pretty, l, replaceLambda) + getSourceOf(node.orderByNode, pretty, l, replaceLambda)
                 + getSourceOf(node.limitClause, pretty, l, replaceLambda);
            } else if (node.streamingInput && node.selectClauseNode && node.orderByNode
                         && node.limitClause) {
                return w() + 'from'
                 + getSourceOf(node.streamingInput, pretty, l, replaceLambda)
                 + getSourceOf(node.selectClauseNode, pretty, l, replaceLambda) + getSourceOf(node.orderByNode, pretty, l, replaceLambda)
                 + getSourceOf(node.limitClause, pretty, l, replaceLambda);
            } else if (node.streamingInput && node.selectClauseNode && node.limitClause) {
                return w() + 'from'
                 + getSourceOf(node.streamingInput, pretty, l, replaceLambda)
                 + getSourceOf(node.selectClauseNode, pretty, l, replaceLambda) + getSourceOf(node.limitClause, pretty, l, replaceLambda);
            } else if (node.streamingInput && node.joinStreamingInput
                         && node.selectClauseNode && node.limitClause) {
                return w() + 'from'
                 + getSourceOf(node.streamingInput, pretty, l, replaceLambda)
                 + getSourceOf(node.joinStreamingInput, pretty, l, replaceLambda)
                 + getSourceOf(node.selectClauseNode, pretty, l, replaceLambda) + getSourceOf(node.limitClause, pretty, l, replaceLambda);
            } else if (node.streamingInput && node.joinStreamingInput
                         && node.selectClauseNode && node.orderByNode) {
                return w() + 'from'
                 + getSourceOf(node.streamingInput, pretty, l, replaceLambda)
                 + getSourceOf(node.joinStreamingInput, pretty, l, replaceLambda)
                 + getSourceOf(node.selectClauseNode, pretty, l, replaceLambda) + getSourceOf(node.orderByNode, pretty, l, replaceLambda);
            } else if (node.streamingInput && node.selectClauseNode && node.orderByNode) {
                return w() + 'from'
                 + getSourceOf(node.streamingInput, pretty, l, replaceLambda)
                 + getSourceOf(node.selectClauseNode, pretty, l, replaceLambda) + getSourceOf(node.orderByNode, pretty, l, replaceLambda);
            } else {
                return w() + 'from'
                 + getSourceOf(node.streamingInput, pretty, l, replaceLambda)
                 + getSourceOf(node.selectClauseNode, pretty, l, replaceLambda);
            }
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
                 + getSourceOf(node.onCommitFunction, pretty, l, replaceLambda) + w() + '{' + indent()
                 + getSourceOf(node.transactionBody, pretty, l, replaceLambda) + outdent() + w() + '}' + w()
                 + 'onretry' + a(' ') + w() + '{' + indent()
                 + getSourceOf(node.onRetryBody, pretty, l, replaceLambda) + outdent() + w() + '}';
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
                 + getSourceOf(node.retryCount, pretty, l, replaceLambda) + w() + '{' + indent()
                 + getSourceOf(node.transactionBody, pretty, l, replaceLambda) + outdent() + w()
                 + '}' + w() + 'onretry' + a(' ') + w() + '{' + indent()
                 + getSourceOf(node.onRetryBody, pretty, l, replaceLambda) + outdent()
                 + w() + '}';
            } else if (node.onCommitFunction && node.transactionBody
                         && node.onRetryBody) {
                return dent() + dent() + w() + 'transaction' + a(' ') + w() + 'with'
                 + a(' ') + w() + 'oncommit' + w() + '='
                 + getSourceOf(node.onCommitFunction, pretty, l, replaceLambda) + w() + '{' + indent()
                 + getSourceOf(node.transactionBody, pretty, l, replaceLambda)
                 + outdent() + w() + '}' + w() + 'onretry' + a(' ') + w() + '{' + indent()
                 + getSourceOf(node.onRetryBody, pretty, l, replaceLambda)
                 + outdent() + w() + '}';
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
                 + getSourceOf(node.onCommitFunction, pretty, l, replaceLambda) + w() + '{' + indent()
                 + getSourceOf(node.transactionBody, pretty, l, replaceLambda) + outdent() + w() + '}';
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
                 + getSourceOf(node.retryCount, pretty, l, replaceLambda) + w() + '{' + indent()
                 + getSourceOf(node.transactionBody, pretty, l, replaceLambda) + outdent() + w() + '}';
            } else if (node.onCommitFunction && node.transactionBody) {
                return dent() + w() + 'transaction' + a(' ') + w() + 'with' + a(' ')
                 + w() + 'oncommit' + w() + '='
                 + getSourceOf(node.onCommitFunction, pretty, l, replaceLambda) + w() + '{' + indent()
                 + getSourceOf(node.transactionBody, pretty, l, replaceLambda) + outdent() + w()
                 + '}';
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
                         && node.variableRefs.length && node.expression) {
                return dent() + w() + 'var' + w() + '('
                 + join(node.variableRefs, pretty, replaceLambda, l, w, '', ',') + w() + ')' + w() + '='
                 + getSourceOf(node.expression, pretty, l, replaceLambda) + w() + ';';
            } else {
                return dent() + w() + '('
                 + join(node.variableRefs, pretty, replaceLambda, l, w, '', ',') + w() + ')' + w() + '='
                 + getSourceOf(node.expression, pretty, l, replaceLambda) + w() + ';';
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
        case 'TypeDefinition':
            if (node.annotationAttachments && node.documentationAttachments
                         && node.deprecatedAttachments && node.name.valueWithBar
                         && node.typeNode && node.valueSet) {
                return dent()
                 + join(node.annotationAttachments, pretty, replaceLambda, l, w, '')
                 + join(node.documentationAttachments, pretty, replaceLambda, l, w, '')
                 + join(node.deprecatedAttachments, pretty, replaceLambda, l, w, '') + (node.public ? w() + 'public' : '') + w()
                 + 'type' + a(' ') + w() + node.name.valueWithBar + a(' ')
                 + getSourceOf(node.typeNode, pretty, l, replaceLambda) + w() + '|'
                 + join(node.valueSet, pretty, replaceLambda, l, w, '', '|') + w() + ';';
            } else {
                return dent()
                 + join(node.annotationAttachments, pretty, replaceLambda, l, w, '')
                 + join(node.documentationAttachments, pretty, replaceLambda, l, w, '')
                 + join(node.deprecatedAttachments, pretty, replaceLambda, l, w, '') + (node.public ? w() + 'public' : '') + w()
                 + 'type' + a(' ') + w() + node.name.valueWithBar + a(' ')
                 + join(node.valueSet, pretty, replaceLambda, l, w, '', '|') + w() + ';';
            }
        case 'TypedescExpression':
            return getSourceOf(node.typeNode, pretty, l, replaceLambda);
        case 'TypeofExpression':
            return w() + 'typeof' + b(' ')
                 + getSourceOf(node.typeNode, pretty, l, replaceLambda);
        case 'TypeInitExpr':
            if (node.noExpressionAvailable && node.noTypeAttached
                         && node.hasParantheses) {
                return w() + 'new' + w(' ') + '(' + w() + ')';
            } else if (node.noExpressionAvailable && node.noTypeAttached) {
                return w() + 'new';
            } else if (node.noExpressionAvailable && node.type) {
                return w() + 'new' + getSourceOf(node.type, pretty, l, replaceLambda)
                 + w(' ') + '(' + w() + ')';
            } else if (node.noTypeAttached && node.expressions) {
                return w() + 'new' + w(' ') + '('
                 + join(node.expressions, pretty, replaceLambda, l, w, '', ',') + w() + ')';
            } else {
                return w() + 'new' + getSourceOf(node.type, pretty, l, replaceLambda)
                 + w(' ') + '('
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
            } else if (node.nullableOperatorAvailable && node.grouped
                         && node.packageAlias.valueWithBar && node.typeName.valueWithBar) {
                return w() + '(' + w() + node.packageAlias.valueWithBar + w() + ':'
                 + w() + node.typeName.valueWithBar + w() + '?' + w() + ')';
            } else if (node.nullableOperatorAvailable && node.packageAlias.valueWithBar
                         && node.typeName.valueWithBar) {
                return w() + node.packageAlias.valueWithBar + w() + ':' + w()
                 + node.typeName.valueWithBar + w() + '?';
            } else if (node.nullableOperatorAvailable && node.grouped
                         && node.typeName.valueWithBar) {
                return w() + '(' + w() + node.typeName.valueWithBar + w() + '?' + w()
                 + ')';
            } else if (node.nullableOperatorAvailable && node.typeName.valueWithBar) {
                return w() + node.typeName.valueWithBar + w() + '?';
            } else if (node.grouped && node.packageAlias.valueWithBar
                         && node.typeName.valueWithBar) {
                return w() + '(' + w() + node.packageAlias.valueWithBar + w() + ':'
                 + w() + node.typeName.valueWithBar + w() + ')';
            } else if (node.packageAlias.valueWithBar && node.typeName.valueWithBar) {
                return w() + node.packageAlias.valueWithBar + w() + ':' + w()
                 + node.typeName.valueWithBar;
            } else if (node.grouped && node.typeName.valueWithBar) {
                return w() + '(' + w() + node.typeName.valueWithBar + w() + ')';
            } else {
                return w() + node.typeName.valueWithBar;
            }
        case 'ValueType':
            if (node.emptyParantheses) {
                return w() + '(' + w() + ')';
            } else if (node.withParantheses && node.typeKind
                         && node.nullableOperatorAvailable) {
                return w() + '(' + w() + node.typeKind + w() + '?' + w() + ')';
            } else if (node.withParantheses && node.typeKind) {
                return w() + '(' + w() + node.typeKind + w() + ')';
            } else if (node.typeKind && node.nullableOperatorAvailable) {
                return w() + node.typeKind + w() + '?';
            } else {
                return w() + node.typeKind;
            }
        case 'Variable':
            if (node.noVisibleName && node.typeNode) {
                return getSourceOf(node.typeNode, pretty, l, replaceLambda);
            } else if (node.endpoint && node.typeNode && node.name.valueWithBar
                         && node.initialExpression) {
                return dent() + dent() + w() + 'endpoint'
                 + getSourceOf(node.typeNode, pretty, l, replaceLambda) + w(' ') + node.name.valueWithBar
                 + a(' ') + w() + '{' + indent()
                 + getSourceOf(node.initialExpression, pretty, l, replaceLambda) + w() + ';' + outdent() + w() + '}';
            } else if (node.endpoint && node.typeNode && node.name.valueWithBar) {
                return dent() + w() + 'endpoint'
                 + getSourceOf(node.typeNode, pretty, l, replaceLambda) + w(' ') + node.name.valueWithBar + a(' ') + w()
                 + '{' + indent() + outdent() + w() + '}';
            } else if (node.serviceEndpoint && node.name.valueWithBar) {
                return w() + 'endpoint' + w(' ') + node.name.valueWithBar + a(' ');
            } else if (node.defaultable && node.annotationAttachments
                         && node.documentationAttachments && node.deprecatedAttachments && node.typeNode
                         && node.name.valueWithBar && node.initialExpression) {
                return join(node.annotationAttachments, pretty, replaceLambda, l, w, '')
                 + join(node.documentationAttachments, pretty, replaceLambda, l, w, '')
                 + join(node.deprecatedAttachments, pretty, replaceLambda, l, w, '') + getSourceOf(node.typeNode, pretty, l, replaceLambda)
                 + w(' ') + node.name.valueWithBar + a(' ') + w(' ') + '='
                 + a(' ')
                 + getSourceOf(node.initialExpression, pretty, l, replaceLambda);
            } else if (node.defaultable && node.annotationAttachments
                         && node.documentationAttachments && node.deprecatedAttachments
                         && node.name.valueWithBar && node.initialExpression) {
                return join(node.annotationAttachments, pretty, replaceLambda, l, w, '')
                 + join(node.documentationAttachments, pretty, replaceLambda, l, w, '')
                 + join(node.deprecatedAttachments, pretty, replaceLambda, l, w, '') + w(' ') + node.name.valueWithBar + a(' ') + w(' ')
                 + '=' + a(' ')
                 + getSourceOf(node.initialExpression, pretty, l, replaceLambda);
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
                 + node.name.valueWithBar + a(' ') + w() + '=?'
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
                 + node.name.valueWithBar + a(' ') + w(' ') + '=' + a(' ')
                 + getSourceOf(node.initialExpression, pretty, l, replaceLambda) + w() + ';';
            } else if (node.global && node.annotationAttachments
                         && node.documentationAttachments && node.deprecatedAttachments && node.typeNode
                         && node.name.valueWithBar) {
                return dent()
                 + join(node.annotationAttachments, pretty, replaceLambda, l, w, '')
                 + join(node.documentationAttachments, pretty, replaceLambda, l, w, '')
                 + join(node.deprecatedAttachments, pretty, replaceLambda, l, w, '') + (node.public ? w() + 'public' + a(' ') : '')
                 + (node.const ? w() + 'const' + a(' ') : '')
                 + getSourceOf(node.typeNode, pretty, l, replaceLambda) + w(' ')
                 + node.name.valueWithBar + a(' ') + w() + ';';
            } else if (node.global && node.annotationAttachments
                         && node.documentationAttachments && node.deprecatedAttachments && node.typeNode
                         && node.name.valueWithBar) {
                return dent()
                 + join(node.annotationAttachments, pretty, replaceLambda, l, w, '')
                 + join(node.documentationAttachments, pretty, replaceLambda, l, w, '')
                 + join(node.deprecatedAttachments, pretty, replaceLambda, l, w, '')
                 + getSourceOf(node.typeNode, pretty, l, replaceLambda) + w(' ') + node.name.valueWithBar + a(' ') + w() + ';';
            } else if (node.safeAssignment && node.typeNode && node.name.valueWithBar
                         && node.initialExpression) {
                return getSourceOf(node.typeNode, pretty, l, replaceLambda) + w(' ')
                 + node.name.valueWithBar + a(' ') + w() + '=?'
                 + getSourceOf(node.initialExpression, pretty, l, replaceLambda);
            } else if (node.typeNode && node.name.valueWithBar
                         && node.initialExpression) {
                return getSourceOf(node.typeNode, pretty, l, replaceLambda) + w(' ')
                 + node.name.valueWithBar + a(' ') + w(' ') + '=' + a(' ')
                 + getSourceOf(node.initialExpression, pretty, l, replaceLambda);
            } else {
                return join(node.annotationAttachments, pretty, replaceLambda, l, w, '')
                 + join(node.documentationAttachments, pretty, replaceLambda, l, w, '')
                 + join(node.deprecatedAttachments, pretty, replaceLambda, l, w, '') + getSourceOf(node.typeNode, pretty, l, replaceLambda)
                 + (node.rest ? w() + '...' : '') + w(' ')
                 + node.name.valueWithBar + a(' ');
            }
        case 'VariableDef':
            if (node.endpoint && node.variable) {
                return getSourceOf(node.variable, pretty, l, replaceLambda);
            } else if (node.defaultable && node.variable) {
                return getSourceOf(node.variable, pretty, l, replaceLambda);
            } else {
                return dent() + getSourceOf(node.variable, pretty, l, replaceLambda)
                 + w() + ';';
            }
        case 'Where':
            return w() + 'where'
                 + getSourceOf(node.expression, pretty, l, replaceLambda);
        case 'While':
            return dent() + w() + 'while' + w(' ') + '('
                 + getSourceOf(node.condition, pretty, l, replaceLambda) + w() + ')' + w(' ') + '{'
                 + indent() + getSourceOf(node.body, pretty, l, replaceLambda) + outdent()
                 + w() + '}';
        case 'WindowClause':
            return w() + 'window'
                 + getSourceOf(node.functionInvocation, pretty, l, replaceLambda);
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

