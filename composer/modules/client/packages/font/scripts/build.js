const fs = require('fs');
const path = require('path');
const shell = require('shelljs');
const webfont = require('webfont').default
const codepoints = {};

const fontsDir = path.resolve(__dirname, '../dist/fonts/');
const stylesDir = path.resolve(__dirname, '../dist/css/');
const outputFilename = 'font-ballerina';

const config = {
    files: path.resolve(__dirname, '../src/icons/*.svg'),
    cssTemplateFontPath: '../fonts/',
    fontName: 'font-ballerina',
    fontHeight: 1000,
    normalize: true,
    cssTemplateClassName: 'fw', // TODO: map with proper class name
    template: path.resolve(__dirname, '../src/template.css.njk'),
    glyphTransformFn: (obj) => {
        codepoints[obj.name] = obj.unicode;
    },
    formats: ['svg', 'ttf', 'eot', 'woff', 'woff2'],
    dest: {
        outputFilename: 'font-ballerina.css',
    },
    hash: new Date().getTime(),
};

function ensureDirectoryExistence(filePath) {
    const dirname = path.dirname(filePath);
    if (fs.existsSync(dirname)) {
        return true;
    }
    ensureDirectoryExistence(dirname);
    fs.mkdirSync(dirname);
}

webfont(config)
    .then((result) => {
        if (!fs.existsSync(fontsDir)) {
            shell.mkdir('-p', fontsDir);
        }
        if (!fs.existsSync(stylesDir)) {
            shell.mkdir('-p', stylesDir);
        }
        fs.writeFileSync(path.join(fontsDir, outputFilename + '.svg'), result.svg);
        fs.writeFileSync(path.join(fontsDir, outputFilename + '.ttf'), result.ttf);
        fs.writeFileSync(path.join(fontsDir, outputFilename + '.eot'), result.eot);
        fs.writeFileSync(path.join(fontsDir, outputFilename + '.woff'), result.woff);
        fs.writeFileSync(path.join(fontsDir, outputFilename + '.woff2'), result.woff2);
        fs.writeFileSync(path.join(stylesDir, outputFilename + '.css'), result.styles);
        fs.writeFileSync(path.resolve(__dirname, '../dist/codepoints.json'), JSON.stringify(codepoints), 'utf8');
        console.info('Successfully built font at ' + fontsDir);
    })
    .catch((error) => {
        console.error('Error while building the font.\n' + error);
    });
