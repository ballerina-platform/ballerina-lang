public const APPLICATION_ERROR_REASON = "{ballerina/sql}ApplicationError";

# Represents the properties which are related to Non SQL errors
#
# + message - Error message
# + cause - cause
public type ApplicationErrorData record {|
    string message;
    error cause?;
|};

public type ApplicationError error<ApplicationErrorData>;

public type OrderCreationError distinct ApplicationError;
public type OrderProcessingError distinct ApplicationError;
public type OrderCreationError2 distinct OrderCreationError;

public type NewPostDefinedError distinct PostDefinedError;
public type PostDefinedError error<ErrorData>;

public type ErrorData record {|
    string code;
|};

public type Data1 record {|
    int num;
|};

public type Data2 record {|
    byte num;
|};

public type Data3 record {|
    int:Signed16 num;
|};

public type ErrorIntersection1 distinct error<Data1> & error<Data2>;
public type ErrorIntersection2 distinct error<Data3> & error<Data2>;
