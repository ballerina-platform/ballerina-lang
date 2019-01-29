/*
 *  Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.ballerinalang.model.tree.clauses;

import org.ballerinalang.model.tree.Node;

/**
 * @since 0.965.0
 * <p>
 * The interface with the APIs to implement the "output rate limiting in ballerina streams/table SQLish syntax.
 * <pre> Grammar:
 *      OUTPUT outputRateType? EVERY ( integerLiteral timeScale | integerLiteral EVENTS )
 *      | OUTPUT SNAPSHOT EVERY integerLiteral timeScale
 *
 * E.g
 *      output last every 10 events
 * </pre>
 */
public interface OutputRateLimitNode extends Node {

    String getOutputRateType();

    void setOutputRateType(boolean isFirst, boolean isLast, boolean isAll);

    String getTimeScale();

    void setTimeScale(String timeScale);

    String getRateLimitValue();

    void setRateLimitValue(String rateLimitValue);

    boolean isSnapshot();

    void setSnapshot(boolean snapshot);

}
