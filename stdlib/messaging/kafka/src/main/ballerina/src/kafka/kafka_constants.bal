// Copyright (c) 2020 WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

// Common constants

// Data types for serializer / deserializer functionality
const STRING = "string";
const INT = "int";
const FLOAT = "float";
const BYTE_ARRAY = "byte[]";
const ANY = "any";

// Consumer related constants

// Deserializer types
# In-built Kafka byte array deserializer.
public const DES_BYTE_ARRAY = "BYTE_ARRAY";

# In-built Kafka string deserializer.
public const DES_STRING = "STRING";

# In-built Kafka int deserializer.
public const DES_INT = "INT";

# In-built Kafka float deserializer.
public const DES_FLOAT = "FLOAT";

# User-defined deserializer.
public const DES_CUSTOM = "CUSTOM";

# Apache Avro deserializer.
public const DES_AVRO = "AVRO";

// Isolation levels
# Consumer isolation level value 'read_committed'
public const ISOLATION_COMMITTED = "read_committed";

# Consumer isolation level value 'read_uncommitted'
public const ISOLATION_UNCOMMITTED = "read_uncommitted";

// Producer related constants
// Produce Ack types
# Producer acknowledgement type 'all'. This will gurantee that the record will not be lost as long as at least one
# in-sync replica is alive.
public const ACKS_ALL = "all";

# Producer acknowledgement type '0'. If the acknowledgement type set to this, the producer will not wait for any
# acknowledgement from the server.
public const ACKS_NONE = "0";

# Producer acknowledgement type '1'. If the acknowledgement type set to this, the leader will write the record to its
# local log but will respond without awaiting full acknowledgement from all followers.
public const ACKS_SINGLE = "1";

// Serializer types
# In-built Kafka Byte Array serializer.
public const SER_BYTE_ARRAY = "BYTE_ARRAY";

# In-built Kafka string serializer.
public const SER_STRING = "STRING";

# In-built Kafka int serializer.
public const SER_INT = "INT";

# In-built Kafka float serializer.
public const SER_FLOAT = "FLOAT";

# User-defined serializer.
public const SER_CUSTOM = "CUSTOM";

# Apache avro serializer.
public const SER_AVRO = "AVRO";

// Compression types
# Kafka compression type. Value 'none'
public const COMPRESSION_NONE = "none";

# Kafka compression type. Value 'gzip'
public const COMPRESSION_GZIP = "gzip";

# Kafka compression type. Value 'snappy'
public const COMPRESSION_SNAPPY = "snappy";

# Kafka compression type. Value 'lz4'
public const COMPRESSION_LZ4 = "lz4";

# Kafka compression type. Value 'zstd'
public const COMPRESSION_ZSTD = "zstd";
