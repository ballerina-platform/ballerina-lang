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

import ballerina/crypto;

# NATS Core Connection Configuration
#
# + connectionName - Connection's optional name
# + maxReconnect - Maximum number of reconnect attempts. Use 0 to turn off auto reconnect.
# Use -1 to turn on infinite reconnects. The reconnect state is entered when the connection is connected and loses
# that connection. During the initial connection attempt, the client will cycle over
# its server list one time, regardless of what maxReconnects is set to.
# + reconnectWait - The time to wait between reconnect attempts to the same server. Measure in seconds
# + connectionTimeout - The timeout for connection attempts. Measure in seconds
# + pingInterval - ** The interval between attempts to pings the server. Measure in minutes. **
# + maxPingsOut - ** the maximum number of pings the client can have in flight. Default to a small number
# + username - the username for basic authentication.
# + password - the password for basic authentication.
# + token - the token for token-based authentication.
# + inboxPrefix - The connection's inbox prefix. All inboxes will start with this string.
# + noEcho - Turn off echo. Prevent the server from echoing messages back to the connection if it
# has subscriptions on the subject being published to.
# + secureSocket - SSL/TLS related options
public type ConnectionConfig record {|
  string connectionName = "ballerina-nats";
  int maxReconnect = 60;
  int reconnectWait = 2;
  int connectionTimeout = 2;
  int pingInterval = 2;
  int maxPingsOut = 2;
  string username?;
  string password?;
  string token?;
  string inboxPrefix = "_INBOX.";
  boolean noEcho = false;
  SecureSocket? secureSocket = ();
|};

# Provides configurations for facilitating secure communication with a remote HTTP endpoint.
#
# + trustStore - Configurations associated with TrustStore
# + keyStore - Configurations associated with KeyStore
# + protocol - The standard name of the requested protocol.
public type SecureSocket record {|
    crypto:TrustStore? trustStore = ();
    crypto:KeyStore? keyStore = ();
    string protocol = "TLS";
|};

# Protocols record represents SSL/TLS protocol related options to be used for HTTP client/service invocation.
#
# + name - SSL Protocol to be used. eg TLS1.2
# + versions - SSL/TLS protocols to be enabled. eg TLSv1,TLSv1.1,TLSv1.2
public type Protocols record {|
    string name = "";
    string[] versions = [];
|};

# ValidateCert record represents options related to check whether a certificate is revoked or not.
#
# + enable - The status of validateCertEnabled
# + cacheSize - Maximum size of the cache
# + cacheValidityPeriod - Time duration of cache validity period
public type ValidateCert record {|
    boolean enable = false;
    int cacheSize = 0;
    int cacheValidityPeriod = 0;
|};
