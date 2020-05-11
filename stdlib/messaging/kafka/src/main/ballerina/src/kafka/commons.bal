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

# Represents the topic partition position in which the consumed record is stored.
#
# + partition - The `kafka:TopicPartition` to which the record is related
# + offset - Offset in which the record is stored in the partition
public type PartitionOffset record {|
    TopicPartition partition;
    int offset;
|};

# Represents a topic partition.
#
# + topic - Topic to which the partition is related
# + partition - Index for the partition
public type TopicPartition record {|
    string topic;
    int partition;
|};

# Configurations for facilitating secure communication with the Kafka server.
#
# + keyStore - Configurations associated with the KeyStore
# + trustStore - Configurations associated with the TrustStore
# + protocol - Configurations related to the SSL/TLS protocol and the version to be used
# + sslProvider - The name of the security provider used for SSL connections. Default value is the default security
#                 provider of the JVM
# + sslKeyPassword - The password of the private key in the key store file. This is optional for the client
# + sslCipherSuites - A list of Cipher suites. This is a named combination of the authentication, encryption, MAC, and key
#                     exchange algorithms used to negotiate the security settings for a network connection using the TLS
#                     or SSL network protocols. By default, all the available Cipher suites are supported
# + sslEndpointIdentificationAlgorithm - The endpoint identification algorithm to validate the server hostname using
#                                        the server certificate
# + sslSecureRandomImplementation - The `SecureRandom` PRNG implementation to use for the SSL cryptography operations
public type SecureSocket record {|
    KeyStore keyStore;
    TrustStore trustStore;
    Protocols protocol;
    string sslProvider?;
    string sslKeyPassword?;
    string sslCipherSuites?;
    string sslEndpointIdentificationAlgorithm?;
    string sslSecureRandomImplementation?;
|};

# Configurations related to the KeyStore.
#
# + keyStoreType - The file format of the KeyStore file. This is optional for the client
# + location - The location of the KeyStore file. This is optional for the client and can be used for two-way
#              authentication for the client
# + password - The store password for the KeyStore file. This is optional for the client and is only needed if
#              the `ssl.keystore.location` is configured
# + keyManagerAlgorithm - The algorithm used by the key manager factory for SSL connections. The default value is the
#                         key manager factory algorithm configured for the JVM
public type KeyStore record {|
    string keyStoreType?;
    string location;
    string password;
    string keyManagerAlgorithm?;
|};

# Configurations related to the TrustStore.
#
# + trustStoreType - The file format of the TrustStore file
# + location - The location of the TrustStore file
# + password - The password for the TrustStore file. If a password is not set, access to the TrustStore is still
#              available but integrity checking is disabled
# + trustManagerAlgorithm - The algorithm used by the trust manager factory for SSL connections. The default value is
#                           the trust manager factory algorithm configured for the JVM
public type TrustStore record {|
    string trustStoreType?;
    string location;
    string password;
    string trustManagerAlgorithm?;
|};

# Configurations related to the SSL/TLS protocol and the versions to be used.
#
# + securityProtocol - The protocol used to communicate with brokers.
# + sslProtocol - The SSL protocol used to generate the SSLContext. The default setting is TLS, which is fine for most
#                 cases. Allowed values in recent JVMs are TLS, TLSv1.1, and TLSv1.2. Also, SSL, SSLv2 and SSLv3 may be
#                 supported in older JVMs but their usage is discouraged due to known security vulnerabilities
# + sslProtocolVersions - The list of protocols enabled for SSL connections
public type Protocols record {|
    string securityProtocol;
    string sslProtocol;
    string sslProtocolVersions;
|};

# Configurations related to Kafka authentication mechanisms.
#
# + mechanism - Type of the authentication mechanism. Currently, SASL_PLAIN and SCRAM are supported. See
#                             `kafka:AuthennticationType` for more information
# + securityProtocol - Type of the security protocol to use in the broker connection
# + username - The username to use to authenticate the Kafka producer/consumer
# + password - The password to use to authenticate the Kafka producer/consumer
public type AuthenticationConfiguration record {|
    AuthenticationMechanism mechanism = AUTH_SASL_PLAIN;
    string securityProtocol = PROTOCOL_SASL_PLAINTEXT;
    string username;
    string password;
|};

# Represents the supported Kafka SASL authentication mechanisms.
public type AuthenticationMechanism AUTH_SASL_PLAIN;

# Represents the supported security protocols for Kafka clients.
public type SecurityProtocol PROTOCOL_SASL_PLAINTEXT|PROTOCOL_SASL_SSL;
