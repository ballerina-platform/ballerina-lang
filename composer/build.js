var fs = require('fs');
var resolve = require('path').resolve;
var join = require('path').join;
var cp = require('child_process');
var os = require('os');

// submodule root
var modulesRoot = resolve(__dirname, './');

function runNpmCommand(args, cwd) {
  // npm cmd based on OS
  var npmCmd = os.platform().startsWith('win') ? 'npm.cmd' : 'npm';

  // execute command sync
  cp.spawnSync(npmCmd, args, { env: process.env, cwd, stdio: 'inherit' });
}

function forEachSubModule(callback) {
    fs.readdirSync(modulesRoot)
        .forEach(function (mod) {
            var modPath = join(modulesRoot, mod);
            // ensure path has package.json
            if (!fs.existsSync(join(modPath, 'package.json'))) {
                return;
            }
            callback(modPath);
        });
}

forEachSubModule((modPath) => {
    runNpmCommand(["i"], modPath);
    runNpmCommand(["run", "build"], modPath);
});
