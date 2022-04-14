const fs = require('fs');
const archiver = require('archiver');

const jniLibPath = __dirname + '/../vpnLib/src/main/jniLibs';
const jniDirList = fs.readdirSync(jniLibPath);

jniDirList.forEach((item) => {
  if (item === '.DS_Store') return;

  const output = fs.createWriteStream(`${__dirname}/${item}.zip`);
  const archive = archiver('zip', {
    zlib: { level: 9 },
  });

  output.on('close', () => {
    const compressedSize = (archive.pointer() / 1024 / 1024).toFixed(2);
    console.log(`Finish compressing [${item}] - ${compressedSize} MB`);
  });

  output.on('end', () => {
    console.log('Data has been drained');
  });

  archive.on('warning', (err) => {
    if (err.code === 'ENOENT') {
      console.log('Warning ' + err);
    } else {
      throw err;
    }
  });

  archive.on('error', (err) => {
    throw err;
  });

  archive.pipe(output);
  archive.directory(`${jniLibPath}/${item}/`, false);
  archive.finalize();
});
