let join;
const tab = '    ';

export default function getSourceOf(node, pretty = false, l = 0) {
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
        if (!shouldIndent && wsI !== undefined) {
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

    function indent() {
        if (shouldIndent) {
            // return '\n' + _.repeat(tab, ++l);
            ++l;
        }
        return '';
    }

    function outdent() {
        if (shouldIndent) {
            return '\n' + _.repeat(tab, --l);
            // l--;
        }
        return '';
    }

    function dent() {
        if (shouldIndent) {
            return '\n' + _.repeat(tab, l);
        }
        return '';
    }

    switch (node.kind) {
        case 'CompilationUnit':
            return join(node.topLevelNodes, pretty, l, w) + w();
        // auto gen start

        case 'PackageDeclaration':
            return dent() + w(' ') + 'package'
                 + join(node.packageName, pretty, l, w, '', '.') + w() + ';';
        case 'Import':
            return dent() + w(' ') + 'import'
                 + join(node.packageName, pretty, l, w, ' ', '.') + w() + ';';
        case 'Identifier':
            return w() + node.value;
        case 'Action':
            if (node.name.value && node.parameters && node.returnParameters
                         && node.returnParameters.length && node.body) {
                return dent() + w(' ') + 'action' + w(' ') + node.name.value + w(' ')
                 + '(' + join(node.parameters, pretty, l, w, ' ', ',') + w() + ')'
                 + w(' ') + '('
                 + join(node.returnParameters, pretty, l, w, ' ', ',') + w() + ')' + w(' ') + '{' + indent()
                 + getSourceOf(node.body, pretty, l) + outdent() + w() + '}';
            } else {
                return dent() + w(' ') + 'action' + w(' ') + node.name.value + w(' ')
                 + '(' + join(node.parameters, pretty, l, w, ' ', ',') + w() + ')'
                 + w(' ') + '{' + indent() + getSourceOf(node.body, pretty, l)
                 + outdent() + w() + '}';
            }
        case 'Annotation':
            if (node.name.value && node.attributes) {
                return dent() + w() + 'annotation' + w() + node.name.value + w() + '{'
                 + indent() + join(node.attributes, pretty, l, w, '', ';', true)
                 + outdent() + w() + '}';
            } else {
                return dent() + w() + 'annotation' + w() + node.name.value + w()
                 + 'attach' + w() + 'resource' + w() + '{' + indent() + outdent() + w()
                 + '}';
            }
        case 'AnnotationAttachment':
            return dent() + w(' ') + '@' + w() + node.packageAlias.value + w() + ':'
                 + w() + node.annotationName.value + w(' ') + '{' + indent()
                 + join(node.attributes, pretty, l, w, ' ', ',') + outdent() + w()
                 + '}';
        case 'AnnotationAttachmentAttribute':
            return w() + node.name + w() + ':' + getSourceOf(node.value, pretty, l);
        case 'AnnotationAttachmentAttributeValue':
            if (node.value) {
                return getSourceOf(node.value, pretty, l);
            } else {
                return w() + '[' + join(node.valueArray, pretty, l, w, '', ',') + w()
                 + ']';
            }
        case 'AnnotationAttribute':
            return getSourceOf(node.typeNode, pretty, l) + w() + node.name.value;
        case 'ArrayLiteralExpr':
            return w(' ') + '[' + join(node.expressions, pretty, l, w, ' ', ',')
                 + w() + ']';
        case 'ArrayType':
            return getSourceOf(node.elementType, pretty, l) + w(' ') + '[' + w()
                 + ']';
        case 'Assignment':
            if (node.declaredWithVar && node.variables && node.expression) {
                return dent() + w(' ') + 'var'
                 + join(node.variables, pretty, l, w, ' ', ',') + w(' ') + '=' + getSourceOf(node.expression, pretty, l)
                 + w() + ';';
            } else {
                return dent() + join(node.variables, pretty, l, w, ' ', ',') + w(' ')
                 + '=' + getSourceOf(node.expression, pretty, l) + w() + ';';
            }
        case 'BinaryExpr':
            return getSourceOf(node.leftExpression, pretty, l) + w(' ')
                 + node.operatorKind + getSourceOf(node.rightExpression, pretty, l);
        case 'Block':
            return join(node.statements, pretty, l, w, ' ');
        case 'Break':
            return dent() + w(' ') + 'break' + w() + ';';
        case 'BuiltInRefType':
            return w(' ') + node.typeKind;
        case 'Comment':
            return w(' ') + node.comment;
        case 'Connector':
            if (node.name.value && node.parameters && node.variableDefs
                         && node.actions) {
                return dent() + w(' ') + 'connector' + w(' ') + node.name.value + w(' ')
                 + '(' + join(node.parameters, pretty, l, w, '', ',') + w() + ')'
                 + w(' ') + '{' + indent()
                 + join(node.variableDefs, pretty, l, w, ' ') + join(node.actions, pretty, l, w, ' ') + outdent() + w()
                 + '}';
            } else {
                return dent() + w(' ') + 'connector' + w(' ') + node.name.value + w(' ')
                 + '(' + join(node.parameters, pretty, l, w, '', ',') + w() + ')'
                 + w(' ') + '{' + indent()
                 + join(node.actions, pretty, l, w, ' ') + outdent() + w() + '}';
            }
        case 'ConnectorInitExpr':
            if (node.connectorType && node.expressions) {
                return w(' ') + 'create' + getSourceOf(node.connectorType, pretty, l)
                 + w(' ') + '(' + join(node.expressions, pretty, l, w, ' ', ',')
                 + w() + ')';
            } else {
                return w(' ') + 'create' + getSourceOf(node.connectorType, pretty, l)
                 + w(' ') + '(' + w() + ')';
            }
        case 'ConstrainedType':
            return '';
        case 'Continue':
            return dent() + w(' ') + 'continue' + w() + ';';
        case 'ExpressionStatement':
            return dent() + getSourceOf(node.expression, pretty, l) + w() + ';';
        case 'FieldBasedAccessExpr':
            return getSourceOf(node.expression, pretty, l) + w(' ') + '.' + w(' ')
                 + node.fieldName.value;
        case 'ForkJoin':
            return dent() + w(' ') + 'fork' + w(' ') + '{' + indent()
                 + join(node.workers, pretty, l, w, ' ') + outdent() + w() + '}'
                 + getSourceOf(node.joinResultVar, pretty, l);
        case 'Function':
            if (node.annotationAttachments && node.name.value && node.parameters
                         && node.returnParameters && node.returnParameters.length
                         && node.body) {
                return dent() + join(node.annotationAttachments, pretty, l, w, ' ')
                 + w(' ') + 'function' + w(' ') + node.name.value + w(' ') + '('
                 + join(node.parameters, pretty, l, w, ' ', ',') + w() + ')' + w(' ')
                 + '(' + join(node.returnParameters, pretty, l, w, ' ', ',') + w()
                 + ')' + w(' ') + '{' + indent()
                 + getSourceOf(node.body, pretty, l) + outdent() + w() + '}';
            } else {
                return dent() + join(node.annotationAttachments, pretty, l, w, ' ')
                 + w(' ') + 'function' + w(' ') + node.name.value + w(' ') + '('
                 + join(node.parameters, pretty, l, w, ' ', ',') + w() + ')' + w(' ')
                 + '{' + indent() + getSourceOf(node.body, pretty, l) + outdent()
                 + w() + '}';
            }
        case 'If':
            if (node.condition && node.body && node.elseStatement
                         && node.ladderParent) {
                return (node.parent.kind === 'If' ? '' : dent()) + w(' ') + 'if'
                 + w(' ') + '(' + getSourceOf(node.condition, pretty, l) + w() + ')'
                 + w(' ') + '{' + indent() + getSourceOf(node.body, pretty, l)
                 + outdent() + w() + '}' + w(' ') + 'else'
                 + getSourceOf(node.elseStatement, pretty, l);
            } else if (node.condition && node.body && node.elseStatement) {
                return (node.parent.kind === 'If' ? '' : dent())
                 + (node.parent.kind === 'If' ? '' : dent()) + w(' ') + 'if' + w(' ') + '('
                 + getSourceOf(node.condition, pretty, l) + w() + ')' + w(' ') + '{' + indent()
                 + getSourceOf(node.body, pretty, l) + outdent() + w() + '}'
                 + w(' ') + 'else' + w(' ') + '{' + indent()
                 + getSourceOf(node.elseStatement, pretty, l) + outdent() + w() + '}';
            } else {
                return (node.parent.kind === 'If' ? '' : dent()) + w(' ') + 'if'
                 + w(' ') + '(' + getSourceOf(node.condition, pretty, l) + w() + ')'
                 + w(' ') + '{' + indent() + getSourceOf(node.body, pretty, l)
                 + outdent() + w() + '}';
            }
        case 'IndexBasedAccessExpr':
            return getSourceOf(node.expression, pretty, l) + w(' ') + '['
                 + getSourceOf(node.index, pretty, l) + w() + ']';
        case 'Invocation':
            if (node.packageAlias.value && node.name.value
                         && node.argumentExpressions) {
                return w(' ') + node.packageAlias.value + w(' ') + ':' + w(' ')
                 + node.name.value + w(' ') + '('
                 + join(node.argumentExpressions, pretty, l, w, ' ', ',') + w() + ')';
            } else if (node.expression && node.name.value && node.argumentExpressions) {
                return getSourceOf(node.expression, pretty, l) + w(' ') + '.' + w(' ')
                 + node.name.value + w(' ') + '('
                 + join(node.argumentExpressions, pretty, l, w, ' ', ',') + w() + ')';
            } else {
                return w(' ') + node.name.value + w(' ') + '('
                 + join(node.argumentExpressions, pretty, l, w, ' ', ',') + w() + ')';
            }
        case 'Literal':
            return w(' ') + node.value;
        case 'RecordLiteralExpr':
            if (node.keyValuePairs) {
                return dent() + w(' ') + '{' + indent()
                 + join(node.keyValuePairs, pretty, l, w, ' ', ',') + outdent() + w() + '}';
            } else {
                return dent() + w(' ') + '{' + indent() + outdent() + w() + '}';
            }
        case 'RecordLiteralKeyValue':
            return getSourceOf(node.key, pretty, l) + w(' ') + ':'
                 + getSourceOf(node.value, pretty, l);
        case 'Resource':
            return dent() + join(node.annotationAttachments, pretty, l, w, ' ')
                 + w() + 'resource' + w() + node.name.value + w() + '('
                 + join(node.parameters, pretty, l, w, ' ', ',') + w() + ')' + w(' ') + '{'
                 + indent() + getSourceOf(node.body, pretty, l) + outdent() + w()
                 + '}';
        case 'Return':
            return dent() + w(' ') + 'return'
                 + join(node.expressions, pretty, l, w, ' ', ',') + w() + ';';
        case 'Service':
            return dent() + join(node.annotationAttachments, pretty, l, w, '') + w()
                 + 'service' + w() + '<' + w()
                 + node.protocolPackageIdentifier.value + w() + '>' + w() + node.name.value + w() + '{' + indent()
                 + join(node.variables, pretty, l, w, '')
                 + join(node.resources, pretty, l, w, '') + outdent() + w() + '}';
        case 'SimpleVariableRef':
            if (node.packageAlias.value && node.variableName.value) {
                return w(' ') + node.packageAlias.value + w(' ') + ':' + w(' ')
                 + node.variableName.value;
            } else {
                return w(' ') + node.variableName.value;
            }
        case 'Struct':
            return dent() + join(node.annotationAttachments, pretty, l, w, ' ')
                 + w(' ') + 'struct' + w(' ') + node.name.value + w(' ') + '{'
                 + indent() + join(node.fields, pretty, l, w, ' ', ';', true) + outdent()
                 + w() + '}';
        case 'Throw':
            return dent() + w(' ') + 'throw'
                 + getSourceOf(node.expressions, pretty, l) + w() + ';';
        case 'Transform':
            return dent() + w() + 'transform' + w() + '{' + indent()
                 + getSourceOf(node.body, pretty, l) + outdent() + w() + '}';
        case 'TypeCastExpr':
            return w(' ') + '(' + getSourceOf(node.typeNode, pretty, l) + w() + ')'
                 + getSourceOf(node.expression, pretty, l);
        case 'TypeConversionExpr':
            return w(' ') + '<' + getSourceOf(node.typeNode, pretty, l) + w(' ')
                 + '>' + getSourceOf(node.expression, pretty, l);
        case 'UnaryExpr':
            return w(' ') + node.operatorKind
                 + getSourceOf(node.expression, pretty, l);
        case 'UserDefinedType':
            if (node.packageAlias.value && node.typeName.value) {
                return w(' ') + node.packageAlias.value + w(' ') + ':' + w(' ')
                 + node.typeName.value;
            } else {
                return w(' ') + node.typeName.value;
            }
        case 'ValueType':
            return w(' ') + node.typeKind;
        case 'Variable':
            if (node.const && node.typeNode && node.name.value
                         && node.initialExpression) {
                return dent() + w(' ') + 'const' + getSourceOf(node.typeNode, pretty, l)
                 + w(' ') + node.name.value + w(' ') + '='
                 + getSourceOf(node.initialExpression, pretty, l) + w() + ';';
            } else if (node.typeNode && node.name.value && node.initialExpression
                         && node.global) {
                return getSourceOf(node.typeNode, pretty, l) + w(' ') + node.name.value
                 + w(' ') + '=' + getSourceOf(node.initialExpression, pretty, l)
                 + w() + ';';
            } else if (node.typeNode && node.name.value && node.initialExpression) {
                return getSourceOf(node.typeNode, pretty, l) + w(' ') + node.name.value
                 + w(' ') + '=' + getSourceOf(node.initialExpression, pretty, l);
            } else if (node.typeNode && node.name.value) {
                return getSourceOf(node.typeNode, pretty, l) + w(' ') + node.name.value;
            } else {
                return getSourceOf(node.typeNode, pretty, l);
            }
        case 'VariableDef':
            return dent() + getSourceOf(node.variable, pretty, l) + w() + ';';
        case 'While':
            return dent() + w(' ') + 'while' + w(' ') + '('
                 + getSourceOf(node.condition, pretty, l) + w() + ')' + w(' ') + '{' + indent()
                 + getSourceOf(node.body, pretty, l) + outdent() + w() + '}';
        case 'Worker':
            return dent() + w(' ') + '{' + indent()
                 + getSourceOf(node.body, pretty, l) + outdent() + w() + '}';
        case 'WorkerReceive':
            return dent() + join(node.expressions, pretty, l, w, ' ') + w(' ')
                 + '<-' + w(' ') + node.workerName.value + w() + ';';
        case 'WorkerSend':
            return dent() + join(node.expressions, pretty, l, w, ' ') + w(' ')
                 + '->' + w(' ') + node.workerName.value + w() + ';';
        case 'XmlAttribute':
            if (node.name && node.value) {
                return getSourceOf(node.name, pretty, l)
                 + getSourceOf(node.value, pretty, l) + w(' ') + '=';
            } else if (node.value) {
                return getSourceOf(node.value, pretty, l) + w(' ') + '=';
            } else {
                return w(' ') + '=';
            }
        case 'XmlCommentLiteral':
            return join(node.textFragments, pretty, l, w, ' ');
        case 'XmlElementLiteral':
            if (node.attributes && node.content && node.endTagName) {
                return w(' ') + '<' + join(node.attributes, pretty, l, w, ' ') + w(' ')
                 + '>' + join(node.content, pretty, l, w, ' ')
                 + getSourceOf(node.endTagName, pretty, l);
            } else {
                return w(' ') + '<' + join(node.attributes, pretty, l, w, ' ') + w(' ')
                 + '/>';
            }
        case 'XmlPiLiteral':
            if (node.target && node.dataTextFragments) {
                return getSourceOf(node.target, pretty, l)
                 + join(node.dataTextFragments, pretty, l, w, ' ');
            } else if (node.dataTextFragments) {
                return join(node.dataTextFragments, pretty, l, w, ' ');
            } else {
                return getSourceOf(node.target, pretty, l);
            }
        case 'XmlQname':
            return w(' ') + '</' + w(' ') + node.localname.value + w(' ') + '>';
        case 'XmlQuotedString':
            return join(node.textFragments, pretty, l, w, ' ');
        case 'XmlTextLiteral':
            return join(node.textFragments, pretty, l, w, ' ');

        // auto gen end
        default:
            console.error('no source gen for' + node.kind);
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
 * @return {string}
 */
join = function (arr, pretty, l, wsFunc, defaultWS, separator, suffixLast = false) {
    let str = '';
    for (let i = 0; i < arr.length; i++) {
        const node = arr[i];
        if (node.kind === 'Identifier') {
            str += wsFunc(defaultWS);
        }
        str += getSourceOf(node, pretty, l);
        if (separator && (suffixLast || i !== arr.length - 1)) {
            str += wsFunc(defaultWS) + separator;
        }
    }

    return str;
};

