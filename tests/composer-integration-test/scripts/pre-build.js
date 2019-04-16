const glob = require("glob");
const fs = require("fs-extra");
const path = require("path");

const extractPath = path.join(__dirname, '..', 'target', 
            'extracted-distributions');

const balToolsPath = path.join(extractPath, 'ballerina-tools');

glob.sync(path.join(extractPath, `ballerina-tools-*`)).forEach((folder) => {
    if (folder.includes('ballerina-tools')) {
        fs.moveSync(folder, balToolsPath);
    }
});
