// Copyright (c) 2019 WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
//
// WSO2 Inc. licenses this file to you under the Apache License,
// Version 2.0 (the "License"); you may not use this file except
// in compliance with the License.
// You may obtain a copy of the License at
//
// http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing,
// software distributed under the License is distributed on an
// "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
// KIND, either express or implied.  See the License for the
// specific language governing permissions and limitations
// under the License.

public type Detail record {
    string message;
    error cause?;
};

// Ballerina MIME Error Types
public const ENCODE_ERROR = "{ballerina/mime}EncodingFailed";
public type EncodeError error<ENCODE_ERROR, Detail>;

public const DECODE_ERROR = "{ballerina/mime}DecodingFailed";
public type DecodeError error<DECODE_ERROR, Detail>;

public const GENERIC_MIME_ERROR = "{ballerina/mime}GenericMimeError";
public type GenericMimeError error<GENERIC_MIME_ERROR, Detail>;

public const SET_HEADER_FAILED = "{ballerina/mime}SetHeaderFailed";
public type SetHeaderError error<SET_HEADER_FAILED, Detail>;

// TODO: Give a proper name for this.
public const READING_HEADER_FAILED = "{ballerina/mime}ReadingHeaderFailed";
public type ReadingHeaderFailed error<READING_HEADER_FAILED, Detail>;

public const PARSER_ERROR = "{ballerina/mime}ParsingEntityBodyFailed";
public type ParserError error<PARSER_ERROR, Detail>;

public const INVALID_CONTENT_TYPE = "{ballerina/mime}InvalidContentType";
public type InvalidContentTypeError error<INVALID_CONTENT_TYPE, Detail>;

public const HEADER_UNAVAILABLE = "{ballerina/mime}HeaderUnavailable";
public type HeaderUnavailableError error<HEADER_UNAVAILABLE, Detail>;

public const IDLE_TIMEOUT_TRIGGERED = "{ballerina/http}IdleTimeoutTriggeredError";
public type IdleTimeoutTriggeredError error<IDLE_TIMEOUT_TRIGGERED, Detail>;

public type Error ParserError|EncodeError|DecodeError|GenericMimeError|SetHeaderError|InvalidContentTypeError
                |ReadingHeaderFailed|InvalidContentTypeError|HeaderUnavailableError|IdleTimeoutTriggeredError;

public function prepareEncodingErrorWithDetail(string detail) returns EncodeError {
    return error(ENCODE_ERROR, message = detail);
}

public function prepareDecodingErrorWithDetail(string detail) returns DecodeError {
    return error(DECODE_ERROR, message = detail);
}
