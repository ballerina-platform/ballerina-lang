package ballerina.http;

public enum Chunking {
    AUTO, ALWAYS, NEVER
}

public enum Compression {
    AUTO, ALWAYS, NEVER
}

public enum TransferEncoding {
    CHUNKING
}

@Description { value:"TrustStore struct represents trust store related options to be used for HTTP client/service invocation"}
@Field {value:"filePath: File path to trust store file"}
@Field {value:"password: Trust store password"}
public struct TrustStore {
    string filePath;
    string password;
}

@Description { value:"KeyStore struct represents key store related options to be used for HTTP client/service invocation"}
@Field {value:"filePath: File path to key store file"}
@Field {value:"password: Key store password"}
public struct KeyStore {
    string filePath;
    string password;
}

@Description { value:"Protocols struct represents SSL/TLS protocol related options to be used for HTTP client/service invocation"}
@Field {value:"protocolVersion: SSL Protocol to be used. eg: TLS1.2"}
@Field {value:"enabledProtocols: SSL/TLS protocols to be enabled. eg: TLSv1,TLSv1.1,TLSv1.2"}
public struct Protocols {
    string protocolName;
    string versions;
}

@Description { value:"ValidateCert struct represents options related to check whether a certificate is revoked or not"}
@Field {value:"enable: The status of validateCertEnabled"}
@Field {value:"cacheSize: Maximum size of the cache"}
@Field {value:"cacheValidityPeriod: Time duration of cache validity period"}
public struct ValidateCert {
    boolean enable;
    int cacheSize;
    int cacheValidityPeriod;
}

@Description { value:"OcspStapling struct represents options related to check whether a certificate is revoked or not"}
@Field {value:"enable: The status of OcspStapling"}
@Field {value:"cacheSize: Maximum size of the cache"}
@Field {value:"cacheValidityPeriod: Time duration of cache validity period"}
public struct ServiceOcspStapling {
    boolean enable;
    int cacheSize;
    int cacheValidityPeriod;
}

@Description {value:"Represent all http payload related errors"}
@Field {value:"message: The error message"}
@Field {value:"cause: The error which caused the entity error"}
public struct PayloadError {
    string message;
    error[] cause;
}

//////////////////////////////
/// Native implementations ///
//////////////////////////////

@Description { value:"Parse headerValue and return value with parameter map"}
@Param { value:"headerValue: The header value" }
@Return { value:"The header value" }
@Return { value:"The header value parameter map" }
@Return { value:"Error occured during header parsing" }
public native function parseHeader (string headerValue) returns (string, map) | error;
