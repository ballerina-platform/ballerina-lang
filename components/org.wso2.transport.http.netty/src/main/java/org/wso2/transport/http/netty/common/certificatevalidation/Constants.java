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

package org.wso2.transport.http.netty.common.certificatevalidation;

/**
 * Caching constants.
 */
public interface Constants {

    int CACHE_MAX_ALLOCATED_SIZE = 10000;
    int CACHE_MIN_ALLOCATED_SIZE = 50;
    int CACHE_DEFAULT_ALLOCATED_SIZE = 50;
    int CACHE_MAX_DELAY_MINS = 60 * 24;
    int CACHE_MIN_DELAY_MINS = 1;
    int CACHE_DEFAULT_DELAY_MINS = 15;
    String BOUNCY_CASTLE_PROVIDER = "BC";
    String X_509 = "X.509";
    String ALGORITHM = "PKIX";
}

