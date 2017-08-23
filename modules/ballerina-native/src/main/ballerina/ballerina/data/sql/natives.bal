package ballerina.data.sql;

import ballerina.doc;

@doc:Description { value: "Parameter struct represents a query parameter for the SQL queries specified in connector actions"}
@doc:Field {value:"sqlType : The data type of the corresponding SQL parameter"}
@doc:Field {value:"value: Value of paramter pass into the SQL query"}
@doc:Field {value:"direction: Direction of the SQL Parameter 0 - IN, 1- OUT, 2 - INOUT"}
@doc:Field {value:"structuredType: Underline SQL type to be used for parameter of SQL array type or custom structure type to be used with struct type"}
struct Parameter {
	string sqlType;
	any value;
	int direction;
	string structuredType;
}

@doc:Description { value: "ConnectionProperties structs represents the properties which are used to configure DB connection pool"}
@doc:Field {value:"url: Platform independent DB access URL"}
@doc:Field {value:"dataSourceClassName: Name of the DataSource class provided by the JDBC driver"}
@doc:Field {value:"connectionTestQuery: Query that will be executed to validate that the connection to the database is still alive"}
@doc:Field {value:"poolName: User-defined name for the connection pool and appears mainly in logging"}
@doc:Field {value:"catalog: Catalog of connections created by this pool"}
@doc:Field {value:"connectionInitSql:  SQL statement that will be executed after every new connection creation before adding it to the pool"}
@doc:Field {value:"driverClassName: Fully qualified Java class name of the JDBC driver to be used"}
@doc:Field {value:"transactionIsolation:  Transaction isolation level of connections returned from the pool. The supported values are TRANSACTION_READ_UNCOMMITTED, TRANSACTION_READ_COMMITTED, TRANSACTION_REPEATABLE_READ and TRANSACTION_SERIALIZABLE"}
@doc:Field {value:"autoCommit: Auto-commit behavior of connections returned from the pool"}
@doc:Field {value:"isolateInternalQueries: Determines whether HikariCP isolates internal pool queries, such as the connection alive test, in their own transaction"}
@doc:Field {value:"allowPoolSuspension: Whether the pool can be suspended and resumed through JMX"}
@doc:Field {value:"readOnly:  Whether Connections obtained from the pool are in read-only mode by default"}
@doc:Field {value:"maximumPoolSize: Maximum size that the pool is allowed to reach, including both idle and in-use connections"}
@doc:Field {value:"connectionTimeout: Maximum number of milliseconds that a client will wait for a connection from the pool"}
@doc:Field {value:"idleTimeout: Maximum amount of time that a connection is allowed to sit idle in the pool"}
@doc:Field {value:"minimumIdle: Minimum number of idle connections that pool tries to maintain in the pool"}
@doc:Field {value:"maxLifetime: Maximum lifetime of a connection in the pool"}
@doc:Field {value:"validationTimeout:  Maximum amount of time that a connection will be tested for aliveness"}
@doc:Field {value:"leakDetectionThreshold: Amount of time that a connection can be out of the pool before a message is logged indicating a possible connection leak"}
@doc:Field {value:"datasourceProperties: Data source specific properties which are used along with the dataSourceClassName"}
struct ConnectionProperties {
	string url;
	string dataSourceClassName;
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

	@doc:Description { value:"The call action implementation for SQL connector to invoke stored procedures/functions."}
	@doc:Param { value:"query: SQL query to execute" }
	@doc:Param { value:"parameters: Parameter array used with the SQL query" }
	@doc:Return { value:"datatable: Result set for the given query" }
	native action call (string query, Parameter[] parameters) (datatable);

	@doc:Description { value:"The select action implementation for SQL connector to select data from tables."}
	@doc:Param { value:"query: SQL query to execute" }
	@doc:Param { value:"parameters: Parameter array used with the SQL query" }
	@doc:Return { value:"datatable: Result set for the given query" }
	native action select (string query, Parameter[] parameters) (datatable);

	@doc:Description { value:"The close action implementation for SQL connector to shutdown the connection pool."}
	native action close ();

	@doc:Description { value:"The update action implementation for SQL connector to update data and schema of the database."}
	@doc:Param { value:"query: SQL query to execute" }
	@doc:Param { value:"parameters: Parameter array used with the SQL query" }
	@doc:Return { value:"int: Updated row count" }
	native action update (string query, Parameter[] parameters) (int);

	@doc:Description { value:"The batchUpdate action implementation for SQL connector to batch data insert."}
	@doc:Param { value:"query: SQL query to execute" }
	@doc:Param { value:"parameters: Parameter array used with the SQL query" }
	@doc:Return { value:"int[]: Array of update counts" }
	native action batchUpdate (string query, Parameter[][] parameters) (int[]);

	@doc:Description { value:"The updateWithGeneratedKeys action implementation for SQL connector which returns the auto generated keys during the update action."}
	@doc:Param { value:"query: SQL query to execute" }
	@doc:Param { value:"parameters: Parameter array used with the SQL query" }
	@doc:Param { value:"keyColumns: Names of auto generated columns for which the auto generated key values are returned" }
	@doc:Return { value:"int: Updated row count during the query exectuion" }
	@doc:Return { value:"string[]: Array of auto generated key values during the query execution" }
	native action updateWithGeneratedKeys (string query, Parameter[] parameters, string[] keyColumns) (int, string[]);

}

@doc:Description { value:"Construct MySQL DB jdbc url in the format of  jdbc:mysql://[HOST]:[PORT]/[database]"}
const string MYSQL = "MYSQL";

@doc:Description { value:"Construct SQL Server DB jdbc url in the format of  jdbc:sqlserver://[HOST]:[PORT];databaseName=[database]"}
const string SQLSERVER = "SQLSERVER";

@doc:Description { value:"Construct Oracle  DB jdbc url in the format of  jdbc:oracle:thin:[username/password]@[HOST]:[PORT]/[database]"}
const string ORACLE = "ORACLE";

@doc:Description { value:"Construct Sybase DB jdbc url in the format of  jdbc:sybase:Tds:[HOST]:[PORT]/[database]"}
const string SYBASE = "SYBASE";

@doc:Description { value:"Construct PostgreSQL DB jdbc url in the format of  jdbc:postgresql://[HOST]:[PORT]/[database]"}
const string POSTGRE = "POSTGRE";

@doc:Description { value:"Construct IBM Db2  DB jdbc url in the format of  jdbc:db2://[HOST]:[PORT]/[database]"}
const string IBMDB2 = "IBMDB2";

@doc:Description { value:"Construct HSQLDB SERVER dB jdbc url in the format of  jdbc:hsqldb:hsql://[HOST]:[PORT]/[database]"}
const string HSQLDB_SERVER = "HSQLDB_SERVER";

@doc:Description { value:"Construct HSQLDB FILE DB jdbc url in the format of  jdbc:hsqldb:file:[path]/[database]"}
const string HSQLDB_FILE = "HSQLDB_FILE";

@doc:Description { value:"Construct H2 SERVER DB jdbc url in the format of  jdbc:h2:tcp://[HOST]:[PORT]/[database]"}
const string H2_SERVER = "H2_SERVER";

@doc:Description { value:"Construct H2 FILE DB jdbc url in the format of  jdbc:h2:file://[path]/[database]"}
const string H2_FILE = "H2_FILE";

@doc:Description { value:"Construct Derby SERVER DB jdbc url in the format of  jdbc:derby://[HOST]:[PORT]/[database]"}
const string DERBY_SERVER = "DERBY_SERVER";

@doc:Description { value:"Construct Derby FILE DB jdbc url in the format of  jdbc:derby://[path]/[database]"}
const string DERBY_FILE = "DERBY_FILE";

