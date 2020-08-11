/*
 * Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.wso2.transport.http.netty.certificatevalidation;

public interface TestConstants {

    //Validity period of a fake certificate made. 1 day (in milliseconds)
    int VALIDITY_PERIOD = 24 * 60 * 60 * 1000;
    //Next update for OCSPResponse or X509CRL will be after Now + NEXT_UPDATE_PERIOD
    int NEXT_UPDATE_PERIOD = 1000000;

    /**
     * These certificates are chosen because the certificate issuers support both CRL and OCSP. Read the certificates
     * for more details.
     * <p>
     * CAUTION: Replace the certificates if they expire or are marked as revoked by their issuers.
     * At the moment they are valid. The expiry dates of the certificates are as follows:
     * <p>
     * DigiCertHighAssuranceEVCA-1   : 10/22/2028
     * DigiCertHighAssuranceEVRootCA : 11/10/2031
     */
    String INTERMEDIATE_CERT = "/simple-test-config/certificatevalidation/DigiCertHighAssuranceEVCA-1";
    String ROOT_CERT = "/simple-test-config/certificatevalidation/DigiCertHighAssuranceEVRootCA";
    String CRL_DISTRIBUTION_POINT_EXTENSION = "2.5.29.31";
}

