
parser grammar BallerinaParser;

options {
    language = Java;
    tokenVocab = BallerinaLexer;
}

//todo revisit blockStatement

// starting point for parsing a bal file
compilationUnit
    :   (importDeclaration | namespaceDeclaration)*
        (documentationString? annotationAttachment* definition)*
        EOF
    ;

packageName
    :   Identifier (DOT Identifier)* version?
    ;

version
    :   VERSION versionPattern
    ;

versionPattern
    :   DecimalIntegerLiteral
    |   DecimalFloatingPointNumber
    |   DecimalExtendedFloatingPointNumber
    ;

importDeclaration
    :   IMPORT (orgName DIV)?  packageName (AS Identifier)? SEMICOLON
    ;

orgName
    :   Identifier
    ;

definition
    :   serviceDefinition
    |   functionDefinition
    |   typeDefinition
    |   annotationDefinition
    |   globalVariableDefinition
    |   constantDefinition
    |   enumDefinition
    ;

serviceDefinition
    :   SERVICE Identifier? ON expressionList serviceBody
    ;

serviceBody
    :   LEFT_BRACE objectMethod* RIGHT_BRACE
    ;

blockFunctionBody
    :   LEFT_BRACE statement* (workerDeclaration+ statement*)? RIGHT_BRACE
    ;

blockStatement
    :   LEFT_BRACE statement* RIGHT_BRACE
    ;

externalFunctionBody
    :   ASSIGN annotationAttachment* EXTERNAL
    ;

exprFunctionBody
    :   EQUAL_GT expression
    ;

functionDefinitionBody
    :   blockFunctionBody
    |   exprFunctionBody SEMICOLON
    |   externalFunctionBody SEMICOLON
    ;

functionDefinition
    :   (PUBLIC | PRIVATE)? REMOTE? FUNCTION anyIdentifierName functionSignature functionDefinitionBody
    ;

anonymousFunctionExpr
    :   explicitAnonymousFunctionExpr
    |   inferAnonymousFunctionExpr
    ;

explicitAnonymousFunctionExpr
    :   FUNCTION functionSignature (blockFunctionBody | exprFunctionBody)
    ;

inferAnonymousFunctionExpr
    :   inferParamList exprFunctionBody
    ;

inferParamList
    :   inferParam
    |   LEFT_PARENTHESIS (inferParam (COMMA inferParam)*)? RIGHT_PARENTHESIS
    ;

inferParam
    :   Identifier
    ;

functionSignature
    :   LEFT_PARENTHESIS formalParameterList? RIGHT_PARENTHESIS returnParameter?
    ;

typeDefinition
    :   PUBLIC? TYPE Identifier finiteType SEMICOLON
    ;

objectBody
    :   (objectFieldDefinition | objectMethod | typeReference)*
    ;

typeReference
    :   MUL simpleTypeName SEMICOLON
    ;

objectFieldDefinition
    :   documentationString? annotationAttachment* (PUBLIC | PRIVATE)? TYPE_READONLY? typeName Identifier
            (ASSIGN expression)? SEMICOLON
    ;

fieldDefinition
    :   documentationString? annotationAttachment* TYPE_READONLY? typeName Identifier QUESTION_MARK?
            (ASSIGN expression)? SEMICOLON
    ;

recordRestFieldDefinition
    :   typeName restDescriptorPredicate ELLIPSIS SEMICOLON
    ;

sealedLiteral
    :   NOT restDescriptorPredicate ELLIPSIS
    ;

restDescriptorPredicate : {_input.get(_input.index() -1).getType() != WS}? ;

objectMethod
    :   methodDeclaration
    |   methodDefinition
    ;

methodDeclaration
    :   documentationString? annotationAttachment* (PUBLIC | PRIVATE)? (REMOTE | RESOURCE)? FUNCTION
            anyIdentifierName functionSignature SEMICOLON
    ;

methodDefinition
    :   documentationString? annotationAttachment* (PUBLIC | PRIVATE)? (REMOTE | RESOURCE)? FUNCTION
            anyIdentifierName functionSignature functionDefinitionBody
    ;

annotationDefinition
    :   PUBLIC? CONST? ANNOTATION typeName? Identifier (ON attachmentPoint (COMMA attachmentPoint)*)? SEMICOLON
    ;

constantDefinition
    :   PUBLIC? CONST typeName? Identifier ASSIGN constantExpression SEMICOLON
    ;

enumDefinition
    :   documentationString? annotationAttachment* PUBLIC? ENUM Identifier LEFT_BRACE
            (enumMember (COMMA enumMember)*)? RIGHT_BRACE
    ;

enumMember
    :   documentationString? annotationAttachment* Identifier (ASSIGN constantExpression)?
    ;

globalVariableDefinition
    :   PUBLIC? LISTENER typeName? Identifier ASSIGN expression SEMICOLON
    |   FINAL? (typeName | VAR) Identifier (ASSIGN expression)? SEMICOLON
    ;

attachmentPoint
    :   dualAttachPoint
    |   sourceOnlyAttachPoint
    ;

dualAttachPoint
    : SOURCE? dualAttachPointIdent
    ;

dualAttachPointIdent
    :   OBJECT? TYPE
    |   (OBJECT | RESOURCE)? FUNCTION
    |   PARAMETER
    |   RETURN
    |   SERVICE
    |   (OBJECT | RECORD)? FIELD
    ;

sourceOnlyAttachPoint
    :   SOURCE sourceOnlyAttachPointIdent
    ;

sourceOnlyAttachPointIdent
    :   ANNOTATION
    |   EXTERNAL
    |   VAR
    |   CONST
    |   LISTENER
    |   WORKER
    ;

workerDeclaration
    :   annotationAttachment* workerDefinition LEFT_BRACE statement* RIGHT_BRACE
    ;

workerDefinition
    :   WORKER Identifier returnParameter?
    ;

finiteType
    :   finiteTypeUnit (PIPE finiteTypeUnit)*
    ;

finiteTypeUnit
    :   simpleLiteral
    |   typeName
    ;

typeName
    :   DISTINCT? simpleTypeName                                                                # simpleTypeNameLabel
    |   typeName (LEFT_BRACKET (integerLiteral | MUL)? RIGHT_BRACKET)+                          # arrayTypeNameLabel
    |   typeName (PIPE typeName)+                                                               # unionTypeNameLabel
    |   typeName BIT_AND typeName                                                               # intersectionTypeNameLabel
    |   typeName QUESTION_MARK                                                                  # nullableTypeNameLabel
    |   LEFT_PARENTHESIS typeName RIGHT_PARENTHESIS                                             # groupTypeNameLabel
    |   tupleTypeDescriptor                                                                     # tupleTypeNameLabel
    |   DISTINCT? ((ABSTRACT? CLIENT?) | (CLIENT? ABSTRACT)) OBJECT LEFT_BRACE objectBody RIGHT_BRACE     # objectTypeNameLabel
    |   inclusiveRecordTypeDescriptor                                                           # inclusiveRecordTypeNameLabel
    |   exclusiveRecordTypeDescriptor                                                           # exclusiveRecordTypeNameLabel
    |   tableTypeDescriptor                                                                     # tableTypeNameLabel
    ;

inclusiveRecordTypeDescriptor
    :   RECORD LEFT_BRACE fieldDescriptor* RIGHT_BRACE
    ;

tupleTypeDescriptor
    : LEFT_BRACKET ((typeName (COMMA typeName)* (COMMA tupleRestDescriptor)?) | tupleRestDescriptor) RIGHT_BRACKET
    ;

tupleRestDescriptor
    : typeName ELLIPSIS
    ;

exclusiveRecordTypeDescriptor
    :   RECORD LEFT_CLOSED_RECORD_DELIMITER fieldDescriptor* recordRestFieldDefinition? RIGHT_CLOSED_RECORD_DELIMITER
    ;

fieldDescriptor
    :   fieldDefinition
    |   typeReference
    ;

// Temporary production rule name
simpleTypeName
    :   TYPE_ANY
    |   TYPE_ANYDATA
    |   TYPE_HANDLE
    |   TYPE_NEVER
    |   TYPE_READONLY
    |   valueTypeName
    |   referenceTypeName
    |   nilLiteral
    ;

referenceTypeName
    :   builtInReferenceTypeName
    |   userDefineTypeName
    ;

userDefineTypeName
    :   nameReference
    ;

valueTypeName
    :   TYPE_BOOL
    |   TYPE_INT
    |   TYPE_BYTE
    |   TYPE_FLOAT
    |   TYPE_DECIMAL
    |   TYPE_STRING
    ;

builtInReferenceTypeName
    :   TYPE_MAP LT typeName GT
    |   TYPE_FUTURE (LT typeName GT)?
    |   TYPE_XML (LT typeName GT)?
    |   TYPE_JSON
    |   TYPE_DESC (LT typeName GT)?
    |   SERVICE
    |   errorTypeName
    |   streamTypeName
    |   functionTypeName
    ;

streamTypeName
    :   TYPE_STREAM (LT typeName (COMMA typeName)? GT)?
    ;

tableConstructorExpr
    :   TYPE_TABLE tableKeySpecifier? LEFT_BRACKET tableRowList? RIGHT_BRACKET
    ;

tableRowList
    :   recordLiteral (COMMA recordLiteral)*
    ;

tableTypeDescriptor
    :   TYPE_TABLE LT typeName GT tableKeyConstraint?
    ;

tableKeyConstraint
    :   tableKeySpecifier | tableKeyTypeConstraint
    ;

tableKeySpecifier
    :   KEY LEFT_PARENTHESIS (Identifier (COMMA Identifier)*)? RIGHT_PARENTHESIS
    ;

tableKeyTypeConstraint
    :   KEY LT typeName GT
    ;

functionTypeName
    :   FUNCTION LEFT_PARENTHESIS (parameterList | parameterTypeNameList)? RIGHT_PARENTHESIS returnParameter?
    ;

errorTypeName
    :   TYPE_ERROR (LT (typeName | MUL) GT)?
    ;

xmlNamespaceName
    :   QuotedStringLiteral
    ;

xmlLocalName
    :   Identifier
    ;

annotationAttachment
    :   AT nameReference recordLiteral?
    ;

 //============================================================================================================
// STATEMENTS / BLOCKS

statement
    :   errorDestructuringStatement
    |   assignmentStatement
    |   variableDefinitionStatement
    |   listDestructuringStatement
    |   recordDestructuringStatement
    |   compoundAssignmentStatement
    |   ifElseStatement
    |   matchStatement
    |   foreachStatement
    |   whileStatement
    |   continueStatement
    |   breakStatement
    |   forkJoinStatement
    |   tryCatchStatement
    |   throwStatement
    |   panicStatement
    |   returnStatement
    |   workerSendAsyncStatement
    |   expressionStmt
    |   transactionStatement
    |   abortStatement
    |   retryStatement
    |   lockStatement
    |   namespaceDeclarationStatement
    |   blockStatement
    ;

variableDefinitionStatement
    :   typeName Identifier SEMICOLON
    |   FINAL? (typeName | VAR) bindingPattern ASSIGN expression SEMICOLON
    ;

recordLiteral
    :   LEFT_BRACE (recordField (COMMA recordField)*)? RIGHT_BRACE
    ;

staticMatchLiterals
    :   simpleLiteral                                                       # staticMatchSimpleLiteral
    |   recordLiteral                                                       # staticMatchRecordLiteral
    |   listConstructorExpr                                                 # staticMatchListLiteral
    |   Identifier                                                          # staticMatchIdentifierLiteral
    |   staticMatchLiterals PIPE staticMatchLiterals                        # staticMatchOrExpression
    ;

recordField
    :   TYPE_READONLY? Identifier
    |   TYPE_READONLY? recordKey COLON expression
    |   ELLIPSIS expression
    ;

recordKey
    :   Identifier
    |   LEFT_BRACKET expression RIGHT_BRACKET
    |   expression
    ;

listConstructorExpr
    :   LEFT_BRACKET expressionList? RIGHT_BRACKET
    ;

assignmentStatement
    :   variableReference ASSIGN expression SEMICOLON
    ;

listDestructuringStatement
    :   listRefBindingPattern ASSIGN expression SEMICOLON
    ;

recordDestructuringStatement
    :   recordRefBindingPattern ASSIGN expression SEMICOLON
    ;

errorDestructuringStatement
    :   errorRefBindingPattern ASSIGN expression SEMICOLON
    ;

compoundAssignmentStatement
    :   variableReference compoundOperator expression SEMICOLON
    ;

compoundOperator
    :   COMPOUND_ADD
    |   COMPOUND_SUB
    |   COMPOUND_MUL
    |   COMPOUND_DIV
    |   COMPOUND_BIT_AND
    |   COMPOUND_BIT_OR
    |   COMPOUND_BIT_XOR
    |   COMPOUND_LEFT_SHIFT
    |   COMPOUND_RIGHT_SHIFT
    |   COMPOUND_LOGICAL_SHIFT
    ;

variableReferenceList
    :   variableReference (COMMA variableReference)*
    ;

ifElseStatement
    :   ifClause elseIfClause* elseClause?
    ;

ifClause
    :   IF expression LEFT_BRACE statement* RIGHT_BRACE
    ;

elseIfClause
    :   ELSE IF expression LEFT_BRACE statement* RIGHT_BRACE
    ;

elseClause
    :   ELSE LEFT_BRACE statement*RIGHT_BRACE
    ;

matchStatement
    :   MATCH  expression  LEFT_BRACE matchPatternClause+ RIGHT_BRACE
    ;

matchPatternClause
    :   staticMatchLiterals EQUAL_GT LEFT_BRACE statement* RIGHT_BRACE
    |   VAR bindingPattern (IF expression)? EQUAL_GT LEFT_BRACE statement* RIGHT_BRACE
    |   errorMatchPattern (IF expression)? EQUAL_GT LEFT_BRACE statement* RIGHT_BRACE
    ;

bindingPattern
    :   Identifier
    |   structuredBindingPattern
    ;

structuredBindingPattern
    :   listBindingPattern
    |   recordBindingPattern
    |   errorBindingPattern
    ;

errorBindingPattern
    :   TYPE_ERROR LEFT_PARENTHESIS errorBindingPatternParamaters RIGHT_PARENTHESIS
    |   userDefineTypeName LEFT_PARENTHESIS errorBindingPatternParamaters RIGHT_PARENTHESIS
    ;

errorBindingPatternParamaters
    : Identifier (COMMA Identifier)? (COMMA errorDetailBindingPattern)* (COMMA errorRestBindingPattern)?
    ;

errorMatchPattern
    :   TYPE_ERROR LEFT_PARENTHESIS errorArgListMatchPattern RIGHT_PARENTHESIS
    |   typeName LEFT_PARENTHESIS errorFieldMatchPatterns RIGHT_PARENTHESIS
    ;

errorArgListMatchPattern
    :   simpleMatchPattern (COMMA errorDetailBindingPattern)* (COMMA restMatchPattern)?
    |   errorDetailBindingPattern (COMMA errorDetailBindingPattern)* (COMMA restMatchPattern)?
    |   restMatchPattern
    ;

errorFieldMatchPatterns
    :   errorDetailBindingPattern (COMMA errorDetailBindingPattern)* (COMMA restMatchPattern)?
    |   restMatchPattern
    ;

errorRestBindingPattern
    :   ELLIPSIS Identifier
    ;

restMatchPattern
    :   ELLIPSIS VAR Identifier
    ;

simpleMatchPattern
    :   VAR? (Identifier | QuotedStringLiteral)
    ;

errorDetailBindingPattern
    :   Identifier ASSIGN bindingPattern
    ;

listBindingPattern
    :   LEFT_BRACKET ((bindingPattern (COMMA bindingPattern)* (COMMA restBindingPattern)?) | restBindingPattern?) RIGHT_BRACKET
    ;

recordBindingPattern
    :   LEFT_BRACE entryBindingPattern RIGHT_BRACE
    ;

entryBindingPattern
    :   fieldBindingPattern (COMMA fieldBindingPattern)* (COMMA restBindingPattern)?
    |   restBindingPattern?
    ;

fieldBindingPattern
    :   Identifier (COLON bindingPattern)?
    ;

restBindingPattern
    :   ELLIPSIS Identifier
    ;

bindingRefPattern
    :   errorRefBindingPattern
    |   variableReference
    |   structuredRefBindingPattern
    ;

structuredRefBindingPattern
    :   listRefBindingPattern
    |   recordRefBindingPattern
    ;

listRefBindingPattern
    :   LEFT_BRACKET ((bindingRefPattern (COMMA bindingRefPattern)* (COMMA listRefRestPattern)?) | listRefRestPattern) RIGHT_BRACKET
    ;

listRefRestPattern
    : ELLIPSIS variableReference
    ;

recordRefBindingPattern
    :  LEFT_BRACE entryRefBindingPattern RIGHT_BRACE
    ;

errorRefBindingPattern
    :   TYPE_ERROR LEFT_PARENTHESIS errorRefArgsPattern RIGHT_PARENTHESIS
    |   typeName LEFT_PARENTHESIS errorRefArgsPattern RIGHT_PARENTHESIS
    ;

errorRefArgsPattern
    :  variableReference (COMMA variableReference)? (COMMA errorNamedArgRefPattern)* (COMMA errorRefRestPattern)?
    ;

errorNamedArgRefPattern
    : Identifier ASSIGN bindingRefPattern
    ;

errorRefRestPattern
    : ELLIPSIS variableReference
    ;

entryRefBindingPattern
    :   fieldRefBindingPattern (COMMA fieldRefBindingPattern)* (COMMA restRefBindingPattern)?
    |   restRefBindingPattern?
    ;

fieldRefBindingPattern
    :   Identifier (COLON bindingRefPattern)?
    ;

restRefBindingPattern
    :   ELLIPSIS variableReference
    |   sealedLiteral
    ;

foreachStatement
    :   FOREACH LEFT_PARENTHESIS? (typeName | VAR) bindingPattern IN expression RIGHT_PARENTHESIS? LEFT_BRACE statement* RIGHT_BRACE
    ;

intRangeExpression
    :   (LEFT_BRACKET | LEFT_PARENTHESIS) expression RANGE expression? (RIGHT_BRACKET | RIGHT_PARENTHESIS)
    ;

whileStatement
    :   WHILE expression LEFT_BRACE statement* RIGHT_BRACE
    ;

continueStatement
    :   CONTINUE SEMICOLON
    ;

breakStatement
    :   BREAK SEMICOLON
    ;

// typeName is only message
forkJoinStatement
    :   FORK LEFT_BRACE workerDeclaration* RIGHT_BRACE
    ;

// Depricated since 0.983.0, use trap expressoin. TODO : Remove this.
tryCatchStatement
    :   TRY LEFT_BRACE statement* RIGHT_BRACE catchClauses
    ;

// TODO : Remove this.
catchClauses
    :   catchClause+ finallyClause?
    |   finallyClause
    ;

// TODO : Remove this.
catchClause
    :   CATCH LEFT_PARENTHESIS typeName Identifier RIGHT_PARENTHESIS LEFT_BRACE statement* RIGHT_BRACE
    ;

// TODO : Remove this.
finallyClause
    :   FINALLY LEFT_BRACE statement* RIGHT_BRACE
    ;

// Depricated since 0.983.0, use panic instead. TODO : Remove this.
throwStatement
    :   THROW expression SEMICOLON
    ;

panicStatement
    :   PANIC expression SEMICOLON
    ;

returnStatement
    :   RETURN expression? SEMICOLON
    ;

workerSendAsyncStatement
    :   expression RARROW peerWorker (COMMA expression)? SEMICOLON
    ;

peerWorker
    : workerName
    | DEFAULT
    ;

workerName
    : Identifier
    ;

flushWorker
    :   FLUSH Identifier?
    ;

waitForCollection
    :   LEFT_BRACE waitKeyValue (COMMA waitKeyValue)* RIGHT_BRACE
    ;

waitKeyValue
    :   Identifier
    |   Identifier COLON expression
    ;

variableReference
    :   nameReference                                                           # simpleVariableReference
    |   variableReference field                                                 # fieldVariableReference
    |   variableReference ANNOTATION_ACCESS nameReference                       # annotAccessExpression
    |   variableReference xmlAttrib                                             # xmlAttribVariableReference
    |   variableReference xmlElementFilter                                      # xmlElementFilterReference
    |   functionInvocation                                                      # functionInvocationReference
    |   LEFT_PARENTHESIS variableReference RIGHT_PARENTHESIS field              # groupFieldVariableReference
    |   LEFT_PARENTHESIS variableReference RIGHT_PARENTHESIS invocation         # groupInvocationReference
    |   LEFT_PARENTHESIS variableReference RIGHT_PARENTHESIS index              # groupMapArrayVariableReference
    |   LEFT_PARENTHESIS QuotedStringLiteral RIGHT_PARENTHESIS invocation       # groupStringFunctionInvocationReference
    |   typeDescExpr invocation                                                 # typeDescExprInvocationReference
    |   QuotedStringLiteral invocation                                          # stringFunctionInvocationReference
    |   variableReference invocation                                            # invocationReference
    |   variableReference index                                                 # mapArrayVariableReference
    |   variableReference xmlStepExpression                                     # xmlStepExpressionReference
    ;

field
    :   (DOT | OPTIONAL_FIELD_ACCESS) ((Identifier COLON)? Identifier | MUL)
    ;

xmlElementFilter
    :   DOT xmlElementNames
    ;

xmlStepExpression
    : DIV xmlElementNames index?
    | DIV MUL index?
    | DIV MUL MUL DIV xmlElementNames index?
    ;

xmlElementNames
    :  LT xmlElementAccessFilter (PIPE xmlElementAccessFilter)*  GT
    ;

xmlElementAccessFilter
    : (Identifier COLON)? (Identifier | MUL)
    ;

index
    :   LEFT_BRACKET (expression | multiKeyIndex) RIGHT_BRACKET
    ;

multiKeyIndex
    :   expression (COMMA expression)+
    ;

xmlAttrib
    :   AT (LEFT_BRACKET expression RIGHT_BRACKET)?
    ;

functionInvocation
    :   functionNameReference LEFT_PARENTHESIS invocationArgList? RIGHT_PARENTHESIS
    ;

invocation
    :   DOT anyIdentifierName LEFT_PARENTHESIS invocationArgList? RIGHT_PARENTHESIS
    ;

invocationArgList
    :   invocationArg (COMMA invocationArg)*
    ;

invocationArg
    :   expression  // required args
    |   namedArgs   // named args
    |   restArgs    // rest args
    ;

actionInvocation
    :   (annotationAttachment* START)? variableReference RARROW functionInvocation
    ;

expressionList
    :   expression (COMMA expression)*
    ;

expressionStmt
    :   expression SEMICOLON
    ;

transactionStatement
    :   transactionClause onretryClause? committedAbortedClauses
    ;

committedAbortedClauses
    :   (committedClause? abortedClause?) | (abortedClause? committedClause?)
    ;

transactionClause
    :   TRANSACTION (WITH transactionPropertyInitStatementList)? LEFT_BRACE statement* RIGHT_BRACE
    ;

transactionPropertyInitStatement
    :   retriesStatement
    ;

transactionPropertyInitStatementList
    :   transactionPropertyInitStatement (COMMA transactionPropertyInitStatement)*
    ;

lockStatement
    :   LOCK LEFT_BRACE statement* RIGHT_BRACE
    ;

onretryClause
    :   ONRETRY LEFT_BRACE statement* RIGHT_BRACE
    ;

committedClause
    :   COMMITTED LEFT_BRACE statement* RIGHT_BRACE
    ;

abortedClause
    :   ABORTED LEFT_BRACE statement* RIGHT_BRACE
    ;

abortStatement
    :   ABORT SEMICOLON
    ;

retryStatement
    :   RETRY SEMICOLON
    ;

retriesStatement
    :   RETRIES ASSIGN expression
    ;

namespaceDeclarationStatement
    :   namespaceDeclaration
    ;

namespaceDeclaration
    :   XMLNS QuotedStringLiteral (AS Identifier)? SEMICOLON
    ;

expression
    :   simpleLiteral                                                       # simpleLiteralExpression
    |   listConstructorExpr                                                 # listConstructorExpression
    |   recordLiteral                                                       # recordLiteralExpression
    |   tableConstructorExpr                                                # tableConstructorExpression
    |   xmlLiteral                                                          # xmlLiteralExpression
    |   stringTemplateLiteral                                               # stringTemplateLiteralExpression
    |   (annotationAttachment* START)? variableReference                    # variableReferenceExpression
    |   actionInvocation                                                    # actionInvocationExpression
    |   typeInitExpr                                                        # typeInitExpression
    |   serviceConstructorExpr                                              # serviceConstructorExpression
    |   CHECK expression                                                    # checkedExpression
    |   CHECKPANIC expression                                               # checkPanickedExpression
    |   (ADD | SUB | BIT_COMPLEMENT | NOT | TYPEOF) expression              # unaryExpression
    |   LT (annotationAttachment+ typeName? | typeName) GT expression       # typeConversionExpression
    |   expression (MUL | DIV | MOD) expression                             # binaryDivMulModExpression
    |   expression (ADD | SUB) expression                                   # binaryAddSubExpression
    |   expression (shiftExpression) expression                             # bitwiseShiftExpression
    |   expression (ELLIPSIS | HALF_OPEN_RANGE) expression                  # integerRangeExpression
    |   expression (LT | GT | LT_EQUAL | GT_EQUAL) expression               # binaryCompareExpression
    |   expression IS typeName                                              # typeTestExpression
    |   expression (EQUAL | NOT_EQUAL) expression                           # binaryEqualExpression
    |   expression JOIN_EQUALS expression                                   # binaryEqualsExpression
    |   expression (REF_EQUAL | REF_NOT_EQUAL) expression                   # binaryRefEqualExpression
    |   expression (BIT_AND | BIT_XOR | PIPE) expression                    # bitwiseExpression
    |   expression AND expression                                           # binaryAndExpression
    |   expression OR expression                                            # binaryOrExpression
    |   expression ELVIS expression                                         # elvisExpression
    |   expression QUESTION_MARK expression COLON expression                # ternaryExpression
    |   explicitAnonymousFunctionExpr                                       # explicitAnonymousFunctionExpression
    |   inferAnonymousFunctionExpr                                          # inferAnonymousFunctionExpression
    |   LEFT_PARENTHESIS expression RIGHT_PARENTHESIS                       # groupExpression
    |   expression SYNCRARROW peerWorker                                    # workerSendSyncExpression
    |   WAIT (waitForCollection | expression)                               # waitExpression
    |   trapExpr                                                            # trapExpression
    |   LARROW peerWorker (COMMA expression)?                               # workerReceiveExpression
    |   flushWorker                                                         # flushWorkerExpression
    |   typeDescExpr                                                        # typeAccessExpression
    |   queryExpr                                                           # queryExpression
    |   queryAction                                                         # queryActionExpression
    |   letExpr                                                             # letExpression
    ;

constantExpression
    :   simpleLiteral                                                       # constSimpleLiteralExpression
    |   recordLiteral                                                       # constRecordLiteralExpression
    |   constantExpression (DIV | MUL) constantExpression                   # constDivMulModExpression
    |   constantExpression (ADD | SUB) constantExpression                   # constAddSubExpression
    |   LEFT_PARENTHESIS constantExpression RIGHT_PARENTHESIS               # constGroupExpression
    ;

letExpr
    : LET letVarDecl (COMMA letVarDecl)* IN expression
    ;

letVarDecl
    : annotationAttachment* (typeName | VAR) bindingPattern ASSIGN expression
    ;

typeDescExpr
    : typeName
    ;

typeInitExpr
    :   NEW (LEFT_PARENTHESIS invocationArgList? RIGHT_PARENTHESIS)?
    |   NEW (userDefineTypeName | streamTypeName) LEFT_PARENTHESIS invocationArgList? RIGHT_PARENTHESIS
    ;

serviceConstructorExpr
    :   annotationAttachment* SERVICE serviceBody
    ;

trapExpr
    :   TRAP expression
    ;

shiftExpression
    :   LT shiftExprPredicate LT
    |   GT shiftExprPredicate GT
    |   GT shiftExprPredicate GT shiftExprPredicate GT
    ;

shiftExprPredicate : {_input.get(_input.index() -1).getType() != WS}? ;

limitClause
    :   LIMIT expression
    ;

onConflictClause
    :   ON CONFLICT expression
    ;

selectClause
    :   SELECT expression
    ;

onClause
    :   ON expression
    ;

whereClause
    :   WHERE expression
    ;

letClause
    :   LET letVarDecl (COMMA letVarDecl)*
    ;

joinClause
    :   (JOIN (typeName | VAR) bindingPattern | OUTER JOIN VAR bindingPattern) IN expression
    ;

fromClause
    :   FROM (typeName | VAR) bindingPattern IN expression
    ;

doClause
    :   DO LEFT_BRACE statement* RIGHT_BRACE
    ;

queryPipeline
    :   fromClause ((fromClause | letClause | whereClause)* | (joinClause onClause)?)
    ;

queryConstructType
    :   TYPE_TABLE tableKeySpecifier | TYPE_STREAM
    ;

queryExpr
    :   queryConstructType? queryPipeline selectClause onConflictClause? limitClause?
    ;

queryAction
    :   queryPipeline doClause limitClause?
    ;

//reusable productions

nameReference
    :   (Identifier COLON)? Identifier
    ;

functionNameReference
    :   (Identifier COLON)? anyIdentifierName
    ;

returnParameter
    :   RETURNS annotationAttachment* typeName
    ;

parameterTypeNameList
    :   parameterTypeName (COMMA parameterTypeName)* (COMMA restParameterTypeName)?
    |   restParameterTypeName
    ;

parameterTypeName
    :   typeName
    ;

parameterList
    :   parameter (COMMA parameter)* (COMMA restParameter)?
    |   restParameter
    ;

parameter
    :   annotationAttachment* PUBLIC? typeName Identifier
    ;

defaultableParameter
    :   parameter ASSIGN expression
    ;

restParameter
    :   annotationAttachment* typeName ELLIPSIS Identifier
    ;

restParameterTypeName
    : typeName restDescriptorPredicate ELLIPSIS
    ;

formalParameterList
    :   (parameter | defaultableParameter) (COMMA (parameter | defaultableParameter))* (COMMA restParameter)?
    |   restParameter
    ;

simpleLiteral
    :   (ADD | SUB)? integerLiteral
    |   (ADD | SUB)? floatingPointLiteral
    |   QuotedStringLiteral
    |   BooleanLiteral
    |   nilLiteral
    |   blobLiteral
    |   NullLiteral
    ;

floatingPointLiteral
    :   DecimalFloatingPointNumber
    |   HexadecimalFloatingPointLiteral
    ;

// §3.10.1 Integer Literals
integerLiteral
    :   DecimalIntegerLiteral
    |   HexIntegerLiteral
    ;

nilLiteral
    :   LEFT_PARENTHESIS RIGHT_PARENTHESIS
    ;

blobLiteral
    :   Base16BlobLiteral
    |   Base64BlobLiteral
    ;

namedArgs
    :   Identifier ASSIGN expression
    ;

restArgs
    :   ELLIPSIS expression
    ;

// XML parsing

xmlLiteral
    :   XMLLiteralStart xmlItem XMLLiteralEnd
    ;

xmlItem
    :   element
    |   procIns
    |   comment
    |   text
    |   CDATA
    ;

content
    :   text? ((element | CDATA | procIns | comment) text?)*
    ;

comment
    :   XML_COMMENT_START (XMLCommentTemplateText expression RIGHT_BRACE)* XMLCommentText* XML_COMMENT_END
    ;

element
    :   startTag content closeTag
    |   emptyTag
    ;

startTag
    :   XML_TAG_OPEN xmlQualifiedName attribute* XML_TAG_CLOSE
    ;

closeTag
    :   XML_TAG_OPEN_SLASH xmlQualifiedName XML_TAG_CLOSE
    ;

emptyTag
    :   XML_TAG_OPEN xmlQualifiedName attribute* XML_TAG_SLASH_CLOSE
    ;

procIns
    :   XML_TAG_SPECIAL_OPEN (XMLPITemplateText expression RIGHT_BRACE)* XMLPIText
    ;

attribute
    :   xmlQualifiedName EQUALS xmlQuotedString;

text
    :   (XMLTemplateText expression RIGHT_BRACE)+ XMLText?
    |   XMLText
    ;

xmlQuotedString
    :   xmlSingleQuotedString
    |   xmlDoubleQuotedString
    ;

xmlSingleQuotedString
    :   SINGLE_QUOTE (XMLSingleQuotedTemplateString expression RIGHT_BRACE)* XMLSingleQuotedString? SINGLE_QUOTE_END
    ;

xmlDoubleQuotedString
    :   DOUBLE_QUOTE (XMLDoubleQuotedTemplateString expression RIGHT_BRACE)* XMLDoubleQuotedString? DOUBLE_QUOTE_END
    ;

xmlQualifiedName
    :   (XMLQName QNAME_SEPARATOR)? XMLQName
    ;

stringTemplateLiteral
    :   StringTemplateLiteralStart stringTemplateContent? StringTemplateLiteralEnd
    ;

stringTemplateContent
    :   (StringTemplateExpressionStart expression RIGHT_BRACE)+ StringTemplateText?
    |   StringTemplateText
    ;


anyIdentifierName
    :   Identifier
    |   reservedWord
    ;

reservedWord
    :   FOREACH
    |   TYPE_MAP
    |   START
    |   CONTINUE
    |   OBJECT_INIT
    |   TYPE_ERROR
    ;

// Markdown documentation
documentationString
    :   documentationLine+ parameterDocumentationLine* returnParameterDocumentationLine? deprecatedParametersDocumentationLine? deprecatedAnnotationDocumentationLine?
    ;

documentationLine
    :   DocumentationLineStart documentationContent
    ;

parameterDocumentationLine
    :   parameterDocumentation parameterDescriptionLine*
    ;

returnParameterDocumentationLine
    :   returnParameterDocumentation returnParameterDescriptionLine*
    ;

deprecatedAnnotationDocumentationLine
    :   deprecatedAnnotationDocumentation deprecateAnnotationDescriptionLine*
    ;

deprecatedParametersDocumentationLine
    :   deprecatedParametersDocumentation parameterDocumentationLine+
    ;

documentationContent
    :   documentationText?
    ;

parameterDescriptionLine
    :   DocumentationLineStart documentationText?
    ;

returnParameterDescriptionLine
    :   DocumentationLineStart documentationText?
    ;

deprecateAnnotationDescriptionLine
    :   DocumentationLineStart documentationText?
    ;

documentationText
    :   (documentationReference | documentationTextContent | referenceType | singleBacktickedBlock | doubleBacktickedBlock | tripleBacktickedBlock)+
    ;

documentationReference
    :   referenceType singleBacktickedContent SingleBacktickEnd
    ;

referenceType
    :   DOCTYPE
    |   DOCSERVICE
    |   DOCVARIABLE
    |   DOCVAR
    |   DOCANNOTATION
    |   DOCMODULE
    |   DOCFUNCTION
    |   DOCPARAMETER
    |   DOCCONST
    ;

parameterDocumentation
    :   ParameterDocumentationStart docParameterName DescriptionSeparator documentationText?
    ;

returnParameterDocumentation
    :   ReturnParameterDocumentationStart documentationText?
    ;

deprecatedAnnotationDocumentation
    :   DeprecatedDocumentation
    ;

deprecatedParametersDocumentation
    :   DeprecatedParametersDocumentation
    ;

docParameterName
    :   ParameterName
    ;

singleBacktickedBlock
    :   SingleBacktickStart singleBacktickedContent SingleBacktickEnd
    ;

singleBacktickedContent
    :   SingleBacktickContent
    ;

doubleBacktickedBlock
    :   DoubleBacktickStart doubleBacktickedContent DoubleBacktickEnd
    ;

doubleBacktickedContent
    :   DoubleBacktickContent
    ;

tripleBacktickedBlock
    :   TripleBacktickStart tripleBacktickedContent TripleBacktickEnd
    ;

tripleBacktickedContent
    :   TripleBacktickContent
    ;

documentationTextContent
    :   DocumentationText
    |   DocumentationEscapedCharacters
    ;

// Rules for parsing the content in the backticked block for documentation validation.
documentationFullyqualifiedIdentifier
    :   documentationIdentifierQualifier? documentationIdentifierTypename? documentationIdentifier braket?
    ;

documentationFullyqualifiedFunctionIdentifier
    :   documentationIdentifierQualifier? documentationIdentifierTypename? documentationIdentifier braket
    ;

documentationIdentifierQualifier
    :   Identifier COLON
    ;

documentationIdentifierTypename
    :   Identifier DOT
    ;

documentationIdentifier
    :   Identifier
    |   TYPE_INT
    |   TYPE_BYTE
    |   TYPE_FLOAT
    |   TYPE_DECIMAL
    |   TYPE_BOOL
    |   TYPE_STRING
    |   TYPE_ERROR
    |   TYPE_MAP
    |   TYPE_JSON
    |   TYPE_XML
    |   TYPE_STREAM
    |   TYPE_TABLE
    |   TYPE_ANY
    |   TYPE_DESC
    |   TYPE_FUTURE
    |   TYPE_ANYDATA
    |   TYPE_HANDLE
    |   TYPE_READONLY
    ;

braket
    :   LEFT_PARENTHESIS RIGHT_PARENTHESIS
    ;
