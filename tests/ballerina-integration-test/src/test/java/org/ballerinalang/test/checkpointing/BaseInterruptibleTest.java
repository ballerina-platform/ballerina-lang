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
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */

package org.ballerinalang.test.checkpointing;

import org.ballerinalang.config.ConfigRegistry;
import org.ballerinalang.persistence.store.impl.FileStorageProvider;
import org.ballerinalang.test.BaseTest;
import org.ballerinalang.test.context.Constant;
import org.ballerinalang.test.context.Utils;

import java.io.File;
import java.util.Locale;

/**
 * Class provides common functions for interruptible test cases.
 *
 * @since 0.981.2
 */
public class BaseInterruptibleTest extends BaseTest {

    protected int servicePort = Constant.DEFAULT_HTTP_PORT;

    protected int[] requiredPorts = new int[] { servicePort };

    protected String[] args;

    FileStorageProvider fileStorageProvider;

    private File statesStorageDir = null;

    public void setup(String statesStorageDirName) {
        statesStorageDir = new File("target" + File.separator + statesStorageDirName);
        if (statesStorageDir.exists()) {
            statesStorageDir.delete();
        }
        String statesStoragePath = statesStorageDir.getAbsolutePath();
        String osName = Utils.getOSName();
        if (osName != null && osName.toLowerCase(Locale.ENGLISH).contains("windows")) {
            statesStoragePath = statesStoragePath.replace("\\", "\\\\");
        }
        args = new String[] { "-e", FileStorageProvider.INTERRUPTIBLE_STATES_FILE_PATH + "=" + statesStoragePath };
        ConfigRegistry.getInstance().addConfiguration(FileStorageProvider.INTERRUPTIBLE_STATES_FILE_PATH,
                                                      statesStoragePath);
        fileStorageProvider = new FileStorageProvider();
    }

    public void cleanup() {
        if (statesStorageDir != null && statesStorageDir.exists()) {
            statesStorageDir.delete();
        }
    }
}
