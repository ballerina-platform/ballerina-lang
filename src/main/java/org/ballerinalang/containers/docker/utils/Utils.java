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
 *
 */

package org.ballerinalang.containers.docker.utils;

import java.io.File;
import java.io.FileNotFoundException;
import java.net.URL;

/**
 * Utility methods.
 */
public class Utils {
    public static File getResourceFile(String resourceName) throws FileNotFoundException {
        ClassLoader classLoader = Utils.class.getClassLoader();
        URL resource = classLoader.getResource(resourceName);
        if (resource != null) {
            return new File(resource.getFile());
        } else {
            throw new FileNotFoundException("Couldn't find file in resources: " + resourceName);
        }
    }

}
