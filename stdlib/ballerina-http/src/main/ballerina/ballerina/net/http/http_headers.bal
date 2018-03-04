package ballerina.net.http;

// Please maintain alphabetical order when adding new header names
@Description {value:"HTTP header key 'Age'. Gives the current age of a cached HTTP response."}
public const string AGE = "Age";
@Description {value:"HTTP header key 'Authorization'"}
public const string AUTHORIZATION = "Authorization";
@Description {value:"HTTP header key 'Cache-Control'. Specifies the cache control directives required for the function of HTTP caches."}
public const string CACHE_CONTROL = "Cache-Control";
@Description {value:"HTTP header key 'Date'. The timestamp at the time the response was generated/received."}
public const string DATE = "Date";
@Description {value:"HTTP header key 'Etag'. A finger print for a resource which is used by HTTP caches to identify whether a resource representation has changed."}
public const string ETAG = "Etag";
@Description {value:"HTTP header key 'Expires'. Specifies the time at which the response becomes stale."}
public const string EXPIRES = "Expires";
@Description {value:"HTTP header key 'If-Match'"}
public const string IF_MATCH = "If-Match";
@Description {value:"HTTP header key 'If-Modified-Since'. Used when validating (with the origin server) whether a cached response is still valid. If the representation of the resource has modified since the timestamp in this field, a 304 response is returned."}
public const string IF_MODIFIED_SINCE = "If-Modified-Since";
@Description {value:"HTTP header key 'If-None-Match'. Used when validating (with the origin server) whether a cached response is still valid. If the ETag provided in this field matches the representation of the requested resource, a 304 response is returned."}
public const string IF_NONE_MATCH = "If-None-Match";
@Description {value:"HTTP header key 'If-Range'"}
public const string IF_RANGE = "If-Range";
@Description {value:"HTTP header key 'If-Unmodified-Since'"}
public const string IF_UNMODIFIED_SINCE = "If-Unmodified-Since";
@Description {value:"HTTP header key 'Last-Modified'. The time at which the resource was last modified."}
public const string LAST_MODIFIED = "Last-Modified";
@Description {value:"HTTP header key 'Pragma'"}
public const string PRAGMA = "Pragma";
@Description {value:"HTTP header key 'Warning'. Specifies warnings generated when serving stale responses from HTTP caches."}
public const string WARNING = "Warning";
