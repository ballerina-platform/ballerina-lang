const fs = require("fs");
const path = require("path");
const packageJson = require("../package.json");
packageJson.version = process.argv[2];

fs.writeFileSync(
    path.join(__dirname, "..", "package.json"),
    JSON.stringify(packageJson, null, 4)+"\n");
