/**
 * Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

const less = require('less');
const path = require('path');
const fs = require('fs-extra');
const rimraf = require('rimraf');

const CleanCSS = require('clean-css');
const RewriteImportPlugin = require("less-plugin-rewrite-import");
const NpmImportPlugin = require('less-plugin-npm-import');

const buildDir = path.join(__dirname, '..', 'build');
const themesDir = path.join(__dirname, '..', 'src', 'themes');

const lessNpmModuleDir = path.dirname(require.resolve('less'));
const semanticUILessModuleDir = path.join(lessNpmModuleDir, '..', 'semantic-ui-less');

const generateThemes = () => {
    fs.readdir(themesDir, function(error, themes) {
        if (error) {
            console.error(error);
        }

        themes.forEach(function(theme) {
            const filePath = path.join(themesDir, theme, 'theme.less');
            const options = {
                compress: false,
                sourceMap: true,
                filename: path.resolve(filePath),
                plugins: [
                    new NpmImportPlugin({ prefix: '~' }),
                    new RewriteImportPlugin({
                        paths: {
                            "../../theme.config": path.join(themesDir, theme, 'theme.config'),
                        }
                    })
                ]
            };

            fs.readFile(filePath, 'utf8', function(error, src) {
                if (error) {
                    console.error(error);
                }

                less.render(src, options)
                    .then(function(output) {
                            const minifiedOutput = new CleanCSS().minify(output.css);

                            fs.writeFile(path.join(buildDir, "ballerina-" + theme + ".css"), output.css, function(error) {
                                if (error) {
                                    console.error("ballerina-" + theme + ".css generation failed.");
                                    console.error(error);
                                } else {
                                    console.info("ballerina-" + theme + ".css generated.");
                                }
                            });
                            fs.writeFile(path.join(buildDir, "ballerina-" + theme + ".css.map"), output.map, function(error) {
                                if (error) {
                                    console.error("ballerina-" + theme + ".css.map generation failed.");
                                    console.error(error);
                                } else {
                                    console.info("ballerina-" + theme + ".css.map generated.");
                                }
                            });
                            fs.writeFile(path.join(buildDir, "ballerina-" + theme + ".min.css"), minifiedOutput.styles, function(error) {
                                if (error) {
                                    console.error("ballerina-" + theme + ".min.css generation failed.");
                                    console.error(error);
                                } else {
                                    console.info("ballerina-" + theme + ".min.css generated.");
                                }
                            });
                            fs.writeFile(path.join(buildDir, "ballerina-" + theme + ".min.css.map"), minifiedOutput.sourceMap, function(error) {
                                if (error) {
                                    console.error("ballerina-" + theme + ".min.css.map generation failed.");
                                    console.error(error);
                                } else {
                                    console.info("ballerina-" + theme + ".min.css.map generated.");
                                }
                            });
                        },
                        function(error) {
                            console.error(error);
                        });
            });
        });
    });
}

const copyAssets = () => {
    fs.copy(path.join(semanticUILessModuleDir, 'themes', 'default', 'assets'), path.join(buildDir, 'assets'), function(error) {
        if (error) {
            console.error(error);
        } else {
            console.error('semantic-ui-less assets copied.');
        }
    });
}

if (!fs.existsSync(buildDir)) {
    fs.mkdirSync(buildDir, function() {
        generateThemes();
        copyAssets();
    });
} else {
    rimraf(buildDir + '/*', function() {
        generateThemes();
        copyAssets();
    });
}
