const { log } = console;
const path = require('path');
const fs = require('fs');


log('\x1b[36mNote:\x1b[0m', 'package-lock.json won\'t b updated during the build');
const cwd = process.cwd();

const readablePl = fs.createReadStream(path.join(cwd, 'package-lock.json'));
readablePl.on("error", function(err) {
    log(error);
});

const writablePlb = fs.createWriteStream(path.join(cwd, 'package-lock.json.bak'));
writablePlb.on("error", function(err) {
    log(error);
});

readablePl.pipe(writablePlb);