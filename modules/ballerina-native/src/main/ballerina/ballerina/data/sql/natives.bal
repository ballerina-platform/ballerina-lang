package ballerina.data.sql;

import ballerina.doc;

@doc:Description { value: "Parameter struct "}
struct Parameter {
	string sqlType;
	any value;
	int direction;
	string structuredType;
}

@doc:Description { value: "DB Connection properties struct "}
struct ConnectionProperties {
	string jdbcUrl;
	string dataSourceClassName;
	string userName;
	string password;
	string connectionTestQuery;
	string poolName;
	string catalog;
	string connectionInitSql;
	string driverClassName;
	string transactionIsolation;
	boolean autoCommit = true;
	boolean isolateInternalQueries;
	boolean allowPoolSuspension;
	boolean readOnly;
	int maximumPoolSize = -1;
	int connectionTimeout = -1;
	int idleTimeout = -1;
	int minimumIdle = -1;
	int maxLifetime = -1;
	int validationTimeout = -1;
	int leakDetectionThreshold = -1;
	map datasourceProperties;
}

connector ClientConnector (string dbType, string hostOrPath, int port, string dbName, string username, string password, ConnectionProperties options) {
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

@doc:Description { value:"Construct MYSQL DB jdbc url in the format of  jdbc:mysql://[HOST]:[PORT]/[database]"}
const string MYSQL = "MYSQL";

@doc:Description { value:"Construct SQLSERVER DB jdbc url in the format of  jdbc:sqlserver://[HOST]:[PORT];databaseName=[database]"}
const string SQLSERVER = "SQLSERVER";

@doc:Description { value:"Construct ORACLE  DB jdbc url in the format of  jdbc:oracle:thin:[username/password]@[HOST]:[PORT]/[database]"}
const string ORACLE = "ORACLE";

@doc:Description { value:"Construct SYBASE DB jdbc url in the format of  jdbc:sybase:Tds:[HOST]:[PORT]/[database]"}
const string SYBASE = "SYBASE";

@doc:Description { value:"Construct MYSQL DB jdbc url in the format of  jdbc:postgresql://[HOST]:[PORT]/[database]"}
const string POSTGRE = "POSTGRE";

@doc:Description { value:"Construct IBMDB2  DB jdbc url in the format of  jdbc:db2://[HOST]:[PORT]/[database]"}
const string IBMDB2 = "IBMDB2";

@doc:Description { value:"Construct HSQLDB SERVER dB jdbc url in the format of  jdbc:hsqldb:hsql://[HOST]:[PORT]/[database]"}
const string HSQLDB_SERVER = "HSQLDB_SERVER";

@doc:Description { value:"Construct HSQLDB FILE DB jdbc url in the format of  jdbc:hsqldb:file:[path]/[database]"}
const string HSQLDB_FILE = "HSQLDB_FILE";

@doc:Description { value:"Construct H2 SERVER DB jdbc url in the format of  jdbc:h2:tcp://[HOST]:[PORT]/[database]"}
const string H2_SERVER = "H2_SERVER";

@doc:Description { value:"Construct H2 FILE DB jdbc url in the format of  jdbc:h2:file://[path]/[database]"}
const string H2_FILE = "H2_FILE";

@doc:Description { value:"Construct DERBY SERVER DB jdbc url in the format of  jdbc:derby://[HOST]:[PORT]/[database]"}
const string DERBY_SERVER = "DERBY_SERVER";

@doc:Description { value:"Construct DERBY FILE DB jdbc url in the format of  jdbc:derby://[path]/[database]"}
const string DERBY_FILE = "DERBY_FILE";

