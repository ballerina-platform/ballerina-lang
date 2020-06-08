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

// Ballerina MIME Error Types
# Represents an `EncodeError` with the message and the cause.
public type EncodeError distinct error;

# Represents a `DecodeError` with the message and the cause.
public type DecodeError distinct error;

# Represents a `GenericMimeError` with the message and the cause.
public type GenericMimeError distinct error;

# Represents a `SetHeaderError` with the message and the cause.
public type SetHeaderError distinct error;

# Represents a `InvalidHeaderValueError` error with the message and the cause.
public type InvalidHeaderValueError distinct error;

# Represents a `InvalidHeaderParamError` error with the message and the cause.
public type InvalidHeaderParamError distinct error;

# Represents a `InvalidContentLengthError` error with the message and the cause.
public type InvalidContentLengthError distinct error;

# Represents a `HeaderNotFoundError` error with the message and the cause.
public type HeaderNotFoundError distinct error;

# Represents a `InvalidHeaderOperationError` error with the message and the cause.
public type InvalidHeaderOperationError distinct error;

# Represents a `SerializationError` error with the message and the cause.
public type SerializationError distinct error;

# Represents a `ParserError` with the message and the cause.
public type ParserError distinct error;

# Represents an `InvalidContentTypeError` with the message and the cause.
public type InvalidContentTypeError distinct error;

# Represents a `HeaderUnavailableError` with the message and the cause.
public type HeaderUnavailableError distinct error;

# Represents an `IdleTimeoutTriggeredError` with the message and the cause.
public type IdleTimeoutTriggeredError distinct error;

# Represents a `NoContentError` with the message and the cause.
public type NoContentError distinct error;

# Represents MIME related errors.
public type Error ParserError|EncodeError|DecodeError|GenericMimeError|SetHeaderError|InvalidContentTypeError
                |HeaderUnavailableError|IdleTimeoutTriggeredError|NoContentError|SerializationError
                |InvalidHeaderValueError|InvalidHeaderParamError|InvalidContentLengthError|HeaderNotFoundError
                |InvalidHeaderOperationError;

# Constructs an `EncodeError` with the given details.
#
# + detail - Error details
# + return - An `EncodeError` with the given details set to the message
public function prepareEncodingErrorWithDetail(string detail) returns EncodeError {
    return EncodeError(detail);
}

# Constructs a `DecodeError` with the given details.
#
# + detail - Error details
# + return - `DecodeError` with the given details set to the message
public function prepareDecodingErrorWithDetail(string detail) returns DecodeError {
    return DecodeError(detail);
}
