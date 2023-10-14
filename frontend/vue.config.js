const dotenv = require('dotenv');
dotenv.config();

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
