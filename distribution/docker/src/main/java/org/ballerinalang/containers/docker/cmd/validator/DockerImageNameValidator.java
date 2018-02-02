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

import com.beust.jcommander.IParameterValidator;
import com.beust.jcommander.ParameterException;

/**
 * Validate Docker image name input.
 */
public class DockerImageNameValidator implements IParameterValidator {
    @Override
    public void validate(String name, String value) throws ParameterException {
        if (value.startsWith(".") || value.startsWith("-")) {
            throw new ParameterException("Docker image name cannot start with period or dash");
        }

        if (value.length() > 128) {
            throw new ParameterException("Docker image name cannot be longer than 128 characters.");
        }
    }
}
