/*
 *  Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.ballerinalang.nativeimpl.jvm.servicetests;

import org.ballerinalang.jvm.api.BRuntime;
import org.ballerinalang.jvm.api.connector.CallableUnitCallback;
import org.ballerinalang.jvm.api.values.BError;
import org.ballerinalang.jvm.api.values.BString;
import org.ballerinalang.jvm.types.BTypes;
import org.ballerinalang.jvm.values.FutureValue;
import org.ballerinalang.jvm.values.MapValueImpl;
import org.ballerinalang.jvm.values.ObjectValue;

/**
 * Helper methods to test properties of service values.
 *
 * @since 2.0
 */
public class ServiceValue {
    public static FutureValue callMethod(ObjectValue l, BString name) {
        CallableUnitCallback c = new CallableUnitCallback() {
            @Override
            public void notifySuccess() { }

            @Override
            public void notifyFailure(BError error) { }
        };

        BRuntime currentRuntime = BRuntime.getCurrentRuntime();
        FutureValue k = currentRuntime.invokeMethodAsync(l, name.getValue(), null, null, null, new MapValueImpl<>(),
                BTypes.typeAny);

        return k;
    }
}
