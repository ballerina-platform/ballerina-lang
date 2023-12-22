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

package io.ballerina.cli.task;

// import dev.sigstore.ImmutableCertificateIdentity;

import io.ballerina.cli.utils.BuildTime;
import io.ballerina.cli.utils.GraalVMCompatibilityUtils;
import io.ballerina.projects.EmitResult;
import io.ballerina.projects.JBallerinaBackend;
import io.ballerina.projects.JvmTarget;
import io.ballerina.projects.PackageCompilation;
import io.ballerina.projects.Project;
import io.ballerina.projects.ProjectException;
import io.ballerina.projects.internal.model.Target;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.Path;
import java.util.regex.Pattern;

// import java.nio.file.Paths;
// import java.security.cert.CertPath;

import static io.ballerina.cli.launcher.LauncherUtils.createLauncherException;

import dev.sigstore.KeylessSignature;
import dev.sigstore.KeylessSigner;
// import java.io.IOException;
// import java.nio.charset.StandardCharsets;
// import java.nio.file.Files;
// import java.nio.file.Path;
// import java.nio.file.Paths;
// import java.nio.file.StandardOpenOption;
// import java.security.InvalidAlgorithmParameterException;
// import java.security.InvalidKeyException;
// import java.security.NoSuchAlgorithmException;
// import java.security.cert.CertificateException;
// import java.security.spec.InvalidKeySpecException;
// import dev.sigstore.ImmutableCertificateIdentity;
// import dev.sigstore.KeylessSignature;
// import dev.sigstore.KeylessSigner;
// import dev.sigstore.KeylessSignerException;
// import dev.sigstore.KeylessVerificationException;
// import dev.sigstore.KeylessVerificationRequest;
// import dev.sigstore.KeylessVerificationRequest.CertificateIdentity;
// import dev.sigstore.KeylessVerificationRequest.VerificationOptions;
// import dev.sigstore.KeylessVerifier;
// import dev.sigstore.bundle.BundleFactory;
// import dev.sigstore.bundle.BundleParseException;

/**
 * Task for creating bala file. Bala file writer is meant for modules only and
 * not for single files.
 *
 * @since 2.0.0
 */
public class SigstoreSignTask implements Task {
    private final transient PrintStream out;
    private boolean flag = false;

    public SigstoreSignTask(PrintStream out, boolean flag) {
        this.out = out;
        this.flag = flag;
    }

    @Override
    public void execute(Project project) {
        this.out.println();
        this.out.println("Signing bala");

        Target target;
        Path balaPath;
        try {
            target = new Target(project.targetDir());
            balaPath = target.getBalaPath();
        } catch (IOException | ProjectException e) {
            throw createLauncherException(e.getMessage());
        }

        try {
            // Path testArtifact = Paths.get("path/to/my/file.jar")

            // var signer = KeylessSigner.builder().sigstorePublicDefaults().build();
            KeylessSigner signer = KeylessSigner.builder().sigstorePublicDefaults().build();
            KeylessSignature result = signer.signFile(balaPath);
            // Extract email
            Pattern pattern = Pattern.compile("RFC822Name: (\\S+)");
            java.util.regex.Matcher matcher = pattern.matcher(result.toString());
            String email = "";
            if (matcher.find()) {
                email = matcher.group(1);
                // Print the extracted email address
            } else {
                //
            }
            String issuer = "";
            if (result.toString().contains("google")) {
                issuer = "https://accounts.google.com";
            } else if (result.toString().contains("github")) {
                issuer = "https://github.com/login/oauth";
            } else if (result.toString().contains("gitlab")) {
                issuer = "https://gitlab.com";
            } else if (result.toString().contains("microsoftonline")) {
                issuer = "https://login.microsoftonline.com";
            } else {
                issuer = null;
            }
        } catch (Exception e) {
            // TODO: handle exception
        }

        JBallerinaBackend jBallerinaBackend;
        EmitResult emitResult;

        try {
            PackageCompilation packageCompilation = project.currentPackage().getCompilation();
            jBallerinaBackend = JBallerinaBackend.from(packageCompilation, JvmTarget.JAVA_17);
            long start = 0;
            if (project.buildOptions().dumpBuildTime()) {
                start = System.currentTimeMillis();
            }
            String warning = GraalVMCompatibilityUtils.getWarningForPackage(
                    project.currentPackage(), jBallerinaBackend.targetPlatform().code());
            if (warning != null) {
                out.println("\n" + warning);
            }
            emitResult = jBallerinaBackend.emit(JBallerinaBackend.OutputType.BALA, balaPath);
            if (project.buildOptions().dumpBuildTime()) {
                BuildTime.getInstance().emitArtifactDuration = System.currentTimeMillis() - start;
                BuildTime.getInstance().compile = true;
            }
        } catch (ProjectException e) {
            throw createLauncherException("BALA creation failed:" + e.getMessage());
        }

        Path relativePathToExecutable;

        try {
            relativePathToExecutable = project.sourceRoot().relativize(emitResult.generatedArtifactPath());
        } catch (IllegalArgumentException e) {
            // For cases where a custom path is given
            relativePathToExecutable = project.sourceRoot().resolve(emitResult.generatedArtifactPath());
        }

        // Print the path of the BALA file
        if (relativePathToExecutable.toString().contains("..") ||
                relativePathToExecutable.toString().contains("." + File.separator)) {
            this.out.println("\t" + emitResult.generatedArtifactPath().toString());
        } else {
            this.out.println("\t" + relativePathToExecutable.toString());
        }
    }

}
