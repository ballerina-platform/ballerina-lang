package ballerina.data.sql;

struct Parameter {
	string sqlType;
	var value;
	int direction;
}