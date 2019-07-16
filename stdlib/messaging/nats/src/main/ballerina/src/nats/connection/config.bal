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

# NATS core connection configuration
#
# + connectionName - Name of the connection. This is optional.
# + maxReconnect - Maximum number of reconnect attempts. Use 0 to turn off auto reconnect.
# Use -1 to turn on infinite reconnects. The reconnect state is entered when the connection is connected and when
# that connection is lost. During the initial connection attempt, the client will cycle over
# its server list one time regardless of the maxReconnects value that is set.
# + reconnectWaitInSeconds - The time to wait between reconnect attempts to reconnect to the same server.
# This is measured in seconds.
# + connectionTimeoutInSeconds - The timeout for connection attempts measured in seconds.
# + pingIntervalInMinutes - The interval between the attempts of pinging the server. This is measured in minutes.
# + maxPingsOut - The maximum number of pings the client can have in flight. The default value will be a small number.
# + username - The username for basic authentication.
# + password - The password for basic authentication.
# + token - The token for token-based authentication.
# + inboxPrefix - The connection's inbox prefix. All inboxes will start with this string.
# + noEcho - Turn off echo. This prevents the server from echoing messages back to the connection if it
# has subscriptions on the subject being published to.
# + enableErrorListener - Enable Connection Error listener.
# + secureSocket - SSL/TLS-related options
public type ConnectionConfig record {|
  string connectionName = "ballerina-nats";
  int maxReconnect = 60;
  int reconnectWaitInSeconds = 2;
  int connectionTimeoutInSeconds = 2;
  int pingIntervalInMinutes = 2;
  int maxPingsOut = 2;
  string username?;
  string password?;
  string token?;
  string inboxPrefix = "_INBOX.";
  boolean noEcho = false;
  boolean enableErrorListener = false;
  SecureSocket? secureSocket = ();
|};

# Provides configurations for facilitating secure communication with a remote HTTP endpoint.
#
# + trustStore - Configurations associated with the TrustStore.
# + keyStore - Configurations associated with the KeyStore.
# + protocol - The standard name of the requested protocol.
public type SecureSocket record {|
    crypto:TrustStore? trustStore = ();
    crypto:KeyStore? keyStore = ();
    string protocol = "TLS";
|};
