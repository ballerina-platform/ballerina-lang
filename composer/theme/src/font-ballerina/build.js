const webfont = require('webfont').default;
const path = require('path');
const fs = require('fs');

const buildDir = path.join(__dirname, '..', '..', 'build');
if(!fs.existsSync(buildDir)) {
    fs.mkdirSync(buildDir);
}

const codepoints = {};

const fontName = 'font-ballerina';
const webfontConfig = {
    files: path.resolve(__dirname, './icons/**/*.svg'),
    fontHeight: 1000,
    normalize: true,
    fontName,
    templateClassName: 'fw',
    template: path.resolve(__dirname, './template.css.njk'),
    glyphTransformFn: (obj) => {
        codepoints[obj.name] = obj.unicode;
        return obj;
    },
    hash: new Date().getTime(),
}

webfont(webfontConfig)
.then(result => {
    if(!fs.existsSync(path.join(buildDir, 'font-ballerina'))) {
        fs.mkdirSync(path.join(buildDir, 'font-ballerina'));
    }
    
    ['svg', 'ttf', 'eot', 'woff', 'woff2'].forEach(ext => {
        const fileName = `font-ballerina.${ext}`;
        const filePath = path.join(buildDir, 'font-ballerina', fileName);
        fs.writeFile(filePath, result[ext], err => {
            if (err) {
                throw err;
            }
            console.log(`${fileName} written.`);
        });
    });

    // Write the css file
    const cssPath = path.join(buildDir, 'font-ballerina', 'font-ballerina.css')
    fs.writeFile(cssPath, result.template, err => {
        if (err) {
            throw err;
        }
        console.log('font-ballerina.css written.');
    });

    // Write the codepoints
    const conpointsPath = path.join(buildDir, 'font-ballerina', 'codepoints.json')
    fs.writeFile(conpointsPath, JSON.stringify(codepoints), err => {result.template
        if (err) {
            throw err;
        }
        console.log('codepoints.json written.');
    });
});