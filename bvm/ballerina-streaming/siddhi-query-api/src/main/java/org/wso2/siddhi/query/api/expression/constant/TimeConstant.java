/*
 * Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.wso2.siddhi.query.api.expression.constant;

/**
 * Time constant expression
 */
public class TimeConstant extends LongConstant {

    private static final long serialVersionUID = 1L;

    public TimeConstant(long i) {
        super(i);
    }

    public TimeConstant milliSec(long i) {
        value += (long) i;
        return this;
    }

    public TimeConstant milliSec(int i) {
        return milliSec((long) i);
    }


    public TimeConstant sec(long i) {
        value += ((long) i) * 1000;
        return this;
    }

    public TimeConstant sec(int i) {
        return sec((long) i);
    }


    public TimeConstant minute(long i) {
        value += ((long) i) * 60 * 1000;
        return this;
    }

    public TimeConstant minute(int i) {
        return minute((long) i);
    }

    public TimeConstant hour(long i) {
        value += ((long) i) * 60 * 60 * 1000;
        return this;
    }

    public TimeConstant hour(int i) {
        return hour((long) i);
    }

    public TimeConstant day(long i) {
        value += ((long) i) * 24 * 60 * 60 * 1000;
        return this;
    }

    public TimeConstant day(int i) {
        return day((long) i);
    }

    public TimeConstant week(long i) {
        value += ((long) i) * 7 * 24 * 60 * 60 * 1000;
        return this;
    }

    public TimeConstant week(int i) {
        return week((long) i);
    }

    public TimeConstant month(long i) {
        value += ((long) i) * 2630000000L; //30.43.. days in a mount
        return this;
    }

    public TimeConstant month(int i) {
        return month((long) i);
    }

    public TimeConstant year(long i) {
        value += ((long) i) * 31556900000L; //365.24.. days in a year
        return this;
    }

    public TimeConstant year(int i) {
        return year((long) i);
    }

    public long value() {
        return value;
    }

}
