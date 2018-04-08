/**
* This is a auto generated file, DO NOT modify this manually.
* Use npm run gen-default-nodes command to generate this file.
*/

export default {
    "createJMSServiceDef": {
        "error": "Index: 0, Size: 0"
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
        "interfaceFunction": false,
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
        "body": {
            "statements": [],
            "kind": "Block"
        },
        "endpointNodes": [],
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
        "readonly": false
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
        "body": {
            "statements": [],
            "kind": "Block"
        },
        "endpointNodes": [],
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
                "readonly": false
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
        "readonly": false
    },
    "createBindStmt": {
        "error": "Index: 0, Size: 0"
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
            "readonly": false
        },
        "kind": "VariableDef"
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
        "body": {
            "statements": [],
            "kind": "Block"
        },
        "endpointNodes": [],
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
                "readonly": false
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
        "readonly": false
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
        "body": {
            "statements": [],
            "kind": "Block"
        },
        "endpointNodes": [],
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
                "readonly": false
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
        "readonly": false
    },
    "createWSEndpointDef": {
        "ws": [
            {
                "ws": "\n",
                "i": 1,
                "text": "endpoint",
                "static": true
            },
            {
                "ws": " ",
                "i": 7,
                "text": "ep",
                "static": false
            },
            {
                "ws": "",
                "i": 17,
                "text": ";",
                "static": true
            }
        ],
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
            "ws": [
                {
                    "ws": " ",
                    "i": 9,
                    "text": "{",
                    "static": true
                },
                {
                    "ws": "\n",
                    "i": 16,
                    "text": "}",
                    "static": true
                }
            ],
            "position": {
                "startColumn": 34,
                "startLine": 2,
                "endColumn": 1,
                "endLine": 4
            },
            "keyValuePairs": [
                {
                    "ws": [
                        {
                            "ws": "",
                            "i": 13,
                            "text": ":",
                            "static": true
                        }
                    ],
                    "value": {
                        "ws": [
                            {
                                "ws": "",
                                "i": 14,
                                "text": "9090",
                                "static": false
                            }
                        ],
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
                        "ws": [
                            {
                                "ws": "\n    ",
                                "i": 12,
                                "text": "port",
                                "static": false
                            }
                        ],
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
            "ws": [
                {
                    "ws": " ",
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
                    "text": "ServiceEndpoint",
                    "static": false
                }
            ],
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
        "final": false,
        "attached": false,
        "lambda": false,
        "parallel": false,
        "connector": false,
        "deprecated": false,
        "readonly": false
    },
    "createWSServiceDef": {
        "ws": [
            {
                "ws": "\n            ",
                "i": 2,
                "text": "service",
                "static": true
            },
            {
                "ws": " ",
                "i": 4,
                "text": "SimpleSecureServer",
                "static": false
            },
            {
                "ws": " ",
                "i": 6,
                "text": "bind",
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
                "i": 75,
                "text": "}",
                "static": true
            }
        ],
        "position": {
            "startColumn": 13,
            "startLine": 2,
            "endColumn": 13,
            "endLine": 15
        },
        "annotationAttachments": [],
        "initFunction": {
            "position": {
                "startColumn": 13,
                "startLine": 2,
                "endColumn": 13,
                "endLine": 15
            },
            "interfaceFunction": false,
            "workers": [],
            "returnTypeAnnotationAttachments": [],
            "name": {
                "literal": false,
                "value": "SimpleSecureServer.<init>",
                "kind": "Identifier"
            },
            "annotationAttachments": [],
            "documentationAttachments": [],
            "returnTypeNode": {
                "position": {
                    "startColumn": 13,
                    "startLine": 2,
                    "endColumn": 13,
                    "endLine": 15
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
                        "expression": {
                            "position": {
                                "startColumn": 13,
                                "startLine": 2,
                                "endColumn": 13,
                                "endLine": 15
                            },
                            "symbolType": [
                                "null"
                            ],
                            "value": "()",
                            "kind": "Literal"
                        },
                        "kind": "Return"
                    }
                ],
                "kind": "Block"
            },
            "endpointNodes": [],
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
            "readonly": false
        },
        "endpointNodes": [],
        "documentationAttachments": [],
        "boundEndpoints": [
            {
                "ws": [
                    {
                        "ws": " ",
                        "i": 8,
                        "text": "ep",
                        "static": false
                    }
                ],
                "position": {
                    "startColumn": 45,
                    "startLine": 2,
                    "endColumn": 45,
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
                "ws": [
                    {
                        "ws": "\n            \n                ",
                        "i": 15,
                        "text": "onOpen",
                        "static": false
                    },
                    {
                        "ws": " ",
                        "i": 17,
                        "text": "(",
                        "static": true
                    },
                    {
                        "ws": " ",
                        "i": 20,
                        "text": "ep",
                        "static": false
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
                        "ws": "\n\n                ",
                        "i": 26,
                        "text": "}",
                        "static": true
                    }
                ],
                "position": {
                    "startColumn": 17,
                    "startLine": 4,
                    "endColumn": 17,
                    "endLine": 6
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
                        "startLine": 4,
                        "endColumn": 17,
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
                "body": {
                    "statements": [],
                    "kind": "Block"
                },
                "endpointNodes": [],
                "parameters": [
                    {
                        "ws": [
                            {
                                "ws": "",
                                "i": 18,
                                "text": "endpoint",
                                "static": true
                            }
                        ],
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
                        "final": false,
                        "attached": false,
                        "lambda": false,
                        "parallel": false,
                        "connector": false,
                        "deprecated": false,
                        "readonly": false
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
                "readonly": false
            },
            {
                "ws": [
                    {
                        "ws": "\n            \n                ",
                        "i": 31,
                        "text": "onTextMessage",
                        "static": false
                    },
                    {
                        "ws": " ",
                        "i": 33,
                        "text": "(",
                        "static": true
                    },
                    {
                        "ws": "",
                        "i": 37,
                        "text": ",",
                        "static": true
                    },
                    {
                        "ws": "",
                        "i": 44,
                        "text": ")",
                        "static": true
                    },
                    {
                        "ws": " ",
                        "i": 46,
                        "text": "{",
                        "static": true
                    },
                    {
                        "ws": "\n\n                ",
                        "i": 49,
                        "text": "}",
                        "static": true
                    }
                ],
                "position": {
                    "startColumn": 17,
                    "startLine": 8,
                    "endColumn": 17,
                    "endLine": 10
                },
                "workers": [],
                "returnTypeAnnotationAttachments": [],
                "name": {
                    "literal": false,
                    "value": "onTextMessage",
                    "kind": "Identifier"
                },
                "annotationAttachments": [],
                "documentationAttachments": [],
                "returnTypeNode": {
                    "position": {
                        "startColumn": 17,
                        "startLine": 8,
                        "endColumn": 17,
                        "endLine": 10
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
                "body": {
                    "statements": [],
                    "kind": "Block"
                },
                "endpointNodes": [],
                "parameters": [
                    {
                        "ws": [
                            {
                                "ws": "",
                                "i": 34,
                                "text": "endpoint",
                                "static": true
                            },
                            {
                                "ws": " ",
                                "i": 36,
                                "text": "conn",
                                "static": false
                            }
                        ],
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
                        "final": false,
                        "attached": false,
                        "lambda": false,
                        "parallel": false,
                        "connector": false,
                        "deprecated": false,
                        "readonly": false
                    },
                    {
                        "ws": [
                            {
                                "ws": " ",
                                "i": 43,
                                "text": "frame",
                                "static": false
                            }
                        ],
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
                            "ws": [
                                {
                                    "ws": " ",
                                    "i": 39,
                                    "text": "http",
                                    "static": false
                                },
                                {
                                    "ws": "",
                                    "i": 40,
                                    "text": ":",
                                    "static": true
                                },
                                {
                                    "ws": "",
                                    "i": 41,
                                    "text": "TextFrame",
                                    "static": false
                                }
                            ],
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
                        "final": false,
                        "attached": false,
                        "lambda": false,
                        "parallel": false,
                        "connector": false,
                        "deprecated": false,
                        "readonly": false
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
                "readonly": false
            },
            {
                "ws": [
                    {
                        "ws": "\n            \n                ",
                        "i": 54,
                        "text": "onClose",
                        "static": false
                    },
                    {
                        "ws": " ",
                        "i": 56,
                        "text": "(",
                        "static": true
                    },
                    {
                        "ws": "",
                        "i": 60,
                        "text": ",",
                        "static": true
                    },
                    {
                        "ws": "",
                        "i": 67,
                        "text": ")",
                        "static": true
                    },
                    {
                        "ws": " ",
                        "i": 69,
                        "text": "{",
                        "static": true
                    },
                    {
                        "ws": "\n\n                ",
                        "i": 72,
                        "text": "}",
                        "static": true
                    }
                ],
                "position": {
                    "startColumn": 17,
                    "startLine": 12,
                    "endColumn": 17,
                    "endLine": 14
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
                        "startLine": 12,
                        "endColumn": 17,
                        "endLine": 14
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
                "body": {
                    "statements": [],
                    "kind": "Block"
                },
                "endpointNodes": [],
                "parameters": [
                    {
                        "ws": [
                            {
                                "ws": "",
                                "i": 57,
                                "text": "endpoint",
                                "static": true
                            },
                            {
                                "ws": " ",
                                "i": 59,
                                "text": "conn",
                                "static": false
                            }
                        ],
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
                        "final": false,
                        "attached": false,
                        "lambda": false,
                        "parallel": false,
                        "connector": false,
                        "deprecated": false,
                        "readonly": false
                    },
                    {
                        "ws": [
                            {
                                "ws": " ",
                                "i": 66,
                                "text": "closeFrame",
                                "static": false
                            }
                        ],
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
                            "ws": [
                                {
                                    "ws": " ",
                                    "i": 62,
                                    "text": "http",
                                    "static": false
                                },
                                {
                                    "ws": "",
                                    "i": 63,
                                    "text": ":",
                                    "static": true
                                },
                                {
                                    "ws": "",
                                    "i": 64,
                                    "text": "CloseFrame",
                                    "static": false
                                }
                            ],
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
                        "final": false,
                        "attached": false,
                        "lambda": false,
                        "parallel": false,
                        "connector": false,
                        "deprecated": false,
                        "readonly": false
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
                "readonly": false
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
            "value": "1",
            "kind": "Literal"
        },
        "declaredWithVar": true,
        "kind": "Assignment"
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
        "body": {
            "statements": [],
            "kind": "Block"
        },
        "endpointNodes": [],
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
                "readonly": false
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
                "readonly": false
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
        "readonly": false
    },
    "createHTTPEndpointDef": {
        "ws": [
            {
                "ws": "\n                ",
                "i": 2,
                "text": "endpoint",
                "static": true
            },
            {
                "ws": " ",
                "i": 8,
                "text": "serviceEp",
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
            "startColumn": 17,
            "startLine": 2,
            "endColumn": 18,
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
                    "ws": "\n                ",
                    "i": 18,
                    "text": "}",
                    "static": true
                }
            ],
            "position": {
                "startColumn": 57,
                "startLine": 2,
                "endColumn": 17,
                "endLine": 4
            },
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
                            "startColumn": 26,
                            "startLine": 3,
                            "endColumn": 26,
                            "endLine": 3
                        },
                        "value": "9090",
                        "kind": "Literal"
                    },
                    "key": {
                        "ws": [
                            {
                                "ws": "\n                    ",
                                "i": 13,
                                "text": "port",
                                "static": false
                            }
                        ],
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
                    "text": "ServiceEndpoint",
                    "static": false
                }
            ],
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
        "final": false,
        "attached": false,
        "lambda": false,
        "parallel": false,
        "connector": false,
        "deprecated": false,
        "readonly": false
    },
    "createTransaction": {
        "error": null
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
    "createInvocation": {
        "error": null
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
    "createTransformer": {
        "error": "Index: 0, Size: 0"
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
            "value": "$anonRecord$0",
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
        "readonly": false
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
                        "expression": {
                            "position": {
                                "startColumn": 13,
                                "startLine": 2,
                                "endColumn": 13,
                                "endLine": 6
                            },
                            "symbolType": [
                                "null"
                            ],
                            "value": "()",
                            "kind": "Literal"
                        },
                        "kind": "Return"
                    }
                ],
                "kind": "Block"
            },
            "endpointNodes": [],
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
            "readonly": false
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
                "body": {
                    "statements": [],
                    "kind": "Block"
                },
                "endpointNodes": [],
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
                        "readonly": false
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
                "readonly": false
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
                        "expression": {
                            "position": {
                                "startColumn": 13,
                                "startLine": 2,
                                "endColumn": 13,
                                "endLine": 6
                            },
                            "symbolType": [
                                "null"
                            ],
                            "value": "()",
                            "kind": "Literal"
                        },
                        "kind": "Return"
                    }
                ],
                "kind": "Block"
            },
            "endpointNodes": [],
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
            "readonly": false
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
                "body": {
                    "statements": [],
                    "kind": "Block"
                },
                "endpointNodes": [],
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
                        "readonly": false
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
                "readonly": false
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
                "ws": "\n                ",
                "i": 2,
                "text": "service",
                "static": true
            },
            {
                "ws": " ",
                "i": 4,
                "text": "serviceName",
                "static": false
            },
            {
                "ws": " ",
                "i": 6,
                "text": "bind",
                "static": true
            },
            {
                "ws": " ",
                "i": 10,
                "text": "{",
                "static": true
            },
            {
                "ws": "\n                ",
                "i": 34,
                "text": "}",
                "static": true
            }
        ],
        "position": {
            "startColumn": 17,
            "startLine": 2,
            "endColumn": 17,
            "endLine": 6
        },
        "annotationAttachments": [],
        "initFunction": {
            "position": {
                "startColumn": 17,
                "startLine": 2,
                "endColumn": 17,
                "endLine": 6
            },
            "interfaceFunction": false,
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
                    "startColumn": 17,
                    "startLine": 2,
                    "endColumn": 17,
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
                        "expression": {
                            "position": {
                                "startColumn": 17,
                                "startLine": 2,
                                "endColumn": 17,
                                "endLine": 6
                            },
                            "symbolType": [
                                "null"
                            ],
                            "value": "()",
                            "kind": "Literal"
                        },
                        "kind": "Return"
                    }
                ],
                "kind": "Block"
            },
            "endpointNodes": [],
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
            "readonly": false
        },
        "endpointNodes": [],
        "documentationAttachments": [],
        "boundEndpoints": [
            {
                "ws": [
                    {
                        "ws": " ",
                        "i": 8,
                        "text": "serviceEp",
                        "static": false
                    }
                ],
                "position": {
                    "startColumn": 42,
                    "startLine": 2,
                    "endColumn": 42,
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
                "ws": [
                    {
                        "ws": "\n                    ",
                        "i": 13,
                        "text": "resourceName",
                        "static": false
                    },
                    {
                        "ws": " ",
                        "i": 15,
                        "text": "(",
                        "static": true
                    },
                    {
                        "ws": "",
                        "i": 19,
                        "text": ",",
                        "static": true
                    },
                    {
                        "ws": "",
                        "i": 26,
                        "text": ")",
                        "static": true
                    },
                    {
                        "ws": " ",
                        "i": 28,
                        "text": "{",
                        "static": true
                    },
                    {
                        "ws": "\n\n                    ",
                        "i": 31,
                        "text": "}",
                        "static": true
                    }
                ],
                "position": {
                    "startColumn": 21,
                    "startLine": 3,
                    "endColumn": 21,
                    "endLine": 5
                },
                "workers": [],
                "returnTypeAnnotationAttachments": [],
                "name": {
                    "literal": false,
                    "value": "resourceName",
                    "kind": "Identifier"
                },
                "annotationAttachments": [],
                "documentationAttachments": [],
                "returnTypeNode": {
                    "position": {
                        "startColumn": 21,
                        "startLine": 3,
                        "endColumn": 21,
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
                "body": {
                    "statements": [],
                    "kind": "Block"
                },
                "endpointNodes": [],
                "parameters": [
                    {
                        "ws": [
                            {
                                "ws": "",
                                "i": 16,
                                "text": "endpoint",
                                "static": true
                            },
                            {
                                "ws": " ",
                                "i": 18,
                                "text": "conn",
                                "static": false
                            }
                        ],
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
                        "final": false,
                        "attached": false,
                        "lambda": false,
                        "parallel": false,
                        "connector": false,
                        "deprecated": false,
                        "readonly": false
                    },
                    {
                        "ws": [
                            {
                                "ws": " ",
                                "i": 25,
                                "text": "req",
                                "static": false
                            }
                        ],
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
                            "ws": [
                                {
                                    "ws": " ",
                                    "i": 21,
                                    "text": "http",
                                    "static": false
                                },
                                {
                                    "ws": "",
                                    "i": 22,
                                    "text": ":",
                                    "static": true
                                },
                                {
                                    "ws": "",
                                    "i": 23,
                                    "text": "Request",
                                    "static": false
                                }
                            ],
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
                        "final": false,
                        "attached": false,
                        "lambda": false,
                        "parallel": false,
                        "connector": false,
                        "deprecated": false,
                        "readonly": false
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
                "readonly": false
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
        "body": {
            "statements": [],
            "kind": "Block"
        },
        "endpointNodes": [],
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
        "readonly": false
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
            "readonly": false
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
                "body": {
                    "statements": [],
                    "kind": "Block"
                },
                "endpointNodes": [],
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
                "readonly": false
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
                "body": {
                    "statements": [],
                    "kind": "Block"
                },
                "endpointNodes": [],
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
                "readonly": false
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
            "readonly": false
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
        "interfaceFunction": false,
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
        "body": {
            "statements": [],
            "kind": "Block"
        },
        "endpointNodes": [],
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
                "readonly": false
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
        "readonly": false
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
                    "readonly": false
                },
                "kind": "Catch"
            }
        ],
        "kind": "Try"
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
        "body": {
            "statements": [],
            "kind": "Block"
        },
        "endpointNodes": [],
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
        "readonly": false
    }
}