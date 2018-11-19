/*
 *  Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.ballerinalang.testerina.coverage;

import org.ballerinalang.testerina.core.entity.TestSuite;

import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * This is for converting the Ip coverage information to output format.
 *
 * @since 0.985.0
 *
 * @param <T> coverage output format. Currently only lcov is supported.
 */
public interface CoverageDataFormatter<T> {

    /**
     * Outputs formatted coverage data for the Ip coverage data for each test suite for the project.
     *
     * @param executedInstructionOrderMap Ip coverage data map for each module of the project
     * @param testSuiteForProject         test suites map for each module of the project
     * @return formatted data list for each test function
     */
    List<T> getFormattedCoverageData(Map<String, List<ExecutedInstruction>> executedInstructionOrderMap,
                                                   Map<String, TestSuite> testSuiteForProject);

    /**
     * Write formatted data to the coverage data file. coverage output file location and file name
     * can be changed with --config configuration file
     *
     * @param packageCovDataList formatted coverage data list
     * @param sourceRoot          project source root folder location
     * @throws IOException throws when output writing to the file fails.
     */
    void writeFormattedCovDataToFile(List<T> packageCovDataList, String sourceRoot) throws IOException;
}
