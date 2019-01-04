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
const tempCSSBuildDir = path.join(__dirname, '..', 'temp');
const themesDir = path.join(__dirname, '..', 'src', 'themes');

const lessNpmModuleDir = path.dirname(require.resolve('less'));
const semanticUILessModuleDir = path.join(lessNpmModuleDir, '..', 'semantic-ui-less');

let promises = [];

const generateThemes = () => {
    const themes = fs.readdirSync(themesDir);

    promises = themes.map(theme => {
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

        const src = fs.readFileSync(filePath, 'utf8');

        return less.render(src, options).then(function(output) {
            const minifiedOutput = new CleanCSS().minify(output.css);
            const files = {
                '.css': output.css,
                '.css.map': output.map,
                '.min.css': minifiedOutput.styles
            };

            Object.keys(files).map(key => writeFile(theme, key, files[key]));
        }, (error) => {
            console.error(error);
        });
    });

    Promise.all(promises).then(function(buffers) {
        copyFiles();
    }).catch(function(error) {
        console.error(error);
    });
};

const writeFile = (theme, file, content) => {
    fs.writeFileSync(path.join(tempCSSBuildDir, "ballerina-" + theme + file), content, (error) => {
        console.error("ballerina-" + theme + file + " generation failed.");
        console.error(error);
    });

    console.info("ballerina-" + theme + file + " generated.");
};

const copyFiles = () => {
    if (!fs.existsSync(buildDir)) {
        fs.mkdirSync(buildDir, () => {
            copyCSS();
            copyAssets();
        });
    } else {
        rimraf(buildDir + '/*', () => {
            copyCSS();
            copyAssets();
        });
    }
};

const copyCSS = () => {
    fs.copy(tempCSSBuildDir, buildDir)
        .then(() => {
            console.error('generated css files copied.');
        })
        .catch((error) => {
            console.error(error);
        });
};

const copyAssets = () => {
    fs.copy(path.join(semanticUILessModuleDir, 'themes', 'default', 'assets'), path.join(buildDir, 'assets'))
        .then(() => {
            console.error('semantic-ui-less assets copied.');
        })
        .catch((error) => {
            console.error(error);
        });
};

if (!fs.existsSync(tempCSSBuildDir)) {
    fs.mkdirSync(tempCSSBuildDir, () => {
        generateThemes();
    });
} else {
    rimraf(tempCSSBuildDir + '/*', () => {
        generateThemes();
    });
}
