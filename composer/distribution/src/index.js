const diagram = require('@ballerina/diagram');

const testJson = {
    "ast": {
        "id": "0f6a3dab-00a8-4976-9a4a-2141eb167bd9",
        "kind": "CompilationUnit",
        "name": "untitled.bal",
        "position": {
            "endColumn": 1,
            "endLine": 1,
            "startColumn": 1,
            "startLine": 1
        },
        "topLevelNodes": [
            {
                "abstract": false,
                "annotationAttachments": [],
                "attached": false,
                "body": {
                    "id": "93c7de37-5b5e-4024-9e9d-062bef877a9e",
                    "kind": "Block",
                    "statements": []
                },
                "compensate": false,
                "connector": false,
                "defaultable": false,
                "defaultableParameters": [],
                "defaultable_checked": false,
                "deprecated": false,
                "deprecatedAttachments": [],
                "endpointNodes": [],
                "final": false,
                "function_final": false,
                "id": "c269dbd9-d02f-495e-9c30-0270759c9821",
                "interface": false,
                "kind": "Function",
                "lambda": false,
                "name": {
                    "id": "33cb324b-6d63-49b8-8471-55bd2d46a744",
                    "kind": "Identifier",
                    "literal": false,
                    "position": {
                        "endColumn": 10,
                        "endLine": 2,
                        "startColumn": 10,
                        "startLine": 2
                    },
                    "value": "name"
                },
                "native": false,
                "parallel": false,
                "parameters": [],
                "position": {
                    "endColumn": 1,
                    "endLine": 4,
                    "startColumn": 1,
                    "startLine": 2
                },
                "private": false,
                "public": false,
                "readonly": false,
                "record": false,
                "returnTypeAnnotationAttachments": [],
                "returnTypeNode": {
                    "grouped": false,
                    "id": "e9ae10bc-b4b9-411d-b630-8536ce809370",
                    "kind": "ValueType",
                    "nullable": false,
                    "position": {
                        "endColumn": 15,
                        "endLine": 2,
                        "startColumn": 10,
                        "startLine": 2
                    },
                    "symbolType": [
                        "null"
                    ],
                    "typeKind": "nil"
                },
                "workers": [],
                "ws": [
                    {
                        "i": 1,
                        "static": true,
                        "text": "function",
                        "ws": "\n"
                    },
                    {
                        "i": 3,
                        "static": false,
                        "text": "name",
                        "ws": " "
                    },
                    {
                        "i": 4,
                        "static": true,
                        "text": "(",
                        "ws": ""
                    },
                    {
                        "i": 5,
                        "static": true,
                        "text": ")",
                        "ws": ""
                    },
                    {
                        "i": 7,
                        "static": true,
                        "text": "{",
                        "ws": " "
                    },
                    {
                        "i": 9,
                        "static": true,
                        "text": "}",
                        "ws": "\n\n"
                    }
                ]
            }
        ],
        "ws": [
            {
                "i": 10,
                "static": true,
                "text": "<EOF>",
                "ws": ""
            }
        ]
    },
    "parseSuccess": true
}

diagram.renderStaticDiagram(document.getElementById('diagram'), testJson.ast)