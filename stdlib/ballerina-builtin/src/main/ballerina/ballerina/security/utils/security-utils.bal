package ballerina.security.utils;

import ballerina.config;
import ballerina.caching;
import ballerina.util;
import ballerina.net.http;

@Description {value:"Configuration entry to check if a cache is enabled"}
const string CACHE_ENABLED = "enabled";
@Description {value:"Configuration entry for cache expiry time"}
const string CACHE_EXPIRY_TIME = "expiryTime";
@Description {value:"Configuration entry for cache capacity"}
const string CACHE_CAPACITY = "capacity";
@Description {value:"Configuration entry for eviction factor"}
const string CACHE_EVICTION_FACTOR = "evictionFactor";
@Description {value:"Authentication header name"}
const string AUTH_HEADER = "Authorization";
@Description {value:"Basic authentication scheme"}
const string AUTH_SCHEME = "Basic";

@Description {value:"Default value for enabling cache"}
const boolean CACHE_ENABLED_DEFAULT_VALUE = true;
@Description {value:"Default value for cache expiry"}
const int CACHE_EXPIRY_DEFAULT_VALUE = 300000;
@Description {value:"Default value for cache capacity"}
const int CACHE_CAPACITY_DEFAULT_VALUE = 100;
@Description {value:"Default value for cache eviction factor"}
const float CACHE_EVICTION_FACTOR_DEFAULT_VALUE = 0.25;

@Description {value:"Creates a cache to store authentication results against basic auth headers"}
@Return {value:"cache: authentication cache instance"}
public function createCache (string cacheName) (caching:Cache) {
    if (isCacheEnabled(cacheName)) {
        int expiryTime;
        int capacity;
        float evictionFactor;
        expiryTime, capacity, evictionFactor = getCacheConfigurations(cacheName);
        return caching:createCache(cacheName, expiryTime, capacity, evictionFactor);
    }
    return null;
}

@Description {value:"Checks if the specified cache is enalbed"}
@Param {value:"cacheName: cache name"}
@Return {value:"boolean: true of the cache is enabled, else false"}
function isCacheEnabled (string cacheName) (boolean) {
    string isCacheEnabled = config:getInstanceValue(cacheName, CACHE_ENABLED);
    boolean boolIsCacheEnabled;
    if (isCacheEnabled == null) {
        // by default we enable the cache
        boolIsCacheEnabled = CACHE_ENABLED_DEFAULT_VALUE;
    } else {
        TypeConversionError typeConversionErr;
        boolIsCacheEnabled, typeConversionErr = <boolean>isCacheEnabled;
        if (typeConversionErr != null) {
            boolIsCacheEnabled = CACHE_ENABLED_DEFAULT_VALUE;
        }
    }
    return boolIsCacheEnabled;
}

@Description {value:"Reads the cache configurations"}
@Param {value:"cacheName: cache name"}
@Return {value:"int: cache expiry time"}
@Return {value:"int: cache capacity"}
@Return {value:"float: cache eviction factor"}
function getCacheConfigurations (string cacheName) (int, int, float) {
    // expiry time
    string expiryTime = config:getInstanceValue(cacheName, CACHE_EXPIRY_TIME);
    int intExpiryTime;
    if (expiryTime == null) {
        // set the default
        intExpiryTime = CACHE_EXPIRY_DEFAULT_VALUE;
    } else {
        TypeConversionError typeConversionErr;
        intExpiryTime, typeConversionErr = <int>expiryTime;
        if (typeConversionErr != null) {
            intExpiryTime = CACHE_EXPIRY_DEFAULT_VALUE;
        }
    }
    // capacity
    string capacity = config:getInstanceValue(cacheName, CACHE_CAPACITY);
    int intCapacity;
    if (capacity == null) {
        intCapacity = CACHE_CAPACITY_DEFAULT_VALUE;
    } else {
        TypeConversionError typeConversionErr;
        intCapacity, typeConversionErr = <int>capacity;
        if (typeConversionErr != null) {
            intCapacity = CACHE_CAPACITY_DEFAULT_VALUE;
        }
    }
    // eviction factor
    string evictionFactor = config:getInstanceValue(cacheName, CACHE_EVICTION_FACTOR);
    float floatEvictionFactor;
    if (evictionFactor == null) {
        floatEvictionFactor = CACHE_EVICTION_FACTOR_DEFAULT_VALUE;
    } else {
        TypeConversionError typeConversionErr;
        floatEvictionFactor, typeConversionErr = <float>evictionFactor;
        if (typeConversionErr != null || floatEvictionFactor > 1.0) {
            floatEvictionFactor = CACHE_EVICTION_FACTOR_DEFAULT_VALUE;
        }
    }

    return intExpiryTime, intCapacity, floatEvictionFactor;
}

@Description {value:"Extracts the basic authentication header value from the request"}
@Param {value:"req: Inrequest instance"}
@Return {value:"string: value of the basic authentication header"}
@Return {value:"error: any error occurred while extracting the basic authentication header"}
public function extractBasicAuthHeaderValue (http:InRequest req) (string, error) {
    // extract authorization header
    string basicAuthHeader = req.getHeader(AUTH_HEADER);
    if (basicAuthHeader == null || !basicAuthHeader.hasPrefix(AUTH_SCHEME)) {
        return null, handleError("Basic authentication header not sent with the request");
    }
    return basicAuthHeader, null;
}

@Description {value:"Extracts the basic authentication credentials from the header value"}
@Param {value:"authHeader: basic authentication header"}
@Return {value:"string: username extracted"}
@Return {value:"string: password extracted"}
@Return {value:"error: any error occurred while extracting creadentials"}
public function extractBasicAuthCredentials (string authHeader) (string, string, error) {
    // extract user credentials from basic auth header
    string decodedBasicAuthHeader;
    try {
        decodedBasicAuthHeader = util:base64Decode(authHeader.subString(5, authHeader.length()).trim());
    } catch (error err) {
        return null, null, err;
    }
    string[] decodedCredentials = decodedBasicAuthHeader.split(":");
    if (lengthof decodedCredentials != 2) {
        return null, null, handleError("Incorrect basic authentication header format");
    } else {
        return decodedCredentials[0], decodedCredentials[1], null;
    }
}

@Description {value:"Error handler"}
@Param {value:"message: error message"}
@Return {value:"error: error populated with the message"}
function handleError (string message) (error) {
    error e = {msg:message};
    return e;
}
