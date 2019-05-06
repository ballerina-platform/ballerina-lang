import ballerina/io;

function fnMissingSigAndBody() returns json {
	json fn = {"nodeKind":"package", "tokenList":[{"tokenType":9, "text":"EOF", "startPos":8, "endPos":8, "lineNumber":1, "index":2, "whiteSpace":null}], "definitionList":[{"nodeKind":"errorNode", "errorFunction":{"nodeKind":"function", "tokenList":[{"tokenType":15, "text":"function", "startPos":1, "endPos":8, "lineNumber":1, "index":1, "whiteSpace":null}], "fnSignature":null, "blockNode":null}}]};
	return fn;
}
function fnIncompleteSignature() returns json {
	json fn = {"nodeKind":"package", "tokenList":[{"tokenType":9, "text":"EOF", "startPos":12, "endPos":12, "lineNumber":1, "index":3, "whiteSpace":null}], "definitionList":[{"nodeKind":"errorNode", "errorFunction":{"nodeKind":"function", "tokenList":[{"tokenType":15, "text":"function", "startPos":1, "endPos":8, "lineNumber":1, "index":1, "whiteSpace":null}], "fnSignature":{"nodeKind":"functionSignatureNode", "functionIdentifier":{"nodeKind":"identifier", "tokenList":[{"tokenType":7, "text":"foo", "startPos":10, "endPos":12, "lineNumber":1, "index":2, "whiteSpace":"space "}], "identifier":"foo"}}, "blockNode":null}}]};
	return fn;
}