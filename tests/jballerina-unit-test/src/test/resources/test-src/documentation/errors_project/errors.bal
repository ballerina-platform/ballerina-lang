# Account error data.
public type AccountErrorData record {
    int accountID;
};

# All account errors.
public type AccountError distinct error<AccountErrorData>;

# Invlaid account id error.
public type InvalidAccountIdError distinct AccountError;

# Acc not found error
public type AccountNotFoundError distinct AccountError;

# Represents the total Cache error type.
public type TotalCacheError distinct CacheError;

# Represents the Cache error type with details. This will be returned if an error occurred while doing any of the cache
# operations.
public type CacheError distinct error;

# Represents the operation canceled(typically by the caller) error.
public type CancelledError distinct error;

# Represents unknown error.(e.g. Status value received is unknown)
public type UnKnownError distinct error;

# Represents Cache related errors.
public type Error CacheError;

# Represents gRPC related errors.
public type GrpcError CancelledError | UnKnownError | CacheError;

# Represents link to GrpcError.
public type LinkToGrpcError GrpcError;

# Represents union of builtin error and string.
public type YErrorType error | string;

# Represents link to YErrorType.
public type LinktoYError YErrorType;
