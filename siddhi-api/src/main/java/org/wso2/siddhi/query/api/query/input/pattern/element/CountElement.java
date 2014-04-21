/*
*  Copyright (c) 2005-2010, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
*  WSO2 Inc. licenses this file to you under the Apache License,
*  Version 2.0 (the "License"); you may not use this file except
*  in compliance with the License.
*  You may obtain a copy of the License at
*
*    http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing,
* software distributed under the License is distributed on an
* "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
* KIND, either express or implied.  See the License for the
* specific language governing permissions and limitations
* under the License.
*/
package org.wso2.siddhi.query.api.query.input.pattern.element;

import org.wso2.siddhi.query.api.query.input.TransformedStream;

public class CountElement implements PatternElement {

    private TransformedStream transformedStream;
    private int minCount = -1;
    private int maxCount = -1;

    public CountElement(TransformedStream transformedStream, int minCount, int maxCount) {
        this.transformedStream = transformedStream;
        transformedStream.setCounterStream(true);
        this.minCount = minCount;
        this.maxCount = maxCount;
    }

    public int getMinCount() {
        return minCount;
    }

    public int getMaxCount() {
        return maxCount;
    }

    public TransformedStream getTransformedStream() {
        return transformedStream;
    }
}
