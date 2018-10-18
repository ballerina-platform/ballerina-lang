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
package org.ballerinalang.toml.parser;

import org.antlr.v4.runtime.ANTLRFileStream;
import org.antlr.v4.runtime.ANTLRInputStream;
import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.tree.ParseTreeWalker;
import org.ballerinalang.toml.model.LockFile;
import org.ballerinalang.toml.util.TomlProcessor;
import org.wso2.ballerinalang.compiler.SourceDirectory;
import org.wso2.ballerinalang.compiler.util.CompilerContext;

import java.io.IOException;
import java.io.InputStream;

/**
 * LockFile Processor which processes the toml file parsed and populate the LockFile POJO.
 *
 * @since 0.973.1
 */
public class LockFileProcessor {

    private static final CompilerContext.Key<LockFileProcessor> LOCK_FILE_PROC_KEY = new CompilerContext.Key<>();
    private final LockFile lockFile;

    private LockFileProcessor(LockFile lockFile) {
        this.lockFile = lockFile;
    }

    /**
     * Get an instance of the LockFileProcessor.
     *
     * @param context compiler context
     * @param lockEnabled if lock is enabled or not
     * @return instance of LockFileProcessor
     */
    public static LockFileProcessor getInstance(CompilerContext context, boolean lockEnabled) {
        if (!lockEnabled) {
            return new LockFileProcessor(new LockFile());
        }
        LockFileProcessor lockFileProcessor = context.get(LOCK_FILE_PROC_KEY);
        if (lockFileProcessor == null) {
            SourceDirectory sourceDirectory = context.get(SourceDirectory.class);
            LockFile lfile = LockFileProcessor.parseTomlContentAsStream(sourceDirectory.getLockFileContent());
            LockFileProcessor instance = new LockFileProcessor(lfile);
            context.put(LOCK_FILE_PROC_KEY, instance);
            return instance;
        }
        return lockFileProcessor;
    }

    /**
     * Get the char stream of the content from file.
     *
     * @param fileName path of the toml file
     * @return lockFile object
     * @throws IOException exception if the file cannot be found
     */
    public static LockFile parseTomlContentFromFile(String fileName) throws IOException {
        ANTLRFileStream in = new ANTLRFileStream(fileName);
        return getLockFile(in);
    }

    /**
     * Get the char stream from string content.
     *
     * @param content toml file content as a string
     * @return lockFile object
     */
    public static LockFile parseTomlContentFromString(String content) {
        ANTLRInputStream in = new ANTLRInputStream(content);
        return getLockFile(in);
    }

    /**
     * Get the char stream from inputstream.
     *
     * @param inputStream inputstream of the toml file content
     * @return lockFile object
     */
    public static LockFile parseTomlContentAsStream(InputStream inputStream) {
        ANTLRInputStream in = null;
        try {
            in = new ANTLRInputStream(inputStream);
        } catch (IOException ignore) {
        }
        return getLockFile(in);
    }

    /**
     * Get the lockFile object by passing the ballerina toml file.
     *
     * @param charStream toml file content as a char stream
     * @return lockFile object
     */
    private static LockFile getLockFile(CharStream charStream) {
        LockFile lockFile = new LockFile();
        ParseTreeWalker walker = new ParseTreeWalker();
        walker.walk(new LockFileBuildListener(lockFile), TomlProcessor.parseTomlContent(charStream));
        return lockFile;
    }

    public LockFile getLockFile() {
        return lockFile;
    }
}
