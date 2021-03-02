import docerina_project.world as prj;

# Represents log level types.
#
# + DEBUG - DEBUG log level
# + ERROR - Error log level
# + INFO - Basic info log level
# + TRACE - Extensive trace log level
public enum LogLevel {
    DEBUG,
    ERROR,
    INFO,
    TRACE,
    # log level of warn
    WARN,
    ALL,
    # Turn off logging
    OFF
}

public type ClientHttp2Settings record {
    boolean http2PriorKnowledge = false;
    LogLevel lvl;
};

# Represents a Human record.
#
# + controller - A MainController to control the human
# + age - Age of the human
public type Human record {|
    readonly MainController controller;
    int age = 25;
    readonly b = "ballerina";
    Listener listnr?;
    Caller caller?;
    map<string> userID = {"user": "Ballerina", "ID": "1234"};
    ClientHttp2Settings settings;
|}

# Represents server listener where one or more services can be registered. so that ballerina program can offer
# service through this listener.
public class Listener {

    private int port = 0;
    private string config;

    # Starts the registered service.
    # ```ballerina
    # error? result = listenerEp.__start();
    # ```
    #
    # + return - An `error` if an error occurs while starting the server or else `()`
    public function 'start() returns error? {
    }

    # Stops the service listener gracefully. Already-accepted requests will be served before the connection closure.
    # ```ballerina
    # error? result = listenerEp.__gracefulStop();
    # ```
    #
    # + return - An `error` if an error occurred during the listener stopping process or else `()`
    public function __gracefulStop() returns error? {
    }

    # Gets called every time a service attaches itself to this endpoint - also happens at module init time.
    # ```ballerina
    # error? result = listenerEp.__attach(helloService);
    # ```
    #
    # + s - The type of the service to be registered
    # + name - Name of the service
    # + return - An `error` if encounters an error while attaching the service or else `()`
    public function attach(int s, string? name = ()) returns error? {
    }
}

# Provides the gRPC remote functions for interacting with caller.
#
# + instanceId - The connection id
public client class Caller {

    private int instanceId = -1;

    # Informs the caller, when the server has sent all the messages.
    # ```ballerina
    # grpc:Error? result = caller->complete();
    # ```
    #
    # + return - A `grpc:Error` if an error occurs while sending the response or else `()`
    remote function complete() returns error?|int {
        return 0;
    }

}

public readonly class MainController {
    int id;
    string[] codes;

    public function init(int id, readonly & string[] codes) {
        self.id = id;
        self.codes = codes;
    }
    # Gets invoked to return id.
    # + return - string builtin type
    public function getId() returns string {
        return string `Main: ${self.id}`;
    }
}

# Represents a person object
public isolated class Person {

    public float wealth = 0;

    # Gets invoked to initialize the Person object
    #
    # + name - Name of the person for constructor
    # + age - Age of the person for constructor
    public function init() {
    }

    # Add wealth to person
    #
    # + amt - Amount to be added
    public function addWealth(int[] amt) {
    }
}

# The types of messages that are accepted by HTTP `client` when sending out the outbound request.
public type RequestMessage Person|string|xml|json|prj:PersonZ|();

# Test type
public type TestType string|xml;

# Add wealth to person
#
# + amt - Amount to be added
# + rate - Interest rate
# + return - Req msg union type
public isolated function addWealth(int? amt, float rate=1.5, string... name) returns RequestMessage {
return "";
}

# Intersection type.
public type Block readonly & byte[];

# Tuple type The key for the `TimeDeltaStart` type.
public const TIME_DELTA_START = "TIME_DELTA_START";

# Tuple type Specifies that message delivery should start with a given historical time delta (from now).
public type TimeDeltaStart [TIME_DELTA_START, int];
