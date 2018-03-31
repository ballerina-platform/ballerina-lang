/*
 *  Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.wso2.ballerinalang.compiler.semantics.model.iterable;

/**
 * Represents supported iterable operations for iterable collections.
 *
 * @since 0.961.0
 */
public enum IterableKind {

    /**
     * This applies the given function to each item of an iterable collection. If the function has any return value it
     * is ignored and foreach() therefore returns nothing.
     */
    FOREACH("foreach", true, true),

    /**
     * This applies the given function to each item of an iterable collection and returns a new iterable collection
     * of equal length with items being of tuple consisting of the return values of the function.
     */
    MAP("map", true, false),

    /**
     * This applies the given function to each item of an iterable collection and returns a the subset of items from
     * the original collection for which the function returns true.
     */
    FILTER("filter", true, false),

    /**
     * This applies the given function to each item of a table and returns a new table of less than or equal size of
     * the original table.
     */
    SELECT("select", true, false),

    /**
     * Return the number of items in the iterable collection.
     */
    COUNT("count", false, true),

    /**
     * Return the sum of all items in a iterable collection of int or float.
     */
    SUM("sum", false, true),

    /**
     * Return the average of all items in a iterable collection of int or float.
     */
    AVERAGE("average", false, true),

    /**
     * Return the minimum of all items in a iterable collection of int or float.
     */
    MIN("min", false, true),

    /**
     * Return the maximum of all items in a iterable collection of int or float.
     */
    MAX("max", false, true),

    UNDEFINED("$undefined", true, true);

    private String kind;
    private boolean lambdaRequired, terminal;

    /**
     * Represents Iterable (Operation) Kind.
     *
     * @param kind           as a String
     * @param lambdaRequired is lambda required for this operation
     * @param isTerminal     is terminal operator
     */
    IterableKind(String kind, boolean lambdaRequired, boolean isTerminal) {
        this.kind = kind;
        this.lambdaRequired = lambdaRequired;
        this.terminal = isTerminal;
    }

    public String getKind() {
        return kind;
    }

    public boolean isLambdaRequired() {
        return lambdaRequired;
    }

    public boolean isTerminal() {
        return terminal;
    }

    @Override
    public String toString() {
        return kind;
    }

    public static IterableKind getFromString(String name) {
        for (IterableKind operation : IterableKind.values()) {
            if (operation.kind.equals(name)) {
                return operation;
            }
        }
        return IterableKind.UNDEFINED;
    }
}
