/*
 * Copyright (c) 2017, WSO2 Inc. (http://wso2.com) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.ballerinalang.containers.docker.cmd.validator;

import com.beust.jcommander.ParameterException;
import org.testng.annotations.Test;

/**
 * Test DockerImageNameValidator.
 */
public class DockerImageNameValidatorTest {
    @Test(expectedExceptions = {ParameterException.class})
    public void testDockerImageNamePeriodStarter() {
        new DockerImageNameValidator().validate("--tag", ".invalidImageName");
    }

    @Test(expectedExceptions = {ParameterException.class})
    public void testDockerImageNameDashStarter() {
        new DockerImageNameValidator().validate("--tag", "-invalidImageName");
    }

    @Test(expectedExceptions = {ParameterException.class})
    public void testDockerImageNameTooLong() {
        new DockerImageNameValidator().validate("--tag",
                "123456789012345678901234567890123456789012345678901234567890123456" +
                        "7890123456789012345678901234567890123456789012345678901234567890");
    }

    @Test
    public void testDockerImageNameValid() {
        new DockerImageNameValidator().validate("--tag", "validImageName");
    }

    @Test
    public void testDockerImageNameValidWithVersion() {
        new DockerImageNameValidator().validate("--tag", "validImageName:withVersion");
    }
}
