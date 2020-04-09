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

# Configuration related to create a NATS streaming subscription.
#
# + connectionName - Name of the connection(This is optional)
# + maxReconnect - Maximum number of reconnect attempts. The reconnect state is entered when the connection is connected
#                  and when that connection is lost. During the initial connection attempt, the client will cycle
#                  over its server list one time regardless of the maxReconnects value that is set.
#                  Use 0 to turn off auto reconnect
#                  Use -1 to turn on infinite reconnects
# + reconnectWaitInSeconds - The time(in seconds) to wait between reconnect attempts to reconnect to the same server
# + connectionTimeoutInSeconds - The timeout(in seconds) for connection attempts
# + pingIntervalInMinutes - The interval(in minutes) between the attempts of pinging the server
# + maxPingsOut - The maximum number of pings the client can have in flight. The default value sets to two
# + username - The username for basic authentication
# + password - The password for basic authentication
# + token - The token for token-based authentication
# + inboxPrefix - The connection's inbox prefix which all inboxes will be start with
# + noEcho - Turns off echo. This prevents the server from echoing messages back to the connection if it
#            has subscriptions on the subject being published to
# + enableErrorListener - Enables Connection Error listener
# + secureSocket - Configurations related to SSL/TLS
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

# Configuration related to facilitate secure communication with a remote HTTP endpoint.
#
# + trustStore - Configuration associated with the TrustStore
# + keyStore - Configuration associated with the KeyStore
# + protocol - The standard name of the requested protocol
public type SecureSocket record {|
    crypto:TrustStore? trustStore = ();
    crypto:KeyStore? keyStore = ();
    string protocol = "TLS";
|};
