const fs = require('fs');
const unzip = require('unzip2');
const path = require('path');

function functionName() {
    fs.createReadStream(path.join(__dirname, '../web/target/ballerina-composer-web-0.85-SNAPSHOT.zip')).pipe(unzip.Extract({ path: './resources' }));
}

functionName();
