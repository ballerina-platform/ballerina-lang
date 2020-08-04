/*
 * Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.ballerinalang.packerina.task;

import org.ballerinalang.packerina.buildcontext.BuildContext;
import org.ballerinalang.packerina.buildcontext.BuildContextField;
import org.ballerinalang.packerina.buildcontext.sourcecontext.SingleFileContext;
import org.ballerinalang.packerina.buildcontext.sourcecontext.SingleModuleContext;
import org.ballerinalang.packerina.buildcontext.sourcecontext.SourceType;
import org.wso2.ballerinalang.compiler.tree.BLangPackage;
import org.wso2.ballerinalang.util.Lists;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import static org.ballerinalang.tool.LauncherUtils.createLauncherException;

/**
 * Task for creating GraalVM native image.
 */
public class CreateGraalVmNativeImageTask implements Task {

    int availableProcessors = Runtime.getRuntime().availableProcessors();
    List<String> commands
            = Lists.of("native-image", "-H:+ReportExceptionStackTraces",
                       "-J-Djava.util.concurrent.ForkJoinPool.common.parallelism=" + availableProcessors,
                       "-H:+AddAllCharsets", "-H:EnableURLProtocols=http,https", "-H:-UseServiceLoaderFeature",
                       "-H:IncludeResources='.*MessagesBundle.properties'", "-H:IncludeResources=loggin.properties",
                       "-H:MaxDuplicationFactor=20",
                       "--initialize-at-build-time=org.h2.Driver," + "org.slf4j.impl.JDK14LoggerAdapter," +
                               "org.slf4j.impl.StaticLoggerBinder," + "org.slf4j.LoggerFactory",
                       "--initialize-at-run-time=io.netty.util.internal.logging.Log4JLogger," +
                               "io.netty.handler.ssl.OpenSsl," + "io.netty.handler.codec.http2.Http2CodecUtil," +
                               "io.netty.handler.codec.http2.Http2ConnectionHandler," +
                               "io.netty.handler.ssl.ConscryptAlpnSslEngine," +
                               "io.netty.handler.ssl.JettyNpnSslEngine," +
                               "io.netty.handler.ssl.JdkNpnApplicationProtocolNegotiator," +
                               "io.netty.internal.tcnative.SSL," +
                               "io.netty.handler.codec.http2.Http2ClientUpgradeCodec," +
                               "io.netty.handler.codec.http2.DefaultHttp2FrameWriter," +
                               "io.netty.handler.ssl.ReferenceCountedOpenSslEngine," +
                               "io.netty.internal.tcnative.CertificateVerifier," +
                               "io.netty.handler.ssl.JettyAlpnSslEngine$ClientEngine," +
                               "io.netty.handler.ssl.JettyAlpnSslEngine$ServerEngine," +
                               "io.netty.handler.ssl.JdkAlpnApplicationProtocolNegotiator," +
                               "io.grpc.netty.shaded.io.netty.handler.ssl.OpenSsl," +
                               "java.rmi.server.ObjID,sun.rmi.transport.ObjectTable," +
                               "sun.rmi.transport.Transport," +
                               "sun.rmi.transport.tcp.TCPEndpoint," +
                               "sun.rmi.registry.RegistryImpl," +
                               "sun.rmi.transport.DGCImpl," +
                               "sun.rmi.transport.DGCClient",
                       "-H:ClassInitialization=io.netty.handler.codec.http2.Http2ServerUpgradeCodec:run_time",
                       "-H:+TraceClassInitialization", "--enable-all-security-services", "--allow-incomplete-classpath",
                       "--report-unsupported-elements-at-runtime", "--no-fallback", "-jar");

    @Override
    public void execute(BuildContext buildContext) {
        BLangPackage executableModule = null;
        for (BLangPackage module : buildContext.getModules()) {
            if (module.symbol.entryPointExists) {
                executableModule = module;
                break;
            }
        }
        // executableModule cannot be null since this task is depend on createExecutable task which will fail if
        // executableModule is null.
        this.generateGraalVmNativeImage(executableModule, buildContext);
    }

    /**
     * Generate GraalVM native image.
     *
     * @param executableModule The module to run.
     */
    private void generateGraalVmNativeImage(BLangPackage executableModule, BuildContext buildContext) {
        try {
            // Sets classpath with executable thin jar and all dependency jar paths.
            commands.add(buildContext.getExecutablePathFromTarget(executableModule.packageID).toString());
            if (!buildContext.getSourceType().equals(SourceType.SINGLE_BAL_FILE)) {
                commands.add(buildContext.getGraalVMNativeImagePathFromTarget(executableModule.packageID).toString());
            }
            ProcessBuilder pb = new ProcessBuilder(commands).inheritIO();
            Process process = pb.start();
            process.waitFor();
        } catch (IOException | InterruptedException e) {
            throw createLauncherException("Error occurred while generating GraalVM native image ", e.getCause());
        }
    }
}
