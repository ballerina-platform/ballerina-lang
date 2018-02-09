const child = require('child_process');

const fileTypes = process.argv.splice(2);

let files = child.execSync('git diff HEAD --name-only --relative=modules/web/');
files = files.toString('utf8').split('\n');

files = files.filter((file) => {
    let fileToBeChecked = false;
    fileTypes.forEach((fileType) => {
        if (file.trim().endsWith(fileType)) {
            fileToBeChecked = true;
        }
    });
    return fileToBeChecked;
});
files = files.join(' ');

if (files.length === 0) {
    process.exit(0);
}

// child.stdout.pipe(process.stdout);
child.exec(`npm run lintfile ${files}`, (err, stdout) => {
    const found = /\(([0-9]) errors,/g.exec(stdout);
    if (parseInt(found[1], 10) > 0) {
        console.log(stdout);
        console.log(`${found[1]} errors found. Please fix them.`);
        process.exit(1);
    } else {
        console.log('File formatting correct');
        process.exit(0);
    }
});

// lintCommand.stdout.on('data', (data) => {
//     console.log(`stdout: ${data}`);
// });
