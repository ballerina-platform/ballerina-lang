const fs = require('fs');
const path = require('path');
const request = require('request');
const { expect } = require('chai');
const { spawn } = require('child_process');

global.targetPath = path.join(__dirname, '..', '..', '..', 'target');

const testEnv = require('./environment');

const testFilesDir = path.join(__dirname, '../resources/passing');
const parserUrl = `http://127.0.0.1:9091/composer/ballerina/parser/file/validate-and-parse`;


describe('Ballerina Composer Test Suite', () => {
    const testFiles = findBalFilesInDirSync(testFilesDir);
    let backEndProcess;
    
    before(function (beforeAllDone) {
        this.timeout(10000);
        const targetPath = path.join(global.targetPath, 'lib', `composer-server.jar`);
        backEndProcess = spawn('java', [`-Dballerina.home=${global.targetPath}`, '-jar', targetPath]);
        backEndProcess.stderr.pipe(process.stderr);
        let stdIndata = '';
        backEndProcess.stdout.on('data', (data) => {
            stdIndata += data;
            if (stdIndata.indexOf("Composer started successfully") < 0) {
                return;
            }
            console.log(stdIndata);
            testEnv.initialize();
            beforeAllDone();
        });
    });

    testFiles.forEach(testFile => {
        describe(path.basename(testFile), () => {
            let model;
            let content;
            
            beforeEach(done => {
                model = undefined;
                content = undefined;
                fs.readFile(testFile, 'utf8', (err, fileContent) => {
                    parse(fileContent, testFile, parsedModel => {
                        content = fileContent;
                        model = parsedModel;
                        if (!model) {
                            done(new Error('could not parse'));
                        }
                        done();
                    });
                })
            })

            it('renders', () => {
                testEnv.render(model)
            })

            it('generates source', () => {
                const generatedSource = testEnv.generateSource(model);
                expect(generatedSource).to.equal(content);
            })
        })
    })

    after(() => {
        backEndProcess.kill();
    });
});

function findBalFilesInDirSync(dir, filelist) {
    const files = fs.readdirSync(dir);
    filelist = filelist || [];
    files.forEach((file) => {
        if (fs.statSync(path.join(dir, file)).isDirectory()) {
            filelist = findBalFilesInDirSync(path.join(dir, file), filelist);
        } else if (path.extname(file) === '.bal') {
            filelist.push(path.join(dir, file));
        }
    });
    return filelist;
}

function parse(content, file, callback) {
    const parseOpts = {
        content,
        fileName: path.basename(file),
        filePath: path.dirname(file),
        includePackageInfo: true,
        includeProgramDir: true,
        includeTree: true,
    }

    const parseReq = request.post({
        url: parserUrl,
        json: parseOpts,
    }, (err, httpResponse, body) => {
        callback(body.model)
    });
}