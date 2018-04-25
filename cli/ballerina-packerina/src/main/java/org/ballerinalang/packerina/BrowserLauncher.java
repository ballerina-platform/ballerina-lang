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
package org.ballerinalang.packerina;

import java.io.IOException;

/**
 * Starting the default browser once the composer starts.
 */
public class BrowserLauncher {

    public static void startInDefaultBrowser(String url) throws IOException {

        Runtime rt = Runtime.getRuntime();
        if (OsUtils.isWindows()) {
            rt.exec("rundll32 url.dll,FileProtocolHandler " + url);

        } else if (OsUtils.isMac()) {
            rt.exec("open " + url);
        } else if (OsUtils.isUnix()) {
            rt.exec("xdg-open " + url);

        } else {
            return;
        }
    }

}
