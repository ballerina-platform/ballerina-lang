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
import java.io.IOException;

/**
 * Java resources for the unit testing of different constructor bindings generated.
 *
 * @since 2.0.0
 */
@SuppressWarnings("all")
public class ConstructorsTestResource {

    // The tool should not generate constructors with private access modifier.
    private ConstructorsTestResource(boolean x) {
    }

    // The tool should not generate constructors with package access modifier.
    ConstructorsTestResource(int x, float y) {
    }

    // The tool should not generate constructors with protected access modifier.
    protected ConstructorsTestResource(String x, String y, String z) {
    }

    // Constructor with no parameters or throwables.
    public ConstructorsTestResource() {
    }

    // Constructor with a single primitive parameter.
    public ConstructorsTestResource(int x) {
    }

    // Constructor with a two primitive parameters.
    public ConstructorsTestResource(int x, short y) {
    }

    // Constructor with a string parameter.
    public ConstructorsTestResource(String x) {
    }

    // Constructor with multiple string parameters.
    public ConstructorsTestResource(String x, String y) {
    }

    // Constructor with string and a two different parameters.
    public ConstructorsTestResource(String x, byte y, char z) {
    }

    // Constructor with an object parameter.
    public ConstructorsTestResource(File x) {
    }

    // Constructor with two object parameters.
    public ConstructorsTestResource(File x, StringBuffer y) {
    }

    // Constructor with a primitive, string and object parameters.
    public ConstructorsTestResource(boolean x, String y, StringBuffer z) {
    }

    // Constructor with a primitive parameter and a checked exception.
    public ConstructorsTestResource(double x) throws IOException {
    }

    // Constructor with a string parameter, primitive parameter and a checked exception.
    public ConstructorsTestResource(String x, int y) throws IOException {
    }

    // Constructor with an object parameter, primitive parameter and a checked exception.
    public ConstructorsTestResource(File x, boolean y) throws InterruptedException {
    }

    // Constructor with a primitive parameter and an unchecked exception.
    public ConstructorsTestResource(File x, char y) throws ArithmeticException {
    }

    // Constructor with a primitive array parameter.
    public ConstructorsTestResource(int[] x) {
    }

    // Constructor with a string array parameter.
    public ConstructorsTestResource(String[] x) {
    }

    // Constructor with a object array parameter.
    public ConstructorsTestResource(Object[] x) {
    }

    // Constructor with a primitive array with a string parameter.
    public ConstructorsTestResource(int[] x, String y) {
    }

    // Constructor with two string array parameters.
    public ConstructorsTestResource(String[] x, String[] y) {
    }

    // Constructor with object array, primitive array and string array parameters.
    public ConstructorsTestResource(Object[] x, boolean y, String[] z) {
    }
}
