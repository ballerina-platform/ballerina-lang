package ballerina.mock;

@Description{value:"Modifies global connector instance's arguments for mocking purposes"}
@Param{value:"mockableConnectorPathExpr: A path like syntax to identify and navigate the connector instances of a ballerina service"}
@Param{value: "value: Mock value to set (e.g.: endpoint URL)"}
native function setValue(string mockableConnectorPathExpr, string value);