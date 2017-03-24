package ballerina.data.sql;

import ballerina.doc;

@doc:Description { value: "Parameter struct "}
struct Parameter {
	string sqlType;
	string value;
	int direction;
	string structuredType;
}