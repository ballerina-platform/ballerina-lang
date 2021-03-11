/*
 *  Copyright (c) 2021, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.ballerinalang.bindgen;

/**
 * Java resources for the unit testing of different constructor bindings generated.
 *
 * @since 2.0.0
 */
public class ConstructorsTestResource {

    public int getInstanceInt = 3;

    // Different constructor combinations

    private ConstructorsTestResource(String obj) {
        getInstanceInt = 5;
    }

    ConstructorsTestResource(String obj1, String obj2) {
        getInstanceInt = 5;
    }

    protected ConstructorsTestResource(String obj1, String obj2, String obj3) {
        getInstanceInt = 5;
    }

    public ConstructorsTestResource() {
        getInstanceInt = 5;
    }

    public ConstructorsTestResource(int x) throws ArithmeticException {
        getInstanceInt = 5 / x;
    }

    public ConstructorsTestResource(int[] x, int[] y) throws ArithmeticException, ArrayIndexOutOfBoundsException {
        getInstanceInt = 5 / x[0];
    }

    public ConstructorsTestResource(boolean[] x, boolean[] y) {
    }

//    public ConstructorsTestResource(byte x, Object obj, byte[] y, byte[][] z) {
//        getInstanceByte = x;
//    }
//
//    public ConstructorsTestResource(char x, StringBuffer[] obj, char[] y, char[][] z) {
//        getInstanceChar = x;
//    }
//
//    public ConstructorsTestResource(short x, Integer[][] obj, Byte[][][] in, short[] y) {
//        getInstanceShort = x;
//    }
//
//    public ConstructorsTestResource(long x, Set<String> obj, short[][] y, long[] z, long[][] a) {
//        getInstanceLong = x;
//    }
//
//    public ConstructorsTestResource(float x, System.Logger.Level obj, float[] y, float[][] z) {
//        getInstanceFloat = x;
//    }
//
//    public ConstructorsTestResource(double x, ArithmeticException obj, double[] y, double[][] z) {
//        getInstanceDouble = x;
//    }

//    public ConstructorsTestResource(boolean x, AbstractList obj, boolean[] y, boolean[][] z) {
//        getInstanceBoolean = x;
//    }
//
//    public ConstructorsTestResource(String x, Map obj, String[] y, String[][] z) {
//        getInstanceString = x;
//    }
//
//    public ConstructorsTestResource(float x, double y, boolean z, String[] a, short[][] b, Object[][][] c)
//            throws ArithmeticException, ArrayIndexOutOfBoundsException {
//        getInstanceFloat = x;
//    }
}
