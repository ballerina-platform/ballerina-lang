package ballerina.http;

import ballerina/file;
import ballerina/io;
import ballerina/mime;

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
    RequestCacheControl cacheControl;
}

public function <Request request> Request() {
    request.cacheControl = {};
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
public native function <Request req> getQueryParams () returns (map);

@Description {value:"Get matrix parameters from the request"}
@Param {value:"req: The request message"}
@Param {value:"path: Path to the location of matrix parameters"}
@Return {value:"A map of matrix paramters which can be found for a given path"}
public native function <Request req> getMatrixParams (string path) returns (map);

@Description {value:"Get the entity from the request"}
@Param {value:"req: The request message"}
@Return {value:"Entity of the request"}
@Return {value:"EntityError will might get thrown during entity construction in case of errors"}
public native function <Request req> getEntity () returns (mime:Entity | mime:EntityError);

@Description {value:"Get the entity from the request without the body. This function is to be used only internally"}
@Param {value:"req: The request message"}
@Return {value:"Entity of the request"}
native function <Request req> getEntityWithoutBody () returns (mime:Entity);

/////////////////////////////////
/// Ballerina Implementations ///
/////////////////////////////////

const string HEADER_VAL_100_CONTINUE = "100-continue";
const string HEADER_KEY_EXPECT = "Expect";

@Description {value:"Check whether the requested header exists"}
@Param {value:"req: The request message"}
@Param {value:"headerName: The header name"}
@Return {value:"Boolean representing the existence of a given header"}
public function <Request req> hasHeader (string headerName) returns (boolean) {
    mime:Entity entity = req.getEntityWithoutBody();
    return entity.hasHeader(headerName);
}

@Description {value:"Returns the header value with the specified header name. If there are more than one header value for the specified header name, the first value is returned."}
@Param {value:"req: The request message"}
@Param {value:"headerName: The header name"}
@Return {value:"The first header value for the provided header name. Returns null if the header does not exist."}
public function <Request req> getHeader (string headerName) returns (string) {
    mime:Entity entity = req.getEntityWithoutBody();
    return entity.getHeader(headerName);
}

@Description {value:"Gets transport headers from the request"}
@Param {value:"req: The request message"}
@Param {value:"headerName: The header name"}
@Return {value:"The header values struct array for a given header name"}
public function <Request req> getHeaders (string headerName) returns (string[]) {
    mime:Entity entity = req.getEntityWithoutBody();
    return entity.getHeaders(headerName);
}

@Description {value:"Sets the value of a transport header"}
@Param {value:"req: The request message"}
@Param {value:"headerName: The header name"}
@Param {value:"headerValue: The header value"}
public function <Request req> setHeader (string headerName, string headerValue) {
    mime:Entity entity = req.getEntityWithoutBody();
    entity.setHeader(headerName, headerValue);
}

@Description {value:"Adds the specified key/value pair as an HTTP header to the request"}
@Param {value:"req: The request message"}
@Param {value:"headerName: The header name"}
@Param {value:"headerValue: The header value"}
public function <Request req> addHeader (string headerName, string headerValue) {
    mime:Entity entity = req.getEntityWithoutBody();
    entity.addHeader(headerName, headerValue);
}

@Description {value:"Removes a transport header from the request"}
@Param {value:"req: The request message"}
@Param {value:"key: The header name"}
public function <Request req> removeHeader (string key) {
    mime:Entity entity = req.getEntityWithoutBody();
    entity.removeHeader(key);
}

@Description {value:"Removes all transport headers from the message"}
@Param {value:"req: The request message"}
public function <Request req> removeAllHeaders () {
    mime:Entity entity = req.getEntityWithoutBody();
    entity.removeAllHeaders();
}

@Description {value:"Get all transport headers from the request. Manipulating the return map does not have any impact to the original copy"}
@Param {value:"req: The request message"}
public function <Request req> getCopyOfAllHeaders () returns (map) {
    mime:Entity entity = req.getEntityWithoutBody();
    return entity.getCopyOfAllHeaders();
}

@Description {value:"Checks whether the client expects a 100-continue response."}
@Param {value:"req: The request message"}
@Return {value:"Returns true if the client expects a 100-continue response. If not, returns false."}
public function <Request req> expects100Continue () returns (boolean) {
    return req.getHeader(HEADER_KEY_EXPECT) ==  HEADER_VAL_100_CONTINUE;
}

@Description {value:"Gets the request payload in JSON format"}
@Param {value:"request: The request message"}
@Return {value:"The JSON reresentation of the message payload or 'PayloadError' in case of errors"}
public function <Request request> getJsonPayload () returns (json | PayloadError) {
    match request.getEntity() {
        mime:EntityError err => return <PayloadError>err;
        mime:Entity mimeEntity => {
            match mimeEntity.getJson() {
                mime:EntityError payloadErr => return <PayloadError>payloadErr;
                json jsonPayload => return jsonPayload;
            }
        }
    }
}

@Description {value:"Gets the request payload in XML format"}
@Param {value:"request: The request message"}
@Return {value:"The XML representation of the message payload or 'PayloadError' in case of errors"}
public function <Request request> getXmlPayload () returns (xml | PayloadError) {
    match request.getEntity() {
        mime:EntityError err => return <PayloadError>err;
        mime:Entity mimeEntity => {
            match mimeEntity.getXml() {
                mime:EntityError payloadErr => return <PayloadError>payloadErr;
                xml xmlPayload => return xmlPayload;
            }
        }
    }
}

@Description {value:"Gets the request payload as a string"}
@Param {value:"request: request message"}
@Return {value:"The string representation of the message payload or 'PayloadError' in case of errors"}
public function <Request request> getStringPayload () returns (string | PayloadError) {
    match request.getEntity() {
        mime:EntityError err => return <PayloadError>err;
        mime:Entity mimeEntity => {
            match mimeEntity.getText() {
                mime:EntityError payloadErr => return <PayloadError>payloadErr;
                string textPayload => return textPayload;
            }
        }
    }
}

@Description {value:"Gets the request payload in blob format"}
@Param {value:"request: The request message"}
@Return {value:"The blob representation of the message payload or 'PayloadError' in case of errors"}
public function <Request request> getBinaryPayload () returns (blob | PayloadError) {
    match request.getEntity() {
        mime:EntityError err => return <PayloadError>err;
        mime:Entity mimeEntity => {
            match mimeEntity.getBlob() {
                mime:EntityError payloadErr => return <PayloadError>payloadErr;
                blob binaryPayload => return binaryPayload;
            }
        }
    }
}

@Description {value:"Get the request payload as a byte channel except for multiparts. In case of multiparts,
please use 'getMultiparts()' instead."}
@Param {value:"request: The request message"}
@Return {value:"A byte channel as the message payload or 'PayloadError' in case of errors"}
public function <Request request> getByteChannel () returns (io:ByteChannel | PayloadError) {
    match request.getEntity() {
        mime:EntityError err => return <PayloadError>err;
            mime:Entity mimeEntity => {
            match mimeEntity.getByteChannel() {
                mime:EntityError payloadErr => return <PayloadError>payloadErr;
                io:ByteChannel byteChannel => return byteChannel;
            }
        }
    }
}

@Description {value:"Gets the form parameters from the HTTP request as a map"}
@Param {value:"req: The request message"}
@Return {value:"The map of form params or 'PayloadError' in case of errors"}
public function <Request request> getFormParams () returns (map | PayloadError) {
    var mimeEntity = request.getEntity();     
    match mimeEntity {
        mime:EntityError err => return <PayloadError>err;
        mime:Entity entity => {

            map parameters = {};
            var entityText = entity.getText();
            match entityText {
                mime:EntityError txtErr => return <PayloadError>txtErr; // TODO: Check if this is ok

                string formData => {
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
                }
            }
            return parameters;
        }
    }
}

@Description {value:"Get multiparts from request"}
@Param {value:"req: The request message"}
@Return {value:"Returns the body parts as an array of entities"}
public function <Request request> getMultiparts () returns (mime:Entity[] | mime:EntityError) {
    var mimeEntity = request.getEntity();
    match mimeEntity {
        mime:Entity entity => return entity.getBodyParts();
        mime:EntityError err => return err;
    }
}

@Description {value:"Sets a JSON as the request payload"}
@Param {value:"request: The request message"}
@Param {value:"payload: The JSON payload to be set to the request"}
public function <Request request> setJsonPayload (json payload) {
    mime:Entity entity = request.getEntityWithoutBody();
    entity.setJson(payload);
    entity.contentType = getMediaTypeFromRequest(request, mime:APPLICATION_JSON);
    request.setEntity(entity);
}

@Description {value:"Sets an XML as the payload"}
@Param {value:"request: The request message"}
@Param {value:"payload: The XML payload object"}
public function <Request request> setXmlPayload (xml payload) {
    mime:Entity entity = request.getEntityWithoutBody();
    entity.setXml(payload);
    entity.contentType = getMediaTypeFromRequest(request, mime:APPLICATION_XML);
    request.setEntity(entity);
}

@Description {value:"Sets a string as the request payload"}
@Param {value:"request: The request message"}
@Param {value:"payload: The payload to be set to the request as a string"}
public function <Request request> setStringPayload (string payload) {
    mime:Entity entity = request.getEntityWithoutBody();
    entity.setText(payload);
    entity.contentType = getMediaTypeFromRequest(request, mime:TEXT_PLAIN);
    request.setEntity(entity);
}

@Description {value:"Sets a blob as the request payload"}
@Param {value:"request: request message"}
@Param {value:"payload: The blob representation of the message payload"}
public function <Request request> setBinaryPayload (blob payload) {
    mime:Entity entity = request.getEntityWithoutBody();
    entity.setBlob(payload);
    entity.contentType = getMediaTypeFromRequest(request, mime:APPLICATION_OCTET_STREAM);
    request.setEntity(entity);
}

@Description {value:"Set multiparts as the request payload"}
@Param {value:"request: The request message"}
@Param {value:"bodyParts: Represent body parts that needs to be set to the request"}
@Param {value:"contentType: Content type of the top level message"}
public function <Request request> setMultiparts (mime:Entity[] bodyParts, string contentType) {
    mime:Entity entity = request.getEntityWithoutBody();
    mime:MediaType mediaType = getMediaTypeFromRequest(request, mime:MULTIPART_MIXED);
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

@Description {value:"Construct MediaType struct from the content-type header value"}
@Param {value:"request: The outbound request message"}
@Param {value:"defaultContentType: Default content-type to be used in case the content-type header doesn't contain any value"}
@Return {value:"Return 'MediaType' struct"}
function getMediaTypeFromRequest (Request request, string defaultContentType) returns (mime:MediaType) {
    mime:MediaType mediaType = mime:getMediaType(defaultContentType);

    if (request.hasHeader(mime:CONTENT_TYPE)) {
        string contentTypeValue = request.getHeader(mime:CONTENT_TYPE);
        if (contentTypeValue != "") { // TODO: may need to trim this before doing an empty string check
            return mime:getMediaType(contentTypeValue);
        } else {
            return mediaType;
        }
    } else {
        return mediaType;
    }
}
