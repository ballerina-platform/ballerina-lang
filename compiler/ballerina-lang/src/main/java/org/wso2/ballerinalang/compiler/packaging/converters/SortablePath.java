/*
 * Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.wso2.ballerinalang.compiler.packaging.converters;

import java.math.BigInteger;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Sorts paths by the latest version.
 */
public class SortablePath implements Comparable<SortablePath> {
    private static final BigInteger MINUS_ONE = BigInteger.valueOf(-1);
    private static final Pattern semVerPattern = Pattern.compile("(\\d+)\\.(\\d+)\\.(\\d+)");

    private final Path path;
    private final BigInteger bigInt;

    public SortablePath(Path path) {
        this.path = path;
        this.bigInt = pathToSemVer(path);
    }

    private BigInteger pathToSemVer(Path path) {
        Path dirName = path.getFileName();
        String version = "";
        if (dirName != null) {
            version = dirName.toString();
        }
        Matcher matcher = semVerPattern.matcher(version);
        if (matcher.matches()) {
            return semVerToBigInt(version);
        } else {
            return MINUS_ONE;
        }
    }

    private BigInteger semVerToBigInt(String version) {
        int[] versionArray = Arrays.stream(version.split("\\.")).mapToInt(Integer::parseInt).toArray();
        BigInteger sum = BigInteger.valueOf(versionArray[0]);
        sum = sum.shiftLeft(Integer.SIZE);
        sum = sum.add(BigInteger.valueOf(versionArray[1]));
        sum = sum.shiftLeft(Integer.SIZE);
        sum = sum.add(BigInteger.valueOf(versionArray[2]));
        return sum;
    }

    public Path getPath() {
        return path;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        SortablePath that = (SortablePath) o;

        return bigInt.equals(that.bigInt);
    }

    @Override
    public int hashCode() {
        return bigInt.hashCode();
    }


    @Override
    public int compareTo(SortablePath sortablePath) {
        if (sortablePath == null) {
            return Integer.MAX_VALUE;
        }
        return bigInt.compareTo(sortablePath.bigInt);
    }

    public boolean valid() {
        return !bigInt.equals(MINUS_ONE);
    }
}
