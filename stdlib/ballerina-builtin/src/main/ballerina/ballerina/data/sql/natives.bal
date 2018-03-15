// Copyright (c) 2017 WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
//
// WSO2 Inc. licenses this file to you under the Apache License,
// Version 2.0 (the "License"); you may not use this file except
// in compliance with the License.
// You may obtain a copy of the License at
//
// http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing,
// software distributed under the License is distributed on an
// "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
// KIND, either express or implied.  See the License for the
// specific language governing permissions and limitations
// under the License.

package ballerina.data.sql;

@Description { value: "Parameter struct represents a query parameter for the SQL queries specified in connector actions"}
@Field {value:"sqlType: The data type of the corresponding SQL parameter"}
@Field {value:"value: Value of paramter pass into the SQL query"}
@Field {value:"direction: Direction of the SQL Parameter IN, OUT, or INOUT"}
@Field {value:"structType: In case of OUT direction, if the sqlType is REFCURSOR, this represents the struct type to
map a result row"}
public struct Parameter {
	Type sqlType;
	any value;
	Direction direction;
	type structType;
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
	string url = "";
	string dataSourceClassName = "";
	string connectionTestQuery = "";
	string poolName = "";
	string catalog = "";
	string connectionInitSql = "";
	string driverClassName = "";
	string transactionIsolation = "";
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

@Description { value:"Initialize the ConnectionProperties with default values"}
public function<ConnectionProperties c> ConnectionProperties () {
	c.datasourceProperties = {};
}

@Description { value:"The Databases which has direct parameter support."}
@Field { value:"MYSQL: MySQL DB with connection url in the format of  jdbc:mysql://[HOST]:[PORT]/[database]"}
@Field { value:"SQLSERVER: SQL Server DB with connection url in the format of jdbc:sqlserver://[HOST]:[PORT];databaseName=[database]"}
@Field { value:"ORACLE: Oracle DB with connection url in the format of  jdbc:oracle:thin:[username/password]@[HOST]:[PORT]/[database]"}
@Field { value:"SYBASE: Sybase DB with connection url in the format of  jdbc:sybase:Tds:[HOST]:[PORT]/[database]"}
@Field { value:"POSTGRES: PostgreSQL DB with connection url in the format of  jdbc:postgresql://[HOST]:[PORT]/[database]"}
@Field { value:"IBMDB2: IBMDB2 DB with connection url in the format of  jdbc:db2://[HOST]:[PORT]/[database]"}
@Field { value:"HSQLDB_SERVER: HSQL Server with connection url in the format of jdbc:hsqldb:hsql://[HOST]:[PORT]/[database]"}
@Field { value:"HSQLDB_FILE: HSQL Server with connection url in the format of jdbc:hsqldb:file:[path]/[database]"}
@Field { value:"H2_SERVER: H2 Server DB with connection url in the format of jdbc:h2:tcp://[HOST]:[PORT]/[database]"}
@Field { value:"H2_FILE: H2 File DB with connection url in the format of jdbc:h2:file://[path]/[database]"}
@Field { value:"H2_MEM: H2 in memory DB with connection url in the format of jdbc:h2:mem:[database]"}
@Field { value:"DERBY_SERVER: DERBY server DB with connection url in the format of jdbc:derby://[HOST]:[PORT]/[database]"}
@Field { value:"DERBY_FILE: Derby file DB with connection url in the format of jdbc:derby://[path]/[database]"}
@Field { value:"GENERIC: Custom DB connection with given connection url"}
public enum DB {
	MYSQL,
	SQLSERVER,
	ORACLE,
	SYBASE,
	POSTGRES,
	IBMDB2,
	HSQLDB_SERVER,
	HSQLDB_FILE,
	H2_SERVER,
	H2_FILE,
	H2_MEM,
	DERBY_SERVER,
	DERBY_FILE,
	GENERIC
}

@Description { value:"The SQL Datatype of the parameter"}
@Field { value:"VARCHAR: Small, variable-length character string"}
@Field { value:"CHAR: Small, fixed-length character string"}
@Field { value:"LONGVARCHAR: Large, variable-length character string"}
@Field { value:"NCHAR: Small, fixed-length character string with unicode support"}
@Field { value:"LONGNVARCHAR: Large, variable-length character string with unicode support"}
@Field { value:"BIT: Single bit value that can be zero or one, or null"}
@Field { value:"BOOLEAN: Boolean value either True or false"}
@Field { value:"TINYINT: 8-bit integer value which may be unsigned or signed"}
@Field { value:"SMALLINT: 16-bit signed integer value which may be unsigned or signed"}
@Field { value:"INTEGER: 32-bit signed integer value which may be unsigned or signed"}
@Field { value:"BIGINT: 64-bit signed integer value which may be unsigned or signed"}
@Field { value:"NUMERIC: Fixed-precision and scaled decimal values"}
@Field { value:"DECIMAL: Fixed-precision and scaled decimal values"}
@Field { value:"REAL: Single precision floating point number"}
@Field { value:"FLOAT: Double precision floating point number"}
@Field { value:"DOUBLE: Double precision floating point number"}
@Field { value:"BINARY: Small, fixed-length binary value"}
@Field { value:"BLOB: Binary Large Object"}
@Field { value:"LONGVARBINARY: Large, variable-length binary value"}
@Field { value:"VARBINARY: Small, variable-length binary value"}
@Field { value:"CLOB: Character Large Object"}
@Field { value:"NCLOB: Character large objects in multibyte national character set"}
@Field { value:"DATE: Date consisting of day, month, and year"}
@Field { value:"TIME: Time consisting of hours, minutes, and seconds"}
@Field { value:"DATETIME: Both DATE and TIME with additional a nanosecond field"}
@Field { value:"TIMESTAMP: Both DATE and TIME with additional a nanosecond field"}
@Field { value:"ARRAY: Composite data value that consists of zero or more elements of a specified data type"}
@Field { value:"STRUCT: User defined structured type, consists of one or more attributes"}
public enum Type {
	VARCHAR,
	CHAR,
	LONGVARCHAR,
	NCHAR,
	LONGNVARCHAR,
	NVARCHAR,
	BIT,
	BOOLEAN,
	TINYINT,
	SMALLINT,
	INTEGER ,
	BIGINT,
	NUMERIC,
	DECIMAL,
	REAL,
	FLOAT,
	DOUBLE,
	BINARY,
	BLOB,
	LONGVARBINARY,
	VARBINARY,
	CLOB,
	NCLOB,
	DATE,
	TIME,
	DATETIME,
	TIMESTAMP,
	ARRAY,
	STRUCT,
	REFCURSOR
}

@Description { value:"The direction of the parameter"}
@Field { value:"IN: IN parameters are used to send values to stored procedures"}
@Field { value:"OUT: OUT parameters are used to get values from stored procedures"}
@Field { value:"INOUT: INOUT parameters are used to send values and get values from stored procedures"}
public enum Direction {
    IN,
	OUT,
	INOUT
}

///////////////////////////////
// SQL Client Connector
///////////////////////////////

@Description { value:"The Client Connector for SQL databases."}
@Param { value:"dbType: SQL database type" }
@Param { value:"hostOrPath: Host name of the database or file path for file based database" }
@Param { value:"port: Port of the database" }
@Param { value:"dbName: Name of the database to connect" }
@Param { value:"username: Username for the database connection" }
@Param { value:"password: Password for the database connection" }
@Param { value:"options: ConnectionProperties for the connection pool configuration" }
public connector ClientConnector (DB dbType, string hostOrPath, int port, string dbName, string username,
								  string password, ConnectionProperties options) {

	@Description { value:"The call action implementation for SQL connector to invoke stored procedures/functions."}
	@Param { value:"sqlQuery: SQL query to execute" }
	@Param { value:"parameters: Parameter array used with the SQL query" }
	@Return { value:"Result set(s) for the given query" }
	native action call (@sensitive string sqlQuery, Parameter[] parameters, type structType) (@tainted table[]);

	@Description { value:"The select action implementation for SQL connector to select data from tables."}
	@Param { value:"sqlQuery: SQL query to execute" }
	@Param { value:"parameters: Parameter array used with the SQL query" }
	@Return { value:"Result set for the given query" }
	native action select (@sensitive string sqlQuery, Parameter[] parameters, type structType) (@tainted table);

	@Description { value:"The close action implementation for SQL connector to shutdown the connection pool."}
	native action close ();

	@Description { value:"The update action implementation for SQL connector to update data and schema of the database."}
	@Param { value:"sqlQuery: SQL query to execute" }
	@Param { value:"parameters: Parameter array used with the SQL query" }
	@Return { value:"Updated row count" }
	native action update (@sensitive string sqlQuery, Parameter[] parameters) (int);

	@Description { value:"The batchUpdate action implementation for SQL connector to batch data insert."}
	@Param { value:"sqlQuery: SQL query to execute" }
	@Param { value:"parameters: Parameter array used with the SQL query" }
	@Return { value:"Array of update counts" }
	native action batchUpdate (@sensitive string sqlQuery, Parameter[][] parameters) (int[]);

	@Description { value:"The updateWithGeneratedKeys action implementation for SQL connector which returns the auto generated keys during the update action."}
	@Param { value:"sqlQuery: SQL query to execute" }
	@Param { value:"parameters: Parameter array used with the SQL query" }
	@Param { value:"keyColumns: Names of auto generated columns for which the auto generated key values are returned" }
	@Return { value:"Updated row count during the query exectuion" }
	@Return { value:"Array of auto generated key values during the query execution" }
	native action updateWithGeneratedKeys (@sensitive string sqlQuery, Parameter[] parameters, string[] keyColumns) (int, string[]);

}

///////////////////////////////
// SQL Client Endpoint
///////////////////////////////

public struct Client {
	string epName;
	ClientEndpointConfiguration config;
}

public struct ClientEndpointConfiguration {
	DB database;
	string host = "";
	int port = 0;
	string name = "";
	string username = "";
	string password = "";
	ConnectionProperties options;
}

public function<ClientEndpointConfiguration c> ClientEndpointConfiguration () {
	c.database = DB.GENERIC;
	c.options = {};
}

@Description { value:"Gets called when the endpoint is being initialize during package init time"}
@Param { value:"epName: The endpoint name" }
@Param { value:"config: The ClientEndpointConfiguration of the endpoint" }
public function <Client ep> init (string epName, ClientEndpointConfiguration config) {
	ep.epName = epName;
	ep.config = config;
	ep.initEndpoint();
}

@Description { value:"Initialize the endpoint"}
public native function<Client ep> initEndpoint();

@Description { value:"Returns the connector that client code uses"}
@Return { value:"The connector that client code uses" }
public native function <Client ep> getConnector () returns (ClientConnector conn);

@Description { value:"Stops the registered service"}
@Param { value:"Type of the service" }
public function <Client ep> register (type serviceType){

}

@Description { value:"Starts the registered service"}
public function <Client ep> start (){

}

@Description { value:"Stops the registered service"}
public function <Client ep> stop () {

}
