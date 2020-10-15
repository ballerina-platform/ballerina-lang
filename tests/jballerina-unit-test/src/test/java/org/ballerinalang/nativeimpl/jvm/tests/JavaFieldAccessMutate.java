/*
 *  Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */
package org.ballerinalang.nativeimpl.jvm.tests;

import io.ballerina.runtime.api.StringUtils;
import io.ballerina.runtime.api.values.BString;

import java.util.Date;
import java.util.UUID;

/**
 * This class is used for Java interoperability tests.
 *
 * @since 1.0.0
 */
public class JavaFieldAccessMutate {
    // Static fields
    public static BString contractId = StringUtils.fromString("Ballerina");
    public static int age = 10;
    public static short aShort = 11;

    // Instance fields
    public Date createdAt = new Date();
    public boolean isEmpty = false;
    public float lkr = 0.0f;
    public UUID uuid = UUID.fromString("951e8356-d08f-429a-b49d-77df6cee37e2");

    public JavaFieldAccessMutate(Date createdAt) {
        this.createdAt = createdAt;
    }

    public JavaFieldAccessMutate() {
    }
}
