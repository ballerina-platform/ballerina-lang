import ballerina/io;

function fnSignatureValid() returns json {
	json fnJson = {"nodeKind":"package", "tokenList":[{"tokenType":9, "text":"EOF", "startPos":1, "endPos":1, "lineNumber":2, "index":7, "whiteSpace":null}], "definitionList":[{"nodeKind":"function", "tokenList":[{"tokenType":15, "text":"function", "startPos":1, "endPos":8, "lineNumber":1, "index":1, "whiteSpace":null}], "fnSignature":{"nodeKind":"functionSignatureNode", "tokenList":[{"tokenType":2, "text":"(", "startPos":13, "endPos":13, "lineNumber":1, "index":3, "whiteSpace":null}, {"tokenType":3, "text":")", "startPos":14, "endPos":14, "lineNumber":1, "index":4, "whiteSpace":null}], "functionIdentifier":{"nodeKind":"identifier", "tokenList":[{"tokenType":7, "text":"foo", "startPos":10, "endPos":12, "lineNumber":1, "index":2, "whiteSpace":" "}], "identifier":"foo"}}, "blockNode":{"nodeKind":"blockNode", "tokenList":[{"tokenType":0, "text":"{", "startPos":15, "endPos":15, "lineNumber":1, "index":5, "whiteSpace":null}, {"tokenType":1, "text":"}", "startPos":1, "endPos":1, "lineNumber":2, "index":6, "whiteSpace":"\n"}], "statementList":[]}}]};
	return fnJson;
}
function fnSigMissingIdentifier() returns json {
	json fnJson = {"nodeKind":"package", "tokenList":[{"tokenType":9, "text":"EOF", "startPos":1, "endPos":1, "lineNumber":2, "index":6, "whiteSpace":null}], "definitionList":[{"nodeKind":"errorNode", "tokenList":[{"tokenType":2, "text":"(", "startPos":10, "endPos":10, "lineNumber":1, "index":2, "whiteSpace":" "}, {"tokenType":3, "text":")", "startPos":11, "endPos":11, "lineNumber":1, "index":3, "whiteSpace":null}, {"tokenType":0, "text":"{", "startPos":12, "endPos":12, "lineNumber":1, "index":4, "whiteSpace":null}, {"tokenType":1, "text":"}", "startPos":1, "endPos":1, "lineNumber":2, "index":5, "whiteSpace":"\n"}], "errorFunction":{"nodeKind":"function", "tokenList":[{"tokenType":15, "text":"function", "startPos":1, "endPos":8, "lineNumber":1, "index":1, "whiteSpace":null}], "fnSignature":null, "blockNode":null}}]};
	return fnJson;
}
function fnSignatureMissingLparen() returns json {
	json fnJson = {"nodeKind":"package", "tokenList":[{"tokenType":9, "text":"EOF", "startPos":1, "endPos":1, "lineNumber":2, "index":6, "whiteSpace":null}], "definitionList":[{"nodeKind":"errorNode", "tokenList":[{"tokenType":3, "text":")", "startPos":14, "endPos":14, "lineNumber":1, "index":3, "whiteSpace":" "}, {"tokenType":0, "text":"{", "startPos":15, "endPos":15, "lineNumber":1, "index":4, "whiteSpace":null}, {"tokenType":1, "text":"}", "startPos":1, "endPos":1, "lineNumber":2, "index":5, "whiteSpace":"\n"}], "errorFunction":{"nodeKind":"function", "tokenList":[{"tokenType":15, "text":"function", "startPos":1, "endPos":8, "lineNumber":1, "index":1, "whiteSpace":null}], "fnSignature":{"nodeKind":"functionSignatureNode", "functionIdentifier":{"nodeKind":"identifier", "tokenList":[{"tokenType":7, "text":"foo", "startPos":10, "endPos":12, "lineNumber":1, "index":2, "whiteSpace":" "}], "identifier":"foo"}}, "blockNode":null}}]};
	return fnJson;
}
function fnSignatureMissingRparen() returns json {
	json fnJson = {"nodeKind":"package", "tokenList":[{"tokenType":9, "text":"EOF", "startPos":1, "endPos":1, "lineNumber":2, "index":6, "whiteSpace":null}], "definitionList":[{"nodeKind":"errorNode", "tokenList":[{"tokenType":0, "text":"{", "startPos":15, "endPos":15, "lineNumber":1, "index":4, "whiteSpace":null}, {"tokenType":1, "text":"}", "startPos":1, "endPos":1, "lineNumber":2, "index":5, "whiteSpace":"\n"}], "errorFunction":{"nodeKind":"function", "tokenList":[{"tokenType":15, "text":"function", "startPos":1, "endPos":8, "lineNumber":1, "index":1, "whiteSpace":null}], "fnSignature":{"nodeKind":"functionSignatureNode", "tokenList":[{"tokenType":2, "text":"(", "startPos":14, "endPos":14, "lineNumber":1, "index":3, "whiteSpace":" "}], "functionIdentifier":{"nodeKind":"identifier", "tokenList":[{"tokenType":7, "text":"foo", "startPos":10, "endPos":12, "lineNumber":1, "index":2, "whiteSpace":" "}], "identifier":"foo"}}, "blockNode":null}}]};
	return fnJson;
}
function fnIncompleteSignature() returns json {
	json fnJson = {"nodeKind":"package", "tokenList":[{"tokenType":9, "text":"EOF", "startPos":12, "endPos":12, "lineNumber":1, "index":3, "whiteSpace":null}], "definitionList":[{"nodeKind":"errorNode", "tokenList":[], "errorFunction":{"nodeKind":"function", "tokenList":[{"tokenType":15, "text":"function", "startPos":1, "endPos":8, "lineNumber":1, "index":1, "whiteSpace":null}], "fnSignature":{"nodeKind":"functionSignatureNode", "functionIdentifier":{"nodeKind":"identifier", "tokenList":[{"tokenType":7, "text":"foo", "startPos":10, "endPos":12, "lineNumber":1, "index":2, "whiteSpace":" "}], "identifier":"foo"}}, "blockNode":null}}]};
	return fnJson;
}
function fnMissingSigAndBody() returns json {
	json fnJson = {"nodeKind":"package", "tokenList":[{"tokenType":9, "text":"EOF", "startPos":8, "endPos":8, "lineNumber":1, "index":2, "whiteSpace":null}], "definitionList":[{"nodeKind":"errorNode", "tokenList":[], "errorFunction":{"nodeKind":"function", "tokenList":[{"tokenType":15, "text":"function", "startPos":1, "endPos":8, "lineNumber":1, "index":1, "whiteSpace":null}], "fnSignature":null, "blockNode":null}}]};
	return fnJson;
}

