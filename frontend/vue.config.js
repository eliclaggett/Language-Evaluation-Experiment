const dotenv = require('dotenv');
const findConfig = require('find-config');
const fs = require('fs');
try {
  const envFile = dotenv.parse(fs.readFileSync(findConfig('.env')));
  for (const key of Object.keys(envFile)) {
    process.env['VUE_APP_'+key] = envFile[key];
  }
  process.env.BREADBOARD_ROOT = envFile.BREADBOARD_ROOT;

  const texts = fs.readFileSync(envFile.EXPERIMENT_DIR + '/texts.json');
  fs.writeFileSync('src/assets/texts.json', texts);

} catch (err) {
  console.error(err);
  console.log('Could not find .env file.');
}

module.exports = {
  outputDir: process.env.BREADBOARD_ROOT || '../breadboard/generated',
  publicPath: '/generated/breadboard-v2.4/',
  transpileDependencies: [
    'vuetify',
    '@human-nature-lab/breadboard-client',
    'vue-quick-chat',
  ],
  filenameHashing: false,
  runtimeCompiler: true,
  devServer: {
    writeToDisk: true,
  },
};
