
@Description { value:"Represents a HTTP/2 Push Promise"}
@Field {value:"path: Resource path"}
@Field {value:"method: Http method"}
public type PushPromise object {
    public {
        string path;
        string method;
    }

    public new (path = "/", method = "GET") {
    }

    @Description {value:"Checks whether the requested header exists"}
    @Param {value:"headerName: The header name"}
    @Return {value:"A boolean representing the existence of a given header"}
    public native function hasHeader (string headerName) returns (boolean);

    @Description {value:"Returns the header value with the specified header name. If there are more than one header value for the specified header name, the first value is returned."}
    @Param {value:"headerName: The header name"}
    @Return {value:"The first header value for the provided header name. Returns null if the header does not exist."}
    public native function getHeader (string headerName) returns (string);

    @Description {value:"Gets transport headers from the Push Promise"}
    @Param {value:"headerName: The header name"}
    @Return {value:"The header values struct array for a given header name"}
    public native function getHeaders (string headerName) returns (string[]);

    @Description {value:"Adds the specified key/value pair as an HTTP header to the Push Promise"}
    @Param {value:"headerName: The header name"}
    @Param {value:"headerValue: The header value"}
    public native function addHeader (string headerName, string headerValue);

    @Description {value:"Sets the value of a transport header in Push Promise"}
    @Param {value:"headerName: The header name"}
    @Param {value:"headerValue: The header value"}
    public native function setHeader (string headerName, string headerValue);

    @Description {value:"Removes a transport header from the Push Promise"}
    @Param {value:"key: The header name"}
    public native function removeHeader (string key);

    @Description {value:"Removes all transport headers from the Push Promise"}
    public native function removeAllHeaders ();

    @Description {value:"Gets all transport header names from the Push Promise."}
    @Return {value:"An array of all transport header names"}
    public native function getHeaderNames () returns (string[]);
};

