import ballerina/java;
import ballerina/sql;

# Represents a Mock database client.
public type Client client object {
    *sql:Client;
    private boolean clientActive = true;

    public function init(public string url, public string? user = (), public string? password = (),
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

    public remote function query(@untainted string|sql:ParameterizedQuery sqlQuery, typedesc<record {}>? rowType = ())
    returns @tainted stream<record{}, sql:Error> {
        if (self.clientActive) {
            return nativeQuery(self, sqlQuery, rowType);
        } else {
            return sql:generateApplicationErrorStream("SQL Client is already closed,"
                + "hence further operations are not allowed");
        }
    }

    public remote function execute(@untainted string|sql:ParameterizedQuery sqlQuery) returns sql:ExecutionResult|sql:Error {
        if (self.clientActive) {
            return nativeExecute(self, sqlQuery);
        } else {
            return sql:ApplicationError("SQL Client is already closed, hence further operations are not allowed");
        }
    }

    public remote function batchExecute(@untainted sql:ParameterizedQuery[] sqlQueries) returns sql:ExecutionResult[]|sql:Error {
        if (sqlQueries.length() == 0) {
            return sql:ApplicationError(" Parameter 'sqlQueries' cannot be empty array");
        }
        if (self.clientActive) {
            return nativeBatchExecute(self, sqlQueries);
        } else {
            return sql:ApplicationError("JDBC Client is already closed, hence further operations are not allowed");
        }
    }

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

function nativeQuery(Client sqlClient, string|sql:ParameterizedQuery sqlQuery, typedesc<record {}>? rowtype)
returns stream<record{}, sql:Error> = @java:Method {
    class: "org.ballerinalang.sql.utils.QueryUtils"
} external;

function nativeExecute(Client sqlClient, string|sql:ParameterizedQuery sqlQuery)
returns sql:ExecutionResult|sql:Error = @java:Method {
    class: "org.ballerinalang.sql.utils.ExecuteUtils"
} external;

function nativeBatchExecute(Client sqlClient, sql:ParameterizedQuery[] sqlQueries)
returns sql:ExecutionResult[]|sql:Error = @java:Method {
    class: "org.ballerinalang.sql.utils.ExecuteUtils"
} external;

function close(Client mysqlClient) returns sql:Error? = @java:Method {
    class: "org.ballerinalang.sql.utils.ClientUtils"
} external;
