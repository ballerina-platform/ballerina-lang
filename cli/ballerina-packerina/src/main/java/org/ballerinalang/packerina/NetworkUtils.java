package org.ballerinalang.packerina;

import org.ballerinalang.bre.Context;
import org.ballerinalang.compiler.CompilerPhase;
import org.ballerinalang.launcher.LauncherUtils;
import org.ballerinalang.launcher.util.BCompileUtil;
import org.ballerinalang.launcher.util.CompileResult;
import org.ballerinalang.util.codegen.PackageInfo;
import org.ballerinalang.util.codegen.ProgramFile;
import org.ballerinalang.util.debugger.Debugger;
import org.ballerinalang.util.program.BLangFunctions;

import java.nio.file.Path;

import static org.ballerinalang.util.BLangConstants.USER_REPO_ARTIFACTS_DIRNAME;
import static org.ballerinalang.util.BLangConstants.USER_REPO_SRC_DIRNAME;

/**
 * Util class for network calls
 */
public class NetworkUtils {
    static CompileResult compileResult;
    private static final String BALLERINA_CENTRAL_REPOSITORY = "http://packages.ballerina.io/";

    /**
     * Pull/Downloads packages from the package repository
     *
     * @param resourceName
     */
    public static void pullPackages(String resourceName) {
        compileResult = compileBalFile();
        // Ballerina central repository URL
        String url = BALLERINA_CENTRAL_REPOSITORY;
        // Destination folder: by default should be downloaded to
        Path targetDirectoryPath = UserRepositoryUtils.initializeUserRepository()
                .resolve(USER_REPO_ARTIFACTS_DIRNAME)
                .resolve(USER_REPO_SRC_DIRNAME);

        String dstPath = targetDirectoryPath + "/" + resourceName;
        String resourcePath = url + "/" + resourceName;

        String[] arguments = new String[]{resourcePath, dstPath};
        LauncherUtils.runMain(compileResult.getProgFile(), arguments);
    }

    /**
     * Compile the bal file
     *
     * @return compile result after compiling the bal file
     */
    public static CompileResult compileBalFile() {
        CompileResult compileResult = BCompileUtil.compile("src", "ballerina.pull", CompilerPhase.CODE_GEN);
        ProgramFile programFile = compileResult.getProgFile();
        PackageInfo packageInfo = programFile.getPackageInfo(compileResult.getProgFile().getEntryPkgName());
        Context context = new Context(programFile);
        Debugger debugger = new Debugger(programFile);
        programFile.setDebugger(debugger);
        compileResult.setContext(context);
        BLangFunctions.invokePackageInitFunction(programFile, packageInfo.getInitFunctionInfo(), context);
        return compileResult;
    }
}