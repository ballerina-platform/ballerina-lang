const { spawn } = require('child_process');
const openport = require('openport');
const path = require('path');
const fs = require('fs');

function _start(cb) {
    openport.find((err, port) => {
        if(err) { 
            console.log("Couldn't find an open port to start the parser service.");
            return;
        }
        console.log()
        const parserServicePath = path.join(__dirname, '..', 'server-build', 'parser-service.jar');
        const parserServiceProcess = this.parserServiceProcess = spawn(
            'java', ['-Dbal.composer.home', '-jar', parserServicePath, '--port', port]);

        parserServiceProcess.on('exit', () => {
            this.parserService = null;
        });

        parserServiceProcess.stdout.on('data', (data) => {
            console.log(`${data}`);
            if (`${data}`.indexOf('Composer started successfully') > -1) {
                cb({port});
            }
        });
    });
}

function start() {
    if(this.parserService) {
        return this.parserService;
    }

    this.parserService = new Promise((resolve, reject) => {
        _start((options) => {options ? resolve(options) : reject()})
    })
    return this.parserService;
}

function end() {
    if(this.parserServiceProcess) {
        const killed =  new Promise((resolve, reject) => {
            this.parserServiceProcess.on('exit', () => {
                resolve();
            });
        });
        this.parserServiceProcess.kill();
        return killed;
    }

    return Promise.resolve();
}

module.exports = {
    start, end
}