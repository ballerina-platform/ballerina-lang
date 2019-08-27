public const APPLICATION_ERROR_REASON = "{ballerina/sql}ApplicationError";

# Represents the properties which are related to Non SQL errors
#
# + message - Error message
public type ApplicationErrorData record {|
    string message;
    error cause?;
|};

public type ApplicationError error<APPLICATION_ERROR_REASON, ApplicationErrorData>;
