/**
* This is a auto generated file, DO NOT modify this manually.
* Use npm run gen-default-nodes command to generate this file.
*/

export default {
    "createHTTPEndpointDef": {
        "position": {
            "startColumn": 17,
            "startLine": 2,
            "endColumn": 18,
            "endLine": 4
        },
        "symbolType": [
            "other"
        ],
        "configurationExpression": {
            "position": {
                "startColumn": 57,
                "startLine": 2,
                "endColumn": 17,
                "endLine": 4
            },
            "keyValuePairs": [
                {
                    "value": {
                        "position": {
                            "startColumn": 26,
                            "startLine": 3,
                            "endColumn": 26,
                            "endLine": 3
                        },
                        "value": "9090",
                        "kind": "Literal"
                    },
                    "key": {
                        "position": {
                            "startColumn": 21,
                            "startLine": 3,
                            "endColumn": 21,
                            "endLine": 3
                        },
                        "variableName": {
                            "literal": false,
                            "value": "port",
                            "kind": "Identifier"
                        },
                        "packageAlias": {
                            "literal": false,
                            "value": "",
                            "kind": "Identifier"
                        },
                        "kind": "SimpleVariableRef"
                    },
                    "kind": "RecordLiteralKeyValue"
                }
            ],
            "kind": "RecordLiteralExpr"
        },
        "name": {
            "literal": false,
            "value": "serviceEp",
            "kind": "Identifier"
        },
        "annotationAttachments": [],
        "kind": "Endpoint",
        "endPointType": {
            "position": {
                "startColumn": 26,
                "startLine": 2,
                "endColumn": 31,
                "endLine": 2
            },
            "symbolType": [
                "other"
            ],
            "packageAlias": {
                "literal": false,
                "value": "http",
                "kind": "Identifier"
            },
            "grouped": false,
            "typeName": {
                "literal": false,
                "value": "ServiceEndpoint",
                "kind": "Identifier"
            },
            "nullable": false,
            "kind": "UserDefinedType"
        },
        "public": false,
        "native": false,
        "const": false,
        "attached": false,
        "lambda": false,
        "parallel": false,
        "connector": false,
        "deprecated": false
    },
    "createForkJoin": {
        "position": {
            "startColumn": 13,
            "startLine": 3,
            "endColumn": 13,
            "endLine": 12
        },
        "timeOutExpression": {
            "position": {
                "startColumn": 23,
                "startLine": 10,
                "endColumn": 23,
                "endLine": 10
            },
            "value": "100",
            "kind": "Literal"
        },
        "joinType": "all",
        "timeOutVariable": {
            "position": {
                "startColumn": 28,
                "startLine": 10,
                "endColumn": 28,
                "endLine": 10
            },
            "safeAssignment": false,
            "typeNode": {
                "position": {
                    "startColumn": 28,
                    "startLine": 10,
                    "endColumn": 28,
                    "endLine": 10
                },
                "grouped": false,
                "typeKind": "map",
                "nullable": false,
                "kind": "BuiltInRefType"
            },
            "deprecatedAttachments": [],
            "annotationAttachments": [],
            "name": {
                "literal": false,
                "value": "results1",
                "kind": "Identifier"
            },
            "documentationAttachments": [],
            "kind": "Variable",
            "public": false,
            "native": false,
            "const": false,
            "attached": false,
            "lambda": false,
            "parallel": false,
            "connector": false,
            "deprecated": false
        },
        "workers": [
            {
                "position": {
                    "startColumn": 17,
                    "startLine": 4,
                    "endColumn": 17,
                    "endLine": 5
                },
                "workers": [],
                "name": {
                    "literal": false,
                    "value": "worker1",
                    "kind": "Identifier"
                },
                "annotationAttachments": [],
                "documentationAttachments": [],
                "defaultableParameters": [],
                "deprecatedAttachments": [],
                "body": {
                    "statements": [],
                    "kind": "Block"
                },
                "endpointNodes": [],
                "returnParameters": [],
                "parameters": [],
                "kind": "Worker",
                "public": false,
                "native": false,
                "const": false,
                "attached": false,
                "lambda": false,
                "parallel": false,
                "connector": false,
                "deprecated": false
            },
            {
                "position": {
                    "startColumn": 17,
                    "startLine": 6,
                    "endColumn": 17,
                    "endLine": 7
                },
                "workers": [],
                "name": {
                    "literal": false,
                    "value": "worker2",
                    "kind": "Identifier"
                },
                "annotationAttachments": [],
                "documentationAttachments": [],
                "defaultableParameters": [],
                "deprecatedAttachments": [],
                "body": {
                    "statements": [],
                    "kind": "Block"
                },
                "endpointNodes": [],
                "returnParameters": [],
                "parameters": [],
                "kind": "Worker",
                "public": false,
                "native": false,
                "const": false,
                "attached": false,
                "lambda": false,
                "parallel": false,
                "connector": false,
                "deprecated": false
            }
        ],
        "joinedWorkerIdentifiers": [],
        "joinResultVar": {
            "position": {
                "startColumn": 25,
                "startLine": 8,
                "endColumn": 25,
                "endLine": 8
            },
            "safeAssignment": false,
            "typeNode": {
                "position": {
                    "startColumn": 25,
                    "startLine": 8,
                    "endColumn": 25,
                    "endLine": 8
                },
                "grouped": false,
                "typeKind": "map",
                "nullable": false,
                "kind": "BuiltInRefType"
            },
            "deprecatedAttachments": [],
            "annotationAttachments": [],
            "name": {
                "literal": false,
                "value": "results",
                "kind": "Identifier"
            },
            "documentationAttachments": [],
            "kind": "Variable",
            "public": false,
            "native": false,
            "const": false,
            "attached": false,
            "lambda": false,
            "parallel": false,
            "connector": false,
            "deprecated": false
        },
        "joinBody": {
            "statements": [],
            "kind": "Block"
        },
        "timeoutBody": {
            "statements": [],
            "kind": "Block"
        },
        "kind": "ForkJoin",
        "joinCount": -1
    },
    "createWhile": {
        "position": {
            "startColumn": 13,
            "startLine": 3,
            "endColumn": 13,
            "endLine": 5
        },
        "condition": {
            "position": {
                "startColumn": 19,
                "startLine": 3,
                "endColumn": 19,
                "endLine": 3
            },
            "value": "true",
            "kind": "Literal"
        },
        "kind": "While",
        "body": {
            "position": {
                "startColumn": 13,
                "startLine": 3,
                "endColumn": 13,
                "endLine": 5
            },
            "statements": [],
            "kind": "Block"
        }
    },
    "createWorkerFragment": {
        "position": {
            "startColumn": 13,
            "startLine": 3,
            "endColumn": 13,
            "endLine": 4
        },
        "workers": [],
        "name": {
            "literal": false,
            "value": "worker1",
            "kind": "Identifier"
        },
        "annotationAttachments": [],
        "documentationAttachments": [],
        "defaultableParameters": [],
        "deprecatedAttachments": [],
        "body": {
            "statements": [],
            "kind": "Block"
        },
        "endpointNodes": [],
        "returnParameters": [],
        "parameters": [],
        "kind": "Worker",
        "public": false,
        "native": false,
        "const": false,
        "attached": false,
        "lambda": false,
        "parallel": false,
        "connector": false,
        "deprecated": false
    },
    "createMainFunction": {
        "position": {
            "startColumn": 13,
            "startLine": 2,
            "endColumn": 13,
            "endLine": 4
        },
        "interfaceFunction": false,
        "workers": [],
        "name": {
            "literal": false,
            "value": "main",
            "kind": "Identifier"
        },
        "annotationAttachments": [],
        "documentationAttachments": [],
        "defaultableParameters": [],
        "deprecatedAttachments": [],
        "body": {
            "statements": [],
            "kind": "Block"
        },
        "endpointNodes": [],
        "returnParameters": [],
        "parameters": [
            {
                "position": {
                    "startColumn": 27,
                    "startLine": 2,
                    "endColumn": 36,
                    "endLine": 2
                },
                "symbolType": [
                    "[]"
                ],
                "safeAssignment": false,
                "typeNode": {
                    "position": {
                        "startColumn": 27,
                        "startLine": 2,
                        "endColumn": 34,
                        "endLine": 2
                    },
                    "symbolType": [
                        "[]"
                    ],
                    "dimensions": 1,
                    "elementType": {
                        "position": {
                            "startColumn": 27,
                            "startLine": 2,
                            "endColumn": 27,
                            "endLine": 2
                        },
                        "symbolType": [
                            "string"
                        ],
                        "typeKind": "string",
                        "grouped": false,
                        "nullable": false,
                        "kind": "ValueType"
                    },
                    "grouped": false,
                    "nullable": false,
                    "kind": "ArrayType"
                },
                "deprecatedAttachments": [],
                "annotationAttachments": [],
                "name": {
                    "literal": false,
                    "value": "args",
                    "kind": "Identifier"
                },
                "documentationAttachments": [],
                "kind": "Variable",
                "public": false,
                "native": false,
                "const": false,
                "attached": false,
                "lambda": false,
                "parallel": false,
                "connector": false,
                "deprecated": false
            }
        ],
        "kind": "Function",
        "public": false,
        "native": false,
        "const": false,
        "attached": false,
        "lambda": false,
        "parallel": false,
        "connector": false,
        "deprecated": false
    },
    "createAbort": {
        "position": {
            "startColumn": 13,
            "startLine": 3,
            "endColumn": 18,
            "endLine": 3
        },
        "kind": "Abort"
    },
    "createInvocation": {
        "position": {
            "startColumn": 13,
            "startLine": 3,
            "endColumn": 33,
            "endLine": 3
        },
        "expression": {
            "position": {
                "startColumn": 13,
                "startLine": 3,
                "endColumn": 32,
                "endLine": 3
            },
            "symbolType": [],
            "packageAlias": {
                "literal": false,
                "value": "",
                "kind": "Identifier"
            },
            "async": false,
            "name": {
                "literal": false,
                "value": "invokeFunction",
                "kind": "Identifier"
            },
            "iterableOperation": false,
            "argumentExpressions": [
                {
                    "position": {
                        "startColumn": 28,
                        "startLine": 3,
                        "endColumn": 28,
                        "endLine": 3
                    },
                    "variableName": {
                        "literal": false,
                        "value": "arg1",
                        "kind": "Identifier"
                    },
                    "packageAlias": {
                        "literal": false,
                        "value": "",
                        "kind": "Identifier"
                    },
                    "kind": "SimpleVariableRef"
                }
            ],
            "actionInvocation": false,
            "kind": "Invocation"
        },
        "kind": "ExpressionStatement"
    },
    "createTransformer": {
        "position": {
            "startColumn": 13,
            "startLine": 2,
            "endColumn": 13,
            "endLine": 4
        },
        "workers": [],
        "name": {
            "literal": false,
            "value": "newTransformer",
            "kind": "Identifier"
        },
        "annotationAttachments": [],
        "documentationAttachments": [],
        "defaultableParameters": [],
        "deprecatedAttachments": [],
        "body": {
            "statements": [],
            "kind": "Block"
        },
        "endpointNodes": [],
        "returnParameters": [
            {
                "position": {
                    "startColumn": 36,
                    "startLine": 2,
                    "endColumn": 43,
                    "endLine": 2
                },
                "symbolType": [
                    "other"
                ],
                "safeAssignment": false,
                "typeNode": {
                    "position": {
                        "startColumn": 36,
                        "startLine": 2,
                        "endColumn": 36,
                        "endLine": 2
                    },
                    "symbolType": [
                        "other"
                    ],
                    "packageAlias": {
                        "literal": false,
                        "value": "",
                        "kind": "Identifier"
                    },
                    "grouped": false,
                    "typeName": {
                        "literal": false,
                        "value": "Target",
                        "kind": "Identifier"
                    },
                    "nullable": false,
                    "kind": "UserDefinedType"
                },
                "deprecatedAttachments": [],
                "annotationAttachments": [],
                "name": {
                    "literal": false,
                    "value": "b",
                    "kind": "Identifier"
                },
                "documentationAttachments": [],
                "kind": "Variable",
                "public": false,
                "native": false,
                "const": false,
                "attached": false,
                "lambda": false,
                "parallel": false,
                "connector": false,
                "deprecated": false
            }
        ],
        "parameters": [],
        "source": {
            "position": {
                "startColumn": 26,
                "startLine": 2,
                "endColumn": 33,
                "endLine": 2
            },
            "symbolType": [
                "other"
            ],
            "safeAssignment": false,
            "typeNode": {
                "position": {
                    "startColumn": 26,
                    "startLine": 2,
                    "endColumn": 26,
                    "endLine": 2
                },
                "symbolType": [
                    "other"
                ],
                "packageAlias": {
                    "literal": false,
                    "value": "",
                    "kind": "Identifier"
                },
                "grouped": false,
                "typeName": {
                    "literal": false,
                    "value": "Source",
                    "kind": "Identifier"
                },
                "nullable": false,
                "kind": "UserDefinedType"
            },
            "deprecatedAttachments": [],
            "annotationAttachments": [],
            "name": {
                "literal": false,
                "value": "a",
                "kind": "Identifier"
            },
            "documentationAttachments": [],
            "kind": "Variable",
            "public": false,
            "native": false,
            "const": false,
            "attached": false,
            "lambda": false,
            "parallel": false,
            "connector": false,
            "deprecated": false
        },
        "kind": "Transformer",
        "public": false,
        "native": false,
        "const": false,
        "attached": false,
        "lambda": false,
        "parallel": false,
        "connector": false,
        "deprecated": false
    },
    "createFTPServiceDef": {
        "position": {
            "startColumn": 13,
            "startLine": 2,
            "endColumn": 13,
            "endLine": 6
        },
        "annotationAttachments": [],
        "serviceTypeStruct": {
            "position": {
                "startColumn": 13,
                "startLine": 2,
                "endColumn": 13,
                "endLine": 6
            },
            "packageAlias": {
                "literal": false,
                "value": "",
                "kind": "Identifier"
            },
            "grouped": false,
            "typeName": {
                "literal": false,
                "value": "ftp",
                "kind": "Identifier"
            },
            "nullable": false,
            "kind": "UserDefinedType"
        },
        "initFunction": {
            "position": {
                "startColumn": 13,
                "startLine": 2,
                "endColumn": 13,
                "endLine": 6
            },
            "interfaceFunction": false,
            "workers": [],
            "name": {
                "literal": false,
                "value": "service1.<init>",
                "kind": "Identifier"
            },
            "annotationAttachments": [],
            "documentationAttachments": [],
            "defaultableParameters": [],
            "deprecatedAttachments": [],
            "body": {
                "position": {
                    "startColumn": 13,
                    "startLine": 2,
                    "endColumn": 13,
                    "endLine": 6
                },
                "statements": [
                    {
                        "position": {
                            "startColumn": 13,
                            "startLine": 2,
                            "endColumn": 13,
                            "endLine": 6
                        },
                        "expressions": [],
                        "kind": "Return"
                    }
                ],
                "kind": "Block"
            },
            "endpointNodes": [],
            "returnParameters": [],
            "parameters": [],
            "kind": "Function",
            "public": true,
            "native": false,
            "const": false,
            "attached": false,
            "lambda": false,
            "parallel": false,
            "connector": false,
            "deprecated": false
        },
        "endpointNodes": [],
        "documentationAttachments": [],
        "boundEndpoints": [],
        "resources": [
            {
                "position": {
                    "startColumn": 17,
                    "startLine": 3,
                    "endColumn": 17,
                    "endLine": 5
                },
                "workers": [],
                "name": {
                    "literal": false,
                    "value": "echo1",
                    "kind": "Identifier"
                },
                "annotationAttachments": [],
                "documentationAttachments": [],
                "defaultableParameters": [],
                "deprecatedAttachments": [],
                "body": {
                    "statements": [],
                    "kind": "Block"
                },
                "endpointNodes": [],
                "returnParameters": [],
                "parameters": [
                    {
                        "position": {
                            "startColumn": 24,
                            "startLine": 3,
                            "endColumn": 43,
                            "endLine": 3
                        },
                        "symbolType": [
                            "other"
                        ],
                        "safeAssignment": false,
                        "typeNode": {
                            "position": {
                                "startColumn": 24,
                                "startLine": 3,
                                "endColumn": 28,
                                "endLine": 3
                            },
                            "symbolType": [
                                "other"
                            ],
                            "packageAlias": {
                                "literal": false,
                                "value": "ftp",
                                "kind": "Identifier"
                            },
                            "grouped": false,
                            "typeName": {
                                "literal": false,
                                "value": "FTPServerEvent",
                                "kind": "Identifier"
                            },
                            "nullable": false,
                            "kind": "UserDefinedType"
                        },
                        "deprecatedAttachments": [],
                        "annotationAttachments": [],
                        "name": {
                            "literal": false,
                            "value": "m",
                            "kind": "Identifier"
                        },
                        "documentationAttachments": [],
                        "kind": "Variable",
                        "public": false,
                        "native": false,
                        "const": false,
                        "attached": false,
                        "lambda": false,
                        "parallel": false,
                        "connector": false,
                        "deprecated": false
                    }
                ],
                "kind": "Resource",
                "public": true,
                "native": false,
                "const": false,
                "attached": false,
                "lambda": false,
                "parallel": false,
                "connector": false,
                "deprecated": false
            }
        ],
        "deprecatedAttachments": [],
        "variables": [],
        "name": {
            "literal": false,
            "value": "service1",
            "kind": "Identifier"
        },
        "kind": "Service"
    },
    "createTransaction": {
        "error": null
    },
    "createReturn": {
        "position": {
            "startColumn": 13,
            "startLine": 3,
            "endColumn": 21,
            "endLine": 3
        },
        "expressions": [
            {
                "position": {
                    "startColumn": 20,
                    "startLine": 3,
                    "endColumn": 20,
                    "endLine": 3
                },
                "variableName": {
                    "literal": false,
                    "value": "m",
                    "kind": "Identifier"
                },
                "packageAlias": {
                    "literal": false,
                    "value": "",
                    "kind": "Identifier"
                },
                "kind": "SimpleVariableRef"
            }
        ],
        "kind": "Return"
    },
    "createIfElse": {
        "position": {
            "startColumn": 13,
            "startLine": 3,
            "endColumn": 13,
            "endLine": 5
        },
        "body": {
            "statements": [],
            "kind": "Block"
        },
        "condition": {
            "position": {
                "startColumn": 17,
                "startLine": 3,
                "endColumn": 17,
                "endLine": 3
            },
            "value": "true",
            "kind": "Literal"
        },
        "elseStatement": {
            "position": {
                "startColumn": 15,
                "startLine": 5,
                "endColumn": 13,
                "endLine": 7
            },
            "body": {
                "statements": [],
                "kind": "Block"
            },
            "condition": {
                "position": {
                    "startColumn": 24,
                    "startLine": 5,
                    "endColumn": 24,
                    "endLine": 5
                },
                "value": "true",
                "kind": "Literal"
            },
            "elseStatement": {
                "position": {
                    "startColumn": 15,
                    "startLine": 7,
                    "endColumn": 13,
                    "endLine": 9
                },
                "statements": [],
                "kind": "Block"
            },
            "kind": "If"
        },
        "kind": "If"
    },
    "createBindStmt": {
        "error": "Index: 0, Size: 0"
    },
    "createStruct": {
        "position": {
            "startColumn": 13,
            "startLine": 2,
            "endColumn": 13,
            "endLine": 4
        },
        "deprecatedAttachments": [],
        "annotationAttachments": [],
        "name": {
            "literal": false,
            "value": "struct1",
            "kind": "Identifier"
        },
        "documentationAttachments": [],
        "kind": "Struct",
        "fields": [],
        "public": false,
        "native": false,
        "const": false,
        "attached": false,
        "lambda": false,
        "parallel": false,
        "connector": false,
        "deprecated": false
    },
    "createFSResource": {
        "position": {
            "startColumn": 13,
            "startLine": 3,
            "endColumn": 13,
            "endLine": 5
        },
        "workers": [],
        "name": {
            "literal": false,
            "value": "echo1",
            "kind": "Identifier"
        },
        "annotationAttachments": [],
        "documentationAttachments": [],
        "defaultableParameters": [],
        "deprecatedAttachments": [],
        "body": {
            "statements": [],
            "kind": "Block"
        },
        "endpointNodes": [],
        "returnParameters": [],
        "parameters": [
            {
                "position": {
                    "startColumn": 20,
                    "startLine": 3,
                    "endColumn": 39,
                    "endLine": 3
                },
                "symbolType": [
                    "other"
                ],
                "safeAssignment": false,
                "typeNode": {
                    "position": {
                        "startColumn": 20,
                        "startLine": 3,
                        "endColumn": 23,
                        "endLine": 3
                    },
                    "symbolType": [
                        "other"
                    ],
                    "packageAlias": {
                        "literal": false,
                        "value": "fs",
                        "kind": "Identifier"
                    },
                    "grouped": false,
                    "typeName": {
                        "literal": false,
                        "value": "FileSystemEvent",
                        "kind": "Identifier"
                    },
                    "nullable": false,
                    "kind": "UserDefinedType"
                },
                "deprecatedAttachments": [],
                "annotationAttachments": [],
                "name": {
                    "literal": false,
                    "value": "m",
                    "kind": "Identifier"
                },
                "documentationAttachments": [],
                "kind": "Variable",
                "public": false,
                "native": false,
                "const": false,
                "attached": false,
                "lambda": false,
                "parallel": false,
                "connector": false,
                "deprecated": false
            }
        ],
        "kind": "Resource",
        "public": true,
        "native": false,
        "const": false,
        "attached": false,
        "lambda": false,
        "parallel": false,
        "connector": false,
        "deprecated": false
    },
    "createFSServiceDef": {
        "position": {
            "startColumn": 13,
            "startLine": 2,
            "endColumn": 13,
            "endLine": 6
        },
        "annotationAttachments": [],
        "serviceTypeStruct": {
            "position": {
                "startColumn": 13,
                "startLine": 2,
                "endColumn": 13,
                "endLine": 6
            },
            "packageAlias": {
                "literal": false,
                "value": "",
                "kind": "Identifier"
            },
            "grouped": false,
            "typeName": {
                "literal": false,
                "value": "fs",
                "kind": "Identifier"
            },
            "nullable": false,
            "kind": "UserDefinedType"
        },
        "initFunction": {
            "position": {
                "startColumn": 13,
                "startLine": 2,
                "endColumn": 13,
                "endLine": 6
            },
            "interfaceFunction": false,
            "workers": [],
            "name": {
                "literal": false,
                "value": "service1.<init>",
                "kind": "Identifier"
            },
            "annotationAttachments": [],
            "documentationAttachments": [],
            "defaultableParameters": [],
            "deprecatedAttachments": [],
            "body": {
                "position": {
                    "startColumn": 13,
                    "startLine": 2,
                    "endColumn": 13,
                    "endLine": 6
                },
                "statements": [
                    {
                        "position": {
                            "startColumn": 13,
                            "startLine": 2,
                            "endColumn": 13,
                            "endLine": 6
                        },
                        "expressions": [],
                        "kind": "Return"
                    }
                ],
                "kind": "Block"
            },
            "endpointNodes": [],
            "returnParameters": [],
            "parameters": [],
            "kind": "Function",
            "public": true,
            "native": false,
            "const": false,
            "attached": false,
            "lambda": false,
            "parallel": false,
            "connector": false,
            "deprecated": false
        },
        "endpointNodes": [],
        "documentationAttachments": [],
        "boundEndpoints": [],
        "resources": [
            {
                "position": {
                    "startColumn": 17,
                    "startLine": 3,
                    "endColumn": 17,
                    "endLine": 5
                },
                "workers": [],
                "name": {
                    "literal": false,
                    "value": "echo1",
                    "kind": "Identifier"
                },
                "annotationAttachments": [],
                "documentationAttachments": [],
                "defaultableParameters": [],
                "deprecatedAttachments": [],
                "body": {
                    "statements": [],
                    "kind": "Block"
                },
                "endpointNodes": [],
                "returnParameters": [],
                "parameters": [
                    {
                        "position": {
                            "startColumn": 24,
                            "startLine": 3,
                            "endColumn": 43,
                            "endLine": 3
                        },
                        "symbolType": [
                            "other"
                        ],
                        "safeAssignment": false,
                        "typeNode": {
                            "position": {
                                "startColumn": 24,
                                "startLine": 3,
                                "endColumn": 27,
                                "endLine": 3
                            },
                            "symbolType": [
                                "other"
                            ],
                            "packageAlias": {
                                "literal": false,
                                "value": "fs",
                                "kind": "Identifier"
                            },
                            "grouped": false,
                            "typeName": {
                                "literal": false,
                                "value": "FileSystemEvent",
                                "kind": "Identifier"
                            },
                            "nullable": false,
                            "kind": "UserDefinedType"
                        },
                        "deprecatedAttachments": [],
                        "annotationAttachments": [],
                        "name": {
                            "literal": false,
                            "value": "m",
                            "kind": "Identifier"
                        },
                        "documentationAttachments": [],
                        "kind": "Variable",
                        "public": false,
                        "native": false,
                        "const": false,
                        "attached": false,
                        "lambda": false,
                        "parallel": false,
                        "connector": false,
                        "deprecated": false
                    }
                ],
                "kind": "Resource",
                "public": true,
                "native": false,
                "const": false,
                "attached": false,
                "lambda": false,
                "parallel": false,
                "connector": false,
                "deprecated": false
            }
        ],
        "deprecatedAttachments": [],
        "variables": [],
        "name": {
            "literal": false,
            "value": "service1",
            "kind": "Identifier"
        },
        "kind": "Service"
    },
    "createWorkerInvocation": {
        "position": {
            "startColumn": 13,
            "startLine": 3,
            "endColumn": 26,
            "endLine": 3
        },
        "workerName": {
            "literal": false,
            "value": "worker1",
            "kind": "Identifier"
        },
        "expressions": [
            {
                "position": {
                    "startColumn": 13,
                    "startLine": 3,
                    "endColumn": 13,
                    "endLine": 3
                },
                "variableName": {
                    "literal": false,
                    "value": "m",
                    "kind": "Identifier"
                },
                "packageAlias": {
                    "literal": false,
                    "value": "",
                    "kind": "Identifier"
                },
                "kind": "SimpleVariableRef"
            }
        ],
        "forkJoinedSend": false,
        "kind": "WorkerSend"
    },
    "createTry": {
        "position": {
            "startColumn": 13,
            "startLine": 3,
            "endColumn": 13,
            "endLine": 9
        },
        "finallyBody": {
            "position": {
                "startColumn": 15,
                "startLine": 7,
                "endColumn": 13,
                "endLine": 9
            },
            "statements": [],
            "kind": "Block"
        },
        "body": {
            "position": {
                "startColumn": 15,
                "startLine": 5,
                "endColumn": -1,
                "endLine": -1
            },
            "statements": [],
            "kind": "Block"
        },
        "catchBlocks": [
            {
                "position": {
                    "startColumn": 15,
                    "startLine": 5,
                    "endColumn": 13,
                    "endLine": 7
                },
                "body": {
                    "statements": [],
                    "kind": "Block"
                },
                "parameter": {
                    "position": {
                        "startColumn": 22,
                        "startLine": 5,
                        "endColumn": 22,
                        "endLine": 5
                    },
                    "safeAssignment": false,
                    "typeNode": {
                        "position": {
                            "startColumn": 22,
                            "startLine": 5,
                            "endColumn": 22,
                            "endLine": 5
                        },
                        "packageAlias": {
                            "literal": false,
                            "value": "",
                            "kind": "Identifier"
                        },
                        "grouped": false,
                        "typeName": {
                            "literal": false,
                            "value": "error",
                            "kind": "Identifier"
                        },
                        "nullable": false,
                        "kind": "UserDefinedType"
                    },
                    "deprecatedAttachments": [],
                    "annotationAttachments": [],
                    "name": {
                        "literal": false,
                        "value": "err",
                        "kind": "Identifier"
                    },
                    "documentationAttachments": [],
                    "kind": "Variable",
                    "public": false,
                    "native": false,
                    "const": false,
                    "attached": false,
                    "lambda": false,
                    "parallel": false,
                    "connector": false,
                    "deprecated": false
                },
                "kind": "Catch"
            }
        ],
        "kind": "Try"
    },
    "createVarDefStmt": {
        "position": {
            "startColumn": 1,
            "startLine": 2,
            "endColumn": 10,
            "endLine": 2
        },
        "variable": {
            "position": {
                "startColumn": 1,
                "startLine": 2,
                "endColumn": 10,
                "endLine": 2
            },
            "safeAssignment": false,
            "typeNode": {
                "position": {
                    "startColumn": 1,
                    "startLine": 2,
                    "endColumn": 1,
                    "endLine": 2
                },
                "typeKind": "int",
                "grouped": false,
                "nullable": false,
                "kind": "ValueType"
            },
            "deprecatedAttachments": [],
            "annotationAttachments": [],
            "name": {
                "literal": false,
                "value": "a",
                "kind": "Identifier"
            },
            "initialExpression": {
                "position": {
                    "startColumn": 9,
                    "startLine": 2,
                    "endColumn": 9,
                    "endLine": 2
                },
                "value": "1",
                "kind": "Literal"
            },
            "documentationAttachments": [],
            "kind": "Variable",
            "public": false,
            "native": false,
            "const": false,
            "attached": false,
            "lambda": false,
            "parallel": false,
            "connector": false,
            "deprecated": false
        },
        "kind": "VariableDef"
    },
    "createDefaultWorkerFragment": {
        "position": {
            "startColumn": 13,
            "startLine": 3,
            "endColumn": 13,
            "endLine": 4
        },
        "workers": [],
        "name": {
            "literal": false,
            "value": "default",
            "kind": "Identifier"
        },
        "annotationAttachments": [],
        "documentationAttachments": [],
        "defaultableParameters": [],
        "deprecatedAttachments": [],
        "body": {
            "statements": [],
            "kind": "Block"
        },
        "endpointNodes": [],
        "returnParameters": [],
        "parameters": [],
        "kind": "Worker",
        "public": false,
        "native": false,
        "const": false,
        "attached": false,
        "lambda": false,
        "parallel": false,
        "connector": false,
        "deprecated": false
    },
    "createWorkerReceive": {
        "position": {
            "startColumn": 13,
            "startLine": 3,
            "endColumn": 26,
            "endLine": 3
        },
        "workerName": {
            "literal": false,
            "value": "worker1",
            "kind": "Identifier"
        },
        "expressions": [
            {
                "position": {
                    "startColumn": 13,
                    "startLine": 3,
                    "endColumn": 13,
                    "endLine": 3
                },
                "variableName": {
                    "literal": false,
                    "value": "m",
                    "kind": "Identifier"
                },
                "packageAlias": {
                    "literal": false,
                    "value": "",
                    "kind": "Identifier"
                },
                "kind": "SimpleVariableRef"
            }
        ],
        "kind": "WorkerReceive"
    },
    "createFTPResource": {
        "position": {
            "startColumn": 13,
            "startLine": 3,
            "endColumn": 13,
            "endLine": 5
        },
        "workers": [],
        "name": {
            "literal": false,
            "value": "echo1",
            "kind": "Identifier"
        },
        "annotationAttachments": [],
        "documentationAttachments": [],
        "defaultableParameters": [],
        "deprecatedAttachments": [],
        "body": {
            "statements": [],
            "kind": "Block"
        },
        "endpointNodes": [],
        "returnParameters": [],
        "parameters": [
            {
                "position": {
                    "startColumn": 20,
                    "startLine": 3,
                    "endColumn": 39,
                    "endLine": 3
                },
                "symbolType": [
                    "other"
                ],
                "safeAssignment": false,
                "typeNode": {
                    "position": {
                        "startColumn": 20,
                        "startLine": 3,
                        "endColumn": 24,
                        "endLine": 3
                    },
                    "symbolType": [
                        "other"
                    ],
                    "packageAlias": {
                        "literal": false,
                        "value": "ftp",
                        "kind": "Identifier"
                    },
                    "grouped": false,
                    "typeName": {
                        "literal": false,
                        "value": "FTPServerEvent",
                        "kind": "Identifier"
                    },
                    "nullable": false,
                    "kind": "UserDefinedType"
                },
                "deprecatedAttachments": [],
                "annotationAttachments": [],
                "name": {
                    "literal": false,
                    "value": "m",
                    "kind": "Identifier"
                },
                "documentationAttachments": [],
                "kind": "Variable",
                "public": false,
                "native": false,
                "const": false,
                "attached": false,
                "lambda": false,
                "parallel": false,
                "connector": false,
                "deprecated": false
            }
        ],
        "kind": "Resource",
        "public": true,
        "native": false,
        "const": false,
        "attached": false,
        "lambda": false,
        "parallel": false,
        "connector": false,
        "deprecated": false
    },
    "createFunction": {
        "position": {
            "startColumn": 13,
            "startLine": 2,
            "endColumn": 13,
            "endLine": 4
        },
        "interfaceFunction": false,
        "workers": [],
        "name": {
            "literal": false,
            "value": "function1",
            "kind": "Identifier"
        },
        "annotationAttachments": [],
        "documentationAttachments": [],
        "defaultableParameters": [],
        "deprecatedAttachments": [],
        "body": {
            "statements": [],
            "kind": "Block"
        },
        "endpointNodes": [],
        "returnParameters": [],
        "parameters": [],
        "kind": "Function",
        "public": false,
        "native": false,
        "const": false,
        "attached": false,
        "lambda": false,
        "parallel": false,
        "connector": false,
        "deprecated": false
    },
    "createIf": {
        "position": {
            "startColumn": 13,
            "startLine": 3,
            "endColumn": 13,
            "endLine": 5
        },
        "body": {
            "statements": [],
            "kind": "Block"
        },
        "condition": {
            "position": {
                "startColumn": 17,
                "startLine": 3,
                "endColumn": 17,
                "endLine": 3
            },
            "value": "true",
            "kind": "Literal"
        },
        "elseStatement": {
            "position": {
                "startColumn": 15,
                "startLine": 5,
                "endColumn": 13,
                "endLine": 7
            },
            "statements": [],
            "kind": "Block"
        },
        "kind": "If"
    },
    "createThrow": {
        "position": {
            "startColumn": 13,
            "startLine": 3,
            "endColumn": 20,
            "endLine": 3
        },
        "kind": "Throw",
        "expressions": {
            "position": {
                "startColumn": 19,
                "startLine": 3,
                "endColumn": 19,
                "endLine": 3
            },
            "variableName": {
                "literal": false,
                "value": "e",
                "kind": "Identifier"
            },
            "packageAlias": {
                "literal": false,
                "value": "",
                "kind": "Identifier"
            },
            "kind": "SimpleVariableRef"
        }
    },
    "createJMSResource": {
        "position": {
            "startColumn": 13,
            "startLine": 3,
            "endColumn": 13,
            "endLine": 5
        },
        "workers": [],
        "name": {
            "literal": false,
            "value": "echo1",
            "kind": "Identifier"
        },
        "annotationAttachments": [],
        "documentationAttachments": [],
        "defaultableParameters": [],
        "deprecatedAttachments": [],
        "body": {
            "statements": [],
            "kind": "Block"
        },
        "endpointNodes": [],
        "returnParameters": [],
        "parameters": [
            {
                "position": {
                    "startColumn": 20,
                    "startLine": 3,
                    "endColumn": 35,
                    "endLine": 3
                },
                "symbolType": [
                    "other"
                ],
                "safeAssignment": false,
                "typeNode": {
                    "position": {
                        "startColumn": 20,
                        "startLine": 3,
                        "endColumn": 24,
                        "endLine": 3
                    },
                    "symbolType": [
                        "other"
                    ],
                    "packageAlias": {
                        "literal": false,
                        "value": "jms",
                        "kind": "Identifier"
                    },
                    "grouped": false,
                    "typeName": {
                        "literal": false,
                        "value": "JMSMessage",
                        "kind": "Identifier"
                    },
                    "nullable": false,
                    "kind": "UserDefinedType"
                },
                "deprecatedAttachments": [],
                "annotationAttachments": [],
                "name": {
                    "literal": false,
                    "value": "request",
                    "kind": "Identifier"
                },
                "documentationAttachments": [],
                "kind": "Variable",
                "public": false,
                "native": false,
                "const": false,
                "attached": false,
                "lambda": false,
                "parallel": false,
                "connector": false,
                "deprecated": false
            }
        ],
        "kind": "Resource",
        "public": true,
        "native": false,
        "const": false,
        "attached": false,
        "lambda": false,
        "parallel": false,
        "connector": false,
        "deprecated": false
    },
    "createAssignmentStmt": {
        "position": {
            "startColumn": 1,
            "startLine": 2,
            "endColumn": 10,
            "endLine": 2
        },
        "variables": [
            {
                "position": {
                    "startColumn": 5,
                    "startLine": 2,
                    "endColumn": 5,
                    "endLine": 2
                },
                "variableName": {
                    "literal": false,
                    "value": "a",
                    "kind": "Identifier"
                },
                "packageAlias": {
                    "literal": false,
                    "value": "",
                    "kind": "Identifier"
                },
                "kind": "SimpleVariableRef"
            }
        ],
        "expression": {
            "position": {
                "startColumn": 9,
                "startLine": 2,
                "endColumn": 9,
                "endLine": 2
            },
            "value": "1",
            "kind": "Literal"
        },
        "declaredWithVar": true,
        "kind": "Assignment"
    },
    "createHTTPResource": {
        "position": {
            "startColumn": 13,
            "startLine": 3,
            "endColumn": 13,
            "endLine": 5
        },
        "workers": [],
        "name": {
            "literal": false,
            "value": "echo1",
            "kind": "Identifier"
        },
        "annotationAttachments": [],
        "documentationAttachments": [],
        "defaultableParameters": [],
        "deprecatedAttachments": [],
        "body": {
            "statements": [],
            "kind": "Block"
        },
        "endpointNodes": [],
        "returnParameters": [],
        "parameters": [
            {
                "position": {
                    "startColumn": 20,
                    "startLine": 3,
                    "endColumn": 36,
                    "endLine": 3
                },
                "symbolType": [
                    "other"
                ],
                "safeAssignment": false,
                "typeNode": {
                    "position": {
                        "startColumn": 20,
                        "startLine": 3,
                        "endColumn": 25,
                        "endLine": 3
                    },
                    "symbolType": [
                        "other"
                    ],
                    "packageAlias": {
                        "literal": false,
                        "value": "http",
                        "kind": "Identifier"
                    },
                    "grouped": false,
                    "typeName": {
                        "literal": false,
                        "value": "Connection",
                        "kind": "Identifier"
                    },
                    "nullable": false,
                    "kind": "UserDefinedType"
                },
                "deprecatedAttachments": [],
                "annotationAttachments": [],
                "name": {
                    "literal": false,
                    "value": "conn",
                    "kind": "Identifier"
                },
                "documentationAttachments": [],
                "kind": "Variable",
                "public": false,
                "native": false,
                "const": false,
                "attached": false,
                "lambda": false,
                "parallel": false,
                "connector": false,
                "deprecated": false
            },
            {
                "position": {
                    "startColumn": 42,
                    "startLine": 3,
                    "endColumn": 55,
                    "endLine": 3
                },
                "symbolType": [
                    "other"
                ],
                "safeAssignment": false,
                "typeNode": {
                    "position": {
                        "startColumn": 42,
                        "startLine": 3,
                        "endColumn": 47,
                        "endLine": 3
                    },
                    "symbolType": [
                        "other"
                    ],
                    "packageAlias": {
                        "literal": false,
                        "value": "http",
                        "kind": "Identifier"
                    },
                    "grouped": false,
                    "typeName": {
                        "literal": false,
                        "value": "Request",
                        "kind": "Identifier"
                    },
                    "nullable": false,
                    "kind": "UserDefinedType"
                },
                "deprecatedAttachments": [],
                "annotationAttachments": [],
                "name": {
                    "literal": false,
                    "value": "req",
                    "kind": "Identifier"
                },
                "documentationAttachments": [],
                "kind": "Variable",
                "public": false,
                "native": false,
                "const": false,
                "attached": false,
                "lambda": false,
                "parallel": false,
                "connector": false,
                "deprecated": false
            }
        ],
        "kind": "Resource",
        "public": true,
        "native": false,
        "const": false,
        "attached": false,
        "lambda": false,
        "parallel": false,
        "connector": false,
        "deprecated": false
    },
    "createWSEndpointDef": {
        "position": {
            "startColumn": 1,
            "startLine": 2,
            "endColumn": 2,
            "endLine": 4
        },
        "symbolType": [
            "other"
        ],
        "configurationExpression": {
            "position": {
                "startColumn": 34,
                "startLine": 2,
                "endColumn": 1,
                "endLine": 4
            },
            "keyValuePairs": [
                {
                    "value": {
                        "position": {
                            "startColumn": 10,
                            "startLine": 3,
                            "endColumn": 10,
                            "endLine": 3
                        },
                        "value": "9090",
                        "kind": "Literal"
                    },
                    "key": {
                        "position": {
                            "startColumn": 5,
                            "startLine": 3,
                            "endColumn": 5,
                            "endLine": 3
                        },
                        "variableName": {
                            "literal": false,
                            "value": "port",
                            "kind": "Identifier"
                        },
                        "packageAlias": {
                            "literal": false,
                            "value": "",
                            "kind": "Identifier"
                        },
                        "kind": "SimpleVariableRef"
                    },
                    "kind": "RecordLiteralKeyValue"
                }
            ],
            "kind": "RecordLiteralExpr"
        },
        "name": {
            "literal": false,
            "value": "ep",
            "kind": "Identifier"
        },
        "annotationAttachments": [],
        "kind": "Endpoint",
        "endPointType": {
            "position": {
                "startColumn": 10,
                "startLine": 2,
                "endColumn": 15,
                "endLine": 2
            },
            "symbolType": [
                "other"
            ],
            "packageAlias": {
                "literal": false,
                "value": "http",
                "kind": "Identifier"
            },
            "grouped": false,
            "typeName": {
                "literal": false,
                "value": "ServiceEndpoint",
                "kind": "Identifier"
            },
            "nullable": false,
            "kind": "UserDefinedType"
        },
        "public": false,
        "native": false,
        "const": false,
        "attached": false,
        "lambda": false,
        "parallel": false,
        "connector": false,
        "deprecated": false
    },
    "createWSServiceDef": {
        "position": {
            "startColumn": 13,
            "startLine": 2,
            "endColumn": 13,
            "endLine": 15
        },
        "annotationAttachments": [],
        "serviceTypeStruct": {
            "position": {
                "startColumn": 13,
                "startLine": 2,
                "endColumn": 13,
                "endLine": 15
            },
            "packageAlias": {
                "literal": false,
                "value": "http",
                "kind": "Identifier"
            },
            "grouped": false,
            "typeName": {
                "literal": false,
                "value": "WebSocketService",
                "kind": "Identifier"
            },
            "nullable": false,
            "kind": "UserDefinedType"
        },
        "initFunction": {
            "position": {
                "startColumn": 13,
                "startLine": 2,
                "endColumn": 13,
                "endLine": 15
            },
            "interfaceFunction": false,
            "workers": [],
            "name": {
                "literal": false,
                "value": "SimpleSecureServer.<init>",
                "kind": "Identifier"
            },
            "annotationAttachments": [],
            "documentationAttachments": [],
            "defaultableParameters": [],
            "deprecatedAttachments": [],
            "body": {
                "position": {
                    "startColumn": 13,
                    "startLine": 2,
                    "endColumn": 13,
                    "endLine": 15
                },
                "statements": [
                    {
                        "position": {
                            "startColumn": 13,
                            "startLine": 2,
                            "endColumn": 13,
                            "endLine": 15
                        },
                        "expressions": [],
                        "kind": "Return"
                    }
                ],
                "kind": "Block"
            },
            "endpointNodes": [],
            "returnParameters": [],
            "parameters": [],
            "kind": "Function",
            "public": true,
            "native": false,
            "const": false,
            "attached": false,
            "lambda": false,
            "parallel": false,
            "connector": false,
            "deprecated": false
        },
        "endpointNodes": [],
        "documentationAttachments": [],
        "boundEndpoints": [
            {
                "position": {
                    "startColumn": 68,
                    "startLine": 2,
                    "endColumn": 68,
                    "endLine": 2
                },
                "variableName": {
                    "literal": false,
                    "value": "ep",
                    "kind": "Identifier"
                },
                "packageAlias": {
                    "literal": false,
                    "value": "",
                    "kind": "Identifier"
                },
                "kind": "SimpleVariableRef"
            }
        ],
        "resources": [
            {
                "position": {
                    "startColumn": 17,
                    "startLine": 4,
                    "endColumn": 17,
                    "endLine": 6
                },
                "workers": [],
                "name": {
                    "literal": false,
                    "value": "onOpen",
                    "kind": "Identifier"
                },
                "annotationAttachments": [],
                "documentationAttachments": [],
                "defaultableParameters": [],
                "deprecatedAttachments": [],
                "body": {
                    "statements": [],
                    "kind": "Block"
                },
                "endpointNodes": [],
                "returnParameters": [],
                "parameters": [
                    {
                        "position": {
                            "startColumn": 25,
                            "startLine": 4,
                            "endColumn": 34,
                            "endLine": 4
                        },
                        "symbolType": [
                            "other"
                        ],
                        "safeAssignment": false,
                        "deprecatedAttachments": [],
                        "annotationAttachments": [],
                        "name": {
                            "literal": false,
                            "value": "$ep",
                            "kind": "Identifier"
                        },
                        "documentationAttachments": [],
                        "kind": "Variable",
                        "public": false,
                        "native": false,
                        "const": false,
                        "attached": false,
                        "lambda": false,
                        "parallel": false,
                        "connector": false,
                        "deprecated": false
                    }
                ],
                "kind": "Resource",
                "public": true,
                "native": false,
                "const": false,
                "attached": false,
                "lambda": false,
                "parallel": false,
                "connector": false,
                "deprecated": false
            },
            {
                "position": {
                    "startColumn": 17,
                    "startLine": 8,
                    "endColumn": 17,
                    "endLine": 10
                },
                "workers": [],
                "name": {
                    "literal": false,
                    "value": "onTextMessage",
                    "kind": "Identifier"
                },
                "annotationAttachments": [],
                "documentationAttachments": [],
                "defaultableParameters": [],
                "deprecatedAttachments": [],
                "body": {
                    "statements": [],
                    "kind": "Block"
                },
                "endpointNodes": [],
                "returnParameters": [],
                "parameters": [
                    {
                        "position": {
                            "startColumn": 32,
                            "startLine": 8,
                            "endColumn": 62,
                            "endLine": 8
                        },
                        "symbolType": [
                            "other"
                        ],
                        "safeAssignment": false,
                        "deprecatedAttachments": [],
                        "annotationAttachments": [],
                        "name": {
                            "literal": false,
                            "value": "$conn",
                            "kind": "Identifier"
                        },
                        "documentationAttachments": [],
                        "kind": "Variable",
                        "public": false,
                        "native": false,
                        "const": false,
                        "attached": false,
                        "lambda": false,
                        "parallel": false,
                        "connector": false,
                        "deprecated": false
                    },
                    {
                        "position": {
                            "startColumn": 47,
                            "startLine": 8,
                            "endColumn": 62,
                            "endLine": 8
                        },
                        "symbolType": [
                            "other"
                        ],
                        "safeAssignment": false,
                        "typeNode": {
                            "position": {
                                "startColumn": 47,
                                "startLine": 8,
                                "endColumn": 52,
                                "endLine": 8
                            },
                            "symbolType": [
                                "other"
                            ],
                            "packageAlias": {
                                "literal": false,
                                "value": "http",
                                "kind": "Identifier"
                            },
                            "grouped": false,
                            "typeName": {
                                "literal": false,
                                "value": "TextFrame",
                                "kind": "Identifier"
                            },
                            "nullable": false,
                            "kind": "UserDefinedType"
                        },
                        "deprecatedAttachments": [],
                        "annotationAttachments": [],
                        "name": {
                            "literal": false,
                            "value": "frame",
                            "kind": "Identifier"
                        },
                        "documentationAttachments": [],
                        "kind": "Variable",
                        "public": false,
                        "native": false,
                        "const": false,
                        "attached": false,
                        "lambda": false,
                        "parallel": false,
                        "connector": false,
                        "deprecated": false
                    }
                ],
                "kind": "Resource",
                "public": true,
                "native": false,
                "const": false,
                "attached": false,
                "lambda": false,
                "parallel": false,
                "connector": false,
                "deprecated": false
            },
            {
                "position": {
                    "startColumn": 17,
                    "startLine": 12,
                    "endColumn": 17,
                    "endLine": 14
                },
                "workers": [],
                "name": {
                    "literal": false,
                    "value": "onClose",
                    "kind": "Identifier"
                },
                "annotationAttachments": [],
                "documentationAttachments": [],
                "defaultableParameters": [],
                "deprecatedAttachments": [],
                "body": {
                    "statements": [],
                    "kind": "Block"
                },
                "endpointNodes": [],
                "returnParameters": [],
                "parameters": [
                    {
                        "position": {
                            "startColumn": 26,
                            "startLine": 12,
                            "endColumn": 57,
                            "endLine": 12
                        },
                        "symbolType": [
                            "other"
                        ],
                        "safeAssignment": false,
                        "deprecatedAttachments": [],
                        "annotationAttachments": [],
                        "name": {
                            "literal": false,
                            "value": "$conn",
                            "kind": "Identifier"
                        },
                        "documentationAttachments": [],
                        "kind": "Variable",
                        "public": false,
                        "native": false,
                        "const": false,
                        "attached": false,
                        "lambda": false,
                        "parallel": false,
                        "connector": false,
                        "deprecated": false
                    },
                    {
                        "position": {
                            "startColumn": 41,
                            "startLine": 12,
                            "endColumn": 57,
                            "endLine": 12
                        },
                        "symbolType": [
                            "other"
                        ],
                        "safeAssignment": false,
                        "typeNode": {
                            "position": {
                                "startColumn": 41,
                                "startLine": 12,
                                "endColumn": 46,
                                "endLine": 12
                            },
                            "symbolType": [
                                "other"
                            ],
                            "packageAlias": {
                                "literal": false,
                                "value": "http",
                                "kind": "Identifier"
                            },
                            "grouped": false,
                            "typeName": {
                                "literal": false,
                                "value": "CloseFrame",
                                "kind": "Identifier"
                            },
                            "nullable": false,
                            "kind": "UserDefinedType"
                        },
                        "deprecatedAttachments": [],
                        "annotationAttachments": [],
                        "name": {
                            "literal": false,
                            "value": "closeFrame",
                            "kind": "Identifier"
                        },
                        "documentationAttachments": [],
                        "kind": "Variable",
                        "public": false,
                        "native": false,
                        "const": false,
                        "attached": false,
                        "lambda": false,
                        "parallel": false,
                        "connector": false,
                        "deprecated": false
                    }
                ],
                "kind": "Resource",
                "public": true,
                "native": false,
                "const": false,
                "attached": false,
                "lambda": false,
                "parallel": false,
                "connector": false,
                "deprecated": false
            }
        ],
        "deprecatedAttachments": [],
        "variables": [],
        "name": {
            "literal": false,
            "value": "SimpleSecureServer",
            "kind": "Identifier"
        },
        "kind": "Service"
    },
    "createJMSServiceDef": {
        "error": "Index: 0, Size: 0"
    },
    "createHTTPServiceDef": {
        "position": {
            "startColumn": 17,
            "startLine": 2,
            "endColumn": 17,
            "endLine": 6
        },
        "annotationAttachments": [],
        "serviceTypeStruct": {
            "position": {
                "startColumn": 17,
                "startLine": 2,
                "endColumn": 17,
                "endLine": 6
            },
            "packageAlias": {
                "literal": false,
                "value": "http",
                "kind": "Identifier"
            },
            "grouped": false,
            "typeName": {
                "literal": false,
                "value": "Service",
                "kind": "Identifier"
            },
            "nullable": false,
            "kind": "UserDefinedType"
        },
        "initFunction": {
            "position": {
                "startColumn": 17,
                "startLine": 2,
                "endColumn": 17,
                "endLine": 6
            },
            "interfaceFunction": false,
            "workers": [],
            "name": {
                "literal": false,
                "value": "serviceName.<init>",
                "kind": "Identifier"
            },
            "annotationAttachments": [],
            "documentationAttachments": [],
            "defaultableParameters": [],
            "deprecatedAttachments": [],
            "body": {
                "position": {
                    "startColumn": 17,
                    "startLine": 2,
                    "endColumn": 17,
                    "endLine": 6
                },
                "statements": [
                    {
                        "position": {
                            "startColumn": 17,
                            "startLine": 2,
                            "endColumn": 17,
                            "endLine": 6
                        },
                        "expressions": [],
                        "kind": "Return"
                    }
                ],
                "kind": "Block"
            },
            "endpointNodes": [],
            "returnParameters": [],
            "parameters": [],
            "kind": "Function",
            "public": true,
            "native": false,
            "const": false,
            "attached": false,
            "lambda": false,
            "parallel": false,
            "connector": false,
            "deprecated": false
        },
        "endpointNodes": [],
        "documentationAttachments": [],
        "boundEndpoints": [
            {
                "position": {
                    "startColumn": 56,
                    "startLine": 2,
                    "endColumn": 56,
                    "endLine": 2
                },
                "variableName": {
                    "literal": false,
                    "value": "serviceEp",
                    "kind": "Identifier"
                },
                "packageAlias": {
                    "literal": false,
                    "value": "",
                    "kind": "Identifier"
                },
                "kind": "SimpleVariableRef"
            }
        ],
        "resources": [
            {
                "position": {
                    "startColumn": 21,
                    "startLine": 3,
                    "endColumn": 21,
                    "endLine": 5
                },
                "workers": [],
                "name": {
                    "literal": false,
                    "value": "resourceName",
                    "kind": "Identifier"
                },
                "annotationAttachments": [],
                "documentationAttachments": [],
                "defaultableParameters": [],
                "deprecatedAttachments": [],
                "body": {
                    "statements": [],
                    "kind": "Block"
                },
                "endpointNodes": [],
                "returnParameters": [],
                "parameters": [
                    {
                        "position": {
                            "startColumn": 35,
                            "startLine": 3,
                            "endColumn": 63,
                            "endLine": 3
                        },
                        "symbolType": [
                            "other"
                        ],
                        "safeAssignment": false,
                        "deprecatedAttachments": [],
                        "annotationAttachments": [],
                        "name": {
                            "literal": false,
                            "value": "$conn",
                            "kind": "Identifier"
                        },
                        "documentationAttachments": [],
                        "kind": "Variable",
                        "public": false,
                        "native": false,
                        "const": false,
                        "attached": false,
                        "lambda": false,
                        "parallel": false,
                        "connector": false,
                        "deprecated": false
                    },
                    {
                        "position": {
                            "startColumn": 50,
                            "startLine": 3,
                            "endColumn": 63,
                            "endLine": 3
                        },
                        "symbolType": [
                            "other"
                        ],
                        "safeAssignment": false,
                        "typeNode": {
                            "position": {
                                "startColumn": 50,
                                "startLine": 3,
                                "endColumn": 55,
                                "endLine": 3
                            },
                            "symbolType": [
                                "other"
                            ],
                            "packageAlias": {
                                "literal": false,
                                "value": "http",
                                "kind": "Identifier"
                            },
                            "grouped": false,
                            "typeName": {
                                "literal": false,
                                "value": "Request",
                                "kind": "Identifier"
                            },
                            "nullable": false,
                            "kind": "UserDefinedType"
                        },
                        "deprecatedAttachments": [],
                        "annotationAttachments": [],
                        "name": {
                            "literal": false,
                            "value": "req",
                            "kind": "Identifier"
                        },
                        "documentationAttachments": [],
                        "kind": "Variable",
                        "public": false,
                        "native": false,
                        "const": false,
                        "attached": false,
                        "lambda": false,
                        "parallel": false,
                        "connector": false,
                        "deprecated": false
                    }
                ],
                "kind": "Resource",
                "public": true,
                "native": false,
                "const": false,
                "attached": false,
                "lambda": false,
                "parallel": false,
                "connector": false,
                "deprecated": false
            }
        ],
        "deprecatedAttachments": [],
        "variables": [],
        "name": {
            "literal": false,
            "value": "serviceName",
            "kind": "Identifier"
        },
        "kind": "Service"
    }
}