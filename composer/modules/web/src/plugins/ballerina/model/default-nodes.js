/**
* This is a auto generated file, DO NOT modify this manually.
* Use npm run gen-default-nodes command to generate this file.
*/

export default {
    "createImportWithOrg": {
        "ws": [
            {
                "ws": "",
                "i": 0,
                "text": "import",
                "static": true
            },
            {
                "ws": " ",
                "i": 2,
                "text": "ballerina",
                "static": false
            },
            {
                "ws": "",
                "i": 3,
                "text": "/",
                "static": true
            },
            {
                "ws": "",
                "i": 4,
                "text": "http",
                "static": false
            },
            {
                "ws": "",
                "i": 5,
                "text": ";",
                "static": true
            }
        ],
        "position": {
            "startColumn": 1,
            "startLine": 1,
            "endColumn": 22,
            "endLine": 1
        },
        "orgName": {
            "literal": false,
            "value": "ballerina",
            "kind": "Identifier"
        },
        "alias": {
            "literal": false,
            "value": "http",
            "kind": "Identifier"
        },
        "packageName": [
            {
                "literal": false,
                "value": "http",
                "kind": "Identifier"
            }
        ],
        "kind": "Import",
        "packageVersion": {
            "literal": false,
            "value": "",
            "kind": "Identifier"
        }
    },
    "createTransaction": {
        "ws": [
            {
                "ws": "\n\n            ",
                "i": 8,
                "text": "transaction",
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
                "i": 15,
                "text": "onretry",
                "static": true
            },
            {
                "ws": " ",
                "i": 17,
                "text": "{",
                "static": true
            },
            {
                "ws": "\n        \n            ",
                "i": 22,
                "text": "}",
                "static": true
            }
        ],
        "position": {
            "startColumn": 13,
            "startLine": 3,
            "endColumn": 13,
            "endLine": 7
        },
        "onRetryBody": {
            "position": {
                "startColumn": 15,
                "startLine": 5,
                "endColumn": 13,
                "endLine": 7
            },
            "statements": [],
            "kind": "Block"
        },
        "transactionBody": {
            "position": {
                "startColumn": 13,
                "startLine": 3,
                "endColumn": 13,
                "endLine": 5
            },
            "statements": [],
            "kind": "Block"
        },
        "kind": "Transaction"
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
            "symbolType": [
                "int"
            ],
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
            "symbolType": [
                "map"
            ],
            "safeAssignment": false,
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
                "symbolType": [
                    "map"
                ],
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
            "final": false,
            "attached": false,
            "lambda": false,
            "parallel": false,
            "connector": false,
            "deprecated": false,
            "readonly": false,
            "function_final": false,
            "interface": false
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
                "returnTypeAnnotationAttachments": [],
                "name": {
                    "literal": false,
                    "value": "worker1",
                    "kind": "Identifier"
                },
                "annotationAttachments": [],
                "documentationAttachments": [],
                "defaultableParameters": [],
                "deprecatedAttachments": [],
                "endpointNodes": [],
                "body": {
                    "statements": [],
                    "kind": "Block"
                },
                "parameters": [],
                "kind": "Worker",
                "public": false,
                "native": false,
                "final": false,
                "attached": false,
                "lambda": false,
                "parallel": false,
                "connector": false,
                "deprecated": false,
                "readonly": false,
                "function_final": false,
                "interface": false
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
                "returnTypeAnnotationAttachments": [],
                "name": {
                    "literal": false,
                    "value": "worker2",
                    "kind": "Identifier"
                },
                "annotationAttachments": [],
                "documentationAttachments": [],
                "defaultableParameters": [],
                "deprecatedAttachments": [],
                "endpointNodes": [],
                "body": {
                    "statements": [],
                    "kind": "Block"
                },
                "parameters": [],
                "kind": "Worker",
                "public": false,
                "native": false,
                "final": false,
                "attached": false,
                "lambda": false,
                "parallel": false,
                "connector": false,
                "deprecated": false,
                "readonly": false,
                "function_final": false,
                "interface": false
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
            "symbolType": [
                "map"
            ],
            "safeAssignment": false,
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
                "symbolType": [
                    "map"
                ],
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
            "final": false,
            "attached": false,
            "lambda": false,
            "parallel": false,
            "connector": false,
            "deprecated": false,
            "readonly": false,
            "function_final": false,
            "interface": false
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
        "expression": {
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
            "symbolType": [
                "other"
            ],
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
        },
        "workerName": {
            "literal": false,
            "value": "worker1",
            "kind": "Identifier"
        },
        "kind": "WorkerReceive"
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
        "expression": {
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
            "symbolType": [
                "other"
            ],
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
        },
        "forkJoinedSend": false,
        "kind": "WorkerSend"
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
        "expression": {
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
            "symbolType": [
                "other"
            ],
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
        },
        "kind": "Return"
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
            "symbolType": [
                "other"
            ],
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
                    "symbolType": [
                        "struct"
                    ],
                    "safeAssignment": false,
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
                        "symbolType": [
                            "struct"
                        ],
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
                    "final": false,
                    "attached": false,
                    "lambda": false,
                    "parallel": false,
                    "connector": false,
                    "deprecated": false,
                    "readonly": false,
                    "function_final": false,
                    "interface": false
                },
                "kind": "Catch"
            }
        ],
        "kind": "Try"
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
            "symbolType": [
                "boolean"
            ],
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
            "symbolType": [
                "other"
            ],
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
            "packageAlias": {
                "literal": false,
                "value": "",
                "kind": "Identifier"
            },
            "async": false,
            "actionInvocation": false,
            "name": {
                "literal": false,
                "value": "invokeFunction",
                "kind": "Identifier"
            },
            "kind": "Invocation"
        },
        "kind": "ExpressionStatement"
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
            "symbolType": [
                "boolean"
            ],
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
                "symbolType": [
                    "boolean"
                ],
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
            "symbolType": [
                "boolean"
            ],
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
            "symbolType": [
                "int"
            ],
            "safeAssignment": false,
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
                "symbolType": [
                    "int"
                ],
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
                "symbolType": [
                    "int"
                ],
                "value": "1",
                "kind": "Literal"
            },
            "documentationAttachments": [],
            "kind": "Variable",
            "public": false,
            "native": false,
            "final": false,
            "attached": false,
            "lambda": false,
            "parallel": false,
            "connector": false,
            "deprecated": false,
            "readonly": false,
            "function_final": false,
            "interface": false
        },
        "kind": "VariableDef"
    },
    "createBindStmt": {
        "error": "Index: 0, Size: 0"
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
        "variable": {
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
            "symbolType": [
                "int"
            ],
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
        },
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
            "symbolType": [
                "int"
            ],
            "value": "1",
            "kind": "Literal"
        },
        "declaredWithVar": true,
        "kind": "Assignment"
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
        "returnTypeAnnotationAttachments": [],
        "name": {
            "literal": false,
            "value": "default",
            "kind": "Identifier"
        },
        "annotationAttachments": [],
        "documentationAttachments": [],
        "returnTypeNode": {
            "position": {
                "startColumn": 10,
                "startLine": 1,
                "endColumn": 23,
                "endLine": 1
            },
            "symbolType": [
                "null"
            ],
            "typeKind": "nil",
            "grouped": false,
            "nullable": false,
            "kind": "ValueType"
        },
        "defaultableParameters": [],
        "deprecatedAttachments": [],
        "endpointNodes": [],
        "body": {
            "statements": [],
            "kind": "Block"
        },
        "parameters": [],
        "kind": "Worker",
        "public": false,
        "native": false,
        "final": false,
        "attached": false,
        "lambda": false,
        "parallel": false,
        "connector": false,
        "deprecated": false,
        "readonly": false,
        "function_final": false,
        "interface": false
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
        "returnTypeAnnotationAttachments": [],
        "name": {
            "literal": false,
            "value": "worker1",
            "kind": "Identifier"
        },
        "annotationAttachments": [],
        "documentationAttachments": [],
        "returnTypeNode": {
            "position": {
                "startColumn": 10,
                "startLine": 1,
                "endColumn": 23,
                "endLine": 1
            },
            "symbolType": [
                "null"
            ],
            "typeKind": "nil",
            "grouped": false,
            "nullable": false,
            "kind": "ValueType"
        },
        "defaultableParameters": [],
        "deprecatedAttachments": [],
        "endpointNodes": [],
        "body": {
            "statements": [],
            "kind": "Block"
        },
        "parameters": [],
        "kind": "Worker",
        "public": false,
        "native": false,
        "final": false,
        "attached": false,
        "lambda": false,
        "parallel": false,
        "connector": false,
        "deprecated": false,
        "readonly": false,
        "function_final": false,
        "interface": false
    },
    "createStruct": {
        "ws": [
            {
                "ws": " ",
                "i": 4,
                "text": "struct1",
                "static": false
            }
        ],
        "position": {
            "startColumn": 13,
            "startLine": 4,
            "endColumn": 28,
            "endLine": 2
        },
        "deprecatedAttachments": [],
        "annotationAttachments": [],
        "fields": [],
        "documentationAttachments": [],
        "kind": "Record",
        "name": {
            "literal": false,
            "value": "$anonRecord$15",
            "kind": "Identifier"
        },
        "public": true,
        "native": false,
        "final": false,
        "attached": false,
        "lambda": false,
        "parallel": false,
        "connector": false,
        "deprecated": false,
        "readonly": false,
        "function_final": false,
        "interface": false
    },
    "createFTPResource": {
        "ws": [
            {
                "ws": "\n\n            ",
                "i": 15,
                "text": "echo1",
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
                "i": 23,
                "text": ")",
                "static": true
            },
            {
                "ws": " ",
                "i": 25,
                "text": "{",
                "static": true
            },
            {
                "ws": "\n\n            ",
                "i": 28,
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
        "workers": [],
        "returnTypeAnnotationAttachments": [],
        "name": {
            "literal": false,
            "value": "echo1",
            "kind": "Identifier"
        },
        "annotationAttachments": [],
        "documentationAttachments": [],
        "returnTypeNode": {
            "position": {
                "startColumn": 13,
                "startLine": 3,
                "endColumn": 13,
                "endLine": 5
            },
            "symbolType": [
                "null"
            ],
            "typeKind": "nil",
            "grouped": false,
            "nullable": false,
            "kind": "ValueType"
        },
        "defaultableParameters": [],
        "deprecatedAttachments": [],
        "endpointNodes": [],
        "body": {
            "statements": [],
            "kind": "Block"
        },
        "parameters": [
            {
                "ws": [
                    {
                        "ws": " ",
                        "i": 22,
                        "text": "m",
                        "static": false
                    }
                ],
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
                    "ws": [
                        {
                            "ws": "",
                            "i": 18,
                            "text": "ftp",
                            "static": false
                        },
                        {
                            "ws": "",
                            "i": 19,
                            "text": ":",
                            "static": true
                        },
                        {
                            "ws": "",
                            "i": 20,
                            "text": "FTPServerEvent",
                            "static": false
                        }
                    ],
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
                "final": false,
                "attached": false,
                "lambda": false,
                "parallel": false,
                "connector": false,
                "deprecated": false,
                "readonly": false,
                "function_final": false,
                "interface": false
            }
        ],
        "kind": "Resource",
        "public": true,
        "native": false,
        "final": false,
        "attached": false,
        "lambda": false,
        "parallel": false,
        "connector": false,
        "deprecated": false,
        "readonly": false,
        "function_final": false,
        "interface": false
    },
    "createJMSResource": {
        "ws": [
            {
                "ws": "\n\n            ",
                "i": 15,
                "text": "echo1",
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
                "i": 23,
                "text": ")",
                "static": true
            },
            {
                "ws": " ",
                "i": 25,
                "text": "{",
                "static": true
            },
            {
                "ws": "\n\n            ",
                "i": 28,
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
        "workers": [],
        "returnTypeAnnotationAttachments": [],
        "name": {
            "literal": false,
            "value": "echo1",
            "kind": "Identifier"
        },
        "annotationAttachments": [],
        "documentationAttachments": [],
        "returnTypeNode": {
            "position": {
                "startColumn": 13,
                "startLine": 3,
                "endColumn": 13,
                "endLine": 5
            },
            "symbolType": [
                "null"
            ],
            "typeKind": "nil",
            "grouped": false,
            "nullable": false,
            "kind": "ValueType"
        },
        "defaultableParameters": [],
        "deprecatedAttachments": [],
        "endpointNodes": [],
        "body": {
            "statements": [],
            "kind": "Block"
        },
        "parameters": [
            {
                "ws": [
                    {
                        "ws": " ",
                        "i": 22,
                        "text": "request",
                        "static": false
                    }
                ],
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
                    "ws": [
                        {
                            "ws": "",
                            "i": 18,
                            "text": "jms",
                            "static": false
                        },
                        {
                            "ws": "",
                            "i": 19,
                            "text": ":",
                            "static": true
                        },
                        {
                            "ws": "",
                            "i": 20,
                            "text": "JMSMessage",
                            "static": false
                        }
                    ],
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
                "final": false,
                "attached": false,
                "lambda": false,
                "parallel": false,
                "connector": false,
                "deprecated": false,
                "readonly": false,
                "function_final": false,
                "interface": false
            }
        ],
        "kind": "Resource",
        "public": true,
        "native": false,
        "final": false,
        "attached": false,
        "lambda": false,
        "parallel": false,
        "connector": false,
        "deprecated": false,
        "readonly": false,
        "function_final": false,
        "interface": false
    },
    "createFSResource": {
        "ws": [
            {
                "ws": "\n\n            ",
                "i": 15,
                "text": "echo1",
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
                "i": 23,
                "text": ")",
                "static": true
            },
            {
                "ws": " ",
                "i": 25,
                "text": "{",
                "static": true
            },
            {
                "ws": "\n\n            ",
                "i": 28,
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
        "workers": [],
        "returnTypeAnnotationAttachments": [],
        "name": {
            "literal": false,
            "value": "echo1",
            "kind": "Identifier"
        },
        "annotationAttachments": [],
        "documentationAttachments": [],
        "returnTypeNode": {
            "position": {
                "startColumn": 13,
                "startLine": 3,
                "endColumn": 13,
                "endLine": 5
            },
            "symbolType": [
                "null"
            ],
            "typeKind": "nil",
            "grouped": false,
            "nullable": false,
            "kind": "ValueType"
        },
        "defaultableParameters": [],
        "deprecatedAttachments": [],
        "endpointNodes": [],
        "body": {
            "statements": [],
            "kind": "Block"
        },
        "parameters": [
            {
                "ws": [
                    {
                        "ws": " ",
                        "i": 22,
                        "text": "m",
                        "static": false
                    }
                ],
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
                    "ws": [
                        {
                            "ws": "",
                            "i": 18,
                            "text": "fs",
                            "static": false
                        },
                        {
                            "ws": "",
                            "i": 19,
                            "text": ":",
                            "static": true
                        },
                        {
                            "ws": "",
                            "i": 20,
                            "text": "FileSystemEvent",
                            "static": false
                        }
                    ],
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
                "final": false,
                "attached": false,
                "lambda": false,
                "parallel": false,
                "connector": false,
                "deprecated": false,
                "readonly": false,
                "function_final": false,
                "interface": false
            }
        ],
        "kind": "Resource",
        "public": true,
        "native": false,
        "final": false,
        "attached": false,
        "lambda": false,
        "parallel": false,
        "connector": false,
        "deprecated": false,
        "readonly": false,
        "function_final": false,
        "interface": false
    },
    "createHTTPResource": {
        "ws": [
            {
                "ws": "\n\n            ",
                "i": 15,
                "text": "echo1",
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
                "i": 23,
                "text": ",",
                "static": true
            },
            {
                "ws": "",
                "i": 30,
                "text": ")",
                "static": true
            },
            {
                "ws": " ",
                "i": 32,
                "text": "{",
                "static": true
            },
            {
                "ws": "\n\n            ",
                "i": 35,
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
        "workers": [],
        "returnTypeAnnotationAttachments": [],
        "name": {
            "literal": false,
            "value": "echo1",
            "kind": "Identifier"
        },
        "annotationAttachments": [],
        "documentationAttachments": [],
        "returnTypeNode": {
            "position": {
                "startColumn": 13,
                "startLine": 3,
                "endColumn": 13,
                "endLine": 5
            },
            "symbolType": [
                "null"
            ],
            "typeKind": "nil",
            "grouped": false,
            "nullable": false,
            "kind": "ValueType"
        },
        "defaultableParameters": [],
        "deprecatedAttachments": [],
        "endpointNodes": [],
        "body": {
            "statements": [],
            "kind": "Block"
        },
        "parameters": [
            {
                "ws": [
                    {
                        "ws": " ",
                        "i": 22,
                        "text": "conn",
                        "static": false
                    }
                ],
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
                    "ws": [
                        {
                            "ws": "",
                            "i": 18,
                            "text": "http",
                            "static": false
                        },
                        {
                            "ws": "",
                            "i": 19,
                            "text": ":",
                            "static": true
                        },
                        {
                            "ws": "",
                            "i": 20,
                            "text": "Connection",
                            "static": false
                        }
                    ],
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
                "final": false,
                "attached": false,
                "lambda": false,
                "parallel": false,
                "connector": false,
                "deprecated": false,
                "readonly": false,
                "function_final": false,
                "interface": false
            },
            {
                "ws": [
                    {
                        "ws": " ",
                        "i": 29,
                        "text": "req",
                        "static": false
                    }
                ],
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
                    "ws": [
                        {
                            "ws": " ",
                            "i": 25,
                            "text": "http",
                            "static": false
                        },
                        {
                            "ws": "",
                            "i": 26,
                            "text": ":",
                            "static": true
                        },
                        {
                            "ws": "",
                            "i": 27,
                            "text": "Request",
                            "static": false
                        }
                    ],
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
                "final": false,
                "attached": false,
                "lambda": false,
                "parallel": false,
                "connector": false,
                "deprecated": false,
                "readonly": false,
                "function_final": false,
                "interface": false
            }
        ],
        "kind": "Resource",
        "public": true,
        "native": false,
        "final": false,
        "attached": false,
        "lambda": false,
        "parallel": false,
        "connector": false,
        "deprecated": false,
        "readonly": false,
        "function_final": false,
        "interface": false
    },
    "createFunction": {
        "ws": [
            {
                "ws": "\n            ",
                "i": 2,
                "text": "function",
                "static": true
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
                "ws": "\n\n            ",
                "i": 11,
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
        "returnTypeAnnotationAttachments": [],
        "name": {
            "literal": false,
            "value": "function1",
            "kind": "Identifier"
        },
        "annotationAttachments": [],
        "documentationAttachments": [],
        "returnTypeNode": {
            "position": {
                "startColumn": 22,
                "startLine": 2,
                "endColumn": 32,
                "endLine": 2
            },
            "symbolType": [
                "null"
            ],
            "typeKind": "nil",
            "grouped": false,
            "nullable": false,
            "kind": "ValueType"
        },
        "defaultableParameters": [],
        "deprecatedAttachments": [],
        "endpointNodes": [],
        "body": {
            "statements": [],
            "kind": "Block"
        },
        "parameters": [],
        "kind": "Function",
        "public": false,
        "native": false,
        "final": false,
        "attached": false,
        "lambda": false,
        "parallel": false,
        "connector": false,
        "deprecated": false,
        "readonly": false,
        "function_final": false,
        "interface": false
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
        "returnTypeAnnotationAttachments": [],
        "name": {
            "literal": false,
            "value": "main",
            "kind": "Identifier"
        },
        "annotationAttachments": [],
        "documentationAttachments": [],
        "returnTypeNode": {
            "position": {
                "startColumn": 22,
                "startLine": 2,
                "endColumn": 40,
                "endLine": 2
            },
            "symbolType": [
                "null"
            ],
            "typeKind": "nil",
            "grouped": false,
            "nullable": false,
            "kind": "ValueType"
        },
        "defaultableParameters": [],
        "deprecatedAttachments": [],
        "endpointNodes": [],
        "body": {
            "statements": [],
            "kind": "Block"
        },
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
                "safeAssignment": false,
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
                "final": false,
                "attached": false,
                "lambda": false,
                "parallel": false,
                "connector": false,
                "deprecated": false,
                "readonly": false,
                "function_final": false,
                "interface": false
            }
        ],
        "kind": "Function",
        "public": false,
        "native": false,
        "final": false,
        "attached": false,
        "lambda": false,
        "parallel": false,
        "connector": false,
        "deprecated": false,
        "readonly": false,
        "function_final": false,
        "interface": false
    },
    "createWSEndpointDef": {
        "ws": [
            {
                "ws": "\n            ",
                "i": 2,
                "text": "endpoint",
                "static": true
            },
            {
                "ws": " ",
                "i": 8,
                "text": "wsEnpointName",
                "static": false
            },
            {
                "ws": "",
                "i": 19,
                "text": ";",
                "static": true
            }
        ],
        "position": {
            "startColumn": 13,
            "startLine": 2,
            "endColumn": 14,
            "endLine": 4
        },
        "symbolType": [
            "other"
        ],
        "configurationExpression": {
            "ws": [
                {
                    "ws": " ",
                    "i": 10,
                    "text": "{",
                    "static": true
                },
                {
                    "ws": "\n            ",
                    "i": 18,
                    "text": "}",
                    "static": true
                }
            ],
            "position": {
                "startColumn": 50,
                "startLine": 2,
                "endColumn": 13,
                "endLine": 4
            },
            "symbolType": [
                "other"
            ],
            "keyValuePairs": [
                {
                    "ws": [
                        {
                            "ws": "",
                            "i": 14,
                            "text": ":",
                            "static": true
                        }
                    ],
                    "value": {
                        "ws": [
                            {
                                "ws": "",
                                "i": 15,
                                "text": "9090",
                                "static": false
                            }
                        ],
                        "position": {
                            "startColumn": 22,
                            "startLine": 3,
                            "endColumn": 22,
                            "endLine": 3
                        },
                        "value": "9090",
                        "kind": "Literal"
                    },
                    "key": {
                        "ws": [
                            {
                                "ws": "\n                ",
                                "i": 13,
                                "text": "port",
                                "static": false
                            }
                        ],
                        "position": {
                            "startColumn": 17,
                            "startLine": 3,
                            "endColumn": 17,
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
            "value": "wsEnpointName",
            "kind": "Identifier"
        },
        "annotationAttachments": [],
        "kind": "Endpoint",
        "endPointType": {
            "ws": [
                {
                    "ws": " ",
                    "i": 4,
                    "text": "http",
                    "static": false
                },
                {
                    "ws": "",
                    "i": 5,
                    "text": ":",
                    "static": true
                },
                {
                    "ws": "",
                    "i": 6,
                    "text": "Listener",
                    "static": false
                }
            ],
            "position": {
                "startColumn": 22,
                "startLine": 2,
                "endColumn": 27,
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
                "value": "Listener",
                "kind": "Identifier"
            },
            "nullable": false,
            "kind": "UserDefinedType"
        },
        "public": false,
        "native": false,
        "final": false,
        "attached": false,
        "lambda": false,
        "parallel": false,
        "connector": false,
        "deprecated": false,
        "readonly": false,
        "function_final": false,
        "interface": false
    },
    "createFTPServiceDef": {
        "ws": [
            {
                "ws": "\n            ",
                "i": 2,
                "text": "service",
                "static": true
            },
            {
                "ws": "",
                "i": 3,
                "text": "<",
                "static": true
            },
            {
                "ws": "",
                "i": 5,
                "text": ">",
                "static": true
            },
            {
                "ws": " ",
                "i": 7,
                "text": "service1",
                "static": false
            },
            {
                "ws": " ",
                "i": 9,
                "text": "{",
                "static": true
            },
            {
                "ws": "\n            ",
                "i": 28,
                "text": "}",
                "static": true
            }
        ],
        "position": {
            "startColumn": 13,
            "startLine": 2,
            "endColumn": 13,
            "endLine": 6
        },
        "annotationAttachments": [],
        "serviceTypeStruct": {
            "ws": [
                {
                    "ws": "",
                    "i": 4,
                    "text": "ftp",
                    "static": false
                }
            ],
            "position": {
                "startColumn": 13,
                "startLine": 2,
                "endColumn": 13,
                "endLine": 6
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
            "workers": [],
            "returnTypeAnnotationAttachments": [],
            "name": {
                "literal": false,
                "value": "service1.<init>",
                "kind": "Identifier"
            },
            "annotationAttachments": [],
            "documentationAttachments": [],
            "returnTypeNode": {
                "position": {
                    "startColumn": 13,
                    "startLine": 2,
                    "endColumn": 13,
                    "endLine": 6
                },
                "symbolType": [
                    "null"
                ],
                "typeKind": "nil",
                "grouped": false,
                "nullable": false,
                "kind": "ValueType"
            },
            "defaultableParameters": [],
            "deprecatedAttachments": [],
            "endpointNodes": [],
            "body": {
                "position": {
                    "startColumn": 13,
                    "startLine": 2,
                    "endColumn": 13,
                    "endLine": 6
                },
                "statements": [],
                "kind": "Block"
            },
            "parameters": [],
            "kind": "Function",
            "public": true,
            "native": false,
            "final": false,
            "attached": false,
            "lambda": false,
            "parallel": false,
            "connector": false,
            "deprecated": false,
            "readonly": false,
            "function_final": false,
            "interface": false
        },
        "endpointNodes": [],
        "documentationAttachments": [],
        "boundEndpoints": [],
        "resources": [
            {
                "ws": [
                    {
                        "ws": "\n                ",
                        "i": 12,
                        "text": "echo1",
                        "static": false
                    },
                    {
                        "ws": " ",
                        "i": 14,
                        "text": "(",
                        "static": true
                    },
                    {
                        "ws": "",
                        "i": 20,
                        "text": ")",
                        "static": true
                    },
                    {
                        "ws": " ",
                        "i": 22,
                        "text": "{",
                        "static": true
                    },
                    {
                        "ws": "\n\n                ",
                        "i": 25,
                        "text": "}",
                        "static": true
                    }
                ],
                "position": {
                    "startColumn": 17,
                    "startLine": 3,
                    "endColumn": 17,
                    "endLine": 5
                },
                "workers": [],
                "returnTypeAnnotationAttachments": [],
                "name": {
                    "literal": false,
                    "value": "echo1",
                    "kind": "Identifier"
                },
                "annotationAttachments": [],
                "documentationAttachments": [],
                "returnTypeNode": {
                    "position": {
                        "startColumn": 17,
                        "startLine": 3,
                        "endColumn": 17,
                        "endLine": 5
                    },
                    "symbolType": [
                        "null"
                    ],
                    "typeKind": "nil",
                    "grouped": false,
                    "nullable": false,
                    "kind": "ValueType"
                },
                "defaultableParameters": [],
                "deprecatedAttachments": [],
                "endpointNodes": [],
                "body": {
                    "statements": [],
                    "kind": "Block"
                },
                "parameters": [
                    {
                        "ws": [
                            {
                                "ws": " ",
                                "i": 19,
                                "text": "m",
                                "static": false
                            }
                        ],
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
                            "ws": [
                                {
                                    "ws": "",
                                    "i": 15,
                                    "text": "ftp",
                                    "static": false
                                },
                                {
                                    "ws": "",
                                    "i": 16,
                                    "text": ":",
                                    "static": true
                                },
                                {
                                    "ws": "",
                                    "i": 17,
                                    "text": "FTPServerEvent",
                                    "static": false
                                }
                            ],
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
                        "final": false,
                        "attached": false,
                        "lambda": false,
                        "parallel": false,
                        "connector": false,
                        "deprecated": false,
                        "readonly": false,
                        "function_final": false,
                        "interface": false
                    }
                ],
                "kind": "Resource",
                "public": true,
                "native": false,
                "final": false,
                "attached": false,
                "lambda": false,
                "parallel": false,
                "connector": false,
                "deprecated": false,
                "readonly": false,
                "function_final": false,
                "interface": false
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
    "createWSServiceDef": {
        "ws": [
            {
                "ws": "\n            ",
                "i": 35,
                "text": "service",
                "static": true
            },
            {
                "ws": "",
                "i": 36,
                "text": "<",
                "static": true
            },
            {
                "ws": "",
                "i": 40,
                "text": ">",
                "static": true
            },
            {
                "ws": " ",
                "i": 42,
                "text": "WSServer",
                "static": false
            },
            {
                "ws": " ",
                "i": 44,
                "text": "bind",
                "static": true
            },
            {
                "ws": " ",
                "i": 48,
                "text": "{",
                "static": true
            },
            {
                "ws": "\n            ",
                "i": 119,
                "text": "}",
                "static": true
            }
        ],
        "position": {
            "startColumn": 13,
            "startLine": 7,
            "endColumn": 13,
            "endLine": 20
        },
        "annotationAttachments": [
            {
                "ws": [
                    {
                        "ws": "\n            ",
                        "i": 2,
                        "text": "@",
                        "static": true
                    },
                    {
                        "ws": "",
                        "i": 3,
                        "text": "http",
                        "static": false
                    },
                    {
                        "ws": "",
                        "i": 4,
                        "text": ":",
                        "static": true
                    },
                    {
                        "ws": "",
                        "i": 5,
                        "text": "WebSocketServiceConfig",
                        "static": false
                    }
                ],
                "position": {
                    "startColumn": 13,
                    "startLine": 2,
                    "endColumn": 13,
                    "endLine": 6
                },
                "expression": {
                    "ws": [
                        {
                            "ws": " ",
                            "i": 7,
                            "text": "{",
                            "static": true
                        },
                        {
                            "ws": "",
                            "i": 13,
                            "text": ",",
                            "static": true
                        },
                        {
                            "ws": "",
                            "i": 24,
                            "text": ",",
                            "static": true
                        },
                        {
                            "ws": "\n            ",
                            "i": 32,
                            "text": "}",
                            "static": true
                        }
                    ],
                    "position": {
                        "startColumn": 42,
                        "startLine": 2,
                        "endColumn": 13,
                        "endLine": 6
                    },
                    "keyValuePairs": [
                        {
                            "ws": [
                                {
                                    "ws": "",
                                    "i": 11,
                                    "text": ":",
                                    "static": true
                                }
                            ],
                            "value": {
                                "ws": [
                                    {
                                        "ws": "",
                                        "i": 12,
                                        "text": "\"/basic/ws\"",
                                        "static": false
                                    }
                                ],
                                "position": {
                                    "startColumn": 26,
                                    "startLine": 3,
                                    "endColumn": 26,
                                    "endLine": 3
                                },
                                "value": "\"/basic/ws\"",
                                "unescapedValue": "/basic/ws",
                                "kind": "Literal"
                            },
                            "key": {
                                "ws": [
                                    {
                                        "ws": "\n                ",
                                        "i": 10,
                                        "text": "basePath",
                                        "static": false
                                    }
                                ],
                                "position": {
                                    "startColumn": 17,
                                    "startLine": 3,
                                    "endColumn": 17,
                                    "endLine": 3
                                },
                                "variableName": {
                                    "literal": false,
                                    "value": "basePath",
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
                        },
                        {
                            "ws": [
                                {
                                    "ws": "",
                                    "i": 17,
                                    "text": ":",
                                    "static": true
                                }
                            ],
                            "value": {
                                "ws": [
                                    {
                                        "ws": "",
                                        "i": 18,
                                        "text": "[",
                                        "static": true
                                    },
                                    {
                                        "ws": "",
                                        "i": 20,
                                        "text": ",",
                                        "static": true
                                    },
                                    {
                                        "ws": "",
                                        "i": 23,
                                        "text": "]",
                                        "static": true
                                    }
                                ],
                                "position": {
                                    "startColumn": 30,
                                    "startLine": 4,
                                    "endColumn": 44,
                                    "endLine": 4
                                },
                                "expressions": [
                                    {
                                        "ws": [
                                            {
                                                "ws": "",
                                                "i": 19,
                                                "text": "\"xml\"",
                                                "static": false
                                            }
                                        ],
                                        "position": {
                                            "startColumn": 31,
                                            "startLine": 4,
                                            "endColumn": 31,
                                            "endLine": 4
                                        },
                                        "value": "\"xml\"",
                                        "unescapedValue": "xml",
                                        "kind": "Literal"
                                    },
                                    {
                                        "ws": [
                                            {
                                                "ws": " ",
                                                "i": 22,
                                                "text": "\"json\"",
                                                "static": false
                                            }
                                        ],
                                        "position": {
                                            "startColumn": 38,
                                            "startLine": 4,
                                            "endColumn": 38,
                                            "endLine": 4
                                        },
                                        "value": "\"json\"",
                                        "unescapedValue": "json",
                                        "kind": "Literal"
                                    }
                                ],
                                "kind": "ArrayLiteralExpr"
                            },
                            "key": {
                                "ws": [
                                    {
                                        "ws": "\n                ",
                                        "i": 16,
                                        "text": "subProtocols",
                                        "static": false
                                    }
                                ],
                                "position": {
                                    "startColumn": 17,
                                    "startLine": 4,
                                    "endColumn": 17,
                                    "endLine": 4
                                },
                                "variableName": {
                                    "literal": false,
                                    "value": "subProtocols",
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
                        },
                        {
                            "ws": [
                                {
                                    "ws": "",
                                    "i": 28,
                                    "text": ":",
                                    "static": true
                                }
                            ],
                            "value": {
                                "ws": [
                                    {
                                        "ws": "",
                                        "i": 29,
                                        "text": "120",
                                        "static": false
                                    }
                                ],
                                "position": {
                                    "startColumn": 38,
                                    "startLine": 5,
                                    "endColumn": 38,
                                    "endLine": 5
                                },
                                "value": "120",
                                "kind": "Literal"
                            },
                            "key": {
                                "ws": [
                                    {
                                        "ws": "\n                ",
                                        "i": 27,
                                        "text": "idleTimeoutInSeconds",
                                        "static": false
                                    }
                                ],
                                "position": {
                                    "startColumn": 17,
                                    "startLine": 5,
                                    "endColumn": 17,
                                    "endLine": 5
                                },
                                "variableName": {
                                    "literal": false,
                                    "value": "idleTimeoutInSeconds",
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
                "attributes": [],
                "annotationName": {
                    "literal": false,
                    "value": "WebSocketServiceConfig",
                    "kind": "Identifier"
                },
                "packageAlias": {
                    "literal": false,
                    "value": "http",
                    "kind": "Identifier"
                },
                "kind": "AnnotationAttachment"
            }
        ],
        "serviceTypeStruct": {
            "ws": [
                {
                    "ws": "",
                    "i": 37,
                    "text": "http",
                    "static": false
                },
                {
                    "ws": "",
                    "i": 38,
                    "text": ":",
                    "static": true
                },
                {
                    "ws": "",
                    "i": 39,
                    "text": "WebSocketService",
                    "static": false
                }
            ],
            "position": {
                "startColumn": 13,
                "startLine": 7,
                "endColumn": 13,
                "endLine": 20
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
                "value": "WebSocketService",
                "kind": "Identifier"
            },
            "nullable": false,
            "kind": "UserDefinedType"
        },
        "initFunction": {
            "position": {
                "startColumn": 13,
                "startLine": 7,
                "endColumn": 13,
                "endLine": 20
            },
            "workers": [],
            "returnTypeAnnotationAttachments": [],
            "name": {
                "literal": false,
                "value": "WSServer.<init>",
                "kind": "Identifier"
            },
            "annotationAttachments": [],
            "documentationAttachments": [],
            "returnTypeNode": {
                "position": {
                    "startColumn": 13,
                    "startLine": 7,
                    "endColumn": 13,
                    "endLine": 20
                },
                "symbolType": [
                    "null"
                ],
                "typeKind": "nil",
                "grouped": false,
                "nullable": false,
                "kind": "ValueType"
            },
            "defaultableParameters": [],
            "deprecatedAttachments": [],
            "endpointNodes": [],
            "body": {
                "position": {
                    "startColumn": 13,
                    "startLine": 7,
                    "endColumn": 13,
                    "endLine": 20
                },
                "statements": [],
                "kind": "Block"
            },
            "parameters": [],
            "kind": "Function",
            "public": true,
            "native": false,
            "final": false,
            "attached": false,
            "lambda": false,
            "parallel": false,
            "connector": false,
            "deprecated": false,
            "readonly": false,
            "function_final": false,
            "interface": false
        },
        "endpointNodes": [],
        "documentationAttachments": [],
        "boundEndpoints": [
            {
                "ws": [
                    {
                        "ws": " ",
                        "i": 46,
                        "text": "wsEnpointName",
                        "static": false
                    }
                ],
                "position": {
                    "startColumn": 58,
                    "startLine": 7,
                    "endColumn": 58,
                    "endLine": 7
                },
                "symbolType": [
                    "other"
                ],
                "variableName": {
                    "literal": false,
                    "value": "wsEnpointName",
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
                "ws": [
                    {
                        "ws": "\n            \n                ",
                        "i": 53,
                        "text": "onOpen",
                        "static": false
                    },
                    {
                        "ws": " ",
                        "i": 55,
                        "text": "(",
                        "static": true
                    },
                    {
                        "ws": " ",
                        "i": 58,
                        "text": "conn",
                        "static": false
                    },
                    {
                        "ws": "",
                        "i": 59,
                        "text": ")",
                        "static": true
                    },
                    {
                        "ws": " ",
                        "i": 61,
                        "text": "{",
                        "static": true
                    },
                    {
                        "ws": "\n\n                ",
                        "i": 64,
                        "text": "}",
                        "static": true
                    }
                ],
                "position": {
                    "startColumn": 17,
                    "startLine": 9,
                    "endColumn": 17,
                    "endLine": 11
                },
                "workers": [],
                "returnTypeAnnotationAttachments": [],
                "name": {
                    "literal": false,
                    "value": "onOpen",
                    "kind": "Identifier"
                },
                "annotationAttachments": [],
                "documentationAttachments": [],
                "returnTypeNode": {
                    "position": {
                        "startColumn": 17,
                        "startLine": 9,
                        "endColumn": 17,
                        "endLine": 11
                    },
                    "symbolType": [
                        "null"
                    ],
                    "typeKind": "nil",
                    "grouped": false,
                    "nullable": false,
                    "kind": "ValueType"
                },
                "defaultableParameters": [],
                "deprecatedAttachments": [],
                "endpointNodes": [],
                "body": {
                    "statements": [],
                    "kind": "Block"
                },
                "parameters": [
                    {
                        "ws": [
                            {
                                "ws": "",
                                "i": 56,
                                "text": "endpoint",
                                "static": true
                            }
                        ],
                        "position": {
                            "startColumn": 25,
                            "startLine": 9,
                            "endColumn": 34,
                            "endLine": 9
                        },
                        "symbolType": [
                            "other"
                        ],
                        "safeAssignment": false,
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
                        "final": false,
                        "attached": false,
                        "lambda": false,
                        "parallel": false,
                        "connector": false,
                        "deprecated": false,
                        "readonly": false,
                        "function_final": false,
                        "interface": false
                    }
                ],
                "kind": "Resource",
                "public": true,
                "native": false,
                "final": false,
                "attached": false,
                "lambda": false,
                "parallel": false,
                "connector": false,
                "deprecated": false,
                "readonly": false,
                "function_final": false,
                "interface": false
            },
            {
                "ws": [
                    {
                        "ws": "\n            \n\n                ",
                        "i": 69,
                        "text": "onText",
                        "static": false
                    },
                    {
                        "ws": " ",
                        "i": 71,
                        "text": "(",
                        "static": true
                    },
                    {
                        "ws": "",
                        "i": 75,
                        "text": ",",
                        "static": true
                    },
                    {
                        "ws": "",
                        "i": 80,
                        "text": ",",
                        "static": true
                    },
                    {
                        "ws": "",
                        "i": 85,
                        "text": ")",
                        "static": true
                    },
                    {
                        "ws": " ",
                        "i": 87,
                        "text": "{",
                        "static": true
                    },
                    {
                        "ws": "\n\n                ",
                        "i": 90,
                        "text": "}",
                        "static": true
                    }
                ],
                "position": {
                    "startColumn": 17,
                    "startLine": 14,
                    "endColumn": 17,
                    "endLine": 16
                },
                "workers": [],
                "returnTypeAnnotationAttachments": [],
                "name": {
                    "literal": false,
                    "value": "onText",
                    "kind": "Identifier"
                },
                "annotationAttachments": [],
                "documentationAttachments": [],
                "returnTypeNode": {
                    "position": {
                        "startColumn": 17,
                        "startLine": 14,
                        "endColumn": 17,
                        "endLine": 16
                    },
                    "symbolType": [
                        "null"
                    ],
                    "typeKind": "nil",
                    "grouped": false,
                    "nullable": false,
                    "kind": "ValueType"
                },
                "defaultableParameters": [],
                "deprecatedAttachments": [],
                "endpointNodes": [],
                "body": {
                    "statements": [],
                    "kind": "Block"
                },
                "parameters": [
                    {
                        "ws": [
                            {
                                "ws": "",
                                "i": 72,
                                "text": "endpoint",
                                "static": true
                            },
                            {
                                "ws": " ",
                                "i": 74,
                                "text": "conn",
                                "static": false
                            }
                        ],
                        "position": {
                            "startColumn": 25,
                            "startLine": 14,
                            "endColumn": 61,
                            "endLine": 14
                        },
                        "symbolType": [
                            "other"
                        ],
                        "safeAssignment": false,
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
                        "final": false,
                        "attached": false,
                        "lambda": false,
                        "parallel": false,
                        "connector": false,
                        "deprecated": false,
                        "readonly": false,
                        "function_final": false,
                        "interface": false
                    },
                    {
                        "ws": [
                            {
                                "ws": " ",
                                "i": 79,
                                "text": "text",
                                "static": false
                            }
                        ],
                        "position": {
                            "startColumn": 40,
                            "startLine": 14,
                            "endColumn": 47,
                            "endLine": 14
                        },
                        "symbolType": [
                            "string"
                        ],
                        "safeAssignment": false,
                        "typeNode": {
                            "ws": [
                                {
                                    "ws": " ",
                                    "i": 77,
                                    "text": "string",
                                    "static": true
                                }
                            ],
                            "position": {
                                "startColumn": 40,
                                "startLine": 14,
                                "endColumn": 40,
                                "endLine": 14
                            },
                            "symbolType": [
                                "string"
                            ],
                            "typeKind": "string",
                            "grouped": false,
                            "nullable": false,
                            "kind": "ValueType"
                        },
                        "deprecatedAttachments": [],
                        "annotationAttachments": [],
                        "name": {
                            "literal": false,
                            "value": "text",
                            "kind": "Identifier"
                        },
                        "documentationAttachments": [],
                        "kind": "Variable",
                        "public": false,
                        "native": false,
                        "final": false,
                        "attached": false,
                        "lambda": false,
                        "parallel": false,
                        "connector": false,
                        "deprecated": false,
                        "readonly": false,
                        "function_final": false,
                        "interface": false
                    },
                    {
                        "ws": [
                            {
                                "ws": " ",
                                "i": 84,
                                "text": "more",
                                "static": false
                            }
                        ],
                        "position": {
                            "startColumn": 53,
                            "startLine": 14,
                            "endColumn": 61,
                            "endLine": 14
                        },
                        "symbolType": [
                            "boolean"
                        ],
                        "safeAssignment": false,
                        "typeNode": {
                            "ws": [
                                {
                                    "ws": " ",
                                    "i": 82,
                                    "text": "boolean",
                                    "static": true
                                }
                            ],
                            "position": {
                                "startColumn": 53,
                                "startLine": 14,
                                "endColumn": 53,
                                "endLine": 14
                            },
                            "symbolType": [
                                "boolean"
                            ],
                            "typeKind": "boolean",
                            "grouped": false,
                            "nullable": false,
                            "kind": "ValueType"
                        },
                        "deprecatedAttachments": [],
                        "annotationAttachments": [],
                        "name": {
                            "literal": false,
                            "value": "more",
                            "kind": "Identifier"
                        },
                        "documentationAttachments": [],
                        "kind": "Variable",
                        "public": false,
                        "native": false,
                        "final": false,
                        "attached": false,
                        "lambda": false,
                        "parallel": false,
                        "connector": false,
                        "deprecated": false,
                        "readonly": false,
                        "function_final": false,
                        "interface": false
                    }
                ],
                "kind": "Resource",
                "public": true,
                "native": false,
                "final": false,
                "attached": false,
                "lambda": false,
                "parallel": false,
                "connector": false,
                "deprecated": false,
                "readonly": false,
                "function_final": false,
                "interface": false
            },
            {
                "ws": [
                    {
                        "ws": "\n            \n                ",
                        "i": 95,
                        "text": "onClose",
                        "static": false
                    },
                    {
                        "ws": " ",
                        "i": 97,
                        "text": "(",
                        "static": true
                    },
                    {
                        "ws": "",
                        "i": 101,
                        "text": ",",
                        "static": true
                    },
                    {
                        "ws": "",
                        "i": 106,
                        "text": ",",
                        "static": true
                    },
                    {
                        "ws": "",
                        "i": 111,
                        "text": ")",
                        "static": true
                    },
                    {
                        "ws": " ",
                        "i": 113,
                        "text": "{",
                        "static": true
                    },
                    {
                        "ws": "\n                ",
                        "i": 116,
                        "text": "}",
                        "static": true
                    }
                ],
                "position": {
                    "startColumn": 17,
                    "startLine": 18,
                    "endColumn": 17,
                    "endLine": 19
                },
                "workers": [],
                "returnTypeAnnotationAttachments": [],
                "name": {
                    "literal": false,
                    "value": "onClose",
                    "kind": "Identifier"
                },
                "annotationAttachments": [],
                "documentationAttachments": [],
                "returnTypeNode": {
                    "position": {
                        "startColumn": 17,
                        "startLine": 18,
                        "endColumn": 17,
                        "endLine": 19
                    },
                    "symbolType": [
                        "null"
                    ],
                    "typeKind": "nil",
                    "grouped": false,
                    "nullable": false,
                    "kind": "ValueType"
                },
                "defaultableParameters": [],
                "deprecatedAttachments": [],
                "endpointNodes": [],
                "body": {
                    "statements": [],
                    "kind": "Block"
                },
                "parameters": [
                    {
                        "ws": [
                            {
                                "ws": "",
                                "i": 98,
                                "text": "endpoint",
                                "static": true
                            },
                            {
                                "ws": " ",
                                "i": 100,
                                "text": "conn",
                                "static": false
                            }
                        ],
                        "position": {
                            "startColumn": 26,
                            "startLine": 18,
                            "endColumn": 64,
                            "endLine": 18
                        },
                        "symbolType": [
                            "other"
                        ],
                        "safeAssignment": false,
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
                        "final": false,
                        "attached": false,
                        "lambda": false,
                        "parallel": false,
                        "connector": false,
                        "deprecated": false,
                        "readonly": false,
                        "function_final": false,
                        "interface": false
                    },
                    {
                        "ws": [
                            {
                                "ws": " ",
                                "i": 105,
                                "text": "statusCode",
                                "static": false
                            }
                        ],
                        "position": {
                            "startColumn": 41,
                            "startLine": 18,
                            "endColumn": 45,
                            "endLine": 18
                        },
                        "symbolType": [
                            "int"
                        ],
                        "safeAssignment": false,
                        "typeNode": {
                            "ws": [
                                {
                                    "ws": " ",
                                    "i": 103,
                                    "text": "int",
                                    "static": true
                                }
                            ],
                            "position": {
                                "startColumn": 41,
                                "startLine": 18,
                                "endColumn": 41,
                                "endLine": 18
                            },
                            "symbolType": [
                                "int"
                            ],
                            "typeKind": "int",
                            "grouped": false,
                            "nullable": false,
                            "kind": "ValueType"
                        },
                        "deprecatedAttachments": [],
                        "annotationAttachments": [],
                        "name": {
                            "literal": false,
                            "value": "statusCode",
                            "kind": "Identifier"
                        },
                        "documentationAttachments": [],
                        "kind": "Variable",
                        "public": false,
                        "native": false,
                        "final": false,
                        "attached": false,
                        "lambda": false,
                        "parallel": false,
                        "connector": false,
                        "deprecated": false,
                        "readonly": false,
                        "function_final": false,
                        "interface": false
                    },
                    {
                        "ws": [
                            {
                                "ws": " ",
                                "i": 110,
                                "text": "reason",
                                "static": false
                            }
                        ],
                        "position": {
                            "startColumn": 57,
                            "startLine": 18,
                            "endColumn": 64,
                            "endLine": 18
                        },
                        "symbolType": [
                            "string"
                        ],
                        "safeAssignment": false,
                        "typeNode": {
                            "ws": [
                                {
                                    "ws": " ",
                                    "i": 108,
                                    "text": "string",
                                    "static": true
                                }
                            ],
                            "position": {
                                "startColumn": 57,
                                "startLine": 18,
                                "endColumn": 57,
                                "endLine": 18
                            },
                            "symbolType": [
                                "string"
                            ],
                            "typeKind": "string",
                            "grouped": false,
                            "nullable": false,
                            "kind": "ValueType"
                        },
                        "deprecatedAttachments": [],
                        "annotationAttachments": [],
                        "name": {
                            "literal": false,
                            "value": "reason",
                            "kind": "Identifier"
                        },
                        "documentationAttachments": [],
                        "kind": "Variable",
                        "public": false,
                        "native": false,
                        "final": false,
                        "attached": false,
                        "lambda": false,
                        "parallel": false,
                        "connector": false,
                        "deprecated": false,
                        "readonly": false,
                        "function_final": false,
                        "interface": false
                    }
                ],
                "kind": "Resource",
                "public": true,
                "native": false,
                "final": false,
                "attached": false,
                "lambda": false,
                "parallel": false,
                "connector": false,
                "deprecated": false,
                "readonly": false,
                "function_final": false,
                "interface": false
            }
        ],
        "deprecatedAttachments": [],
        "variables": [],
        "name": {
            "literal": false,
            "value": "WSServer",
            "kind": "Identifier"
        },
        "kind": "Service"
    },
    "createFSServiceDef": {
        "ws": [
            {
                "ws": "\n            ",
                "i": 2,
                "text": "service",
                "static": true
            },
            {
                "ws": "",
                "i": 3,
                "text": "<",
                "static": true
            },
            {
                "ws": "",
                "i": 5,
                "text": ">",
                "static": true
            },
            {
                "ws": " ",
                "i": 7,
                "text": "service1",
                "static": false
            },
            {
                "ws": " ",
                "i": 9,
                "text": "{",
                "static": true
            },
            {
                "ws": "\n            ",
                "i": 28,
                "text": "}",
                "static": true
            }
        ],
        "position": {
            "startColumn": 13,
            "startLine": 2,
            "endColumn": 13,
            "endLine": 6
        },
        "annotationAttachments": [],
        "serviceTypeStruct": {
            "ws": [
                {
                    "ws": "",
                    "i": 4,
                    "text": "fs",
                    "static": false
                }
            ],
            "position": {
                "startColumn": 13,
                "startLine": 2,
                "endColumn": 13,
                "endLine": 6
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
            "workers": [],
            "returnTypeAnnotationAttachments": [],
            "name": {
                "literal": false,
                "value": "service1.<init>",
                "kind": "Identifier"
            },
            "annotationAttachments": [],
            "documentationAttachments": [],
            "returnTypeNode": {
                "position": {
                    "startColumn": 13,
                    "startLine": 2,
                    "endColumn": 13,
                    "endLine": 6
                },
                "symbolType": [
                    "null"
                ],
                "typeKind": "nil",
                "grouped": false,
                "nullable": false,
                "kind": "ValueType"
            },
            "defaultableParameters": [],
            "deprecatedAttachments": [],
            "endpointNodes": [],
            "body": {
                "position": {
                    "startColumn": 13,
                    "startLine": 2,
                    "endColumn": 13,
                    "endLine": 6
                },
                "statements": [],
                "kind": "Block"
            },
            "parameters": [],
            "kind": "Function",
            "public": true,
            "native": false,
            "final": false,
            "attached": false,
            "lambda": false,
            "parallel": false,
            "connector": false,
            "deprecated": false,
            "readonly": false,
            "function_final": false,
            "interface": false
        },
        "endpointNodes": [],
        "documentationAttachments": [],
        "boundEndpoints": [],
        "resources": [
            {
                "ws": [
                    {
                        "ws": "\n                ",
                        "i": 12,
                        "text": "echo1",
                        "static": false
                    },
                    {
                        "ws": " ",
                        "i": 14,
                        "text": "(",
                        "static": true
                    },
                    {
                        "ws": "",
                        "i": 20,
                        "text": ")",
                        "static": true
                    },
                    {
                        "ws": " ",
                        "i": 22,
                        "text": "{",
                        "static": true
                    },
                    {
                        "ws": "\n\n                ",
                        "i": 25,
                        "text": "}",
                        "static": true
                    }
                ],
                "position": {
                    "startColumn": 17,
                    "startLine": 3,
                    "endColumn": 17,
                    "endLine": 5
                },
                "workers": [],
                "returnTypeAnnotationAttachments": [],
                "name": {
                    "literal": false,
                    "value": "echo1",
                    "kind": "Identifier"
                },
                "annotationAttachments": [],
                "documentationAttachments": [],
                "returnTypeNode": {
                    "position": {
                        "startColumn": 17,
                        "startLine": 3,
                        "endColumn": 17,
                        "endLine": 5
                    },
                    "symbolType": [
                        "null"
                    ],
                    "typeKind": "nil",
                    "grouped": false,
                    "nullable": false,
                    "kind": "ValueType"
                },
                "defaultableParameters": [],
                "deprecatedAttachments": [],
                "endpointNodes": [],
                "body": {
                    "statements": [],
                    "kind": "Block"
                },
                "parameters": [
                    {
                        "ws": [
                            {
                                "ws": " ",
                                "i": 19,
                                "text": "m",
                                "static": false
                            }
                        ],
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
                            "ws": [
                                {
                                    "ws": "",
                                    "i": 15,
                                    "text": "fs",
                                    "static": false
                                },
                                {
                                    "ws": "",
                                    "i": 16,
                                    "text": ":",
                                    "static": true
                                },
                                {
                                    "ws": "",
                                    "i": 17,
                                    "text": "FileSystemEvent",
                                    "static": false
                                }
                            ],
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
                        "final": false,
                        "attached": false,
                        "lambda": false,
                        "parallel": false,
                        "connector": false,
                        "deprecated": false,
                        "readonly": false,
                        "function_final": false,
                        "interface": false
                    }
                ],
                "kind": "Resource",
                "public": true,
                "native": false,
                "final": false,
                "attached": false,
                "lambda": false,
                "parallel": false,
                "connector": false,
                "deprecated": false,
                "readonly": false,
                "function_final": false,
                "interface": false
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
    "createHTTPServiceDef": {
        "ws": [
            {
                "ws": "\n            ",
                "i": 2,
                "text": "service",
                "static": true
            },
            {
                "ws": "",
                "i": 3,
                "text": "<",
                "static": true
            },
            {
                "ws": "",
                "i": 7,
                "text": ">",
                "static": true
            },
            {
                "ws": " ",
                "i": 9,
                "text": "serviceName",
                "static": false
            },
            {
                "ws": " ",
                "i": 11,
                "text": "bind",
                "static": true
            },
            {
                "ws": " ",
                "i": 15,
                "text": "{",
                "static": true
            },
            {
                "ws": "\n            ",
                "i": 67,
                "text": "}",
                "static": true
            }
        ],
        "position": {
            "startColumn": 13,
            "startLine": 2,
            "endColumn": 13,
            "endLine": 8
        },
        "annotationAttachments": [],
        "serviceTypeStruct": {
            "ws": [
                {
                    "ws": "",
                    "i": 4,
                    "text": "http",
                    "static": false
                },
                {
                    "ws": "",
                    "i": 5,
                    "text": ":",
                    "static": true
                },
                {
                    "ws": "",
                    "i": 6,
                    "text": "Service",
                    "static": false
                }
            ],
            "position": {
                "startColumn": 13,
                "startLine": 2,
                "endColumn": 13,
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
                "value": "Service",
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
                "endLine": 8
            },
            "workers": [],
            "returnTypeAnnotationAttachments": [],
            "name": {
                "literal": false,
                "value": "serviceName.<init>",
                "kind": "Identifier"
            },
            "annotationAttachments": [],
            "documentationAttachments": [],
            "returnTypeNode": {
                "position": {
                    "startColumn": 13,
                    "startLine": 2,
                    "endColumn": 13,
                    "endLine": 8
                },
                "symbolType": [
                    "null"
                ],
                "typeKind": "nil",
                "grouped": false,
                "nullable": false,
                "kind": "ValueType"
            },
            "defaultableParameters": [],
            "deprecatedAttachments": [],
            "endpointNodes": [],
            "body": {
                "position": {
                    "startColumn": 13,
                    "startLine": 2,
                    "endColumn": 13,
                    "endLine": 8
                },
                "statements": [],
                "kind": "Block"
            },
            "parameters": [],
            "kind": "Function",
            "public": true,
            "native": false,
            "final": false,
            "attached": false,
            "lambda": false,
            "parallel": false,
            "connector": false,
            "deprecated": false,
            "readonly": false,
            "function_final": false,
            "interface": false
        },
        "endpointNodes": [],
        "documentationAttachments": [],
        "boundEndpoints": [
            {
                "ws": [
                    {
                        "ws": " ",
                        "i": 13,
                        "text": "endpointName",
                        "static": false
                    }
                ],
                "position": {
                    "startColumn": 52,
                    "startLine": 2,
                    "endColumn": 52,
                    "endLine": 2
                },
                "symbolType": [
                    "other"
                ],
                "variableName": {
                    "literal": false,
                    "value": "endpointName",
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
                "ws": [
                    {
                        "ws": "\n                ",
                        "i": 18,
                        "text": "getAction",
                        "static": false
                    },
                    {
                        "ws": " ",
                        "i": 20,
                        "text": "(",
                        "static": true
                    },
                    {
                        "ws": " ",
                        "i": 23,
                        "text": "client",
                        "static": false
                    },
                    {
                        "ws": "",
                        "i": 24,
                        "text": ")",
                        "static": true
                    },
                    {
                        "ws": "",
                        "i": 25,
                        "text": "{",
                        "static": true
                    },
                    {
                        "ws": "\n                ",
                        "i": 64,
                        "text": "}",
                        "static": true
                    }
                ],
                "position": {
                    "startColumn": 17,
                    "startLine": 3,
                    "endColumn": 17,
                    "endLine": 7
                },
                "workers": [],
                "returnTypeAnnotationAttachments": [],
                "name": {
                    "literal": false,
                    "value": "getAction",
                    "kind": "Identifier"
                },
                "annotationAttachments": [],
                "documentationAttachments": [],
                "returnTypeNode": {
                    "position": {
                        "startColumn": 17,
                        "startLine": 3,
                        "endColumn": 17,
                        "endLine": 7
                    },
                    "symbolType": [
                        "null"
                    ],
                    "typeKind": "nil",
                    "grouped": false,
                    "nullable": false,
                    "kind": "ValueType"
                },
                "defaultableParameters": [],
                "deprecatedAttachments": [],
                "endpointNodes": [],
                "body": {
                    "statements": [
                        {
                            "ws": [
                                {
                                    "ws": "",
                                    "i": 37,
                                    "text": ";",
                                    "static": true
                                }
                            ],
                            "position": {
                                "startColumn": 21,
                                "startLine": 4,
                                "endColumn": 44,
                                "endLine": 4
                            },
                            "variable": {
                                "ws": [
                                    {
                                        "ws": " ",
                                        "i": 32,
                                        "text": "res",
                                        "static": false
                                    },
                                    {
                                        "ws": " ",
                                        "i": 34,
                                        "text": "=",
                                        "static": true
                                    }
                                ],
                                "position": {
                                    "startColumn": 21,
                                    "startLine": 4,
                                    "endColumn": 44,
                                    "endLine": 4
                                },
                                "symbolType": [
                                    "other"
                                ],
                                "safeAssignment": false,
                                "typeNode": {
                                    "ws": [
                                        {
                                            "ws": "\n                    ",
                                            "i": 28,
                                            "text": "http",
                                            "static": false
                                        },
                                        {
                                            "ws": "",
                                            "i": 29,
                                            "text": ":",
                                            "static": true
                                        },
                                        {
                                            "ws": "",
                                            "i": 30,
                                            "text": "Response",
                                            "static": false
                                        }
                                    ],
                                    "position": {
                                        "startColumn": 21,
                                        "startLine": 4,
                                        "endColumn": 26,
                                        "endLine": 4
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
                                        "value": "Response",
                                        "kind": "Identifier"
                                    },
                                    "nullable": false,
                                    "kind": "UserDefinedType"
                                },
                                "deprecatedAttachments": [],
                                "annotationAttachments": [],
                                "name": {
                                    "literal": false,
                                    "value": "res",
                                    "kind": "Identifier"
                                },
                                "initialExpression": {
                                    "ws": [
                                        {
                                            "ws": " ",
                                            "i": 36,
                                            "text": "new",
                                            "static": true
                                        }
                                    ],
                                    "position": {
                                        "startColumn": 41,
                                        "startLine": 4,
                                        "endColumn": 41,
                                        "endLine": 4
                                    },
                                    "symbolType": [
                                        "other"
                                    ],
                                    "expressions": [],
                                    "kind": "TypeInitExpr"
                                },
                                "documentationAttachments": [],
                                "kind": "Variable",
                                "public": false,
                                "native": false,
                                "final": false,
                                "attached": false,
                                "lambda": false,
                                "parallel": false,
                                "connector": false,
                                "deprecated": false,
                                "readonly": false,
                                "function_final": false,
                                "interface": false
                            },
                            "kind": "VariableDef"
                        },
                        {
                            "ws": [
                                {
                                    "ws": "",
                                    "i": 46,
                                    "text": ";",
                                    "static": true
                                }
                            ],
                            "position": {
                                "startColumn": 21,
                                "startLine": 5,
                                "endColumn": 55,
                                "endLine": 5
                            },
                            "expression": {
                                "ws": [
                                    {
                                        "ws": "",
                                        "i": 41,
                                        "text": ".",
                                        "static": true
                                    },
                                    {
                                        "ws": "",
                                        "i": 42,
                                        "text": "setStringPayload",
                                        "static": false
                                    },
                                    {
                                        "ws": "",
                                        "i": 43,
                                        "text": "(",
                                        "static": true
                                    },
                                    {
                                        "ws": "",
                                        "i": 45,
                                        "text": ")",
                                        "static": true
                                    }
                                ],
                                "position": {
                                    "startColumn": 21,
                                    "startLine": 5,
                                    "endColumn": 54,
                                    "endLine": 5
                                },
                                "symbolType": [
                                    "other"
                                ],
                                "iterableOperation": false,
                                "argumentExpressions": [
                                    {
                                        "ws": [
                                            {
                                                "ws": "",
                                                "i": 44,
                                                "text": "\"Successful\"",
                                                "static": false
                                            }
                                        ],
                                        "position": {
                                            "startColumn": 42,
                                            "startLine": 5,
                                            "endColumn": 42,
                                            "endLine": 5
                                        },
                                        "value": "\"Successful\"",
                                        "unescapedValue": "Successful",
                                        "kind": "Literal"
                                    }
                                ],
                                "packageAlias": {
                                    "literal": false,
                                    "value": "",
                                    "kind": "Identifier"
                                },
                                "async": false,
                                "actionInvocation": false,
                                "name": {
                                    "literal": false,
                                    "value": "setStringPayload",
                                    "kind": "Identifier"
                                },
                                "expression": {
                                    "ws": [
                                        {
                                            "ws": "\n                    ",
                                            "i": 40,
                                            "text": "res",
                                            "static": false
                                        }
                                    ],
                                    "position": {
                                        "startColumn": 21,
                                        "startLine": 5,
                                        "endColumn": 21,
                                        "endLine": 5
                                    },
                                    "symbolType": [
                                        "other"
                                    ],
                                    "variableName": {
                                        "literal": false,
                                        "value": "res",
                                        "kind": "Identifier"
                                    },
                                    "packageAlias": {
                                        "literal": false,
                                        "value": "",
                                        "kind": "Identifier"
                                    },
                                    "kind": "SimpleVariableRef"
                                },
                                "kind": "Invocation"
                            },
                            "kind": "ExpressionStatement"
                        },
                        {
                            "ws": [
                                {
                                    "ws": " ",
                                    "i": 51,
                                    "text": "=",
                                    "static": true
                                },
                                {
                                    "ws": "",
                                    "i": 61,
                                    "text": ";",
                                    "static": true
                                }
                            ],
                            "position": {
                                "startColumn": 21,
                                "startLine": 6,
                                "endColumn": 47,
                                "endLine": 6
                            },
                            "variable": {
                                "ws": [
                                    {
                                        "ws": "\n                    ",
                                        "i": 49,
                                        "text": "_",
                                        "static": false
                                    }
                                ],
                                "position": {
                                    "startColumn": 21,
                                    "startLine": 6,
                                    "endColumn": 21,
                                    "endLine": 6
                                },
                                "symbolType": [
                                    "other"
                                ],
                                "variableName": {
                                    "literal": false,
                                    "value": "_",
                                    "kind": "Identifier"
                                },
                                "packageAlias": {
                                    "literal": false,
                                    "value": "",
                                    "kind": "Identifier"
                                },
                                "kind": "SimpleVariableRef"
                            },
                            "expression": {
                                "ws": [
                                    {
                                        "ws": " ",
                                        "i": 55,
                                        "text": "->",
                                        "static": true
                                    },
                                    {
                                        "ws": "",
                                        "i": 58,
                                        "text": "(",
                                        "static": true
                                    },
                                    {
                                        "ws": "",
                                        "i": 60,
                                        "text": ")",
                                        "static": true
                                    }
                                ],
                                "position": {
                                    "startColumn": 25,
                                    "startLine": 6,
                                    "endColumn": 46,
                                    "endLine": 6
                                },
                                "symbolType": [
                                    "other"
                                ],
                                "iterableOperation": false,
                                "argumentExpressions": [
                                    {
                                        "ws": [
                                            {
                                                "ws": "",
                                                "i": 59,
                                                "text": "res",
                                                "static": false
                                            }
                                        ],
                                        "position": {
                                            "startColumn": 43,
                                            "startLine": 6,
                                            "endColumn": 43,
                                            "endLine": 6
                                        },
                                        "variableName": {
                                            "literal": false,
                                            "value": "res",
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
                                "packageAlias": {
                                    "literal": false,
                                    "value": "",
                                    "kind": "Identifier"
                                },
                                "async": false,
                                "actionInvocation": true,
                                "name": {
                                    "literal": false,
                                    "value": "respond",
                                    "kind": "Identifier"
                                },
                                "expression": {
                                    "ws": [
                                        {
                                            "ws": " ",
                                            "i": 53,
                                            "text": "client",
                                            "static": false
                                        }
                                    ],
                                    "position": {
                                        "startColumn": 25,
                                        "startLine": 6,
                                        "endColumn": 25,
                                        "endLine": 6
                                    },
                                    "symbolType": [
                                        "other"
                                    ],
                                    "variableName": {
                                        "literal": false,
                                        "value": "client",
                                        "kind": "Identifier"
                                    },
                                    "packageAlias": {
                                        "literal": false,
                                        "value": "",
                                        "kind": "Identifier"
                                    },
                                    "kind": "SimpleVariableRef"
                                },
                                "kind": "Invocation"
                            },
                            "declaredWithVar": false,
                            "kind": "Assignment"
                        }
                    ],
                    "kind": "Block"
                },
                "parameters": [
                    {
                        "ws": [
                            {
                                "ws": "",
                                "i": 21,
                                "text": "endpoint",
                                "static": true
                            }
                        ],
                        "position": {
                            "startColumn": 28,
                            "startLine": 3,
                            "endColumn": 37,
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
                            "value": "client",
                            "kind": "Identifier"
                        },
                        "documentationAttachments": [],
                        "kind": "Variable",
                        "public": false,
                        "native": false,
                        "final": false,
                        "attached": false,
                        "lambda": false,
                        "parallel": false,
                        "connector": false,
                        "deprecated": false,
                        "readonly": false,
                        "function_final": false,
                        "interface": false
                    }
                ],
                "kind": "Resource",
                "public": true,
                "native": false,
                "final": false,
                "attached": false,
                "lambda": false,
                "parallel": false,
                "connector": false,
                "deprecated": false,
                "readonly": false,
                "function_final": false,
                "interface": false
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
    },
    "createJMSServiceDef": {
        "error": "Index: 0, Size: 0"
    },
    "createHTTPEndpointDef": {
        "ws": [
            {
                "ws": "\n            ",
                "i": 2,
                "text": "endpoint",
                "static": true
            },
            {
                "ws": " ",
                "i": 8,
                "text": "endpointName",
                "static": false
            },
            {
                "ws": "",
                "i": 19,
                "text": ";",
                "static": true
            }
        ],
        "position": {
            "startColumn": 13,
            "startLine": 2,
            "endColumn": 14,
            "endLine": 4
        },
        "symbolType": [
            "other"
        ],
        "configurationExpression": {
            "ws": [
                {
                    "ws": " ",
                    "i": 10,
                    "text": "{",
                    "static": true
                },
                {
                    "ws": "\n            ",
                    "i": 18,
                    "text": "}",
                    "static": true
                }
            ],
            "position": {
                "startColumn": 49,
                "startLine": 2,
                "endColumn": 13,
                "endLine": 4
            },
            "symbolType": [
                "other"
            ],
            "keyValuePairs": [
                {
                    "ws": [
                        {
                            "ws": "",
                            "i": 14,
                            "text": ":",
                            "static": true
                        }
                    ],
                    "value": {
                        "ws": [
                            {
                                "ws": "",
                                "i": 15,
                                "text": "9095",
                                "static": false
                            }
                        ],
                        "position": {
                            "startColumn": 22,
                            "startLine": 3,
                            "endColumn": 22,
                            "endLine": 3
                        },
                        "value": "9095",
                        "kind": "Literal"
                    },
                    "key": {
                        "ws": [
                            {
                                "ws": "\n                ",
                                "i": 13,
                                "text": "port",
                                "static": false
                            }
                        ],
                        "position": {
                            "startColumn": 17,
                            "startLine": 3,
                            "endColumn": 17,
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
            "value": "endpointName",
            "kind": "Identifier"
        },
        "annotationAttachments": [],
        "kind": "Endpoint",
        "endPointType": {
            "ws": [
                {
                    "ws": " ",
                    "i": 4,
                    "text": "http",
                    "static": false
                },
                {
                    "ws": "",
                    "i": 5,
                    "text": ":",
                    "static": true
                },
                {
                    "ws": "",
                    "i": 6,
                    "text": "Listener",
                    "static": false
                }
            ],
            "position": {
                "startColumn": 22,
                "startLine": 2,
                "endColumn": 27,
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
                "value": "Listener",
                "kind": "Identifier"
            },
            "nullable": false,
            "kind": "UserDefinedType"
        },
        "public": false,
        "native": false,
        "final": false,
        "attached": false,
        "lambda": false,
        "parallel": false,
        "connector": false,
        "deprecated": false,
        "readonly": false,
        "function_final": false,
        "interface": false
    }
}