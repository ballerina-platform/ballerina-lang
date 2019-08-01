const glob = require("glob");
const fs = require("fs-extra");
const path = require("path");
const cp = require("child_process");

const extractPath = path.join(__dirname, '..', 'target', 
            'extracted-distributions');

const balToolsPath = path.join(extractPath, 'ballerina-tools');

glob.sync(path.join(extractPath, `ballerina-tools-*`)).forEach((folder) => {
    if (folder.includes('ballerina-tools')) {
        fs.moveSync(folder, balToolsPath);
    }
});

const composerPath = path.join(__dirname, "..", "..", "..", "composer")
cp.execSync("npm link " + composerPath, {
    cwd: path.join(__dirname),
});