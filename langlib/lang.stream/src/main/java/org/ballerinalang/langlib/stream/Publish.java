/*
*  Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.ballerinalang.langlib.stream;

import org.ballerinalang.jvm.scheduling.Strand;
import org.ballerinalang.jvm.values.StreamValue;
import org.ballerinalang.model.types.TypeKind;
import org.ballerinalang.natives.annotations.Argument;
import org.ballerinalang.natives.annotations.BallerinaFunction;

/**
 * {@code Publish} is the function to publish data to a stream.
 *
 * @since 0.964.0
 */
@BallerinaFunction(
        orgName = "ballerina", packageName = "lang.stream",
        functionName = "publish",
        args = {
                @Argument(name = "strm", type = TypeKind.STREAM),
                @Argument(name = "data", type = TypeKind.ANY)
        },
        isPublic = true)
public class Publish {

    public static void publish(Strand strand, StreamValue strm, Object data) {
        strm.publish(strand, data);
    }
}
