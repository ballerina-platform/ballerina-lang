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

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.AbstractSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Java resources for the unit testing of different method bindings generated.
 *
 * @since 2.0.0
 */
public class MethodsTestResource extends RestrictedTestResource implements InterfaceTestResource {

    // Different instance method combinations

    private void privateMethod() {
    }

    protected void protectedMethod() {
    }

    void defaultMethod() {
    }

    public void returnVoid() {
    }

    public void returnOptionalError() throws Exception {
    }

    public IOException returnError() {
        return new IOException();
    }

    public Map returnInterface() {
        return new HashMap();
    }

    public Set<String> returnGenericObject() {
        return new HashSet<>();
    }

    public AbstractSet returnAbstractObject() {
        return new HashSet<>();
    }

    public System.Logger.Level returnEnum() {
        return System.Logger.Level.INFO;
    }

    public void errorParam(IOException x, String[] y) {
    }

    public void interfaceParam(Map x, Object[] y, Object[] z) {
    }

    public void unsupportedParam(Object[][] y) {
    }

    public Object[][] unsupportedReturnType() {
        return new Object[1][];
    }

    public Set<String> genericObjectParam(Set<String> x, int y, String[] z) {
        return x;
    }

    public void abstractObjectParam(AbstractSet x, Object y, String z) {
    }

    public System.Logger.Level enumParam(System.Logger.Level x) {
        return x;
    }

    public File returnObject() {
        return new File("test.txt");
    }

    public File returnObjectThrowable() throws FileNotFoundException {
        return new File("test.txt");
    }

    public File[] returnObjectThrowableError() throws FileNotFoundException {
        return new File[2];
    }

    public File returnObjectThrowableError(int[] x) throws FileNotFoundException {
        return new File("test.txt");
    }

    public File[] returnObjectError1() {
        return new File[2];
    }

    public File returnObjectError2(int[] x) {
        return new File("test.txt");
    }

    public byte returnByte(byte x) throws ArithmeticException {
        return x;
    }

    public char returnChar(char x, int[] y) {
        return x;
    }

    public short returnInt(short x, int y) throws ArithmeticException {
        return x;
    }

    public int returnInt(long x) {
        return 5;
    }

    public long returnInt() {
        return 6;
    }

    public float returnFloat(float x) {
        return x;
    }

    public float returnFloat(float x, String y) {
        return x;
    }

    public double returnDouble(double x) {
        return x;
    }

    public double returnDouble(double x, Object y) {
        return x;
    }

    public boolean returnBoolean(boolean x) {
        return x;
    }

    public boolean returnBoolean(boolean x, Object[] y) throws ArithmeticException {
        return x;
    }

    public String returnString(String x) {
        return x;
    }

    public byte[] returnByteArray(byte[] x) throws ArithmeticException {
        return x;
    }

    public char[] returnCharArray(char[] x, int[] y, short[] z) {
        return x;
    }

    public short[] returnShortArray(short x, int y) throws ArithmeticException {
        return new short[2];
    }

    public int[] returnIntArray(int[] x, boolean y, boolean[] z) {
        return x;
    }

    public long[] returnLongArray(long[] x, Object y) throws ArithmeticException {
        return x;
    }

    public float[] returnFloatArray(float[] x, String y) {
        return x;
    }

    public double[] returnDoubleArray(double[] x, double[] y) {
        return x;
    }

    public boolean[] returnBooleanArray() {
        return new boolean[2];
    }

    public String[] returnStringArray1(String[] x, StringBuffer y, int z) {
        return x;
    }

    public String[] returnStringArray2(String[] x, StringBuffer y, int z) throws InterruptedException {
        return x;
    }

    public byte[] returnByteMultiArray(byte[] x) throws ArithmeticException {
        return x;
    }

    public char[] returnCharMultiArray(char[] x, int[] y, Object[] z) {
        return x;
    }

    public short[] returnShortMultiArray(short x, int y, short[] z) {
        return new short[2];
    }

    public int[] returnIntMultiArray(int[] x, boolean y) throws ArithmeticException {
        return x;
    }

    public double[] returnDoubleMultiArray(double[] x, double[] y) {
        return x;
    }

    public boolean[] returnBooleanMultiArray() {
        return new boolean[2];
    }

    // Different static method combinations

    private static void privateStaticMethod() {
    }

    protected static void protectedStaticMethod() {
    }

    static void defaultStaticMethod() {
    }

    public static void returnStaticVoid() {
    }

    // A reserved word in a static method.
    public static void join() {
    }

    public static void returnStaticOptionalError() throws Exception {
    }

    public static IOException returnStaticError() {
        return new IOException();
    }

    public static Map returnStaticInterface() {
        return new HashMap();
    }

    public static Set<String> returnStaticGenericObject() {
        return new HashSet<>();
    }

    public static AbstractSet returnStaticAbstractObject() {
        return new HashSet<>();
    }

    public static System.Logger.Level returnStaticEnum() {
        return System.Logger.Level.INFO;
    }

    public static void interfaceStaticParam(Map x, boolean[] y) {
    }

    public static Set<String> genericObjectStaticParam(Set<String> x) {
        return x;
    }

    public static void abstractObjectStaticParam(AbstractSet x, int y, Object z) {
    }

    public static System.Logger.Level enumStaticParam(System.Logger.Level x, Object[] y) {
        return x;
    }

    public static File returnStaticObject() {
        return new File("test.txt");
    }

    public static File[] returnStaticObjectThrowableError1() throws FileNotFoundException {
        return new File[2];
    }

    @Override
    public int testMethod(int x) {
        return 0;
    }

    @Override
    public String[] returnStringArray() {
        return new String[0];
    }
}
