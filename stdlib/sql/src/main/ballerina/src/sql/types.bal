import ballerina/config;
import ballerinax/java;

# Represents the properties which are used to configure DB connection pool.
# Default values of the fields can be set through the configuration API.
#
# + maxOpenConnections - The maximum number of open connections that the pool is allowed to have, including both idle and in-use connections.
#                     Default value is 15 and it can be changed through the configuration API with the key
#                     `b7a.sql.pool.maximumPoolSize`.
# + minIdleConnections - The minimum number of idle connections that pool tries to maintain in the pool. Default is the same as
#                 maxOpenConnections and it can be changed through the configuration API with the key
#                 `b7a.sql.pool.minimumIdle`.
# + maxConnectionLifeTimeSeconds - The maximum lifetime of a connection in the pool. Default value is 1800 seconds (30 minutes)
#                  and it can be changed through the configuration API with the key `b7a.sql.pool.maxLifetimeInMillis`.
#                  A value of 0 indicates unlimited maximum lifetime (infinite lifetime).
public type ConnectionPool record {|
    int maxOpenConnections = config:getAsInt("\"b7a.sql.pool.maxOpenConnections\"", 15);
    decimal maxConnectionLifeTimeSeconds = <decimal>config:getAsFloat("b7a.sql.pool.maxConnectionLifeTime", 1800.0);
    int minIdleConnections = config:getAsInt("b7a.sql.pool.minIdleConnections", 15);
|};

// This is a container object that holds the global pool config and initializes the internal map of connection pools
type GlobalConnectionPoolContainer object {
    private ConnectionPool connectionPool = {};

    function __init() {
        // poolConfig record is frozen so that it cannot be modified during runtime
        ConnectionPool frozenConfig = self.connectionPool.cloneReadOnly();
        initGlobalPoolContainer(frozenConfig);
    }

    public function getGlobalConnectionPool() returns ConnectionPool {
        return self.connectionPool;
    }
};

function initGlobalPoolContainer(ConnectionPool poolConfig) = @java:Method {
    class: "org.ballerinalang.sql.utils.ConnectionPoolUtils"
} external;

// This is an instance of GlobalPoolConfigContainer object type. The __init functions of database clients pass
// poolConfig member of this instance to the external client creation logic in order to access the internal map
// of connection pools.
final GlobalConnectionPoolContainer globalPoolContainer = new;

public function getGlobalConnectionPool() returns ConnectionPool{
    return globalPoolContainer.getGlobalConnectionPool();
}