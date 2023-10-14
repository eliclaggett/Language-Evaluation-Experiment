<template>
  <v-row>
    <v-col
      v-if="this.player.partnerReport == ''"
      class="push-10 center"
      xl="4"
      lg="6"
      md="6"
    >
      <h1 class="text-center mb-4">Waiting for your partner to finish...</h1>
      <p>
        We will ask you to review your partner's answers after they finish
        writing their report. You will wait a maximum of {{ remainingTime }}.
      </p>
    </v-col>
    <v-col
      v-if="this.player.partnerReport != ''"
      class="push-10 center"
      xl="4"
      lg="6"
      md="6"
    >
      <h1 class="text-center mb-4">
        According to your partner,<br />your opinion is:
      </h1>
      <v-card class="partnerReport">
        <v-card-text>
          {{ partnerReport }}
        </v-card-text>
      </v-card>
      <h3 class="text-center">
        Please rate how much you agree with their assessment.
      </h3>
      <p class="text-center">
        Your answer will have no effect on your partner's bonus.
      </p>
    </v-col>
    <v-col v-if="this.player.partnerReport != ''" cols="12" class="center">
      <LikertQuestion @chooseItem="chooseItem($event)" />
    </v-col>
  </v-row>
</template>

<script>
/* global Breadboard */
import LikertQuestion from '../components/LikertQuestion.vue';

export default {
  name: 'PartnerAnswerStep',
  components: { LikertQuestion },
  // watch: {
  //   player(val) {
  //     console.log(val.submit);
  //   }
  // },

  mounted() {
    document.querySelectorAll('button').forEach((el) => {
      el.className =
        'v-btn v-btn--is-elevated v-btn--has-bg theme--light v-size--default';
    });

    document.addEventListener('submit', (ev) => {
      ev.preventDefault();
      let formData = new FormData(ev.target);

      var object = {};
      formData.forEach((value, key) => {
        // Reflect.has in favor of: object.hasOwnProperty(key)
        if (!Reflect.has(object, key)) {
          object[key] = value;
          return;
        }
        if (!Array.isArray(object[key])) {
          object[key] = [object[key]];
        }
        object[key].push(value);
      });
      object['action'] = ev.target.action;
      object['method'] = ev.target.method;
      object['html'] = ev.target.outerHTML;
      var json = JSON.stringify(object);

      Breadboard.send('submitHIT', json);
      ev.target.submit();
    });
  },

  props: {
    player: Object,
  },

  filters: {
    money(val) {
      return val.toLocaleString('en-US', {
        style: 'currency',
        currency: 'USD',
      });
    },
  },

  methods: {
    chooseItem: function (item) {
      Breadboard.send('reportPartnerAnswer', { val: item });
    },
  },
  data: () => {
    return {
      feedback: '',
    };
  },
  computed: {
    partnerReport() {
      if (this.player.partnerReport == 'blank') {
        return 'No answer';
      }
      return this.player.partnerReport['summary'];
    },
    remainingTime() {
      if (this.player.surveyStartTime) {
        var curTime = Date.now() + this.player.utcOffset;
        var endTime =
          (this.player.surveyStartTime +
            this.player.surveyTime * 60 +
            this.player.donationTime * 60) *
          1000;

        var diff = (endTime - curTime) / 1000;
        if (diff < 60) {
          return '<1 minute';
        } else if (diff < 90) {
          return '1 minute';
        } else {
          return Math.round(diff / 60) + ' minutes';
        }
      } else {
        return '';
      }
    },
    submitText() {
      return this.player.platform == 'mturk'
        ? 'able to submit this HIT'
        : 'given a completion code for this study';
    },
  },
};
</script>

<style>
.partnerReport {
  margin-bottom: 1em;
  color: #000 !important;
}
.partnerReport .v-card__text {
  color: #000 !important;
  font-size: 1rem !important;
}
</style>
