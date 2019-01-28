# Configurations for managing HTTP client connection pool.
#
# + maxActiveConnections - Max active connections per route(host:port). Default value is -1 which indicates unlimited.
# + waitTimeinMillis - Maximum amount of time, the client should wait for an idle connection before it sends an error when the pool is exhausted
# + maxActiveStreamsPerConnection - Maximum active streams per connection. This only applies to HTTP/2.
public type PoolConfiguration record {
    int maxActiveConnections = config:getAsInt("b7a.http.pool.maxActiveConnections", default = -1);
    int waitTimeinMillis = config:getAsInt("b7a.http.pool.waitTimeinMillis", default = 60000);
    int maxActiveStreamsPerConnection = config:getAsInt("b7a.http.pool.maxActiveStreamsPerConnection", default = 50);
};

//This is a hack to get the global map initialized, without involving locking.
type ConnectionManager object {
    public PoolConfiguration poolConfig = {};
    public function __init() {
        self.initGlobalPool(self.poolConfig);
    }
    extern function initGlobalPool(PoolConfiguration poolConfig);
};

ConnectionManager connectionManager = new;
PoolConfiguration globalHttpClientConnPool = connectionManager.poolConfig;
