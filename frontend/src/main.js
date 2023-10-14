// Transpiler for older browsers
import 'core-js/stable';
// optional but required for transforming generator fns.
import 'regenerator-runtime/runtime';

import Vue from 'vue';
import App from './App.vue';
import vuetify from './plugins/vuetify';
import './plugins/vue-mask';

Vue.prototype.window = window;
Vue.config.productionTip = false;
Vue.config.devtools = false;

new Vue({
  vuetify,
  render: (h) => h(App),
}).$mount('#app');

// app.config.globalProperties.window = window; // Vue 3
