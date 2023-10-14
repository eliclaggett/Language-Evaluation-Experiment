<template>
  <v-container class="col-sm-6 push-10">
    <v-col align="center">
      <v-img
        :src="require(`../assets/undraw_welcome_cats_thqn.svg`)"
        width="500"
        center
      ></v-img>
      <p class="pt-4 pb-4" v-if="player.platform == 'mturk'">
        Ready to chat? Complete the captcha to start the pre-evaluation.
      </p>
      <p class="pt-4 pb-4" v-if="player.platform == 'prolific'">
        Ready to chat? Complete the captcha to start the first task.
      </p>
      <div id="recaptcha"></div>
    </v-col>
  </v-container>
</template>

<script>
export default {
  /* global Breadboard */
  /* global grecaptcha */
  name: 'RecaptchaStep',
  props: {
    player: Object,
  },
  mounted() {
    this.initRecaptcha();
  },
  methods: {
    initRecaptcha() {
      setTimeout(() => {
        if (
          typeof grecaptcha === 'undefined' ||
          typeof grecaptcha.render === 'undefined'
        ) {
          this.initRecaptcha();
        } else {
          grecaptcha.render('recaptcha', {
            sitekey: '6LcJ3hknAAAAANlF8Wp0Uh9RLrsyDSTjZyehZdrM',
            theme: 'light',
            callback: this.onRecaptchaResponse,
          });
        }
      }, 100);
    },
    onRecaptchaResponse(response) {
      let data = {};
      data.response = response;
      data.time = new Date().getTime();
      Breadboard.send('verifyRecaptcha', data);
    },
  },
};
</script>
