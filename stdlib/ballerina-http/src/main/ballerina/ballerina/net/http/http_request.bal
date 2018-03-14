package ballerina.net.http;

import ballerina.mime;
import ballerina.file;
import ballerina.io;

@Description { value:"Represents an HTTP request message"}
@Field {value:"path: Resource path of request URI"}
@Field {value:"method: HTTP request method"}
@Field {value:"httpVersion: The version of HTTP"}
@Field {value:"userAgent: User-Agent request header"}
@Field {value:"extraPathInfo: Additional information associated with the URL sent by the client"}
public struct Request {
    string rawPath;
    string method;
    string httpVersion;
    string userAgent;
    string extraPathInfo;
}

//////////////////////////////
/// Native implementations ///
//////////////////////////////

@Description {value:"Set the entity to request"}
@Param {value:"req: The request message"}
@Return {value:"Entity of the request"}
public native function <Request req> setEntity (mime:Entity entity);

@Description {value:"Gets the query parameters from the HTTP request as a map"}
@Param {value:"req: The request message"}
@Return {value:"The map of query params"}
public native function <Request req> getQueryParams () (map);

@Description {value:"Retrieves the named property from the request"}
@Param {value:"req: The request message"}
@Param {value:"propertyName: The name of the property"}
@Return {value:"The property value"}
public native function <Request req> getProperty (string propertyName) (string);

@Description {value:"Sets a request property"}
@Param {value:"req: The request message"}
@Param {value:"propertyName: The name of the property"}
@Param {value:"propertyValue: The value of the property"}
public native function <Request req> setProperty (string propertyName, string propertyValue);

@Description {value:"Get matrix parameters from the request"}
@Param {value:"req: The request message"}
@Param {value:"path: Path to the location of matrix parameters"}
@Return {value:"A map of matrix paramters which can be found for a given path"}
public native function <Request req> getMatrixParams (string path) (map);

@Description {value:"Get the entity from the request"}
@Param {value:"req: The request message"}
@Return {value:"Entity of the request"}
@Return {value:"EntityError will might get thrown during entity construction in case of errors"}
public native function <Request req> getEntity () (mime:Entity, mime:EntityError);

@Description {value:"Get the entity from the request without the body. This function is to be used only internally"}
@Param {value:"req: The request message"}
@Return {value:"Entity of the request"}
native function <Request req> getEntityWithoutBody () (mime:Entity);

/////////////////////////////////
/// Ballerina Implementations ///
/////////////////////////////////

const string HEADER_VAL_100_CONTINUE = "100-continue";
const string HEADER_KEY_EXPECT = "Expect";

@Description {value:"Returns the header value with the specified header name. If there are more than one header value for the specified header name, the first value is returned."}
@Param {value:"req: The request message"}
@Param {value:"headerName: The header name"}
@Return {value:"The first header value for the provided header name. Returns null if the header does not exist."}
public function <Request req> getHeader (string headerName) (string) {
    mime:Entity entity = req.getEntityWithoutBody();
    return getFirstHeaderFromEntity(entity, headerName);
}

@Description {value:"Gets transport headers from the request"}
@Param {value:"req: The request message"}
@Param {value:"headerName: The header name"}
@Return {value:"The header values struct array for a given header name"}
public function <Request req> getHeaders (string headerName) (string[]) {
    mime:Entity entity = req.getEntityWithoutBody();
    return getHeadersFromEntity(entity, headerName);
}

@Description {value:"Sets the value of a transport header"}
@Param {value:"req: The request message"}
@Param {value:"headerName: The header name"}
@Param {value:"headerValue: The header value"}
public function <Request req> setHeader (string headerName, string headerValue) {
    mime:Entity entity = req.getEntityWithoutBody();
    setHeaderToEntity(entity, headerName, headerValue);
}

@Description {value:"Adds the specified key/value pair as an HTTP header to the request"}
@Param {value:"req: The request message"}
@Param {value:"headerName: The header name"}
@Param {value:"headerValue: The header value"}
public function <Request req> addHeader (string headerName, string headerValue) {
    mime:Entity entity = req.getEntityWithoutBody();
    addHeaderToEntity(entity, headerName, headerValue);
}

@Description {value:"Removes a transport header from the request"}
@Param {value:"req: The request message"}
@Param {value:"key: The header name"}
public function <Request req> removeHeader (string key) {
    mime:Entity entity = req.getEntityWithoutBody();
    _ = entity.headers.remove(key);
}

@Description {value:"Removes all transport headers from the message"}
@Param {value:"req: The request message"}
public function <Request req> removeAllHeaders () {
    mime:Entity entity = req.getEntityWithoutBody();
    entity.headers = {};
}

@Description {value:"Checks whether the client expects a 100-continue response."}
@Param {value:"req: The request message"}
@Return {value:"Returns true if the client expects a 100-continue response. If not, returns false."}
public function <Request req> expects100Continue () (boolean) {
    var expectHeader = req.getHeader(HEADER_KEY_EXPECT);
    if (expectHeader != null && expectHeader == HEADER_VAL_100_CONTINUE) {
        return true;
    }
    return false;
}

@Description {value:"Gets the Content-Length header from the request"}
@Param {value:"req: The request message"}
@Return {value:"length of the message"}
public function <Request request> getContentLength () (int) {
    if (request.getHeader(CONTENT_LENGTH) != null) {
        string strContentLength = request.getHeader(CONTENT_LENGTH);
        return getContentLengthIntValue(strContentLength);
    }
    return -1;
}

@Description {value:"Gets the request payload in JSON format"}
@Param {value:"request: The request message"}
@Return {value:"The JSON reresentation of the message payload"}
public function <Request request> getJsonPayload () (json, mime:EntityError) {
    var entity, entityError = request.getEntity();
    if (entityError != null) {
        return null, entityError;
    }
    return entity.getJson();
}

@Description {value:"Gets the request payload in XML format"}
@Param {value:"request: The request message"}
@Return {value:"The XML representation of the message payload"}
public function <Request request> getXmlPayload () (xml, mime:EntityError) {
    var entity, entityError = request.getEntity();
    if (entityError != null) {
        return null, entityError;
    }
    return entity.getXml();
}

@Description {value:"Gets the request payload as a string"}
@Param {value:"request: request message"}
@Return {value:"The string representation of the message payload"}
public function <Request request> getStringPayload () (string, mime:EntityError) {
    var entity, entityError = request.getEntity();
    if (entityError != null) {
        return null, entityError;
    }
    return entity.getText();
}

@Description {value:"Gets the request payload in blob format"}
@Param {value:"request: The request message"}
@Return {value:"The blob representation of the message payload"}
public function <Request request> getBinaryPayload () (blob, mime:EntityError) {
    var entity, entityError = request.getEntity();
    if (entityError != null) {
        blob byteData;
        return byteData, entityError;
    }
    return entity.getBlob();
}

@Description {value:"Get the request payload as a byte channel except for multiparts. In case of multiparts,
please use 'getMultiparts()' instead."}
@Param {value:"request: The request message"}
@Return {value:"A byte channel as the message payload"}
public function <Request request> getByteChannel () (io:ByteChannel, mime:EntityError) {
    var entity, entityError = request.getEntity();
    if (entityError != null) {
        return null, entityError;
    }
    return entity.getByteChannel();
}

@Description {value:"Gets the form parameters from the HTTP request as a map"}
@Param {value:"req: The request message"}
@Return {value:"The map of form params"}
public function <Request request> getFormParams () (map, mime:EntityError) {
    var entity, entityError = request.getEntity();
    if (entityError != null) {
        return null, entityError;
    }
    var formData, entityErr = entity.getText();
    map parameters = {};
    if (entityErr != null) {
        return parameters, entityErr;
    }
    if (formData != null && formData != "") {
        string[] entries = formData.split("&");
        int entryIndex = 0;
        while (entryIndex < lengthof entries) {
            int index = entries[entryIndex].indexOf("=");
            if (index != -1) {
                string name = entries[entryIndex].subString(0, index).trim();
                int size = entries[entryIndex].length();
                string value = entries[entryIndex].subString(index + 1, size).trim();
                if (value != "") {
                    parameters[name] = value;
                }
            }
            entryIndex = entryIndex + 1;
        }
    }
    return parameters, null;
}

@Description {value:"Get multiparts from request"}
@Param {value:"req: The request message"}
@Return {value:"Returns the body parts as an array of entities"}
public function <Request request> getMultiparts () (mime:Entity[], mime:EntityError) {
    var entity, entityError = request.getEntity();
    if (entityError != null) {
        return null, entityError;
    }
    return entity.getBodyParts();
}

@Description {value:"Sets a JSON as the request payload"}
@Param {value:"request: The request message"}
@Param {value:"payload: The JSON payload to be set to the request"}
public function <Request request> setJsonPayload (json payload) {
    mime:Entity entity = request.getEntityWithoutBody();
    entity.setJson(payload);
    mime:MediaType mediaType = mime:getMediaType(mime:APPLICATION_JSON);
    entity.contentType = mediaType;
    request.setEntity(entity);
}

@Description {value:"Sets an XML as the payload"}
@Param {value:"request: The request message"}
@Param {value:"payload: The XML payload object"}
public function <Request request> setXmlPayload (xml payload) {
    mime:Entity entity = request.getEntityWithoutBody();
    entity.setXml(payload);
    mime:MediaType mediaType = mime:getMediaType(mime:APPLICATION_XML);
    entity.contentType = mediaType;
    request.setEntity(entity);
}

@Description {value:"Sets a string as the request payload"}
@Param {value:"request: The request message"}
@Param {value:"payload: The payload to be set to the request as a string"}
public function <Request request> setStringPayload (string payload) {
    mime:Entity entity = request.getEntityWithoutBody();
    entity.setText(payload);
    mime:MediaType mediaType = mime:getMediaType(mime:TEXT_PLAIN);
    entity.contentType = mediaType;
    request.setEntity(entity);
}

@Description {value:"Sets a blob as the request payload"}
@Param {value:"request: request message"}
@Param {value:"payload: The blob representation of the message payload"}
public function <Request request> setBinaryPayload (blob payload) {
    mime:Entity entity = request.getEntityWithoutBody();
    entity.setBlob(payload);
    mime:MediaType mediaType = mime:getMediaType(mime:APPLICATION_OCTET_STREAM);
    entity.contentType = mediaType;
    request.setEntity(entity);
}

@Description {value:"Set multiparts as the request payload"}
@Param {value:"request: The request message"}
@Param {value:"bodyParts: Represent body parts that needs to be set to the request"}
@Param {value:"contentType: Content type of the top level message"}
public function <Request request> setMultiparts (mime:Entity[] bodyParts, string contentType) {
    mime:Entity entity = request.getEntityWithoutBody();
    mime:MediaType mediaType = mime:getMediaType(mime:MULTIPART_MIXED);
    if (contentType != null && contentType != "") {
        mediaType = mime:getMediaType(contentType);
    }
    entity.contentType = mediaType;
    entity.setBodyParts(bodyParts);
    request.setEntity(entity);
}

@Description {value:"Sets the entity body of the request with the given file content"}
@Param {value:"request: The request message"}
@Param {value:"fileHandler: File that needs to be set to the payload"}
@Param {value:"contentType: Content-Type of the given file"}
public function <Request request> setFileAsPayload (file:File fileHandler, string contentType) {
    mime:MediaType mediaType = mime:getMediaType(contentType);
    mime:Entity entity = request.getEntityWithoutBody();
    entity.setFileAsEntityBody(fileHandler);
    entity.contentType = mediaType;
    request.setEntity(entity);
}

@Description {value:"Set a byte channel as the request payload"}
@Param {value:"request: request message"}
@Param {value:"payload: The byte channel representation of the message payload"}
public function <Request request> setByteChannel (io:ByteChannel payload) {
    mime:Entity entity = request.getEntityWithoutBody();
    entity.setByteChannel(payload);
    request.setEntity(entity);
}
