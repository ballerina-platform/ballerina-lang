import docerina_project.world as prj;
import ballerina/lang.'int as ints;

# Record representing the common-response to be returned.
#
# + headers - Additional headers to be included in the `http:Response`
# + settings - Content to be included in the `http:Response` body
type CommonResponse record {|
    map<string|string[]> headers?;
    ClientHttp2Settings settings;
    DistObj|Person timeLow;
    readonly ClientHttp2Settings clienthttpSettings;
    string...;
|};

# Record representing the subscription / unsubscription intent verification success.
public type SubscriptionVerificationSuccess record {
    *CommonResponse;
};

public isolated function inlineRecordReturn(CommonResponse prvtRecord, SubscriptionVerificationSuccess publicRecord)
    returns record {|
        string latitude;
        decimal longitude;
        json...;
    |} {
}

# Anydata type apram
@typeParam
type AnydataType anydata;

# A type param
@typeParam
type TypeParam any|error;

# Built-in subtype of string containing strings of length 1.
@builtinSubtype
type Char string;

public isolated function cancelFuture(future<any|error> f) returns {
}

# Docs for tuple module variable.
public [int, float] [a, b] = [1, 1.5];

# A public variable of string type
public string pubString = "123";

# A configurable variable of string type without default value
configurable string confString = ?;

# A configurable variable of string type with default value
configurable string defaultConfString = "confidential";

public type Coordinates record {|
    decimal latitude;
    decimal longitude;
|};

public type City record {
    *Coordinates;
    string name;
};

# Link to a distinct object DistObj.
public type LinkToDistinctObj distinct DistObj;

# Distinct object
public type DistObj distinct object {
    public function getS();
};

public type KeyValues record {|
    never msg?;
    never 'error?;
    json...;
|};

public isolated function printDebug(*KeyValues keyValues) {
}

# A function that returns anydata type
public type Valuer isolated function() returns anydata;

# A value of anydata type
public type Value anydata|Valuer;

# Holds the seconds as a decimal value.
public type Seconds decimal;

# Contains links to readonly object type and record
#
public function testReadonlyRecordObjectsLinks(Controller cnt, Uuid uuid) {
}

# Represents a readonly object type
#
public type Controller readonly & object {
   int id;
   string[] codes;

   function getId() returns string;
};

# Represents a readonly record
#
public type Uuid readonly & record {
    ints:Unsigned32 timeLow;
    ints:Unsigned16 timeMid;
    ints:Unsigned16 timeHiAndVersion;
    ints:Unsigned8 clockSeqHiAndReserved;
    ints:Unsigned8 clockSeqLo;
    int node;    // Should be Unsigned48, but not available in lang.int at the moment
};

# Test inline record with typedesc
# + rowType - Typedesc with empty record
public function testTypeDesc(typedesc<record {}>? rowType) {
}

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
|};

# Represents StudentA object type
public type StudentA object {
    *PersonA;
    public int classId;
};

# Represents PersonA object type
#
# + firstName - First name of the person
public type PersonA object {
    public string firstName;
    public string lastName;

    # Get full name method
    #
    # + middleName - Middle name of person
    # + return - The full name
    public function getFullName(string middleName) returns string;
};

# The type representing services that can be declared to receive details of people on request.
public type DetailsRequestService service object {
    # The remote method that will be called when a request is opened.
    # + name - the name of person
    remote function onRequestOpened(string name);
    # The remote method that will be called when a request is commented.
    # + name - the name of person
    remote function onRequestCommented(string name);
};

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
    public function gracefulStop() returns error? {
    }

    # Stops the service listener immediately. Already-accepted requests will be served before the connection closure.
    # ```ballerina
    # error? result = listenerEp.__immediateStop();
    # ```
    #
    # + return - An `error` if an error occurred during the listener stopping process or else `()`
    public function immediateStop() returns error? {
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

    # Gets called every time detaches a service itself to this endpoint - also happens at module init time.
    # ```ballerina
    # error? result = listenerEp.__detach(helloService);
    # ```
    #
    # + s - The type of the service to be registered
    # + name - Name of the service
    # + return - An `error` if encounters an error while attaching the service or else `()`
    public function detach(int s, string? name = ()) returns error? {
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

    private float wealth = 0;

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
