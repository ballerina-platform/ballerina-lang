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
package org.wso2.siddhi.query.api.expression.constant;

public class TimeConstant implements  Constant {

    private long value = 0;

    public TimeConstant(long i) {
        value = i;
    }


    public TimeConstant milliSec(int i) {
        value += (long) i;
        return this;
    }

    public TimeConstant sec(int i) {
        value += ((long) i) * 1000;
        return this;
    }


    public TimeConstant minute(int i) {
        value += ((long) i) * 60 * 1000;
        return this;
    }


    public TimeConstant hour(int i) {
        value += ((long) i) * 60 * 60 * 1000;
        return this;
    }


    public TimeConstant day(int i) {
        value += ((long) i) * 24 * 60 * 60 * 1000;
        return this;
    }

    public TimeConstant week(int i) {
        value += ((long) i) * 7 * 24 * 60 * 60 * 1000;
        return this;
    }

    public TimeConstant month(int i) {
        value += ((long) i) * 30* 24 * 60 * 60 * 1000;
        return this;
    }

    public TimeConstant year(int i) {
        value += ((long) i) * 365* 24 * 60 * 60 * 1000;
        return this;
    }

    public long value() {
        return value;
    }

}
