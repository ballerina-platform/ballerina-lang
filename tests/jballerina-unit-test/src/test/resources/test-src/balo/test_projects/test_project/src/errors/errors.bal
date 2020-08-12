public const APPLICATION_ERROR_REASON = "{ballerina/sql}ApplicationError";

# Represents the properties which are related to Non SQL errors
#
# + message - Error message
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
