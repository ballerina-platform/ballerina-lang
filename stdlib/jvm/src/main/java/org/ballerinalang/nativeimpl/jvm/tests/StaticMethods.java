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

import org.ballerinalang.jvm.types.BTypes;
import org.ballerinalang.jvm.values.ArrayValue;
import org.ballerinalang.jvm.values.MapValue;

import java.io.IOException;
import java.util.Date;
import java.util.UUID;

/**
 * This class contains a set of utility static methods required for interoperability testing.
 *
 * @since 1.0.0
 */
public class StaticMethods {
    private StaticMethods() {
    }

    public static void acceptNothingAndReturnNothing() {
    }

    public static Date acceptNothingButReturnDate() {
        return new Date();
    }

    public static Date acceptSomethingAndReturnSomething(Date date) {
        return date;
    }

    public static String acceptTwoParamsAndReturnSomething(String s1, String s2) {
        return s1 + s2;
    }

    public static Integer acceptThreeParamsAndReturnSomething(Integer s1, Integer s2, Integer s3) {
        return s1 + s2 + s3;
    }

    // This scenario is for map value to be passed to interop and return array value.
    public static ArrayValue getArrayValueFromMap(String key, MapValue mapValue) {
        ArrayValue arrayValue = new ArrayValue(BTypes.typeInt);
        arrayValue.add(0, 1);
        long fromMap = mapValue.getIntValue(key);
        arrayValue.add(1, fromMap);
        return arrayValue;
    }

    public static String getString(UUID uuid) {
        return uuid.toString() + ": Sameera";
    }

    public static void acceptNothingReturnNothingAndThrowsCheckedException() throws JavaInteropTestCheckedException {

    }

    public static void acceptNothingReturnNothingAndThrowsMultipleCheckedException()
            throws JavaInteropTestCheckedException, IOException, ClassNotFoundException {

    }

    public static void acceptNothingReturnNothingAndThrowsUncheckedException() throws UnsupportedOperationException {

    }

    public static void acceptNothingReturnNothingAndThrowsCheckedAndUncheckedException()
            throws JavaInteropTestCheckedException, UnsupportedOperationException, ClassNotFoundException {

    }


    public static void acceptNothingReturnSomethingAndThrowsCheckedException() throws JavaInteropTestCheckedException {

    }

    public static Date acceptNothingReturnSomethingAndThrowsMultipleCheckedException()
            throws JavaInteropTestCheckedException, IOException, ClassNotFoundException {
        return new Date();
    }

    public static Date acceptNothingReturnSomethingAndThrowsUncheckedException() throws UnsupportedOperationException {
        return new Date();
    }

    public static Date acceptSomethingReturnSomethingAndThrowsCheckedAndUncheckedException(Date date)
            throws JavaInteropTestCheckedException, UnsupportedOperationException, ClassNotFoundException {
        return date;
    }

    public static Date acceptSomethingReturnSomethingAndThrowsCheckedException(Date date)
            throws JavaInteropTestCheckedException {
        return date;
    }

    public static Date acceptSomethingReturnSomethingAndThrowsMultipleCheckedException(Date date)
            throws JavaInteropTestCheckedException, IOException, ClassNotFoundException {
        return date;
    }

    public static Date acceptSomethingReturnSomethingAndThrowsUncheckedException(Date date)
            throws UnsupportedOperationException {
        return date;
    }

    public static Date acceptNothingReturnSomethingAndThrowsCheckedAndUncheckedException(Date date)
            throws JavaInteropTestCheckedException, UnsupportedOperationException, ClassNotFoundException {
        return date;
    }
}
