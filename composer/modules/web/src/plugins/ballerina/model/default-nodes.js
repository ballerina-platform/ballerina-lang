/**
* This is a auto generated file, DO NOT modify this manually.
* Use npm run gen-default-nodes command to generate this file.
*/

export default {
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
        "deprecated": false,
        "endpoint": false
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
        "deprecatedAttachments": [],
        "endpointNodes": [],
        "body": {
            "statements": [],
            "kind": "Block"
        },
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
                "deprecated": false,
                "endpoint": false
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
        "deprecated": false,
        "endpoint": false
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
        "deprecatedAttachments": [],
        "endpointNodes": [],
        "body": {
            "statements": [],
            "kind": "Block"
        },
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
                "deprecated": false,
                "endpoint": false
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
            "deprecated": false,
            "endpoint": false
        },
        "kind": "Transformer",
        "public": false,
        "native": false,
        "const": false,
        "attached": false,
        "lambda": false,
        "parallel": false,
        "connector": false,
        "deprecated": false,
        "endpoint": false
    },
    "createHTTPResource": {
        "ws": [
            {
                "ws": "\n\n            ",
                "i": 9,
                "text": "resource",
                "static": true
            },
            {
                "ws": " ",
                "i": 11,
                "text": "echo1",
                "static": false
            },
            {
                "ws": " ",
                "i": 13,
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
                "ws": "\n\n            ",
                "i": 31,
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
        "name": {
            "literal": false,
            "value": "echo1",
            "kind": "Identifier"
        },
        "deprecatedAttachments": [],
        "annotationAttachments": [],
        "endpointNodes": [],
        "body": {
            "statements": [],
            "kind": "Block"
        },
        "returnParameters": [],
        "parameters": [
            {
                "ws": [
                    {
                        "ws": " ",
                        "i": 18,
                        "text": "conn",
                        "static": false
                    }
                ],
                "position": {
                    "startColumn": 29,
                    "startLine": 3,
                    "endColumn": 45,
                    "endLine": 3
                },
                "symbolType": [
                    "other"
                ],
                "typeNode": {
                    "ws": [
                        {
                            "ws": "",
                            "i": 14,
                            "text": "http",
                            "static": false
                        },
                        {
                            "ws": "",
                            "i": 15,
                            "text": ":",
                            "static": true
                        },
                        {
                            "ws": "",
                            "i": 16,
                            "text": "Connection",
                            "static": false
                        }
                    ],
                    "position": {
                        "startColumn": 29,
                        "startLine": 3,
                        "endColumn": 34,
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
                    "typeName": {
                        "literal": false,
                        "value": "Connection",
                        "kind": "Identifier"
                    },
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
                "deprecated": false,
                "endpoint": false
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
                    "startColumn": 51,
                    "startLine": 3,
                    "endColumn": 66,
                    "endLine": 3
                },
                "symbolType": [
                    "other"
                ],
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
                            "text": "InRequest",
                            "static": false
                        }
                    ],
                    "position": {
                        "startColumn": 51,
                        "startLine": 3,
                        "endColumn": 56,
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
                    "typeName": {
                        "literal": false,
                        "value": "InRequest",
                        "kind": "Identifier"
                    },
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
                "deprecated": false,
                "endpoint": false
            }
        ],
        "documentationAttachments": [],
        "kind": "Resource",
        "public": true,
        "native": false,
        "const": false,
        "attached": false,
        "lambda": false,
        "parallel": false,
        "connector": false,
        "deprecated": false,
        "endpoint": false
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
        "deprecatedAttachments": [],
        "annotationAttachments": [],
        "endpointNodes": [],
        "body": {
            "statements": [],
            "kind": "Block"
        },
        "returnParameters": [],
        "parameters": [],
        "documentationAttachments": [],
        "kind": "Worker",
        "public": false,
        "native": false,
        "const": false,
        "attached": false,
        "lambda": false,
        "parallel": false,
        "connector": false,
        "deprecated": false,
        "endpoint": false
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
            "deprecated": false,
            "endpoint": false
        },
        "kind": "VariableDef"
    },
    "createAnnotation": {
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
    "createJMSServiceDef": {
        "ws": [
            {
                "ws": "\n                ",
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
                "ws": "\n                ",
                "i": 30,
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
        "resources": [
            {
                "ws": [
                    {
                        "ws": "\n                    ",
                        "i": 12,
                        "text": "resource",
                        "static": true
                    },
                    {
                        "ws": " ",
                        "i": 14,
                        "text": "echo1",
                        "static": false
                    },
                    {
                        "ws": " ",
                        "i": 16,
                        "text": "(",
                        "static": true
                    },
                    {
                        "ws": "",
                        "i": 22,
                        "text": ")",
                        "static": true
                    },
                    {
                        "ws": " ",
                        "i": 24,
                        "text": "{",
                        "static": true
                    },
                    {
                        "ws": "\n\n                    ",
                        "i": 27,
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
                "name": {
                    "literal": false,
                    "value": "echo1",
                    "kind": "Identifier"
                },
                "deprecatedAttachments": [],
                "annotationAttachments": [],
                "endpointNodes": [],
                "body": {
                    "statements": [],
                    "kind": "Block"
                },
                "returnParameters": [],
                "parameters": [
                    {
                        "ws": [
                            {
                                "ws": " ",
                                "i": 21,
                                "text": "request",
                                "static": false
                            }
                        ],
                        "position": {
                            "startColumn": 37,
                            "startLine": 3,
                            "endColumn": 52,
                            "endLine": 3
                        },
                        "symbolType": [
                            "other"
                        ],
                        "typeNode": {
                            "ws": [
                                {
                                    "ws": "",
                                    "i": 17,
                                    "text": "jms",
                                    "static": false
                                },
                                {
                                    "ws": "",
                                    "i": 18,
                                    "text": ":",
                                    "static": true
                                },
                                {
                                    "ws": "",
                                    "i": 19,
                                    "text": "JMSMessage",
                                    "static": false
                                }
                            ],
                            "position": {
                                "startColumn": 37,
                                "startLine": 3,
                                "endColumn": 41,
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
                            "typeName": {
                                "literal": false,
                                "value": "JMSMessage",
                                "kind": "Identifier"
                            },
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
                        "deprecated": false,
                        "endpoint": false
                    }
                ],
                "documentationAttachments": [],
                "kind": "Resource",
                "public": true,
                "native": false,
                "const": false,
                "attached": false,
                "lambda": false,
                "parallel": false,
                "connector": false,
                "deprecated": false,
                "endpoint": false
            }
        ],
        "deprecatedAttachments": [],
        "endpointType": {
            "ws": [
                {
                    "ws": "",
                    "i": 4,
                    "text": "jms",
                    "static": false
                }
            ],
            "position": {
                "startColumn": 17,
                "startLine": 2,
                "endColumn": 17,
                "endLine": 6
            },
            "packageAlias": {
                "literal": false,
                "value": "",
                "kind": "Identifier"
            },
            "typeName": {
                "literal": false,
                "value": "jms",
                "kind": "Identifier"
            },
            "kind": "UserDefinedType"
        },
        "annotationAttachments": [],
        "initFunction": {
            "position": {
                "startColumn": 17,
                "startLine": 2,
                "endColumn": 17,
                "endLine": 6
            },
            "workers": [],
            "name": {
                "literal": false,
                "value": "service1.<init>",
                "kind": "Identifier"
            },
            "annotationAttachments": [],
            "documentationAttachments": [],
            "deprecatedAttachments": [],
            "endpointNodes": [],
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
            "deprecated": false,
            "endpoint": false
        },
        "variables": [],
        "name": {
            "literal": false,
            "value": "service1",
            "kind": "Identifier"
        },
        "endpointNodes": [],
        "documentationAttachments": [],
        "kind": "Service"
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
        "deprecatedAttachments": [],
        "endpointNodes": [],
        "body": {
            "statements": [],
            "kind": "Block"
        },
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
        "deprecated": false,
        "endpoint": false
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
        "deprecated": false,
        "endpoint": false
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
                "i": 30,
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
        "resources": [
            {
                "ws": [
                    {
                        "ws": "\n                ",
                        "i": 12,
                        "text": "resource",
                        "static": true
                    },
                    {
                        "ws": " ",
                        "i": 14,
                        "text": "echo1",
                        "static": false
                    },
                    {
                        "ws": " ",
                        "i": 16,
                        "text": "(",
                        "static": true
                    },
                    {
                        "ws": "",
                        "i": 22,
                        "text": ")",
                        "static": true
                    },
                    {
                        "ws": " ",
                        "i": 24,
                        "text": "{",
                        "static": true
                    },
                    {
                        "ws": "\n\n                ",
                        "i": 27,
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
                "name": {
                    "literal": false,
                    "value": "echo1",
                    "kind": "Identifier"
                },
                "deprecatedAttachments": [],
                "annotationAttachments": [],
                "endpointNodes": [],
                "body": {
                    "statements": [],
                    "kind": "Block"
                },
                "returnParameters": [],
                "parameters": [
                    {
                        "ws": [
                            {
                                "ws": " ",
                                "i": 21,
                                "text": "m",
                                "static": false
                            }
                        ],
                        "position": {
                            "startColumn": 33,
                            "startLine": 3,
                            "endColumn": 52,
                            "endLine": 3
                        },
                        "symbolType": [
                            "other"
                        ],
                        "typeNode": {
                            "ws": [
                                {
                                    "ws": "",
                                    "i": 17,
                                    "text": "ftp",
                                    "static": false
                                },
                                {
                                    "ws": "",
                                    "i": 18,
                                    "text": ":",
                                    "static": true
                                },
                                {
                                    "ws": "",
                                    "i": 19,
                                    "text": "FTPServerEvent",
                                    "static": false
                                }
                            ],
                            "position": {
                                "startColumn": 33,
                                "startLine": 3,
                                "endColumn": 37,
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
                            "typeName": {
                                "literal": false,
                                "value": "FTPServerEvent",
                                "kind": "Identifier"
                            },
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
                        "deprecated": false,
                        "endpoint": false
                    }
                ],
                "documentationAttachments": [],
                "kind": "Resource",
                "public": true,
                "native": false,
                "const": false,
                "attached": false,
                "lambda": false,
                "parallel": false,
                "connector": false,
                "deprecated": false,
                "endpoint": false
            }
        ],
        "deprecatedAttachments": [],
        "endpointType": {
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
            "typeName": {
                "literal": false,
                "value": "ftp",
                "kind": "Identifier"
            },
            "kind": "UserDefinedType"
        },
        "annotationAttachments": [],
        "initFunction": {
            "position": {
                "startColumn": 13,
                "startLine": 2,
                "endColumn": 13,
                "endLine": 6
            },
            "workers": [],
            "name": {
                "literal": false,
                "value": "service1.<init>",
                "kind": "Identifier"
            },
            "annotationAttachments": [],
            "documentationAttachments": [],
            "deprecatedAttachments": [],
            "endpointNodes": [],
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
            "deprecated": false,
            "endpoint": false
        },
        "variables": [],
        "name": {
            "literal": false,
            "value": "service1",
            "kind": "Identifier"
        },
        "endpointNodes": [],
        "documentationAttachments": [],
        "kind": "Service"
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
    "createConnector": {
        "ws": [
            {
                "ws": "\n            ",
                "i": 2,
                "text": "connector",
                "static": true
            },
            {
                "ws": " ",
                "i": 4,
                "text": "ClientConnector",
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
                "i": 9,
                "text": ")",
                "static": true
            },
            {
                "ws": " ",
                "i": 11,
                "text": "{",
                "static": true
            },
            {
                "ws": "\n            ",
                "i": 25,
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
        "actions": [
            {
                "ws": [
                    {
                        "ws": "\n                ",
                        "i": 14,
                        "text": "action",
                        "static": true
                    },
                    {
                        "ws": " ",
                        "i": 16,
                        "text": "action1",
                        "static": false
                    },
                    {
                        "ws": "",
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
                        "ws": "\n\n                ",
                        "i": 22,
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
                "name": {
                    "literal": false,
                    "value": "action1",
                    "kind": "Identifier"
                },
                "deprecatedAttachments": [],
                "annotationAttachments": [],
                "endpointNodes": [],
                "body": {
                    "statements": [],
                    "kind": "Block"
                },
                "returnParameters": [],
                "parameters": [],
                "documentationAttachments": [],
                "kind": "Action",
                "public": true,
                "native": false,
                "const": false,
                "attached": false,
                "lambda": false,
                "parallel": false,
                "connector": false,
                "deprecated": false,
                "endpoint": false
            }
        ],
        "name": {
            "literal": false,
            "value": "ClientConnector",
            "kind": "Identifier"
        },
        "annotationAttachments": [],
        "documentationAttachments": [],
        "initAction": {
            "position": {
                "startColumn": 13,
                "startLine": 2,
                "endColumn": 13,
                "endLine": 6
            },
            "workers": [],
            "name": {
                "literal": false,
                "value": "<init>",
                "kind": "Identifier"
            },
            "deprecatedAttachments": [],
            "annotationAttachments": [],
            "endpointNodes": [],
            "returnParameters": [],
            "parameters": [],
            "documentationAttachments": [],
            "kind": "Action",
            "public": true,
            "native": true,
            "const": false,
            "attached": false,
            "lambda": false,
            "parallel": false,
            "connector": false,
            "deprecated": false,
            "endpoint": false
        },
        "variableDefs": [],
        "endpointNodes": [],
        "deprecatedAttachments": [],
        "initFunction": {
            "position": {
                "startColumn": 13,
                "startLine": 2,
                "endColumn": 13,
                "endLine": 6
            },
            "workers": [],
            "name": {
                "literal": false,
                "value": "ClientConnector.<init>",
                "kind": "Identifier"
            },
            "annotationAttachments": [],
            "documentationAttachments": [],
            "deprecatedAttachments": [],
            "endpointNodes": [],
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
            "returnParameters": [],
            "parameters": [
                {
                    "position": {
                        "startColumn": 13,
                        "startLine": 2,
                        "endColumn": 13,
                        "endLine": 6
                    },
                    "symbolType": [
                        "connector"
                    ],
                    "typeNode": {
                        "position": {
                            "startColumn": 13,
                            "startLine": 2,
                            "endColumn": 13,
                            "endLine": 6
                        },
                        "symbolType": [
                            "connector"
                        ],
                        "packageAlias": {
                            "literal": false,
                            "value": "",
                            "kind": "Identifier"
                        },
                        "public": false,
                        "native": false,
                        "const": false,
                        "attached": false,
                        "lambda": false,
                        "parallel": false,
                        "connector": true,
                        "deprecated": false,
                        "endpoint": false,
                        "typeName": {
                            "literal": false,
                            "value": "ClientConnector",
                            "kind": "Identifier"
                        },
                        "kind": "UserDefinedType"
                    },
                    "deprecatedAttachments": [],
                    "annotationAttachments": [],
                    "name": {
                        "literal": false,
                        "value": "connector",
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
                    "deprecated": false,
                    "endpoint": false
                }
            ],
            "kind": "Function",
            "public": true,
            "native": false,
            "const": false,
            "attached": false,
            "lambda": false,
            "parallel": false,
            "connector": false,
            "deprecated": false,
            "endpoint": false
        },
        "parameters": [
            {
                "ws": [
                    {
                        "ws": " ",
                        "i": 8,
                        "text": "url",
                        "static": false
                    }
                ],
                "position": {
                    "startColumn": 39,
                    "startLine": 2,
                    "endColumn": 46,
                    "endLine": 2
                },
                "symbolType": [
                    "string"
                ],
                "typeNode": {
                    "ws": [
                        {
                            "ws": "",
                            "i": 6,
                            "text": "string",
                            "static": true
                        }
                    ],
                    "position": {
                        "startColumn": 39,
                        "startLine": 2,
                        "endColumn": 39,
                        "endLine": 2
                    },
                    "symbolType": [
                        "string"
                    ],
                    "typeKind": "string",
                    "kind": "ValueType"
                },
                "deprecatedAttachments": [],
                "annotationAttachments": [],
                "name": {
                    "literal": false,
                    "value": "url",
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
                "deprecated": false,
                "endpoint": false
            }
        ],
        "kind": "Connector",
        "public": false,
        "native": false,
        "const": false,
        "attached": false,
        "lambda": false,
        "parallel": false,
        "connector": false,
        "deprecated": false,
        "endpoint": false
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
                "ws": "\n                ",
                "i": 37,
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
        "resources": [
            {
                "ws": [
                    {
                        "ws": "\n                    ",
                        "i": 12,
                        "text": "resource",
                        "static": true
                    },
                    {
                        "ws": " ",
                        "i": 14,
                        "text": "echo1",
                        "static": false
                    },
                    {
                        "ws": " ",
                        "i": 16,
                        "text": "(",
                        "static": true
                    },
                    {
                        "ws": "",
                        "i": 22,
                        "text": ",",
                        "static": true
                    },
                    {
                        "ws": "",
                        "i": 29,
                        "text": ")",
                        "static": true
                    },
                    {
                        "ws": " ",
                        "i": 31,
                        "text": "{",
                        "static": true
                    },
                    {
                        "ws": "\n\n                    ",
                        "i": 34,
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
                "name": {
                    "literal": false,
                    "value": "echo1",
                    "kind": "Identifier"
                },
                "deprecatedAttachments": [],
                "annotationAttachments": [],
                "endpointNodes": [],
                "body": {
                    "statements": [],
                    "kind": "Block"
                },
                "returnParameters": [],
                "parameters": [
                    {
                        "ws": [
                            {
                                "ws": " ",
                                "i": 21,
                                "text": "conn",
                                "static": false
                            }
                        ],
                        "position": {
                            "startColumn": 37,
                            "startLine": 3,
                            "endColumn": 53,
                            "endLine": 3
                        },
                        "symbolType": [
                            "other"
                        ],
                        "typeNode": {
                            "ws": [
                                {
                                    "ws": "",
                                    "i": 17,
                                    "text": "http",
                                    "static": false
                                },
                                {
                                    "ws": "",
                                    "i": 18,
                                    "text": ":",
                                    "static": true
                                },
                                {
                                    "ws": "",
                                    "i": 19,
                                    "text": "Connection",
                                    "static": false
                                }
                            ],
                            "position": {
                                "startColumn": 37,
                                "startLine": 3,
                                "endColumn": 42,
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
                            "typeName": {
                                "literal": false,
                                "value": "Connection",
                                "kind": "Identifier"
                            },
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
                        "deprecated": false,
                        "endpoint": false
                    },
                    {
                        "ws": [
                            {
                                "ws": " ",
                                "i": 28,
                                "text": "req",
                                "static": false
                            }
                        ],
                        "position": {
                            "startColumn": 59,
                            "startLine": 3,
                            "endColumn": 74,
                            "endLine": 3
                        },
                        "symbolType": [
                            "other"
                        ],
                        "typeNode": {
                            "ws": [
                                {
                                    "ws": " ",
                                    "i": 24,
                                    "text": "http",
                                    "static": false
                                },
                                {
                                    "ws": "",
                                    "i": 25,
                                    "text": ":",
                                    "static": true
                                },
                                {
                                    "ws": "",
                                    "i": 26,
                                    "text": "InRequest",
                                    "static": false
                                }
                            ],
                            "position": {
                                "startColumn": 59,
                                "startLine": 3,
                                "endColumn": 64,
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
                            "typeName": {
                                "literal": false,
                                "value": "InRequest",
                                "kind": "Identifier"
                            },
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
                        "deprecated": false,
                        "endpoint": false
                    }
                ],
                "documentationAttachments": [],
                "kind": "Resource",
                "public": true,
                "native": false,
                "const": false,
                "attached": false,
                "lambda": false,
                "parallel": false,
                "connector": false,
                "deprecated": false,
                "endpoint": false
            }
        ],
        "deprecatedAttachments": [],
        "endpointType": {
            "ws": [
                {
                    "ws": "",
                    "i": 4,
                    "text": "http",
                    "static": false
                }
            ],
            "position": {
                "startColumn": 17,
                "startLine": 2,
                "endColumn": 17,
                "endLine": 6
            },
            "packageAlias": {
                "literal": false,
                "value": "",
                "kind": "Identifier"
            },
            "typeName": {
                "literal": false,
                "value": "http",
                "kind": "Identifier"
            },
            "kind": "UserDefinedType"
        },
        "annotationAttachments": [],
        "initFunction": {
            "position": {
                "startColumn": 17,
                "startLine": 2,
                "endColumn": 17,
                "endLine": 6
            },
            "workers": [],
            "name": {
                "literal": false,
                "value": "service1.<init>",
                "kind": "Identifier"
            },
            "annotationAttachments": [],
            "documentationAttachments": [],
            "deprecatedAttachments": [],
            "endpointNodes": [],
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
            "deprecated": false,
            "endpoint": false
        },
        "variables": [],
        "name": {
            "literal": false,
            "value": "service1",
            "kind": "Identifier"
        },
        "endpointNodes": [],
        "documentationAttachments": [],
        "kind": "Service"
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
        "deprecatedAttachments": [],
        "annotationAttachments": [],
        "endpointNodes": [],
        "body": {
            "statements": [],
            "kind": "Block"
        },
        "returnParameters": [],
        "parameters": [],
        "documentationAttachments": [],
        "kind": "Worker",
        "public": false,
        "native": false,
        "const": false,
        "attached": false,
        "lambda": false,
        "parallel": false,
        "connector": false,
        "deprecated": false,
        "endpoint": false
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
                    "deprecated": false,
                    "endpoint": false
                },
                "kind": "Catch"
            }
        ],
        "kind": "Try"
    },
    "createJMSResource": {
        "ws": [
            {
                "ws": "\n\n            ",
                "i": 9,
                "text": "resource",
                "static": true
            },
            {
                "ws": " ",
                "i": 11,
                "text": "echo1",
                "static": false
            },
            {
                "ws": " ",
                "i": 13,
                "text": "(",
                "static": true
            },
            {
                "ws": "",
                "i": 19,
                "text": ")",
                "static": true
            },
            {
                "ws": " ",
                "i": 21,
                "text": "{",
                "static": true
            },
            {
                "ws": "\n\n            ",
                "i": 24,
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
        "name": {
            "literal": false,
            "value": "echo1",
            "kind": "Identifier"
        },
        "deprecatedAttachments": [],
        "annotationAttachments": [],
        "endpointNodes": [],
        "body": {
            "statements": [],
            "kind": "Block"
        },
        "returnParameters": [],
        "parameters": [
            {
                "ws": [
                    {
                        "ws": " ",
                        "i": 18,
                        "text": "request",
                        "static": false
                    }
                ],
                "position": {
                    "startColumn": 29,
                    "startLine": 3,
                    "endColumn": 44,
                    "endLine": 3
                },
                "symbolType": [
                    "other"
                ],
                "typeNode": {
                    "ws": [
                        {
                            "ws": "",
                            "i": 14,
                            "text": "jms",
                            "static": false
                        },
                        {
                            "ws": "",
                            "i": 15,
                            "text": ":",
                            "static": true
                        },
                        {
                            "ws": "",
                            "i": 16,
                            "text": "JMSMessage",
                            "static": false
                        }
                    ],
                    "position": {
                        "startColumn": 29,
                        "startLine": 3,
                        "endColumn": 33,
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
                    "typeName": {
                        "literal": false,
                        "value": "JMSMessage",
                        "kind": "Identifier"
                    },
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
                "deprecated": false,
                "endpoint": false
            }
        ],
        "documentationAttachments": [],
        "kind": "Resource",
        "public": true,
        "native": false,
        "const": false,
        "attached": false,
        "lambda": false,
        "parallel": false,
        "connector": false,
        "deprecated": false,
        "endpoint": false
    },
    "createBindStmt": {
        "error": "Incorrect format of the generated JSON"
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
                "i": 30,
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
        "resources": [
            {
                "ws": [
                    {
                        "ws": "\n                ",
                        "i": 12,
                        "text": "resource",
                        "static": true
                    },
                    {
                        "ws": " ",
                        "i": 14,
                        "text": "echo1",
                        "static": false
                    },
                    {
                        "ws": " ",
                        "i": 16,
                        "text": "(",
                        "static": true
                    },
                    {
                        "ws": "",
                        "i": 22,
                        "text": ")",
                        "static": true
                    },
                    {
                        "ws": " ",
                        "i": 24,
                        "text": "{",
                        "static": true
                    },
                    {
                        "ws": "\n\n                ",
                        "i": 27,
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
                "name": {
                    "literal": false,
                    "value": "echo1",
                    "kind": "Identifier"
                },
                "deprecatedAttachments": [],
                "annotationAttachments": [],
                "endpointNodes": [],
                "body": {
                    "statements": [],
                    "kind": "Block"
                },
                "returnParameters": [],
                "parameters": [
                    {
                        "ws": [
                            {
                                "ws": " ",
                                "i": 21,
                                "text": "m",
                                "static": false
                            }
                        ],
                        "position": {
                            "startColumn": 33,
                            "startLine": 3,
                            "endColumn": 52,
                            "endLine": 3
                        },
                        "symbolType": [
                            "other"
                        ],
                        "typeNode": {
                            "ws": [
                                {
                                    "ws": "",
                                    "i": 17,
                                    "text": "fs",
                                    "static": false
                                },
                                {
                                    "ws": "",
                                    "i": 18,
                                    "text": ":",
                                    "static": true
                                },
                                {
                                    "ws": "",
                                    "i": 19,
                                    "text": "FileSystemEvent",
                                    "static": false
                                }
                            ],
                            "position": {
                                "startColumn": 33,
                                "startLine": 3,
                                "endColumn": 36,
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
                            "typeName": {
                                "literal": false,
                                "value": "FileSystemEvent",
                                "kind": "Identifier"
                            },
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
                        "deprecated": false,
                        "endpoint": false
                    }
                ],
                "documentationAttachments": [],
                "kind": "Resource",
                "public": true,
                "native": false,
                "const": false,
                "attached": false,
                "lambda": false,
                "parallel": false,
                "connector": false,
                "deprecated": false,
                "endpoint": false
            }
        ],
        "deprecatedAttachments": [],
        "endpointType": {
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
            "typeName": {
                "literal": false,
                "value": "fs",
                "kind": "Identifier"
            },
            "kind": "UserDefinedType"
        },
        "annotationAttachments": [],
        "initFunction": {
            "position": {
                "startColumn": 13,
                "startLine": 2,
                "endColumn": 13,
                "endLine": 6
            },
            "workers": [],
            "name": {
                "literal": false,
                "value": "service1.<init>",
                "kind": "Identifier"
            },
            "annotationAttachments": [],
            "documentationAttachments": [],
            "deprecatedAttachments": [],
            "endpointNodes": [],
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
            "deprecated": false,
            "endpoint": false
        },
        "variables": [],
        "name": {
            "literal": false,
            "value": "service1",
            "kind": "Identifier"
        },
        "endpointNodes": [],
        "documentationAttachments": [],
        "kind": "Service"
    },
    "createFSResource": {
        "ws": [
            {
                "ws": "\n\n            ",
                "i": 9,
                "text": "resource",
                "static": true
            },
            {
                "ws": " ",
                "i": 11,
                "text": "echo1",
                "static": false
            },
            {
                "ws": " ",
                "i": 13,
                "text": "(",
                "static": true
            },
            {
                "ws": "",
                "i": 19,
                "text": ")",
                "static": true
            },
            {
                "ws": " ",
                "i": 21,
                "text": "{",
                "static": true
            },
            {
                "ws": "\n\n            ",
                "i": 24,
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
        "name": {
            "literal": false,
            "value": "echo1",
            "kind": "Identifier"
        },
        "deprecatedAttachments": [],
        "annotationAttachments": [],
        "endpointNodes": [],
        "body": {
            "statements": [],
            "kind": "Block"
        },
        "returnParameters": [],
        "parameters": [
            {
                "ws": [
                    {
                        "ws": " ",
                        "i": 18,
                        "text": "m",
                        "static": false
                    }
                ],
                "position": {
                    "startColumn": 29,
                    "startLine": 3,
                    "endColumn": 48,
                    "endLine": 3
                },
                "symbolType": [
                    "other"
                ],
                "typeNode": {
                    "ws": [
                        {
                            "ws": "",
                            "i": 14,
                            "text": "fs",
                            "static": false
                        },
                        {
                            "ws": "",
                            "i": 15,
                            "text": ":",
                            "static": true
                        },
                        {
                            "ws": "",
                            "i": 16,
                            "text": "FileSystemEvent",
                            "static": false
                        }
                    ],
                    "position": {
                        "startColumn": 29,
                        "startLine": 3,
                        "endColumn": 32,
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
                    "typeName": {
                        "literal": false,
                        "value": "FileSystemEvent",
                        "kind": "Identifier"
                    },
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
                "deprecated": false,
                "endpoint": false
            }
        ],
        "documentationAttachments": [],
        "kind": "Resource",
        "public": true,
        "native": false,
        "const": false,
        "attached": false,
        "lambda": false,
        "parallel": false,
        "connector": false,
        "deprecated": false,
        "endpoint": false
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
            "deprecated": false,
            "endpoint": false
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
                "deprecatedAttachments": [],
                "annotationAttachments": [],
                "endpointNodes": [],
                "body": {
                    "statements": [],
                    "kind": "Block"
                },
                "returnParameters": [],
                "parameters": [],
                "documentationAttachments": [],
                "kind": "Worker",
                "public": false,
                "native": false,
                "const": false,
                "attached": false,
                "lambda": false,
                "parallel": false,
                "connector": false,
                "deprecated": false,
                "endpoint": false
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
                "deprecatedAttachments": [],
                "annotationAttachments": [],
                "endpointNodes": [],
                "body": {
                    "statements": [],
                    "kind": "Block"
                },
                "returnParameters": [],
                "parameters": [],
                "documentationAttachments": [],
                "kind": "Worker",
                "public": false,
                "native": false,
                "const": false,
                "attached": false,
                "lambda": false,
                "parallel": false,
                "connector": false,
                "deprecated": false,
                "endpoint": false
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
            "deprecated": false,
            "endpoint": false
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
    "createWSServiceDef": {
        "ws": [
            {
                "ws": "\n                ",
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
                "ws": "\n                ",
                "i": 83,
                "text": "}",
                "static": true
            }
        ],
        "position": {
            "startColumn": 17,
            "startLine": 2,
            "endColumn": 17,
            "endLine": 12
        },
        "resources": [
            {
                "ws": [
                    {
                        "ws": "\n                    ",
                        "i": 12,
                        "text": "resource",
                        "static": true
                    },
                    {
                        "ws": " ",
                        "i": 14,
                        "text": "onOpen",
                        "static": false
                    },
                    {
                        "ws": "",
                        "i": 15,
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
                        "ws": "\n            \n                    ",
                        "i": 28,
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
                "name": {
                    "literal": false,
                    "value": "onOpen",
                    "kind": "Identifier"
                },
                "deprecatedAttachments": [],
                "annotationAttachments": [],
                "endpointNodes": [],
                "body": {
                    "statements": [],
                    "kind": "Block"
                },
                "returnParameters": [],
                "parameters": [
                    {
                        "ws": [
                            {
                                "ws": " ",
                                "i": 20,
                                "text": "conn",
                                "static": false
                            }
                        ],
                        "position": {
                            "startColumn": 37,
                            "startLine": 3,
                            "endColumn": 51,
                            "endLine": 3
                        },
                        "symbolType": [
                            "other"
                        ],
                        "typeNode": {
                            "ws": [
                                {
                                    "ws": "",
                                    "i": 16,
                                    "text": "ws",
                                    "static": false
                                },
                                {
                                    "ws": "",
                                    "i": 17,
                                    "text": ":",
                                    "static": true
                                },
                                {
                                    "ws": "",
                                    "i": 18,
                                    "text": "Connection",
                                    "static": false
                                }
                            ],
                            "position": {
                                "startColumn": 37,
                                "startLine": 3,
                                "endColumn": 40,
                                "endLine": 3
                            },
                            "symbolType": [
                                "other"
                            ],
                            "packageAlias": {
                                "literal": false,
                                "value": "ws",
                                "kind": "Identifier"
                            },
                            "typeName": {
                                "literal": false,
                                "value": "Connection",
                                "kind": "Identifier"
                            },
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
                        "deprecated": false,
                        "endpoint": false
                    }
                ],
                "documentationAttachments": [],
                "kind": "Resource",
                "public": true,
                "native": false,
                "const": false,
                "attached": false,
                "lambda": false,
                "parallel": false,
                "connector": false,
                "deprecated": false,
                "endpoint": false
            },
            {
                "ws": [
                    {
                        "ws": "\n                    ",
                        "i": 31,
                        "text": "resource",
                        "static": true
                    },
                    {
                        "ws": " ",
                        "i": 33,
                        "text": "onTextMessage",
                        "static": false
                    },
                    {
                        "ws": "",
                        "i": 34,
                        "text": "(",
                        "static": true
                    },
                    {
                        "ws": "",
                        "i": 40,
                        "text": ",",
                        "static": true
                    },
                    {
                        "ws": "",
                        "i": 47,
                        "text": ")",
                        "static": true
                    },
                    {
                        "ws": " ",
                        "i": 49,
                        "text": "{",
                        "static": true
                    },
                    {
                        "ws": "\n            \n                    ",
                        "i": 54,
                        "text": "}",
                        "static": true
                    }
                ],
                "position": {
                    "startColumn": 21,
                    "startLine": 6,
                    "endColumn": 21,
                    "endLine": 8
                },
                "workers": [],
                "name": {
                    "literal": false,
                    "value": "onTextMessage",
                    "kind": "Identifier"
                },
                "deprecatedAttachments": [],
                "annotationAttachments": [],
                "endpointNodes": [],
                "body": {
                    "statements": [],
                    "kind": "Block"
                },
                "returnParameters": [],
                "parameters": [
                    {
                        "ws": [
                            {
                                "ws": " ",
                                "i": 39,
                                "text": "conn",
                                "static": false
                            }
                        ],
                        "position": {
                            "startColumn": 44,
                            "startLine": 6,
                            "endColumn": 58,
                            "endLine": 6
                        },
                        "symbolType": [
                            "other"
                        ],
                        "typeNode": {
                            "ws": [
                                {
                                    "ws": "",
                                    "i": 35,
                                    "text": "ws",
                                    "static": false
                                },
                                {
                                    "ws": "",
                                    "i": 36,
                                    "text": ":",
                                    "static": true
                                },
                                {
                                    "ws": "",
                                    "i": 37,
                                    "text": "Connection",
                                    "static": false
                                }
                            ],
                            "position": {
                                "startColumn": 44,
                                "startLine": 6,
                                "endColumn": 47,
                                "endLine": 6
                            },
                            "symbolType": [
                                "other"
                            ],
                            "packageAlias": {
                                "literal": false,
                                "value": "ws",
                                "kind": "Identifier"
                            },
                            "typeName": {
                                "literal": false,
                                "value": "Connection",
                                "kind": "Identifier"
                            },
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
                        "deprecated": false,
                        "endpoint": false
                    },
                    {
                        "ws": [
                            {
                                "ws": " ",
                                "i": 46,
                                "text": "frame",
                                "static": false
                            }
                        ],
                        "position": {
                            "startColumn": 64,
                            "startLine": 6,
                            "endColumn": 77,
                            "endLine": 6
                        },
                        "symbolType": [
                            "other"
                        ],
                        "typeNode": {
                            "ws": [
                                {
                                    "ws": " ",
                                    "i": 42,
                                    "text": "ws",
                                    "static": false
                                },
                                {
                                    "ws": "",
                                    "i": 43,
                                    "text": ":",
                                    "static": true
                                },
                                {
                                    "ws": "",
                                    "i": 44,
                                    "text": "TextFrame",
                                    "static": false
                                }
                            ],
                            "position": {
                                "startColumn": 64,
                                "startLine": 6,
                                "endColumn": 67,
                                "endLine": 6
                            },
                            "symbolType": [
                                "other"
                            ],
                            "packageAlias": {
                                "literal": false,
                                "value": "ws",
                                "kind": "Identifier"
                            },
                            "typeName": {
                                "literal": false,
                                "value": "TextFrame",
                                "kind": "Identifier"
                            },
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
                        "deprecated": false,
                        "endpoint": false
                    }
                ],
                "documentationAttachments": [],
                "kind": "Resource",
                "public": true,
                "native": false,
                "const": false,
                "attached": false,
                "lambda": false,
                "parallel": false,
                "connector": false,
                "deprecated": false,
                "endpoint": false
            },
            {
                "ws": [
                    {
                        "ws": "\n                    ",
                        "i": 57,
                        "text": "resource",
                        "static": true
                    },
                    {
                        "ws": " ",
                        "i": 59,
                        "text": "onClose",
                        "static": false
                    },
                    {
                        "ws": "",
                        "i": 60,
                        "text": "(",
                        "static": true
                    },
                    {
                        "ws": "",
                        "i": 66,
                        "text": ",",
                        "static": true
                    },
                    {
                        "ws": "",
                        "i": 73,
                        "text": ")",
                        "static": true
                    },
                    {
                        "ws": " ",
                        "i": 75,
                        "text": "{",
                        "static": true
                    },
                    {
                        "ws": "\n            \n                    ",
                        "i": 80,
                        "text": "}",
                        "static": true
                    }
                ],
                "position": {
                    "startColumn": 21,
                    "startLine": 9,
                    "endColumn": 21,
                    "endLine": 11
                },
                "workers": [],
                "name": {
                    "literal": false,
                    "value": "onClose",
                    "kind": "Identifier"
                },
                "deprecatedAttachments": [],
                "annotationAttachments": [],
                "endpointNodes": [],
                "body": {
                    "statements": [],
                    "kind": "Block"
                },
                "returnParameters": [],
                "parameters": [
                    {
                        "ws": [
                            {
                                "ws": " ",
                                "i": 65,
                                "text": "conn",
                                "static": false
                            }
                        ],
                        "position": {
                            "startColumn": 38,
                            "startLine": 9,
                            "endColumn": 52,
                            "endLine": 9
                        },
                        "symbolType": [
                            "other"
                        ],
                        "typeNode": {
                            "ws": [
                                {
                                    "ws": "",
                                    "i": 61,
                                    "text": "ws",
                                    "static": false
                                },
                                {
                                    "ws": "",
                                    "i": 62,
                                    "text": ":",
                                    "static": true
                                },
                                {
                                    "ws": "",
                                    "i": 63,
                                    "text": "Connection",
                                    "static": false
                                }
                            ],
                            "position": {
                                "startColumn": 38,
                                "startLine": 9,
                                "endColumn": 41,
                                "endLine": 9
                            },
                            "symbolType": [
                                "other"
                            ],
                            "packageAlias": {
                                "literal": false,
                                "value": "ws",
                                "kind": "Identifier"
                            },
                            "typeName": {
                                "literal": false,
                                "value": "Connection",
                                "kind": "Identifier"
                            },
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
                        "deprecated": false,
                        "endpoint": false
                    },
                    {
                        "ws": [
                            {
                                "ws": " ",
                                "i": 72,
                                "text": "frame",
                                "static": false
                            }
                        ],
                        "position": {
                            "startColumn": 58,
                            "startLine": 9,
                            "endColumn": 72,
                            "endLine": 9
                        },
                        "symbolType": [
                            "other"
                        ],
                        "typeNode": {
                            "ws": [
                                {
                                    "ws": " ",
                                    "i": 68,
                                    "text": "ws",
                                    "static": false
                                },
                                {
                                    "ws": "",
                                    "i": 69,
                                    "text": ":",
                                    "static": true
                                },
                                {
                                    "ws": "",
                                    "i": 70,
                                    "text": "CloseFrame",
                                    "static": false
                                }
                            ],
                            "position": {
                                "startColumn": 58,
                                "startLine": 9,
                                "endColumn": 61,
                                "endLine": 9
                            },
                            "symbolType": [
                                "other"
                            ],
                            "packageAlias": {
                                "literal": false,
                                "value": "ws",
                                "kind": "Identifier"
                            },
                            "typeName": {
                                "literal": false,
                                "value": "CloseFrame",
                                "kind": "Identifier"
                            },
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
                        "deprecated": false,
                        "endpoint": false
                    }
                ],
                "documentationAttachments": [],
                "kind": "Resource",
                "public": true,
                "native": false,
                "const": false,
                "attached": false,
                "lambda": false,
                "parallel": false,
                "connector": false,
                "deprecated": false,
                "endpoint": false
            }
        ],
        "deprecatedAttachments": [],
        "endpointType": {
            "ws": [
                {
                    "ws": "",
                    "i": 4,
                    "text": "ws",
                    "static": false
                }
            ],
            "position": {
                "startColumn": 17,
                "startLine": 2,
                "endColumn": 17,
                "endLine": 12
            },
            "packageAlias": {
                "literal": false,
                "value": "",
                "kind": "Identifier"
            },
            "typeName": {
                "literal": false,
                "value": "ws",
                "kind": "Identifier"
            },
            "kind": "UserDefinedType"
        },
        "annotationAttachments": [],
        "initFunction": {
            "position": {
                "startColumn": 17,
                "startLine": 2,
                "endColumn": 17,
                "endLine": 12
            },
            "workers": [],
            "name": {
                "literal": false,
                "value": "service1.<init>",
                "kind": "Identifier"
            },
            "annotationAttachments": [],
            "documentationAttachments": [],
            "deprecatedAttachments": [],
            "endpointNodes": [],
            "body": {
                "position": {
                    "startColumn": 17,
                    "startLine": 2,
                    "endColumn": 17,
                    "endLine": 12
                },
                "statements": [
                    {
                        "position": {
                            "startColumn": 17,
                            "startLine": 2,
                            "endColumn": 17,
                            "endLine": 12
                        },
                        "expressions": [],
                        "kind": "Return"
                    }
                ],
                "kind": "Block"
            },
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
            "deprecated": false,
            "endpoint": false
        },
        "variables": [],
        "name": {
            "literal": false,
            "value": "service1",
            "kind": "Identifier"
        },
        "endpointNodes": [],
        "documentationAttachments": [],
        "kind": "Service"
    },
    "createConnectorAction": {
        "ws": [
            {
                "ws": "\n\n            ",
                "i": 11,
                "text": "action",
                "static": true
            },
            {
                "ws": " ",
                "i": 13,
                "text": "action1",
                "static": false
            },
            {
                "ws": "",
                "i": 14,
                "text": "(",
                "static": true
            },
            {
                "ws": "",
                "i": 15,
                "text": ")",
                "static": true
            },
            {
                "ws": "",
                "i": 16,
                "text": "{",
                "static": true
            },
            {
                "ws": "\n\n            ",
                "i": 19,
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
        "name": {
            "literal": false,
            "value": "action1",
            "kind": "Identifier"
        },
        "deprecatedAttachments": [],
        "annotationAttachments": [],
        "endpointNodes": [],
        "body": {
            "statements": [],
            "kind": "Block"
        },
        "returnParameters": [],
        "parameters": [],
        "documentationAttachments": [],
        "kind": "Action",
        "public": true,
        "native": false,
        "const": false,
        "attached": false,
        "lambda": false,
        "parallel": false,
        "connector": false,
        "deprecated": false,
        "endpoint": false
    },
    "createFTPResource": {
        "ws": [
            {
                "ws": "\n\n            ",
                "i": 9,
                "text": "resource",
                "static": true
            },
            {
                "ws": " ",
                "i": 11,
                "text": "echo1",
                "static": false
            },
            {
                "ws": " ",
                "i": 13,
                "text": "(",
                "static": true
            },
            {
                "ws": "",
                "i": 19,
                "text": ")",
                "static": true
            },
            {
                "ws": " ",
                "i": 21,
                "text": "{",
                "static": true
            },
            {
                "ws": "\n\n            ",
                "i": 24,
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
        "name": {
            "literal": false,
            "value": "echo1",
            "kind": "Identifier"
        },
        "deprecatedAttachments": [],
        "annotationAttachments": [],
        "endpointNodes": [],
        "body": {
            "statements": [],
            "kind": "Block"
        },
        "returnParameters": [],
        "parameters": [
            {
                "ws": [
                    {
                        "ws": " ",
                        "i": 18,
                        "text": "m",
                        "static": false
                    }
                ],
                "position": {
                    "startColumn": 29,
                    "startLine": 3,
                    "endColumn": 48,
                    "endLine": 3
                },
                "symbolType": [
                    "other"
                ],
                "typeNode": {
                    "ws": [
                        {
                            "ws": "",
                            "i": 14,
                            "text": "ftp",
                            "static": false
                        },
                        {
                            "ws": "",
                            "i": 15,
                            "text": ":",
                            "static": true
                        },
                        {
                            "ws": "",
                            "i": 16,
                            "text": "FTPServerEvent",
                            "static": false
                        }
                    ],
                    "position": {
                        "startColumn": 29,
                        "startLine": 3,
                        "endColumn": 33,
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
                    "typeName": {
                        "literal": false,
                        "value": "FTPServerEvent",
                        "kind": "Identifier"
                    },
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
                "deprecated": false,
                "endpoint": false
            }
        ],
        "documentationAttachments": [],
        "kind": "Resource",
        "public": true,
        "native": false,
        "const": false,
        "attached": false,
        "lambda": false,
        "parallel": false,
        "connector": false,
        "deprecated": false,
        "endpoint": false
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
    "createTransaction": {
        "error": "Incorrect format of the generated JSON"
    }
}