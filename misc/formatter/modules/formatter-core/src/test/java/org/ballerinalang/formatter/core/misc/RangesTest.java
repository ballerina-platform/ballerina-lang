/*
 * Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.ballerinalang.formatter.core.misc;

import org.ballerinalang.formatter.core.FormatterException;
import org.ballerinalang.formatter.core.RangeFormatter;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.io.IOException;
import java.nio.file.Paths;

/**
 * Test range formatting functionality with manually added fixed ranges.
 *
 * @since 1.2.10
 */
public class RangesTest extends RangeFormatter {

    @Test(dataProvider = "test-file-provider")
    @Override
    public void test(String source, int[][] positions) throws IOException, FormatterException {

        super.test(source, positions);
    }

    /**
     * Defines the data provider object for test execution.
     *
     * @return Data provider for tests
     */
    @DataProvider(name = "test-file-provider")
    @Override
    public Object[][] dataProvider() {

        return new Object[][]{
                {
                        "ranges_1.bal", new int[][] {
                            new int[] {5, 30, 9, 34},
                            new int[] {12, 31, 16, 29},
                            new int[] {16, 20, 22, 38},
                            new int[] {24, 10, 31, 36},
                            new int[] {31, 53, 31, 88},
                            new int[] {37, 26, 37, 33},
                        }
                },
                {
                        "ranges_2.bal", new int[][] {
                                new int[] {0, 1, 1, 0},
                                new int[] {2, 52, 14, 12},
                                new int[] {7, 22, 25, 8},
                                new int[] {27, 18, 27, 94},
                                new int[] {35, 12, 31, 88},
                                new int[] {32, 7, 32, 25},
                                new int[] {34, 12, 40, 7},
                        }
                },
                {
                        "ranges_3.bal", new int[][] {
                        new int[] {0, 6, 9, 16},
                        new int[] {5, 15, 27, 8},
                        new int[] {26, 37, 35, 30},
                        new int[] {45, 4, 48, 12},
                        new int[] {48, 4, 48, 44},
                        }
                },
                {
                        "ranges_4.bal", new int[][] {
                        new int[] {2, 14, 12, 25},
                        new int[] {9, 15, 23, 8},
                        new int[] {18, 8, 22, 1},
                        }
                },
                {
                        "ranges_5.bal", new int[][] {
                        new int[] {1, 1, 129, 1},
                }
                },
        };
    }

    @Override
    public String getTestResourceDir() {
        return Paths.get("misc", "ranges").toString();
    }
}
