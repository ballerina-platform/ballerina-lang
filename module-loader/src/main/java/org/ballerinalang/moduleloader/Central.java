package org.ballerinalang.moduleloader;

import org.ballerinalang.cli.module.util.Utils;
import org.ballerinalang.jvm.JSONParser;
import org.ballerinalang.jvm.values.ArrayValue;
import org.ballerinalang.jvm.values.MapValue;
import org.ballerinalang.jvm.values.MapValueImpl;
import org.ballerinalang.moduleloader.model.ModuleId;
import org.wso2.ballerinalang.util.RepoUtils;
import org.wso2.ballerinalang.util.TomlParserUtils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

import static org.ballerinalang.cli.module.util.Utils.convertToUrl;
import static org.ballerinalang.cli.module.util.Utils.createHttpUrlConnection;
import static org.ballerinalang.cli.module.util.Utils.initializeSsl;
import static org.ballerinalang.cli.module.util.Utils.setRequestMethod;
import static org.ballerinalang.moduleloader.Util.isValidVersion;

public class Central implements Repo {

    @Override
    public List<String> resolveVersions(ModuleId moduleId, String filter) throws IOException {
        try {
            List<String> versions = getCentralVersions(moduleId.orgName, moduleId.moduleName);
            for (String version : versions) {
                if (!isValidVersion(moduleId.version, filter)) {
                    versions.remove(version);
                }
            }
            return versions;
        } catch (IOException e) {
            throw new IOException(
                    "retrieving versions from central failed for " + moduleId.orgName + "/" + moduleId.moduleName, e);
        }
    }

    @Override
    public boolean isModuleExists(ModuleId moduleId) {
        return false;
    }

    @Override
    public void pullModule(ModuleId moduleId, Cache cache) {

    }

    public static List<String> getCentralVersions(String orgName, String moduleName) throws IOException {
        List<String> versions = new ArrayList<>();
        initializeSsl();
        String url = RepoUtils.getRemoteRepoURL() + "/modules/info/" + orgName + "/" + moduleName + "/*/";
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
                    ArrayValue allVersions = ((MapValueImpl) payload).getArrayValue("allVersions");
                    for (int i = 0; i < allVersions.size(); i++) {
                        versions.add((String) allVersions.get(i));
                    }
                    return versions;
                }
            }
        }
        throw new IOException("retrieving central versions failed");
    }
}
