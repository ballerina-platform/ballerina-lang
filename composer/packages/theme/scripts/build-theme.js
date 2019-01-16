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

const less = require('less');
const path = require('path');
const fs = require('fs-extra');
const rimraf = require('rimraf');
const lessToJs = require('less-vars-to-js');

const CleanCSS = require('clean-css');
const RewriteImportPlugin = require("less-plugin-rewrite-import");
const NpmImportPlugin = require('less-plugin-npm-import');
const InlineUrlsPlugin = require('less-plugin-inline-urls');

const libDir = path.join(__dirname, '..', 'lib');
const buildDir = path.join(__dirname, '..', 'build');
const themesDir = path.join(__dirname, '..', 'src', 'themes');
const semanticImports = path.join(__dirname, '..', 'src', 'semantic.imports');
const semanticIndexLess = path.join(buildDir, "semantic.less");

const lessNpmModuleDir = path.dirname(require.resolve('less'));
const semanticUILessModuleDir = path.join(lessNpmModuleDir, '..', 'semantic-ui-less');

const createSemanticIndexLess = () => {
    const src = fs.readFileSync(semanticImports, 'utf8');
    const extractedVariables = lessToJs(src, {
        resolveVariables: true,
        stripPrefix: true
    });
    let lessString = '';

    Object.keys(extractedVariables).map(key => {
        const components = extractedVariables[key].replace(/\s/g, '').split(',');

        components.map(component => {
            lessString += '& {\n' +
                '    @import "~semantic-ui-less/themes/default/' + key + '/' + component + '.variables";\n' +
                '    @import "~semantic-ui-less/definitions/' + key + '/' + component + '.less";\n' +
                '}\n';
        });
    });

    fs.writeFileSync(semanticIndexLess, lessString, (error) => {
        console.error("semantic.less generation failed.");
        console.error(error);
    });

    copyAssets();

    console.info("semantic.less generated.");
};

const generateThemes = () => {
    const themes = fs.readdirSync(themesDir);

    let fileWritePromises = themes.map(theme => {
        const filePath = path.join(themesDir, theme, 'theme.less');
        const options = {
            ieCompat: false,
            compress: false,
            sourceMap: true,
            javascriptEnabled: true,
            filename: path.resolve(filePath),
            plugins: [
                new NpmImportPlugin({ prefix: '~' }),
                new RewriteImportPlugin({
                    paths: {
                        "../../theme.config": path.join(themesDir, theme, 'theme.config'),
                    }
                }),
                InlineUrlsPlugin
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

    Promise.all(fileWritePromises).then(function(buffers) {
        copyFiles();
    }).catch(function(error) {
        console.error(error);
    });
};

const writeFile = (theme, file, content) => {
    fs.writeFileSync(path.join(buildDir, "ballerina-" + theme + file), content, (error) => {
        console.error("ballerina-" + theme + file + " generation failed.");
        console.error(error);
    });

    console.info("ballerina-" + theme + file + " generated.");
};

const copyFiles = () => {
    if (!fs.existsSync(libDir)) {
        fs.mkdirSync(libDir);
        copyCSS();
    } else {
        rimraf(libDir + '/*', () => {
            copyCSS();
        });
    }
};

const copyCSS = () => {
    fs.copy(buildDir, libDir)
        .then(() => {
            console.error('generated css files copied.');
            fs.removeSync(buildDir);
            console.error('Done.');
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

if (!fs.existsSync(buildDir)) {
    fs.mkdirSync(buildDir);
    createSemanticIndexLess();
    generateThemes();
} else {
    rimraf(buildDir + '/*', () => {
        createSemanticIndexLess();
        generateThemes();
    });
}
