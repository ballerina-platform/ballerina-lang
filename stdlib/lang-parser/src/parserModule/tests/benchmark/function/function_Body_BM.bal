// Copyright (c) 2019 WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
//
// WSO2 Inc. licenses this file to you under the Apache License,
// Version 2.0 (the "License"); you may not use this file except
// in compliance with the License.
// You may obtain a copy of the License at
//
// http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing,
// software distributed under the License is distributed on an
// "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
// KIND, either express or implied.  See the License for the
// specific language governing permissions and limitations
// under the License.

function fnBodyValid() returns json {
    json fnBody = {"nodeKind":"package", "tokenList":[{"tokenType":51, "text":"EOF", "startPos":1, "endPos":1, "lineNumber":4, "index":10, "whiteSpace":"\n"}], "definitionList":[{"nodeKind":"function", "tokenList":[{"tokenType":53, "text":"function", "startPos":1, "endPos":8, "lineNumber":1, "index":1, "whiteSpace":null}], "fnSignature":{"nodeKind":"functionSignature", "tokenList":[{"tokenType":6, "text":"(", "startPos":14, "endPos":14, "lineNumber":1, "index":3, "whiteSpace":" "}, {"tokenType":7, "text":")", "startPos":15, "endPos":15, "lineNumber":1, "index":4, "whiteSpace":null}], "functionIdentifier":{"nodeKind":"identifier", "tokenList":[{"tokenType":46, "text":"foo", "startPos":10, "endPos":12, "lineNumber":1, "index":2, "whiteSpace":" "}], "identifier":"foo"}}, "blockNode":{"nodeKind":"blockNode", "tokenList":[{"tokenType":0, "text":"{", "startPos":16, "endPos":16, "lineNumber":1, "index":5, "whiteSpace":null}, {"tokenType":1, "text":"}", "startPos":1, "endPos":1, "lineNumber":3, "index":9, "whiteSpace":"\n"}], "statementList":[{"nodeKind":"variableDefinitionStatement", "tokenList":[{"tokenType":54, "text":"int", "startPos":1, "endPos":3, "lineNumber":2, "index":6, "whiteSpace":"\n"}, {"tokenType":2, "text":";", "startPos":6, "endPos":6, "lineNumber":2, "index":8, "whiteSpace":null}], "valueKind":"int", "varIdentifier":{"nodeKind":"variableReferenceIdentifier", "tokenList":[{"tokenType":46, "text":"a", "startPos":5, "endPos":5, "lineNumber":2, "index":7, "whiteSpace":" "}], "varIdentifier":"a"}, "expression":null}]}}]};
    return fnBody;
}

function fnBodyMissingLbrace() returns json {
    json fnBody = {"nodeKind":"package", "tokenList":[{"tokenType":51, "text":"EOF", "startPos":1, "endPos":1, "lineNumber":2, "index":6, "whiteSpace":"\n"}], "definitionList":[{"nodeKind":"function", "tokenList":[{"tokenType":53, "text":"function", "startPos":1, "endPos":8, "lineNumber":1, "index":1, "whiteSpace":null}], "fnSignature":{"nodeKind":"functionSignature", "tokenList":[{"tokenType":6, "text":"(", "startPos":14, "endPos":14, "lineNumber":1, "index":3, "whiteSpace":" "}, {"tokenType":7, "text":")", "startPos":15, "endPos":15, "lineNumber":1, "index":4, "whiteSpace":null}], "functionIdentifier":{"nodeKind":"identifier", "tokenList":[{"tokenType":46, "text":"foo", "startPos":10, "endPos":12, "lineNumber":1, "index":2, "whiteSpace":" "}], "identifier":"foo"}}, "blockNode":{"nodeKind":"errorFunctionBody", "tokenList":[{"tokenType":0, "text":"{", "startPos":-1, "endPos":-1, "lineNumber":0, "index":-1, "whiteSpace":""}, {"tokenType":1, "text":"}", "startPos":16, "endPos":16, "lineNumber":1, "index":5, "whiteSpace":null}], "statementList":[]}}]};
    return fnBody;
}

function fnBodyMissingRbrace() returns json {
    json fnBody = {"nodeKind":"package", "tokenList":[{"tokenType":51, "text":"EOF", "startPos":1, "endPos":1, "lineNumber":2, "index":6, "whiteSpace":"\n"}], "definitionList":[{"nodeKind":"function", "tokenList":[{"tokenType":53, "text":"function", "startPos":1, "endPos":8, "lineNumber":1, "index":1, "whiteSpace":null}], "fnSignature":{"nodeKind":"functionSignature", "tokenList":[{"tokenType":6, "text":"(", "startPos":14, "endPos":14, "lineNumber":1, "index":3, "whiteSpace":" "}, {"tokenType":7, "text":")", "startPos":15, "endPos":15, "lineNumber":1, "index":4, "whiteSpace":null}], "functionIdentifier":{"nodeKind":"identifier", "tokenList":[{"tokenType":46, "text":"foo", "startPos":10, "endPos":12, "lineNumber":1, "index":2, "whiteSpace":" "}], "identifier":"foo"}}, "blockNode":{"nodeKind":"errorFunctionBody", "tokenList":[{"tokenType":0, "text":"{", "startPos":16, "endPos":16, "lineNumber":1, "index":5, "whiteSpace":null}, {"tokenType":1, "text":"}", "startPos":-1, "endPos":-1, "lineNumber":0, "index":-1, "whiteSpace":""}], "statementList":[]}}]};
    return fnBody;
}
