/*
 * Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.ballerinalang.net.http.nativeimpl.connection;

import org.ballerinalang.jvm.StringUtils;
import org.ballerinalang.jvm.values.ObjectValue;
import org.ballerinalang.jvm.values.api.BString;
import org.ballerinalang.net.http.HttpConstants;

import java.net.InetSocketAddress;

/**
 * Extern function to get the hostname from the remote address. This method may trigger DNS reverse lookup if the
 * address was created with a literal IP address.
 *
 * @since 1.1.5
 */
public class GetRemoteHostName {

    public static BString nativeGetRemoteHostName(ObjectValue caller) {
        Object remoteSocketAddress = caller.getNativeData(HttpConstants.REMOTE_SOCKET_ADDRESS);
        if (remoteSocketAddress instanceof InetSocketAddress) {
            return StringUtils.fromString(((InetSocketAddress) remoteSocketAddress).getHostName());
        }
        return null;
    }
}
