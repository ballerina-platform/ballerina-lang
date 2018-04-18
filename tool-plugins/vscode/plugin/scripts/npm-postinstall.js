const { log } = console;
const path = require('path');
const fs = require('fs');

const cwd = process.cwd();

const readablePl = fs.createReadStream(path.join(cwd, 'package-lock.json.bak'));
readablePl.on("error", function(err) {
    log(error);
});

const writablePlb = fs.createWriteStream(path.join(cwd, 'package-lock.json'));
writablePlb.on("error", function(err) {
    log(error);
});

readablePl.pipe(writablePlb);

fs.unlinkSync(path.join(cwd, 'package-lock.json.bak'));