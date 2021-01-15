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
