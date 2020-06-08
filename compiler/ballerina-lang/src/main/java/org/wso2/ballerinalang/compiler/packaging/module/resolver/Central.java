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

public class Central implements Repo {

    private static final String PRODUCTION_URL = "https://api.central.ballerina.io/1.0";

    @Override
    public List<String> resolveVersions(PackageID moduleId, String filter) throws ModuleResolveException {
        try {
            List<String> versions = getCentralVersions(moduleId.getOrgName().getValue(), moduleId.getName().getValue());
            for (String version : versions) {
                if (!isValidVersion(moduleId.version.getValue(), filter)) {
                    versions.remove(version);
                }
            }
            return versions;
        } catch (IOException e) {
            throw new ModuleResolveException("retrieving versions from central failed for " + moduleId.getOrgName()
                    + "/" + moduleId.getName(), e);
        }
    }

    @Override
    public boolean isModuleExists(PackageID moduleId) {
        return false;
    }

    public void pullModule(PackageID moduleId) {
        String remoteUri =
                PRODUCTION_URL + "/modules/" + moduleId.getOrgName() + "/" + moduleId.getName().getValue() + "/*/";
        Path modulePathInBaloCache = RepoUtils.createAndGetHomeReposPath()
                .resolve(ProjectDirConstants.BALO_CACHE_DIR_NAME).resolve(moduleId.getOrgName().getValue())
                .resolve(moduleId.getName().getValue());
        String moduleNameWithOrg = moduleId.getOrgName() + "/" + moduleId.getName().getValue();

        Pull.execute(remoteUri, String.valueOf(modulePathInBaloCache), moduleNameWithOrg, null, 0, null, null, "",
                false, true, IMPLEMENTATION_VERSION, "java8");
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
                    ArrayValue allVersions = ((MapValueImpl) payload)
                            .getArrayValue(StringUtils.fromString("allVersions"));
                    for (int i = 0; i < allVersions.size(); i++) {
                        versions.add(allVersions.get(i).toString());
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
