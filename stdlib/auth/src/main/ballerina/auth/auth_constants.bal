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

# Constant for the auth error code.
public const AUTH_ERROR_CODE = "{ballerina/auth}AuthError";

# Constant for empty string.
const string EMPTY_STRING = "";

# Default charset to be used with password hashing.
public const string DEFAULT_CHARSET = "UTF-8";

# Prefix used to denote special configuration values.
public const string CONFIG_PREFIX = "@";

# Prefix used to denote that the config value is a SHA-256 hash.
public const string CONFIG_PREFIX_SHA256 = "@sha256:";

# Prefix used to denote that the config value is a SHA-384 hash.
public const string CONFIG_PREFIX_SHA384 = "@sha384:";

# Prefix used to denote that the config value is a SHA-512 hash.
public const string CONFIG_PREFIX_SHA512 = "@sha512:";
