//package ballerina.queue;
//
//import ballerina/io;
//
//public type ConsumerEndpoint object {
//
//    private {
//        Context context;
//        ServiceEndpointConfiguration config;
//    }
//
//
//    public native function initEndpoint ();
//
//    public native function  register (typedesc serviceType);
//
//    public native function  start ();
//
//    public native function  getClient () returns (Context);
//
//    public native function  stop ();
//};
//
//public type ServiceEndpointConfiguration {
//    string initialContextFactory = "ConnectionFactory";
//    string providerUrl;
//    string connectionFactoryName;
//    string destinationType = "queue";
//    boolean clientCaching = true;
//    string connectionUsername;
//    string connectionPassword;
//    string configFilePath;
//    int connectionCount = 5;
//    int sessionCount = 5;
//    map properties;
//};
//
//public function ConsumerEndpoint::init (ServiceEndpointConfiguration config) {
//    self.config = config;
//    self.initEndpoint();
//}
//
