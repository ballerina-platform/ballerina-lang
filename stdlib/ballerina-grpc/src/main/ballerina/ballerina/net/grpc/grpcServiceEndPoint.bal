package ballerina.net.grpc;

//////////////////////////////
/// Grpc Service Endpoint ///
/////////////////////////////
public struct GrpcService {
    string epName;
    ServiceEndpointConfiguration config;
    ServiceEndpoint serviceEndpoint;
}

public function <GrpcService ep> GrpcService () {
    ep.serviceEndpoint = {};
}

@Description {value:"Gets called when the endpoint is being initialize during package init time"}
@Param {value:"epName: The endpoint name"}
@Param {value:"config: The ServiceEndpointConfiguration of the endpoint"}
@Return {value:"Error occured during initialization"}
public function <GrpcService ep> init (string epName, ServiceEndpointConfiguration config) {
    ep.serviceEndpoint.init(epName, config);
}

@Description {value:"gets called every time a service attaches itself to this endpoint - also happens at package init time"}
@Param {value:"conn: The server connector connection"}
@Param {value:"res: The outbound response message"}
@Return {value:"Error occured during registration"}
public function <GrpcService ep> register (type serviceType) {
    ep.serviceEndpoint.register(serviceType);
}

@Description {value:"Starts the registered service"}
@Return {value:"Error occured during registration"}
public function <GrpcService ep> start () {
    ep.serviceEndpoint.start();
}

@Description {value:"Returns the connector that client code uses"}
@Return {value:"The connector that client code uses"}
@Return {value:"Error occured during registration"}
public function <GrpcService ep> getConnector () returns (ResponseConnector) {
    return ep.serviceEndpoint.getConnector();
}

@Description {value:"Stops the registered service"}
@Return {value:"Error occured during registration"}
public function <GrpcService ep> stop () {
    ep.serviceEndpoint.stop();
}

@Description {value:"Stops the registered service"}
@Return {value:"Error occured during registration"}
public function <GrpcService ep> send (any res) returns (ConnectorError) {
   return ep.serviceEndpoint.send(res);
}

@Description {value:"Stops the registered service"}
@Return {value:"Error occured during registration"}
public function <GrpcService ep> complete () returns (ConnectorError) {
    return ep.serviceEndpoint.complete();
}

@Description {value:"Stops the registered service"}
@Return {value:"Error occured during registration"}
public function <GrpcService ep> isCancelled () returns (boolean) {
    return ep.serviceEndpoint.isCancelled();
}

