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

# This type represents topic partition position in which consumed record is stored.
#
# + partition - TopicPartition which record is related.
# + offset - Offset in which record is stored in partition.
public type PartitionOffset record {|
    TopicPartition partition;
    int offset;
|};

# This type represents a topic partition.
#
# + topic - Topic which partition is related.
# + partition - Index for the partition.
public type TopicPartition record {|
    string topic;
    int partition;
|};

# Provides configurations for facilitating secure communication with the Kafka server.
#
# + keyStore - Configurations associated with KeyStore.
# + trustStore - Configurations associated with TrustStore.
# + protocol - Configurations related to SSL/TLS protocol and version to be used.
# + sslProvider - The name of the security provider used for SSL connections. Default value is the default security
#               provider of the JVM.
# + sslKeyPassword - The password of the private key in the key store file. This is optional for client.
# + sslCipherSuites - A list of cipher suites. This is a named combination of authentication, encryption, MAC and key
#               exchange algorithm used to negotiate the security settings for a network connection using TLS or SSL
#               network protocol. By default all the available cipher suites are supported.
# + sslEndpointIdentificationAlgorithm - The endpoint identification algorithm to validate server hostname using server
#               certificate.
# + sslSecureRandomImplementation - The SecureRandom PRNG implementation to use for SSL cryptography operations.
public type SecureSocket record {|
    KeyStore keyStore; // KEY_STORE_CONFIG
    TrustStore trustStore; // TRUST_STORE_CONFIG
    Protocols protocol; // PROTOCOL_CONFIG
    string sslProvider?; // SSL_PROVIDER_CONFIG 1
    string sslKeyPassword?; // SSL_KEY_PASSWORD_CONFIG 2
    string sslCipherSuites?; // SSL_CIPHER_SUITES_CONFIG 3
    string sslEndpointIdentificationAlgorithm?; // SSL_ENDPOINT_IDENTIFICATION_ALGORITHM_CONFIG 4
    string sslSecureRandomImplementation?; // SSL_SECURE_RANDOM_IMPLEMENTATION_CONFIG 5
|};

# Record for providing key-store related configurations.
#
# + keyStoreType - The file format of the key store file. This is optional for client.
# + location - The location of the key store file. This is optional for client and can be used for two-way
#               authentication for client.
# + password - The store password for the key store file. This is optional for client and only needed if
#               ssl.keystore.location is configured.
# + keyManagerAlgorithm - The algorithm used by key manager factory for SSL connections. Default value is the key
#               manager factory algorithm configured for the Java Virtual Machine.
public type KeyStore record {|
    string keyStoreType?; // SSL_KEYSTORE_TYPE_CONFIG 1
    string location = ""; // SSL_KEYSTORE_LOCATION_CONFIG 2
    string password = ""; // SSL_KEYSTORE_PASSWORD_CONFIG 3
    string keyManagerAlgorithm?; // SSL_KEYMANAGER_ALGORITHM_CONFIG 4
|};

# Record for providing trust-store related configurations.
#
# + trustStoreType - The file format of the trust store file.
# + location - The location of the trust store file.
# + password - The password for the trust store file. If a password is not set access to the trust-store is still
#               available, but integrity checking is disabled.
# + trustManagerAlgorithm - The algorithm used by trust manager factory for SSL connections. Default value is the trust
#               manager factory algorithm configured for the Java Virtual Machine.
public type TrustStore record {|
    string trustStoreType?; // SSL_TRUSTSTORE_TYPE_CONFIG 1
    string location = ""; // SSL_TRUSTSTORE_LOCATION_CONFIG 2
    string password = ""; // SSL_TRUSTSTORE_PASSWORD_CONFIG 3
    string trustManagerAlgorithm?; // SSL_TRUSTMANAGER_ALGORITHM_CONFIG 4
|};

# A record for configuring SSL/TLS protocol and version to be used.
#
# + securityProtocol - Protocol used to communicate with brokers.
# + sslProtocol - The SSL protocol used to generate the SSLContext. Default setting is TLS, which is fine for most
#               cases. Allowed values in recent JVMs are TLS, TLSv1.1 and TLSv1.2. SSL, SSLv2 and SSLv3 may be supported
#               in older JVMs, but their usage is discouraged due to known security vulnerabilities.
# + sslProtocolVersions - The list of protocols enabled for SSL connections.
public type Protocols record {|
    string securityProtocol = ""; // SECURITY_PROTOCOL_CONFIG 1
    string sslProtocol = ""; // SSL_PROTOCOL_CONFIG 2
    string sslProtocolVersions = ""; // SSL_ENABLED_PROTOCOLS_CONFIG 3
|};

const STRING = "string";
const INT = "int";
const FLOAT = "float";
const BYTE_ARRAY = "byte[]";
const ANY = "any";
