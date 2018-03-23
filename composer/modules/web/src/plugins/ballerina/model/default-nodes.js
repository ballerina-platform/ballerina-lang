/**
* This is a auto generated file, DO NOT modify this manually.
* Use npm run gen-default-nodes command to generate this file.
*/

export default {
    "createInvocation": {
        "ws": [
            {
                "ws": "",
                "i": 12,
                "text": ";",
                "static": true
            }
        ],
        "position": {
            "startColumn": 13,
            "startLine": 3,
            "endColumn": 33,
            "endLine": 3
        },
        "expression": {
            "ws": [
                {
                    "ws": "\n\n            ",
                    "i": 8,
                    "text": "invokeFunction",
                    "static": false
                },
                {
                    "ws": "",
                    "i": 9,
                    "text": "(",
                    "static": true
                },
                {
                    "ws": "",
                    "i": 11,
                    "text": ")",
                    "static": true
                }
            ],
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
                    "ws": [
                        {
                            "ws": "",
                            "i": 10,
                            "text": "arg1",
                            "static": false
                        }
                    ],
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
    "createJMSResource": {
        "error": "Incorrect format of the generated JSON"
    },
    "createTransformer": {
        "ws": [
            {
                "ws": "\n            ",
                "i": 2,
                "text": "transformer",
                "static": true
            },
            {
                "ws": " ",
                "i": 4,
                "text": "<",
                "static": true
            },
            {
                "ws": "",
                "i": 8,
                "text": ",",
                "static": true
            },
            {
                "ws": "",
                "i": 13,
                "text": ">",
                "static": true
            },
            {
                "ws": " ",
                "i": 15,
                "text": "newTransformer",
                "static": false
            },
            {
                "ws": " ",
                "i": 17,
                "text": "(",
                "static": true
            },
            {
                "ws": "",
                "i": 18,
                "text": ")",
                "static": true
            },
            {
                "ws": "",
                "i": 19,
                "text": "{",
                "static": true
            },
            {
                "ws": "\n\n            ",
                "i": 22,
                "text": "}",
                "static": true
            }
        ],
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
                "ws": [
                    {
                        "ws": " ",
                        "i": 12,
                        "text": "b",
                        "static": false
                    }
                ],
                "position": {
                    "startColumn": 36,
                    "startLine": 2,
                    "endColumn": 43,
                    "endLine": 2
                },
                "symbolType": [
                    "other"
                ],
                "typeNode": {
                    "ws": [
                        {
                            "ws": " ",
                            "i": 10,
                            "text": "Target",
                            "static": false
                        }
                    ],
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
                    "typeName": {
                        "literal": false,
                        "value": "Target",
                        "kind": "Identifier"
                    },
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
            "ws": [
                {
                    "ws": " ",
                    "i": 7,
                    "text": "a",
                    "static": false
                }
            ],
            "position": {
                "startColumn": 26,
                "startLine": 2,
                "endColumn": 33,
                "endLine": 2
            },
            "symbolType": [
                "other"
            ],
            "typeNode": {
                "ws": [
                    {
                        "ws": "",
                        "i": 5,
                        "text": "Source",
                        "static": false
                    }
                ],
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
                "typeName": {
                    "literal": false,
                    "value": "Source",
                    "kind": "Identifier"
                },
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
    "createFSServiceDef": {
        "error": "Incorrect format of the generated JSON"
    },
    "createStruct": {
        "ws": [
            {
                "ws": "\n            ",
                "i": 2,
                "text": "struct",
                "static": true
            },
            {
                "ws": " ",
                "i": 4,
                "text": "struct1",
                "static": false
            },
            {
                "ws": " ",
                "i": 6,
                "text": "{",
                "static": true
            },
            {
                "ws": "\n\n            ",
                "i": 9,
                "text": "}",
                "static": true
            }
        ],
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
        "error": "Incorrect format of the generated JSON"
    },
    "createJMSServiceDef": {
        "error": "Incorrect format of the generated JSON"
    },
    "createConnectorAction": {
        "error": "Incorrect format of the generated JSON"
    },
    "createEnum": {
        "position": {
            "startColumn": 13,
            "startLine": 2,
            "endColumn": -1,
            "endLine": -1
        },
        "name": {
            "literal": false,
            "value": "name",
            "kind": "Identifier"
        },
        "deprecatedAttachments": [],
        "enumerators": [
            {
                "ws": [
                    {
                        "ws": "\n                ",
                        "i": 9,
                        "text": "ENUMERATOR",
                        "static": false
                    }
                ],
                "position": {
                    "startColumn": 17,
                    "startLine": 3,
                    "endColumn": 17,
                    "endLine": 3
                },
                "name": {
                    "literal": false,
                    "value": "ENUMERATOR",
                    "kind": "Identifier"
                },
                "kind": "Enumerator"
            }
        ],
        "annotationAttachments": [],
        "documentationAttachments": [],
        "kind": "Enum",
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
        "error": "Incorrect format of the generated JSON"
    },
    "createIf": {
        "ws": [
            {
                "ws": "\n\n            ",
                "i": 8,
                "text": "if",
                "static": true
            },
            {
                "ws": " ",
                "i": 10,
                "text": "(",
                "static": true
            },
            {
                "ws": "",
                "i": 12,
                "text": ")",
                "static": true
            },
            {
                "ws": " ",
                "i": 14,
                "text": "{",
                "static": true
            },
            {
                "ws": "\n\n            ",
                "i": 17,
                "text": "}",
                "static": true
            },
            {
                "ws": " ",
                "i": 19,
                "text": "else",
                "static": true
            },
            {
                "ws": " ",
                "i": 21,
                "text": "{",
                "static": true
            },
            {
                "ws": "\n            \n            ",
                "i": 26,
                "text": "}",
                "static": true
            }
        ],
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
            "ws": [
                {
                    "ws": "",
                    "i": 11,
                    "text": "true",
                    "static": false
                }
            ],
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
    "createWSServiceDef": {
        "error": "Incorrect format of the generated JSON"
    },
    "createConnector": {
        "error": "Incorrect format of the generated JSON"
    },
    "createReturn": {
        "ws": [
            {
                "ws": "\n\n            ",
                "i": 8,
                "text": "return",
                "static": true
            },
            {
                "ws": "",
                "i": 11,
                "text": ";",
                "static": true
            }
        ],
        "position": {
            "startColumn": 13,
            "startLine": 3,
            "endColumn": 21,
            "endLine": 3
        },
        "expressions": [
            {
                "ws": [
                    {
                        "ws": " ",
                        "i": 10,
                        "text": "m",
                        "static": false
                    }
                ],
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
    "createTransaction": {
        "error": "Incorrect format of the generated JSON"
    },
    "createAssignmentStmt": {
        "ws": [
            {
                "ws": "\n",
                "i": 7,
                "text": "var",
                "static": true
            },
            {
                "ws": " ",
                "i": 11,
                "text": "=",
                "static": true
            },
            {
                "ws": "",
                "i": 14,
                "text": ";",
                "static": true
            }
        ],
        "position": {
            "startColumn": 1,
            "startLine": 2,
            "endColumn": 10,
            "endLine": 2
        },
        "variables": [
            {
                "ws": [
                    {
                        "ws": " ",
                        "i": 9,
                        "text": "a",
                        "static": false
                    }
                ],
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
            "ws": [
                {
                    "ws": " ",
                    "i": 13,
                    "text": "1",
                    "static": false
                }
            ],
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
    "createFTPResource": {
        "error": "Incorrect format of the generated JSON"
    },
    "createDefaultWorkerFragment": {
        "ws": [
            {
                "ws": "\n\n            ",
                "i": 8,
                "text": "worker",
                "static": true
            },
            {
                "ws": " ",
                "i": 10,
                "text": "default",
                "static": false
            },
            {
                "ws": " ",
                "i": 12,
                "text": "{",
                "static": true
            },
            {
                "ws": "\n            ",
                "i": 15,
                "text": "}",
                "static": true
            }
        ],
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
    "createXmlns": {
        "position": {
            "startColumn": 13,
            "startLine": 3,
            "endColumn": 40,
            "endLine": 3
        },
        "namespaceDeclaration": {
            "ws": [
                {
                    "ws": "\n\n            ",
                    "i": 8,
                    "text": "xmlns",
                    "static": true
                },
                {
                    "ws": " ",
                    "i": 12,
                    "text": "as",
                    "static": true
                },
                {
                    "ws": " ",
                    "i": 14,
                    "text": "xn",
                    "static": false
                },
                {
                    "ws": "",
                    "i": 15,
                    "text": ";",
                    "static": true
                }
            ],
            "position": {
                "startColumn": 13,
                "startLine": 3,
                "endColumn": 40,
                "endLine": 3
            },
            "namespaceURI": {
                "ws": [
                    {
                        "ws": " ",
                        "i": 10,
                        "text": "\"namespace.uri\"",
                        "static": false
                    }
                ],
                "position": {
                    "startColumn": 13,
                    "startLine": 3,
                    "endColumn": 40,
                    "endLine": 3
                },
                "value": "\"namespace.uri\"",
                "unescapedValue": "namespace.uri",
                "kind": "Literal"
            },
            "kind": "Xmlns",
            "prefix": {
                "position": {
                    "startColumn": 13,
                    "startLine": 3,
                    "endColumn": 40,
                    "endLine": 3
                },
                "literal": false,
                "value": "xn",
                "kind": "Identifier"
            }
        },
        "kind": "Xmlns"
    },
    "createBindStmt": {
        "error": "Incorrect format of the generated JSON"
    },
    "createHTTPResource": {
        "error": "Incorrect format of the generated JSON"
    },
    "createForkJoin": {
        "ws": [
            {
                "ws": "\n\n            ",
                "i": 8,
                "text": "fork",
                "static": true
            },
            {
                "ws": " ",
                "i": 10,
                "text": "{",
                "static": true
            },
            {
                "ws": "\n            ",
                "i": 33,
                "text": "}",
                "static": true
            },
            {
                "ws": " ",
                "i": 35,
                "text": "join",
                "static": true
            },
            {
                "ws": "",
                "i": 36,
                "text": "(",
                "static": true
            },
            {
                "ws": "",
                "i": 37,
                "text": "all",
                "static": true
            },
            {
                "ws": "",
                "i": 38,
                "text": ")",
                "static": true
            },
            {
                "ws": "",
                "i": 39,
                "text": "(",
                "static": true
            },
            {
                "ws": "",
                "i": 43,
                "text": ")",
                "static": true
            },
            {
                "ws": " ",
                "i": 45,
                "text": "{",
                "static": true
            },
            {
                "ws": "\n            \n            ",
                "i": 50,
                "text": "}",
                "static": true
            },
            {
                "ws": " ",
                "i": 52,
                "text": "timeout",
                "static": true
            },
            {
                "ws": "",
                "i": 53,
                "text": "(",
                "static": true
            },
            {
                "ws": "",
                "i": 55,
                "text": ")",
                "static": true
            },
            {
                "ws": "",
                "i": 56,
                "text": "(",
                "static": true
            },
            {
                "ws": "",
                "i": 60,
                "text": ")",
                "static": true
            },
            {
                "ws": " ",
                "i": 62,
                "text": "{",
                "static": true
            },
            {
                "ws": "\n            \n            ",
                "i": 67,
                "text": "}",
                "static": true
            }
        ],
        "position": {
            "startColumn": 13,
            "startLine": 3,
            "endColumn": 13,
            "endLine": 12
        },
        "timeOutExpression": {
            "ws": [
                {
                    "ws": "",
                    "i": 54,
                    "text": "100",
                    "static": false
                }
            ],
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
            "ws": [
                {
                    "ws": " ",
                    "i": 59,
                    "text": "results1",
                    "static": false
                }
            ],
            "position": {
                "startColumn": 28,
                "startLine": 10,
                "endColumn": 28,
                "endLine": 10
            },
            "typeNode": {
                "ws": [
                    {
                        "ws": "",
                        "i": 57,
                        "text": "map",
                        "static": true
                    }
                ],
                "position": {
                    "startColumn": 28,
                    "startLine": 10,
                    "endColumn": 28,
                    "endLine": 10
                },
                "typeKind": "map",
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
                "ws": [
                    {
                        "ws": "\n                ",
                        "i": 13,
                        "text": "worker",
                        "static": true
                    },
                    {
                        "ws": " ",
                        "i": 15,
                        "text": "worker1",
                        "static": false
                    },
                    {
                        "ws": " ",
                        "i": 17,
                        "text": "{",
                        "static": true
                    },
                    {
                        "ws": "\n                ",
                        "i": 20,
                        "text": "}",
                        "static": true
                    }
                ],
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
                "ws": [
                    {
                        "ws": "\n                ",
                        "i": 23,
                        "text": "worker",
                        "static": true
                    },
                    {
                        "ws": " ",
                        "i": 25,
                        "text": "worker2",
                        "static": false
                    },
                    {
                        "ws": " ",
                        "i": 27,
                        "text": "{",
                        "static": true
                    },
                    {
                        "ws": "\n                ",
                        "i": 30,
                        "text": "}",
                        "static": true
                    }
                ],
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
            "ws": [
                {
                    "ws": " ",
                    "i": 42,
                    "text": "results",
                    "static": false
                }
            ],
            "position": {
                "startColumn": 25,
                "startLine": 8,
                "endColumn": 25,
                "endLine": 8
            },
            "typeNode": {
                "ws": [
                    {
                        "ws": "",
                        "i": 40,
                        "text": "map",
                        "static": true
                    }
                ],
                "position": {
                    "startColumn": 25,
                    "startLine": 8,
                    "endColumn": 25,
                    "endLine": 8
                },
                "typeKind": "map",
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
    "createAnnotation": {
        "error": "Incorrect format of the generated JSON"
    },
    "createAbort": {
        "ws": [
            {
                "ws": "\n\n            ",
                "i": 8,
                "text": "abort",
                "static": true
            },
            {
                "ws": "",
                "i": 9,
                "text": ";",
                "static": true
            }
        ],
        "position": {
            "startColumn": 13,
            "startLine": 3,
            "endColumn": 18,
            "endLine": 3
        },
        "kind": "Abort"
    },
    "createVarDefStmt": {
        "ws": [
            {
                "ws": "",
                "i": 14,
                "text": ";",
                "static": true
            }
        ],
        "position": {
            "startColumn": 1,
            "startLine": 2,
            "endColumn": 10,
            "endLine": 2
        },
        "variable": {
            "ws": [
                {
                    "ws": " ",
                    "i": 9,
                    "text": "a",
                    "static": false
                },
                {
                    "ws": " ",
                    "i": 11,
                    "text": "=",
                    "static": true
                }
            ],
            "position": {
                "startColumn": 1,
                "startLine": 2,
                "endColumn": 10,
                "endLine": 2
            },
            "typeNode": {
                "ws": [
                    {
                        "ws": "\n",
                        "i": 7,
                        "text": "int",
                        "static": true
                    }
                ],
                "position": {
                    "startColumn": 1,
                    "startLine": 2,
                    "endColumn": 1,
                    "endLine": 2
                },
                "typeKind": "int",
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
                "ws": [
                    {
                        "ws": " ",
                        "i": 13,
                        "text": "1",
                        "static": false
                    }
                ],
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
    "createThrow": {
        "ws": [
            {
                "ws": "\n\n            ",
                "i": 8,
                "text": "throw",
                "static": true
            },
            {
                "ws": "",
                "i": 11,
                "text": ";",
                "static": true
            }
        ],
        "position": {
            "startColumn": 13,
            "startLine": 3,
            "endColumn": 20,
            "endLine": 3
        },
        "kind": "Throw",
        "expressions": {
            "ws": [
                {
                    "ws": " ",
                    "i": 10,
                    "text": "e",
                    "static": false
                }
            ],
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
    "createHTTPServiceDef": {
        "error": "Incorrect format of the generated JSON"
    },
    "createWorkerInvocation": {
        "ws": [
            {
                "ws": " ",
                "i": 10,
                "text": "->",
                "static": true
            },
            {
                "ws": " ",
                "i": 12,
                "text": "worker1",
                "static": false
            },
            {
                "ws": " ",
                "i": 14,
                "text": ";",
                "static": true
            }
        ],
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
                "ws": [
                    {
                        "ws": "\n\n            ",
                        "i": 8,
                        "text": "m",
                        "static": false
                    }
                ],
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
    "createWhile": {
        "ws": [
            {
                "ws": "\n\n            ",
                "i": 8,
                "text": "while",
                "static": true
            },
            {
                "ws": "",
                "i": 9,
                "text": "(",
                "static": true
            },
            {
                "ws": "",
                "i": 11,
                "text": ")",
                "static": true
            },
            {
                "ws": " ",
                "i": 13,
                "text": "{",
                "static": true
            },
            {
                "ws": "\n\n            ",
                "i": 16,
                "text": "}",
                "static": true
            }
        ],
        "position": {
            "startColumn": 13,
            "startLine": 3,
            "endColumn": 13,
            "endLine": 5
        },
        "condition": {
            "ws": [
                {
                    "ws": "",
                    "i": 10,
                    "text": "true",
                    "static": false
                }
            ],
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
        "ws": [
            {
                "ws": "\n\n            ",
                "i": 8,
                "text": "worker",
                "static": true
            },
            {
                "ws": " ",
                "i": 10,
                "text": "worker1",
                "static": false
            },
            {
                "ws": " ",
                "i": 12,
                "text": "{",
                "static": true
            },
            {
                "ws": "\n            ",
                "i": 15,
                "text": "}",
                "static": true
            }
        ],
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
    "createWorkerReceive": {
        "ws": [
            {
                "ws": " ",
                "i": 10,
                "text": "<-",
                "static": true
            },
            {
                "ws": " ",
                "i": 12,
                "text": "worker1",
                "static": false
            },
            {
                "ws": " ",
                "i": 14,
                "text": ";",
                "static": true
            }
        ],
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
                "ws": [
                    {
                        "ws": "\n\n            ",
                        "i": 8,
                        "text": "m",
                        "static": false
                    }
                ],
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
    "createBreak": {
        "ws": [
            {
                "ws": "\n\n            ",
                "i": 8,
                "text": "break",
                "static": true
            },
            {
                "ws": "",
                "i": 9,
                "text": ";",
                "static": true
            }
        ],
        "position": {
            "startColumn": 13,
            "startLine": 3,
            "endColumn": 18,
            "endLine": 3
        },
        "kind": "Break"
    },
    "createMainFunction": {
        "ws": [
            {
                "ws": "\n            ",
                "i": 2,
                "text": "function",
                "static": true
            },
            {
                "ws": " ",
                "i": 4,
                "text": "main",
                "static": false
            },
            {
                "ws": "",
                "i": 5,
                "text": "(",
                "static": true
            },
            {
                "ws": "",
                "i": 11,
                "text": ")",
                "static": true
            },
            {
                "ws": " ",
                "i": 13,
                "text": "{",
                "static": true
            },
            {
                "ws": "\n\n            ",
                "i": 16,
                "text": "}",
                "static": true
            }
        ],
        "position": {
            "startColumn": 13,
            "startLine": 2,
            "endColumn": 13,
            "endLine": 4
        },
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
                "ws": [
                    {
                        "ws": " ",
                        "i": 10,
                        "text": "args",
                        "static": false
                    }
                ],
                "position": {
                    "startColumn": 27,
                    "startLine": 2,
                    "endColumn": 36,
                    "endLine": 2
                },
                "symbolType": [
                    "[]"
                ],
                "typeNode": {
                    "ws": [
                        {
                            "ws": "",
                            "i": 7,
                            "text": "[",
                            "static": true
                        },
                        {
                            "ws": "",
                            "i": 8,
                            "text": "]",
                            "static": true
                        }
                    ],
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
                        "ws": [
                            {
                                "ws": "",
                                "i": 6,
                                "text": "string",
                                "static": true
                            }
                        ],
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
                        "kind": "ValueType"
                    },
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
    "createIfElse": {
        "ws": [
            {
                "ws": "\n\n            ",
                "i": 8,
                "text": "if",
                "static": true
            },
            {
                "ws": " ",
                "i": 10,
                "text": "(",
                "static": true
            },
            {
                "ws": "",
                "i": 12,
                "text": ")",
                "static": true
            },
            {
                "ws": " ",
                "i": 14,
                "text": "{",
                "static": true
            },
            {
                "ws": "\n\n            ",
                "i": 17,
                "text": "}",
                "static": true
            },
            {
                "ws": " ",
                "i": 19,
                "text": "else",
                "static": true
            }
        ],
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
            "ws": [
                {
                    "ws": "",
                    "i": 11,
                    "text": "true",
                    "static": false
                }
            ],
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
            "ws": [
                {
                    "ws": " ",
                    "i": 21,
                    "text": "if",
                    "static": true
                },
                {
                    "ws": " ",
                    "i": 23,
                    "text": "(",
                    "static": true
                },
                {
                    "ws": "",
                    "i": 25,
                    "text": ")",
                    "static": true
                },
                {
                    "ws": " ",
                    "i": 27,
                    "text": "{",
                    "static": true
                },
                {
                    "ws": "\n            \n            ",
                    "i": 32,
                    "text": "}",
                    "static": true
                },
                {
                    "ws": " ",
                    "i": 34,
                    "text": "else",
                    "static": true
                },
                {
                    "ws": " ",
                    "i": 36,
                    "text": "{",
                    "static": true
                },
                {
                    "ws": "\n            \n            ",
                    "i": 41,
                    "text": "}",
                    "static": true
                }
            ],
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
                "ws": [
                    {
                        "ws": "",
                        "i": 24,
                        "text": "true",
                        "static": false
                    }
                ],
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
    "createNext": {
        "ws": [
            {
                "ws": "\n\n            ",
                "i": 8,
                "text": "next",
                "static": true
            },
            {
                "ws": "",
                "i": 9,
                "text": ";",
                "static": true
            }
        ],
        "position": {
            "startColumn": 13,
            "startLine": 3,
            "endColumn": 17,
            "endLine": 3
        },
        "kind": "Next"
    },
    "createFunction": {
        "ws": [
            {
                "ws": "\n                    ",
                "i": 2,
                "text": "function",
                "static": true
            },
            {
                "ws": " ",
                "i": 4,
                "text": "function1",
                "static": false
            },
            {
                "ws": "",
                "i": 5,
                "text": "(",
                "static": true
            },
            {
                "ws": "",
                "i": 6,
                "text": ")",
                "static": true
            },
            {
                "ws": " ",
                "i": 8,
                "text": "{",
                "static": true
            },
            {
                "ws": "\n\n                    ",
                "i": 11,
                "text": "}",
                "static": true
            }
        ],
        "position": {
            "startColumn": 21,
            "startLine": 2,
            "endColumn": 21,
            "endLine": 4
        },
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
    "createTry": {
        "ws": [
            {
                "ws": "\n\n            ",
                "i": 8,
                "text": "try",
                "static": true
            },
            {
                "ws": " ",
                "i": 10,
                "text": "{",
                "static": true
            },
            {
                "ws": "\n\n            ",
                "i": 13,
                "text": "}",
                "static": true
            },
            {
                "ws": " ",
                "i": 28,
                "text": "finally",
                "static": true
            },
            {
                "ws": " ",
                "i": 30,
                "text": "{",
                "static": true
            },
            {
                "ws": "\n            \n            ",
                "i": 35,
                "text": "}",
                "static": true
            }
        ],
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
                "ws": [
                    {
                        "ws": " ",
                        "i": 15,
                        "text": "catch",
                        "static": true
                    },
                    {
                        "ws": " ",
                        "i": 17,
                        "text": "(",
                        "static": true
                    },
                    {
                        "ws": "",
                        "i": 21,
                        "text": ")",
                        "static": true
                    },
                    {
                        "ws": " ",
                        "i": 23,
                        "text": "{",
                        "static": true
                    },
                    {
                        "ws": "\n\n            ",
                        "i": 26,
                        "text": "}",
                        "static": true
                    }
                ],
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
                    "ws": [
                        {
                            "ws": " ",
                            "i": 20,
                            "text": "err",
                            "static": false
                        }
                    ],
                    "position": {
                        "startColumn": 22,
                        "startLine": 5,
                        "endColumn": 22,
                        "endLine": 5
                    },
                    "typeNode": {
                        "ws": [
                            {
                                "ws": "",
                                "i": 18,
                                "text": "error",
                                "static": false
                            }
                        ],
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
                        "typeName": {
                            "literal": false,
                            "value": "error",
                            "kind": "Identifier"
                        },
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
    }
}