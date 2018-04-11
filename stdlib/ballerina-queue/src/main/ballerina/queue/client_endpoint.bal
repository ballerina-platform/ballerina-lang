package ballerina.queue;


@Description {value:"Represents a JMS client endpoint"}
@Field {value:"epName: The name of the endpoint"}
//@Field {value:"config: The configurations associated with the endpoint"}
public type QueueEndpoint object {
    public {
        ClientEndpointConfiguration config;
    }


    public native function initEndpoint ();

    public native function createTextMessage (string content) returns (Message);

    public function register (typedesc serviceType) {

    }

    public function init (ClientEndpointConfiguration config) {
        self.config = config;
        self.initEndpoint();
    }

    @Description { value:"Returns the connector that client code uses"}
    @Return { value:"The connector that client code uses" }
    public native function getClient () returns (ClientConnector);

};

@Description { value:"JMS Client connector properties to pass JMS client connector configurations"}
//@Field {value:"initialContextFactory: Initial context factory name, specific to the provider"}
//@Field {value:"providerUrl: Connection URL of the provider"}
//@Field {value:"connectionFactoryName: Name of the connection factory"}
//@Field {value:"connectionFactoryType: Type of the connection factory (queue/topic)"}
@Field {value:"acknowledgementMode: Ack mode (auto-ack, client-ack, dups-ok-ack, transacted, xa)"}
@Field {value:"clientCaching: Is client caching enabled (default: enabled)"}
//@Field {value:"connectionUsername: Connection factory username"}
//@Field {value:"connectionPassword: Connection factory password"}
//@Field {value:"configFilePath: Path to be used for locating jndi configuration"}
//@Field {value:"connectionCount: Number of pooled connections to be used in the transport level (default: 5)"}
//@Field {value:"sessionCount: Number of pooled sessions to be used per connection in the transport level (default: 10)"}
@Field {value:"properties: Additional Properties"}
public type ClientEndpointConfiguration {
//string initialContextFactory;
//string providerUrl;
//string connectionFactoryName;

        string destination = "MyQueue";
        string destinationType = "queue";
        string acknowledgementMode = "AUTO_ACKNOWLEDGE";
        boolean clientCaching = true;
        map properties;


//string connectionUsername;
//string connectionPassword;
//string configFilePath;
//int connectionCount;
//int sessionCount;
};

//public function <ClientEndpointConfiguration config> ClientEndpointConfiguration() {
//    //config.connectionFactoryName = "QueueConnectionFactory";
//    config.destinationType = "queue";
//    config.acknowledgementMode = "AUTO_ACKNOWLEDGE";
//    config.clientCaching = true;
//    config.destination = "MyQueue";
//    //config.connectionCount = 5;
//    //config.sessionCount = 10;
//}

public type ClientConnector object {
    public {
        string connectorId;
        ClientEndpointConfiguration config;
    }

    @Description {value:"SEND action implementation of the JMS Connector"}
    @Param {value:"destinationName: Destination Name"}
    @Param {value:"message: Message"}
    public native function send (string destinationName, Message m);

    @Description {value:"POLL action implementation of the JMS Connector"}
    @Param {value:"destinationName: Destination Name"}
    @Param {value:"time: Timeout that needs to blocked on"}
    public native function receive (string destinationName, int time) returns (Message);

    @Description {value:"POLL action implementation with selector support of the JMS Connector"}
    @Param {value:"destinationName: Destination Name"}
    @Param {value:"time: Timeout that needs to blocked on"}
    @Param {value:"selector: Selector to filter out messages"}
    public native function receiveWithSelector (string destinationName, int time, string selector)
    returns (Message);

    @Description {value:"Receive message by correlating with given map"}
    @Param {value:"correlationMap: Correlation map"}
    public native function correlate (json correlationMap)
    returns (Message);

};
