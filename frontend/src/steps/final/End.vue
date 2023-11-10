<!-- Finish! Explain the final payment -->
<template>
  <v-col class="push-10 center" xl="3" lg="4" md="6" sm="8">
    <v-col class="center" xl="6" lg="8" md="10" sm="6">
      <v-img :src="require(`@/assets/undraw_happy_announcement_re_tsm0.svg`)"></v-img>
    </v-col>
    <h1 class="text-center">Thank you for participating!</h1>
    <div v-if="player.bonusOption == 2 && player.partnerBonusOption == -1">
      <p class="pt-4">
        Your earnings for this {{ hitText }} cannot be calculated yet.
      </p>
      <v-col align="center">
        <table class="receipt">
          <tr>
            <td>Base Pay</td>
            <td>{{ player.basePay | money }}</td>
          </tr>
          <tr>
            <td>Completion Bonus</td>
            <td>?</td>
          </tr>
          <tr class="sum">
            <td>Subtotal</td>
            <td>{{ finalPay | money }}</td>
          </tr>
        </table>
      </v-col>
      <p>
        The total will be calculated when your partner selects a bonus option.
        After seeing your total earnings, you will be {{ submitText }}. You will
        wait a maximum of {{ remainingTime }}.
      </p>
    </div>
    <div v-if="player.bonusOption == 1 || player.partnerBonusOption != -1">
      <p class="pt-4">Your total earnings for this {{ hitText }} are:</p>
      <v-col align="center">
        <table class="receipt mt-2 mb-6">
          <tr>
            <td>Base Pay</td>
            <td>{{ player.basePay | money }}</td>
          </tr>
          <tr>
            <td>Completion Bonus</td>
            <td>{{ bonusPay | money }}</td>
          </tr>
          <tr class="sum">
            <td>Total</td>
            <td>{{ finalPay | money }}</td>
          </tr>
        </table>
      </v-col>
      <p>
        <b>{{ partnerChoiceExplanation }}</b>
      </p>
      <div v-if="player.platform == 'mturk'">
        <p>Please click "Submit Study" when you are ready.</p>
        <div v-html="player.submit" v-if="player.submit" class="submitHit text-center mt-6"></div>
      </div>
      <div v-if="player.platform == 'prolific'">
        <p>
          Please submit this Completion Code when you are ready:<br />
          <strong class="completioncode">{{ player.prolificCompletionCodes[player.whichCompletionCode] }}</strong><br />
          <b>We will pay the difference between your total earnings and the base
          pay separately after you submit this code.</b>
        </p>
      </div>
    </div>
    <p class="pt-6 selectable">
      If you have time, please tell us why you chose the bonus option that you
      did! Please add any other feedback here, too.
    </p>
    <v-form class="d-block text-center">
      <v-textarea v-model="feedback" label="" auto-grow outlined></v-textarea>
      <v-btn @click="submitFeedback" id="sendBtn">Send Feedback</v-btn>
      <br /><br />
    </v-form>
    <p class="mt-4">
      For other comments or questions, please contact us at cmu.hcii.slab<span class="block-spam"
        aria-hidden="true">spam</span>@gmail.com
    </p>
    <p class="credits">
      Image Credits:<br />
      <a href="https://www.flaticon.com/free-icons/fist-bump" target="_blank" title="fist bump icons">Fist bump icons
        created by Freepik - Flaticon</a><br />
      <a href="https://www.flaticon.com/free-icons/shake-hands" target="_blank" title="shake hands icons">Shake hands
        icons created by Flat Icons - Flaticon</a>
    </p>
  </v-col>
</template>

<script>
/* global Breadboard */

export default {

  name: 'EndStep',

  mounted() {

    // For some reason, the button styles don't always load on their own
    document.querySelectorAll('button').forEach((el) => {
      el.className =
        'v-btn v-btn--is-elevated v-btn--has-bg theme--light v-size--default';
    });

    document.addEventListener('submit', (ev) => {
      // Prevent default submit action
      // Record "submitted hit" action with Breadboard
      // Then submit
      ev.preventDefault();
      let formData = new FormData(ev.target);
      var object = {};
      formData.forEach((value, key) => {
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
    submitFeedback() {
      // Record participants' feedback
      var data = {};
      data.feedback = this.feedback;
      document.querySelector('#sendBtn').classList.add('success');
      document.querySelector('#sendBtn').innerText =
        'Successfully sent feedback! Thank you!';
      window.Breadboard.send('submitFeedback', data);
    },
  },
  data: () => {
    return {
      feedback: '',
    };
  },
  computed: {
    finalPay() {
      var bonus = 0;
      if (this.player.bonusOption == 1) {
        bonus = this.player.completionBonus;
      } else if (
        this.player.bonusOption == 2 &&
        this.player.partnerBonusOption == 2
      ) {
        bonus = this.player.mutualCompletionBonus;
      }
      return this.player.basePay + bonus;
    },
    bonusPay() {
      if (this.player.bonusOption == 1) {
        return this.player.completionBonus;
      } else if (
        this.player.bonusOption == 2 &&
        this.player.partnerBonusOption == 2
      ) {
        return this.player.mutualCompletionBonus;
      } else {
        return 0;
      }
    },
    partnerChoiceExplanation() {
      if (this.player.partnerBonusOption == 1 && this.player.bonusOption == 2) {
        return 'Your partner selected the default bonus option. Therefore, you will not receive a bonus payment for this study';
      } else if (
        this.player.partnerBonusOption == 2 &&
        this.player.bonusOption == 2
      ) {
        return 'Great news! Your partner also chose the extra bonus option!';
      }
      return '';
    },
    remainingTime() {
      if (this.player.surveyStartTime) {
        var curTime = Date.now() + this.player.utcOffset;
        var endTime =
          (this.player.surveyStartTime +
            this.player.surveyTime * 60 +
            this.player.cooperationTime * 60) *
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
    hitText() {
      return this.player.platform == 'mturk' ? 'HIT' : 'study';
    },
  },
};
</script>

<style>
.credits,
.credits a {
  color: #000 !important;
}

.credits {
  opacity: 0.3;
}

.receipt {
  border-collapse: collapse;
}

.receipt td:last-child {
  text-align: right;
  padding-left: 1em;
}

.sum td {
  border-top: solid 1px #000;
  font-weight: bold;
}

.submitHit textarea {
  border: solid 1px #ccc;
  border-radius: 4px;
}
</style>
