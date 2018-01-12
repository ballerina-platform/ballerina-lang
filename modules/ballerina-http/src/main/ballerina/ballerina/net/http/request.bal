package ballerina.net.http;

import ballerina.mime;
import ballerina.file;

const string HEADER_VAL_100_CONTINUE = "100-continue";
const string HEADER_KEY_EXPECT = "Expect";

@Description {value:"Gets a transport header from the request"}
@Param {value:"req: A request message"}
@Param {value:"headerName: The header name"}
@Return {value:"The first header value struct for the provided header name. Returns null if the header does not exist."}
public function <Request req> getHeader (string headerName) (mime:HeaderValue) {
    mime:Entity entity = req.getEntityWithoutBody();
    if (entity.headers == null) {
        return null;
    }
    var headerValues = entity.headers[headerName];
    if (headerValues == null) {
        return null;
    }
    return getHeaderValueArray(headerValues, headerName)[0];
}

@Description {value:"Adds the specified key/value pair as an HTTP header to the request"}
@Param {value:"req: A request message"}
@Param {value:"headerName: The header name"}
@Param {value:"headerValue: The header value"}
public function <Request req> addHeader (string headerName, string headerValue) {
    mime:Entity entity = req.getEntityWithoutBody();
    if (entity.headers == null) {
        entity.headers = {};
    }
    var headerValues = entity.headers[headerName];
    if (headerValues == null) {
        mime:HeaderValue[] headers = [{value:headerValue}];
        entity.headers[headerName] = headers;
    } else {
        var valueArray = getHeaderValueArray(headerValues, headerName);
        valueArray[lengthof valueArray] = {value:headerValue};
    }
}

@Description {value:"Gets transport headers from the request"}
@Param {value:"req: A request message"}
@Param {value:"headerName: The header name"}
@Return {value:"The header values struct array for a given header name"}
public function <Request req> getHeaders (string headerName) (mime:HeaderValue[]) {
    mime:Entity entity = req.getEntityWithoutBody();
    if (entity.headers == null) {
        return null;
    }
    var headerValues = entity.headers[headerName];
    if (headerValues == null) {
        return null;
    }
    return getHeaderValueArray(headerValues, headerName);
}

@Description {value:"Sets the value of a transport header"}
@Param {value:"req: A request message"}
@Param {value:"headerName: The header name"}
@Param {value:"headerValue: The header value"}
public function <Request req> setHeader (string headerName, string headerValue) {
    mime:Entity entity = req.getEntityWithoutBody();
    if (entity.headers == null) {
        entity.headers = {};
    }
    mime:HeaderValue[] header = [{value:headerValue}];
    entity.headers[headerName] = header;
}

@Description {value:"Sets the value of a transport header with multiple header values"}
@Param {value:"req: A request message"}
@Param {value:"headerName: The header name"}
@Param {value:"headerValues: An array of header values"}
public function <Request req> setHeaders (string headerName, mime:HeaderValue[] headerValues) {
    mime:Entity entity = req.getEntityWithoutBody();
    if (entity.headers == null) {
        entity.headers = {};
    }
    entity.headers[headerName] = headerValues;
}

@Description {value:"Removes a transport header from the request"}
@Param {value:"req: A request message"}
@Param {value:"key: The header name"}
public function <Request req> removeHeader (string key) {
    mime:Entity entity = req.getEntityWithoutBody();
    if (entity.headers == null) {
        return;
    }
    entity.headers.remove(key);
}

@Description {value:"Removes all transport headers from the message"}
@Param {value:"req: A request message"}
public function <Request req> removeAllHeaders () {
    mime:Entity entity = req.getEntityWithoutBody();
    entity.headers = {};
}

@Description {value:"Checks whether the client expects a 100-continue response."}
@Param {value:"req: A request struct"}
@Return {value:"Returns true if the client expects a 100-continue response. If not, returns false."}
public function <Request req> expects100Continue () (boolean) {
    var expectHeader = req.getHeader(HEADER_KEY_EXPECT);
    if (expectHeader != null && expectHeader.value == HEADER_VAL_100_CONTINUE) {
        return true;
    }
    return false;
}

@Description {value:"Gets the Content-Length header from the request"}
@Param {value:"req: A request message"}
@Return {value:"length of the message"}
public function <Request request> getContentLength () (int) {
    if (request.getHeader(CONTENT_LENGTH) != null) {
        string strContentLength = request.getHeader(CONTENT_LENGTH).value;
        var contentLength, conversionErr = <int>strContentLength;
        if (conversionErr != null) {
            contentLength = -1;
            throw conversionErr;
        }
        return contentLength;
    }
    return -1;
}

@Description {value:"Gets the request payload in JSON format"}
@Param {value:"request: The request message"}
@Return {value:"The JSON reresentation of the message payload"}
public function <Request request> getJsonPayload () (json) {
    mime:Entity entity = request.getEntity();
    return mime:getJson(entity);
}

@Description {value:"Gets the request payload in XML format"}
@Param {value:"request: The request message"}
@Return {value:"The XML representation of the message payload"}
public function <Request request> getXmlPayload () (xml) {
    mime:Entity entity = request.getEntity();
    return mime:getXml(entity);
}

@Description {value:"Gets the request payload as a string"}
@Param {value:"request: The request message"}
@Return {value:"The string representation of the message payload"}
public function <Request request> getStringPayload () (string) {
    mime:Entity entity = request.getEntity();
    return mime:getText(entity);
}

@Description {value:"Gets the request payload in blob format"}
@Param {value:"request: The request message"}
@Return {value:"The blob representation of the message payload"}
public function <Request request> getBinaryPayload () (blob) {
    mime:Entity entity = request.getEntity();
    return mime:getBlob(entity);
}

@Description {value:"Gets the form parameters from the HTTP request as a map"}
@Param {value:"req: The request message"}
@Return {value:"The map of form params"}
public function <Request request> getFormParams () (map) {
    mime:Entity entity = request.getEntity();
    string formData = mime:getText(entity);
    map parameters = {};
    if (formData != null) {
        string[] entries = formData.split("&");
        int i = 0;
        while (i < lengthof entries) {
            int index = entries[i].indexOf("=");
            if (index != -1) {
                string name = entries[i].subString(0, index).trim();
                int size = entries[i].length();
                string value = entries[i].subString(index + 1, size).trim();
                if (value != "") {
                    parameters[name] = value;
                }
            }
            i = i + 1;
        }
    }
    return parameters;
}

@Description {value:"Sets a JSON as the request payload"}
@Param {value:"request: The request message"}
@Param {value:"payload: The JSON payload to be set to the request"}
public function <Request request> setJsonPayload (json payload) {
    mime:Entity entity = {};
    entity.jsonData = payload;
    mime:MediaType mediaType = mime:getMediaType(mime:APPLICATION_JSON);
    entity.contentType = mediaType;
    entity.isInMemory = true;
    request.setEntity(entity);
}

@Description {value:"Sets an XML as the payload"}
@Param {value:"request: The request message"}
@Param {value:"payload: The XML payload object"}
public function <Request request> setXmlPayload (xml payload) {
    mime:Entity entity = {};
    entity.xmlData = payload;
    mime:MediaType mediaType = mime:getMediaType(mime:APPLICATION_XML);
    entity.contentType = mediaType;
    entity.isInMemory = true;
    request.setEntity(entity);
}

@Description {value:"Sets a string as the request payload"}
@Param {value:"request: The request message"}
@Param {value:"payload: The payload to be set to the request as a string"}
public function <Request request> setStringPayload (string payload) {
    mime:Entity entity = {};
    entity.textData = payload;
    mime:MediaType mediaType = mime:getMediaType(mime:TEXT_PLAIN);
    entity.contentType = mediaType;
    entity.isInMemory = true;
    request.setEntity(entity);
}

@Description {value:"Sets a blob as the request payload"}
@Param {value:"request: The request message"}
@Param {value:"payload: The blob representation of the message payload"}
public function <Request request> setBinaryPayload (blob payload) {
    mime:Entity entity = {};
    entity.byteData = payload;
    mime:MediaType mediaType = mime:getMediaType(mime:APPLICATION_OCTET_STREAM);
    entity.contentType = mediaType;
    entity.isInMemory = true;
    request.setEntity(entity);
}

@Description {value:"Sets the entity body of the request with the given file content"}
@Param {value:"request: The request message"}
@Param {value:"content: File containing the actual content"}
@Param {value:"contentType: Content-Type of the given data"}
public function <Request request> setEntityBody (file:File content, string contentType) {
    mime:MediaType mediaType = mime:getMediaType(contentType);
    mime:Entity entity = request.getEntityWithoutBody();
    entity.contentType = mediaType;
    entity.isInMemory = false;
    entity.overflowData = content;
    request.setEntity(entity);
}
