package ballerina.data.sql;

import ballerina.doc;

@doc:Description { value: "Parameter struct "}
struct Parameter {
	string sqlType;
	any value;
	int direction;
	string structuredType;
}