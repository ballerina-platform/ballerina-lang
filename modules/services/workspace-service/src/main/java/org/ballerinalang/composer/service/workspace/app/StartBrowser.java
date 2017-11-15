/*
 * Copyright (c) 2016, WSO2 Inc. (http://wso2.com) All Rights Reserved.
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
package org.ballerinalang.composer.service.workspace.app;
public class StartBrowser {

    public static void startInDefaultBrowser(String url) {
        String os = System.getProperty("os.name").toLowerCase();
        Runtime rt = Runtime.getRuntime();

        try {

            if (os.indexOf("win") >= 0) {
                rt.exec("rundll32 url.dll,FileProtocolHandler " + url);

            } else if (os.indexOf("mac") >= 0) {
                rt.exec("open " + url);

            } else if (os.indexOf("nix") >= 0 || os.indexOf("nux") >= 0) {
                rt.exec("xdg-open " + url);

            } else {
                return;
            }
        } catch (Exception e) {
            return;
        }
    }

}
