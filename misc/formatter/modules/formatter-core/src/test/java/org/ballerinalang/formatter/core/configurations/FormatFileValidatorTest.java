/*
 *  Copyright (c) 2024, WSO2 LLC. (https://www.wso2.com).
 *
 *  WSO2 LLC. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied. See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */

package org.ballerinalang.formatter.core.configurations;

import org.ballerinalang.formatter.core.FormatterException;
import org.ballerinalang.formatter.core.FormatterUtils;
import org.testng.annotations.Test;

import java.nio.file.Path;

/**
 * Test validation of formatting configuration file.
 *
 * @since 2201.9.0
 */
public class FormatFileValidatorTest {

    private final Path resDir = Path.of("src", "test", "resources", "configurations", "validator");

    @Test(description = "Test validator on valid formatting configuration file")
    public void testValidatorOnValidFile() throws FormatterException {
        Path valid = resDir.resolve("valid");
        FormatterUtils.getFormattingConfigurations(valid, valid.resolve("Format.toml").toString());
    }

    @Test(description = "Test validtor on invalid formatting configuration files",
            expectedExceptions = FormatterException.class,
            expectedExceptionsMessageRegExp = "invalid formatting configuration file.*")
    public void testValidatorOnInvalidFile() throws FormatterException {
        Path invalid = resDir.resolve("invalid");
        FormatterUtils.getFormattingConfigurations(invalid, invalid.resolve("Format.toml").toString());
    }
}
