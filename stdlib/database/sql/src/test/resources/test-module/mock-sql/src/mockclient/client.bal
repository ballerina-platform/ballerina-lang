import ballerina/java;
import ballerina/sql;

# Represents a Mock database client.
public type Client client object {
    *sql:Client;
    private boolean clientActive = true;

    public function __init(public string url, public string? user = (), public string? password = (),
        public string? datasourceName = (), public map<anydata>? options = (),
        public sql:ConnectionPool? connectionPool = (), public map<anydata>? connectionPoolOptions = ()) returns sql:Error? {
        SQLParams sqlParams = {
            url: url,
            user: user,
            password: password,
            datasourceName: datasourceName,
            options: options,
            connectionPool: connectionPool,
            connectionPoolOptions: connectionPoolOptions
        };
        return createSqlClient(self, sqlParams, sql:getGlobalConnectionPool());
    }

    # Executes the sql query provided by the user, and returns the result as stream.
    #
    # + sqlQuery - The query which needs to be executed as `string` or `ParameterizedString` if the SQL query has
    #              params to be passed in.
    # + rowType - The `typedesc` of the record that should be returned as a result. If this is not provided the default
    #             column names of the query result set be used for the record attributes
    # + return - Stream of records in the type of `rowType`
    public remote function query(@untainted string|sql:ParameterizedString sqlQuery, typedesc<record {}>? rowType = ())
    returns @tainted stream<record{}, sql:Error> {
        if (self.clientActive) {
            sql:ParameterizedString sqlParamString;
            if (sqlQuery is string) {
                sqlParamString = {
                    parts: [sqlQuery],
                    insertions: []
                };
            } else {
                sqlParamString = sqlQuery;
            }
            return nativeQuery(self, sqlParamString, rowType);
        } else {
            return sql:generateApplicationErrorStream("MySQL Client is already closed,"
                + "hence further operations are not allowed");
        }
    }

    # Executes the DDL or DML sql queries provided by the user, and returns summary of the execution.
    #
    # + sqlQuery - The DDL or DML query such as INSERT, DELETE, UPDATE, etc
    # + return - Summary of the sql update query as `sql:ExecuteResult` or returns `sql:Error`
    #           if any error occured when executing the query
    public remote function execute(@untainted string sqlQuery) returns sql:ExecuteResult|sql:Error? {
        if (self.clientActive) {
            return nativeExecute(self, java:fromString(sqlQuery));
        } else {
            return sql:ApplicationError( message = "JDBC Client is already closed,"
                + " hence further operations are not allowed");
        }
    }


    # Close the SQL client.
    #
    # + return - Possible error during closing the client
    public function close() returns sql:Error? {
        self.clientActive = false;
        return close(self);
    }
};

type SQLParams record {|
    string? url;
    string? user;
    string? password;
    string? datasourceName;
    map<anydata>? options;
    sql:ConnectionPool? connectionPool;
    map<anydata>? connectionPoolOptions;
|};

function createSqlClient(Client sqlClient, SQLParams sqlParams, sql:ConnectionPool globalConnPool)
returns sql:Error? = @java:Method {
    class: "org.ballerinalang.sql.utils.ClientUtils"
} external;

function nativeQuery(Client sqlClient, sql:ParameterizedString sqlQuery, typedesc<record {}>? rowtype)
returns stream<record{}, sql:Error> = @java:Method {
    class: "org.ballerinalang.sql.utils.QueryUtils"
} external;

function nativeExecute(Client sqlClient,@untainted handle sqlQuery)
returns sql:ExecuteResult|sql:Error? = @java:Method {
    class: "org.ballerinalang.sql.utils.ExecuteUtils"
} external;

function close(Client mysqlClient) returns sql:Error? = @java:Method {
    class: "org.ballerinalang.sql.utils.ClientUtils"
} external;
