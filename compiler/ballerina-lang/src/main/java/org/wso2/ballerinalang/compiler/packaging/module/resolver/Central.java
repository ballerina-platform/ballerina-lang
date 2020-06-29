/*
 *  Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.ballerinalang.compiler.packaging.module.resolver;

import org.ballerinalang.cli.module.Pull;
import org.ballerinalang.cli.module.util.Utils;
import org.ballerinalang.jvm.JSONParser;
import org.ballerinalang.jvm.StringUtils;
import org.ballerinalang.jvm.values.ArrayValue;
import org.ballerinalang.jvm.values.MapValue;
import org.ballerinalang.jvm.values.MapValueImpl;
import org.ballerinalang.model.elements.PackageID;
import org.wso2.ballerinalang.compiler.packaging.module.resolver.model.ModuleResolveException;
import org.wso2.ballerinalang.compiler.util.ProjectDirConstants;
import org.wso2.ballerinalang.util.RepoUtils;
import org.wso2.ballerinalang.util.TomlParserUtils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.nio.charset.Charset;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import static org.ballerinalang.cli.module.util.Utils.convertToUrl;
import static org.ballerinalang.cli.module.util.Utils.createHttpUrlConnection;
import static org.ballerinalang.cli.module.util.Utils.initializeSsl;
import static org.ballerinalang.cli.module.util.Utils.setRequestMethod;
import static org.wso2.ballerinalang.compiler.packaging.module.resolver.Util.isValidVersion;
import static org.wso2.ballerinalang.programfile.ProgramFileConstants.IMPLEMENTATION_VERSION;
import static org.wso2.ballerinalang.util.RepoUtils.getRemoteRepoURL;

/**
 * Repo for Central.
 */
public class Central implements Repo {

    @Override
    public List<String> resolveVersions(PackageID moduleId, String filter) throws ModuleResolveException {
        try {
            return getCentralVersions(moduleId.getOrgName().getValue(), moduleId.getName().getValue(), filter);
        } catch (IOException e) {
            throw new ModuleResolveException(
                    "retrieving versions from central failed for " + moduleId.getOrgName() + "/" + moduleId.getName(),
                    e);
        }
    }

    @Override
    public boolean isModuleExists(PackageID moduleId) {
        return false;
    }

    public void pullModule(PackageID moduleId) {
        String remoteUri =
                getRemoteRepoURL() + "/modules/" + moduleId.getOrgName() + "/" + moduleId.getName().getValue() + "/*/";
        Path modulePathInBaloCache = RepoUtils.createAndGetHomeReposPath()
                .resolve(ProjectDirConstants.BALO_CACHE_DIR_NAME).resolve(moduleId.getOrgName().getValue())
                .resolve(moduleId.getName().getValue());
        String moduleNameWithOrg = moduleId.getOrgName() + "/" + moduleId.getName().getValue();

        Pull.execute(remoteUri, String.valueOf(modulePathInBaloCache), moduleNameWithOrg, "", 0, "", "", "", false,
                true, IMPLEMENTATION_VERSION, "java8", RepoUtils.getBallerinaVersion());
    }

    public static List<String> getCentralVersions(String orgName, String moduleName, String filter) throws IOException {
        List<String> versions = new ArrayList<>();
        initializeSsl();
        String url = getRemoteRepoURL() + "/modules/info/" + orgName + "/" + moduleName + "/*/";
        org.ballerinalang.toml.model.Proxy proxy = TomlParserUtils.readSettings().getProxy();
        HttpURLConnection conn = createHttpUrlConnection(convertToUrl(url), proxy.getHost(), proxy.getPort(),
                proxy.getUserName(), proxy.getPassword());
        conn.setInstanceFollowRedirects(false);
        setRequestMethod(conn, Utils.RequestMethod.GET);

        int statusCode = conn.getResponseCode();
        if (statusCode == 200) {
            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(conn.getInputStream(), Charset.defaultCharset()))) {
                StringBuilder result = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    result.append(line);
                }
                Object payload = JSONParser.parse(result.toString());
                if (payload instanceof MapValue) {
                    ArrayValue allVersions = ((MapValueImpl) payload)
                            .getArrayValue(StringUtils.fromString("allVersions"));
                    for (int i = 0; i < allVersions.size(); i++) {
                        if (isValidVersion(allVersions.get(i).toString(), filter)) {
                            versions.add(allVersions.get(i).toString());
                        }
                    }
                    return versions;
                }
            }
        } else if (statusCode == 404) { // module not found in the central
            return versions;
        }
        throw new IOException("retrieving central versions failed");
    }
}
