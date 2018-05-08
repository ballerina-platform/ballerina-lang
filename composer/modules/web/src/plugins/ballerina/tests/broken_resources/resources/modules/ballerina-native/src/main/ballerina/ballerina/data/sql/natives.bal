
import ballerina/doc;

connector ClientConnector (map options) {
    map sharedMap = {};

	@doc:Description { value:"The call action implementation for SQL connector."}
	@doc:Param { value:"c: Connector" }
	@doc:Param { value:"query: String" }
	@doc:Param { value:"parameters: Parameter array" }
	@doc:Return { value:"datatable: Result set for the query" }
	native action call (ClientConnector c, string query, Parameter[] parameters) (datatable);

	@doc:Description { value:"The select action implementation for SQL connector."}
	@doc:Param { value:"c: Connector" }
	@doc:Param { value:"query: String" }
	@doc:Param { value:"parameters: Parameter array" }
	@doc:Return { value:"datatable: Result set for the query" }
	native action select (ClientConnector c, string query, Parameter[] parameters) (datatable);

	@doc:Description { value:"The close action implementation for SQL connector."}
	@doc:Param { value:"c: Connector" }
	native action close (ClientConnector c);

	@doc:Description { value:"The update action implementation for SQL connector."}
	@doc:Param { value:"c: Connector" }
	@doc:Param { value:"query: String" }
	@doc:Param { value:"parameters: Parameter array" }
	@doc:Return { value:"integer: Updated row count" }
	native action update (ClientConnector c, string query, Parameter[] parameters) (int);

	@doc:Description { value:"The call action implementation for SQL connector."}
	@doc:Param { value:"c: Connector" }
	@doc:Param { value:"query: String" }
	@doc:Param { value:"parameters: Parameter array" }
	@doc:Return { value:"updatedCounts: Array of update counts" }
	native action batchUpdate (ClientConnector c, string query, Parameter[][] parameters) (int[]);

	@doc:Description { value:"The update with generated keys given columns action implementation for SQL connector."}
	@doc:Param { value:"c: Connector" }
	@doc:Param { value:"query: String" }
	@doc:Param { value:"parameters: Parameter array" }
	@doc:Param { value:"keyColumns: String array" }
	@doc:Return { value:"rowCount: Updated row count" }
	@doc:Return { value:"generatedKeys: Generated keys array" }
	native action updateWithGeneratedKeys (ClientConnector c, string query, Parameter[] parameters, string[] keyColumns) (int, string[]);

}
