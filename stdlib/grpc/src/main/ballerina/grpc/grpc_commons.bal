// Copyright (c) 2018 WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

documentation {
    TrustStore record represents trust store related options to be used for HTTP client/service invocation.

    F{{path}} File path to trust store file
    F{{password}} Trust store password
}
public type TrustStore record {
    string path,
    string password,
};

documentation {
    KeyStore record represents key store related options to be used for HTTP client/service invocation.

    F{{path}} File path to key store file
    F{{password}} Key store password
}
public type KeyStore record {
    string path,
    string password,
};

documentation {
    Protocols record represents SSL/TLS protocol related options to be used for HTTP client/service invocation.

    F{{name}} SSL Protocol to be used. eg TLS1.2
    F{{versions}} SSL/TLS protocols to be enabled. eg TLSv1,TLSv1.1,TLSv1.2
}
public type Protocols record {
    string name,
    string[] versions,
};

documentation {
    ValidateCert record represents options related to check whether a certificate is revoked or not.

    F{{enable}} The status of validateCertEnabled
    F{{cacheSize}} Maximum size of the cache
    F{{cacheValidityPeriod}} Time duration of cache validity period
}
public type ValidateCert record {
    boolean enable,
    int cacheSize,
    int cacheValidityPeriod,
};

documentation {
    OcspStapling record represents options related to check whether a certificate is revoked or not.

    F{{enable}} The status of OcspStapling
    F{{cacheSize}} Maximum size of the cache
    F{{cacheValidityPeriod}} Time duration of cache validity period
}
public type ServiceOcspStapling record {
    boolean enable,
    int cacheSize,
    int cacheValidityPeriod,
};
