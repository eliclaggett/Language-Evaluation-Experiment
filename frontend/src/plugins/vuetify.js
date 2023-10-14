import '@mdi/font/css/materialdesignicons.css';
import Vue from 'vue';
import Vuetify from 'vuetify/lib/framework';

Vue.use(Vuetify);

export default new Vuetify({
  icons: {
    iconfont: 'mdiSvg',
  },
  theme: {
    themes: {
      light: {
        primary: '#6C63FF', // #E53935
        secondary: '#3A34B3', // #FFCDD2
        accent: '#FFAD7D', // #3F51B5
      },
    },
  },
});
