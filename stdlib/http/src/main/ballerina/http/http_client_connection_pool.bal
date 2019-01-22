public type PoolConfiguration record {
    //Max active connections per route(host:port); -1 indicates //unlimited
    int maxActiveConnections = -1;
    //Maximum amount of time (in milliseconds) the client should wait for an idle connection before it sends an error when the //pool is exhausted
    int waitTimeinMillis = 10000;
    //Applies only to HTTP/2
    int maxActiveStreamsPerConnection = 20;
    ////Following two fields are useful only when you need to define different values for per route pools
    //Route? poolKey = ();
    //map<PoolConfiguration>? perRoutePoolConfigs = ();
};

public type Route record {
    string protocol; //http or https
    string host;
    int port;
};

//This is a hack to get the global map initialized, without involving locking
type ConnectionManager object {
    public PoolConfiguration poolConfig = {};
    public function __init() {
        self.initGlobalPool(self.poolConfig);
    }
    extern function initGlobalPool(PoolConfiguration poolConfig);
};

ConnectionManager connectionManager = new;
PoolConfiguration globalHttpClientConnPool = connectionManager.poolConfig;

function poolingSetDefaultMaxActiveConns (int n) {

}

function poolingSetDefaulWaitTime (int n) {

}

function poolingSetMaxStreamsPerConnection (int n) {

}

//function poolingConfigure (string hostOrIP, int port, int maxActiveConnections = -1, int waitTimeMillis = 10000, int
//    maxStreams = 20) {
//
//}
