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

package org.wso2.transport.http.netty.contractimpl.common.certificatevalidation;

/**
 * Caching constants.
 */
public final class Constants {

    public static final int CACHE_MAX_ALLOCATED_SIZE = 10000;
    public static final int CACHE_MIN_ALLOCATED_SIZE = 50;
    public static final int CACHE_DEFAULT_ALLOCATED_SIZE = 50;
    public static final int CACHE_MAX_DELAY_MINS = 60 * 24;
    public static final int CACHE_MIN_DELAY_MINS = 1;
    public static final int CACHE_DEFAULT_DELAY_MINS = 15;
    public static final String BOUNCY_CASTLE_PROVIDER = "BC";
    public static final String X_509 = "X.509";
    public static final String ALGORITHM = "PKIX";

    private Constants() {
    }
}

