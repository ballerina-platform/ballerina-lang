/**
 * Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 *
 */

"use strict";

import * as fs from "fs";
import * as glob from "glob";
import * as paths from "path";

const NYC = require("nyc");
const Mocha = require("mocha");

let nyc: any;

let mocha = new Mocha({
    ui: "tdd",
    useColors: true,
});

function configure(mochaOpts: any): void {
    mocha = new Mocha(mochaOpts);
}
exports.configure = configure;

function _readCoverOptions(testsRoot: string): ITestRunnerOptions | undefined {
    let coverConfigPath = paths.join(testsRoot, "..", "..", "coverconfig.json");
    const coverConfigType: string = process.env.COVER_CONFIG || "";
    if (fs.existsSync(coverConfigPath)) {
        let configContent = fs.readFileSync(coverConfigPath, "utf-8");
        const configContentJson = JSON.parse(configContent);
        return configContentJson[coverConfigType] || configContentJson['default'];
    }
}

function run(testsRoot: string, clb: any): any {
    // Enable source map support

    // Read configuration for the coverage file
    let coverOptions = _readCoverOptions(testsRoot);
    if (coverOptions && coverOptions.enabled) {
        // Setup coverage pre-test, including post-test hook to report
        let coverageRunner = new CoverageRunner(coverOptions, testsRoot);
        coverageRunner.setupCoverage();
    }

    // Glob test files
    glob("**/**.test.js", { cwd: testsRoot }, (error, files): any => {
        if (error) {
            return clb(error);
        }
        try {
            // Fill into Mocha
            files.forEach((f): Mocha => {
                return mocha.addFile(paths.join(testsRoot, f));
            });
            // Run the tests
            let failureCount = 0;

            mocha.run()
                .on("fail", (): void => {
                failureCount++;
            })
            .on("end", (): void => {
                clb(undefined, failureCount);
            });
        } catch (error) {
            return clb(error);
        }
    });
}
exports.run = run;

interface ITestRunnerOptions {
    enabled?: boolean;
    relativeCoverageDir: string;
    relativeSourcePath: string;
    ignorePatterns: string[];
    includePid?: boolean;
    reports?: string[];
    verbose?: boolean;
}

class CoverageRunner {
    constructor(private options: ITestRunnerOptions, private testsRoot: string) {
        if (!options.relativeSourcePath) {
            return;
        }
    }

    public setupCoverage(): void {
        let sourceRoot = paths.join(this.testsRoot, this.options.relativeSourcePath);
        nyc = new NYC({
            cwd: sourceRoot,
            tempDir: '.nyc_output',
            reporter: "json",
        });
        // _mkDirIfExists(paths.join(sourceRoot, '.nyc_output'));
        nyc.addAllFiles();
        process.on("exit", (code) => {
            this.reportCoverage();
        });
    }
    reportCoverage() {
        nyc.report();
    }
}