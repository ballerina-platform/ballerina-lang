# Represents the Cache error type with details. This will be returned if an error occurred while doing any of the cache
# operations.
public type CacheError distinct error;

# Represents Cache related errors.
public type Error CacheError;
