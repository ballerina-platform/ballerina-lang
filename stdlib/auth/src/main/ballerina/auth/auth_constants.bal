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

# Constant for the auth error code
public final string AUTH_ERROR_CODE = "{ballerina/auth}AuthError";

public type InboundAuthScheme BASIC_AUTH|JWT;

public type OutboundAuthScheme BASIC_AUTH|OAUTH2|JWT;

public const BASIC_AUTH = "Basic";
public const OAUTH2 = "OAuth2";
public const JWT = "JWT";

public type AuthStoreProvider CONFIG|LDAP;

public const CONFIG = "Config";
public const LDAP = "LDAP";