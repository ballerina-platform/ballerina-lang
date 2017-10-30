package ballerina.data.sql;

@Description { value: "Parameter struct represents a query parameter for the SQL queries specified in connector actions"}
@Field {value:"sqlType: The data type of the corresponding SQL parameter"}
@Field {value:"value: Value of paramter pass into the SQL query"}
@Field {value:"direction: Direction of the SQL Parameter 0 - IN, 1- OUT, 2 - INOUT"}
public struct Parameter {
	string sqlType;
	any value;
	int direction;
}

@Description { value: "ConnectionProperties structs represents the properties which are used to configure DB connection pool"}
@Field {value:"url: Platform independent DB access URL"}
@Field {value:"dataSourceClassName: Name of the DataSource class provided by the JDBC driver"}
@Field {value:"connectionTestQuery: Query that will be executed to validate that the connection to the database is still alive"}
@Field {value:"poolName: User-defined name for the connection pool and appears mainly in logging"}
@Field {value:"catalog: Catalog of connections created by this pool"}
@Field {value:"connectionInitSql:  SQL statement that will be executed after every new connection creation before adding it to the pool"}
@Field {value:"driverClassName: Fully qualified Java class name of the JDBC driver to be used"}
@Field {value:"transactionIsolation:  Transaction isolation level of connections returned from the pool. The supported values are TRANSACTION_READ_UNCOMMITTED, TRANSACTION_READ_COMMITTED, TRANSACTION_REPEATABLE_READ and TRANSACTION_SERIALIZABLE"}
@Field {value:"autoCommit: Auto-commit behavior of connections returned from the pool"}
@Field {value:"isolateInternalQueries: Determines whether HikariCP isolates internal pool queries, such as the connection alive test, in their own transaction"}
@Field {value:"allowPoolSuspension: Whether the pool can be suspended and resumed through JMX"}
@Field {value:"readOnly:  Whether Connections obtained from the pool are in read-only mode by default"}
@Field {value:"isXA:  Whether Connections are used for a distributed transaction"}
@Field {value:"maximumPoolSize: Maximum size that the pool is allowed to reach, including both idle and in-use connections"}
@Field {value:"connectionTimeout: Maximum number of milliseconds that a client will wait for a connection from the pool"}
@Field {value:"idleTimeout: Maximum amount of time that a connection is allowed to sit idle in the pool"}
@Field {value:"minimumIdle: Minimum number of idle connections that pool tries to maintain in the pool"}
@Field {value:"maxLifetime: Maximum lifetime of a connection in the pool"}
@Field {value:"validationTimeout:  Maximum amount of time that a connection will be tested for aliveness"}
@Field {value:"leakDetectionThreshold: Amount of time that a connection can be out of the pool before a message is logged indicating a possible connection leak"}
@Field {value:"datasourceProperties: Data source specific properties which are used along with the dataSourceClassName"}
public struct ConnectionProperties {
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
	boolean isXA;
	int maximumPoolSize = -1;
	int connectionTimeout = -1;
	int idleTimeout = -1;
	int minimumIdle = -1;
	int maxLifetime = -1;
	int validationTimeout = -1;
	int leakDetectionThreshold = -1;
	map datasourceProperties;
}

public connector ClientConnector (string dbType, string hostOrPath, int port, string dbName, string username, string password, ConnectionProperties options) {
    map sharedMap = {};

	@Description { value:"The call action implementation for SQL connector to invoke stored procedures/functions."}
	@Param { value:"query: SQL query to execute" }
	@Param { value:"parameters: Parameter array used with the SQL query" }
	@Return { value:"datatable: Result set for the given query" }
	native action call (string query, Parameter[] parameters) (datatable);

	@Description { value:"The select action implementation for SQL connector to select data from tables."}
	@Param { value:"query: SQL query to execute" }
	@Param { value:"parameters: Parameter array used with the SQL query" }
	@Return { value:"datatable: Result set for the given query" }
	native action select (string query, Parameter[] parameters) (datatable);

	@Description { value:"The close action implementation for SQL connector to shutdown the connection pool."}
	native action close ();

	@Description { value:"The update action implementation for SQL connector to update data and schema of the database."}
	@Param { value:"query: SQL query to execute" }
	@Param { value:"parameters: Parameter array used with the SQL query" }
	@Return { value:"int: Updated row count" }
	native action update (string query, Parameter[] parameters) (int);

	@Description { value:"The batchUpdate action implementation for SQL connector to batch data insert."}
	@Param { value:"query: SQL query to execute" }
	@Param { value:"parameters: Parameter array used with the SQL query" }
	@Return { value:"int[]: Array of update counts" }
	native action batchUpdate (string query, Parameter[][] parameters) (int[]);

	@Description { value:"The updateWithGeneratedKeys action implementation for SQL connector which returns the auto generated keys during the update action."}
	@Param { value:"query: SQL query to execute" }
	@Param { value:"parameters: Parameter array used with the SQL query" }
	@Param { value:"keyColumns: Names of auto generated columns for which the auto generated key values are returned" }
	@Return { value:"int: Updated row count during the query exectuion" }
	@Return { value:"string[]: Array of auto generated key values during the query execution" }
	native action updateWithGeneratedKeys (string query, Parameter[] parameters, string[] keyColumns) (int, string[]);

}

@Description { value:"Construct MySQL DB jdbc url in the format of  jdbc:mysql://[HOST]:[PORT]/[database]"}
public const string MYSQL = "MYSQL";

@Description { value:"Construct SQL Server DB jdbc url in the format of  jdbc:sqlserver://[HOST]:[PORT];databaseName=[database]"}
public const string SQLSERVER = "SQLSERVER";

@Description { value:"Construct Oracle  DB jdbc url in the format of  jdbc:oracle:thin:[username/password]@[HOST]:[PORT]/[database]"}
public const string ORACLE = "ORACLE";

@Description { value:"Construct Sybase DB jdbc url in the format of  jdbc:sybase:Tds:[HOST]:[PORT]/[database]"}
public const string SYBASE = "SYBASE";

@Description { value:"Construct PostgreSQL DB jdbc url in the format of  jdbc:postgresql://[HOST]:[PORT]/[database]"}
public const string POSTGRE = "POSTGRE";

@Description { value:"Construct IBM Db2  DB jdbc url in the format of  jdbc:db2://[HOST]:[PORT]/[database]"}
public const string IBMDB2 = "IBMDB2";

@Description { value:"Construct HSQLDB SERVER dB jdbc url in the format of  jdbc:hsqldb:hsql://[HOST]:[PORT]/[database]"}
public const string HSQLDB_SERVER = "HSQLDB_SERVER";

@Description { value:"Construct HSQLDB FILE DB jdbc url in the format of  jdbc:hsqldb:file:[path]/[database]"}
public const string HSQLDB_FILE = "HSQLDB_FILE";

@Description { value:"Construct H2 SERVER DB jdbc url in the format of  jdbc:h2:tcp://[HOST]:[PORT]/[database]"}
public const string H2_SERVER = "H2_SERVER";

@Description { value:"Construct H2 FILE DB jdbc url in the format of  jdbc:h2:file://[path]/[database]"}
public const string H2_FILE = "H2_FILE";

@Description { value:"Construct Derby SERVER DB jdbc url in the format of  jdbc:derby://[HOST]:[PORT]/[database]"}
public const string DERBY_SERVER = "DERBY_SERVER";

@Description { value:"Construct Derby FILE DB jdbc url in the format of  jdbc:derby://[path]/[database]"}
public const string DERBY_FILE = "DERBY_FILE";

