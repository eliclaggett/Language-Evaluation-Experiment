<template>
  <v-col class="push-10 center" xl="6" lg="8" md="8">
    <div
      v-if="!player.doneFakeLoading && false"
      class="text-center center overflow-hidden"
    >
      <h2 class="text-center">Verification in progress...</h2>
      <v-progress-linear indeterminate class="ma-10 center text-center" />
      <p class="text-center">
        {{ loadingText }}
      </p>
    </div>
    <div v-if="player.doneFakeLoading || true">
      <h1 class="text-center mb-4">You're eligible for a bonus!</h1>
      <p class="text-center">Please select an option below.</p>
      <v-layout
        style="margin-top: 1.5em; align-items: stretch"
        justify-center
        class="col-sm-8 center"
      >
        <!-- <v-radio-group v-model="bonusOption" class="nostyle">
            <v-col class="aligned-col">
              <div>
                <v-icon class="bonusIcon">mdi-cash</v-icon>
                <h3>Default Bonus</h3>
                <p>
                  Award yourself {{ player.completionBonus | money }} for your great work on this task.
                </p>
              </div>
              <v-radio key="q1-default" value="1" class="center"/>
            </v-col>
            <v-col class="aligned-col">
              <div>
              <v-icon class="bonusIcon">mdi-cash-multiple</v-icon>
                <h3>Extra Bonus</h3>
                <p>
                  Award yourself {{ player.mutualCompletionBonus | money }} for your great work on this task. <b>If your partner does not agree to this, you will receive no bonus.</b>
                </p>
              </div>
              <v-radio key="q1-extra" value="2" class="center"/>
            </v-col>
          </v-radio-group> -->
        <v-radio-group v-model="bonusOption">
          <v-row class="radiorow">
            <v-radio key="q1-default" value="1" class="center" />
            <div>
              <p>
                Award yourself a {{ player.completionBonus | money }} bonus.
              </p>
            </div>
          </v-row>
          <v-row class="radiorow">
            <v-radio key="q1-extra" value="2" class="center" />
            <div>
              <p>
                Award you and your partner a
                {{ player.mutualCompletionBonus | money }} bonus.
                <span class="smalltext"
                  >* If you select this but your partner does not, you will
                  receive no bonus.</span
                >
              </p>
            </div>
          </v-row>
        </v-radio-group>
      </v-layout>
    </div>
    <v-layout justify-center>
      <v-btn class="center mt-4" @click="selectBonusOption">Confirm</v-btn>
    </v-layout>
  </v-col>
</template>

<script>
/* global Breadboard */

export default {
  name: 'GameStep',

  props: {
    player: Object,
  },

  mounted() {},

  watch: {
    player(val) {
      if (
        val.gameStep == 'game' &&
        !val.doneFakeLoading &&
        !this.startFakeLoading
      ) {
        this.startFakeLoading = true;
        setTimeout(() => {
          this.loadingText = 'Analyzing content';
        }, 1000);

        setTimeout(() => {
          Breadboard.send('doneFakeLoading');
        }, 6000);
      }
    },
  },

  components: {},

  filters: {
    money(val) {
      return val.toLocaleString('en-US', {
        style: 'currency',
        currency: 'USD',
      });
    },
  },

  data: () => ({
    value: '1.5',
    label: 'money',
    loadingText: 'Waiting for review',
    startFakeLoading: false,
    bonusOption: '1',
  }),

  computed: {
    valueHint() {
      return this.value.toLocaleString('en-US', {
        style: 'currency',
        currency: 'USD',
      });
    },
    kept() {
      return this.player.completionBonus - this.value;
    },
    remainingTime() {
      if (this.player.donationStartTime) {
        var curTime = Date.now() + this.player.utcOffset;
        var endTime =
          (this.player.donationStartTime + this.player.donationTime * 60) *
          1000;

        var diff = (endTime - curTime) / 1000;
        return Math.round(diff) + ' seconds remaining';
      } else {
        return '';
      }
    },
    keptFontSize() {
      return 4 - (this.value / this.player.completionBonus + 1.5);
    },
    contributedFontSize() {
      return this.value / this.player.completionBonus + 1.5;
    },
  },

  methods: {
    selectBonusOption() {
      document.body.scrollTop = 0; // For Safari
      document.documentElement.scrollTop = 0;
      Breadboard.send('completeCooperationGame', { option: this.bonusOption });
    },
  },
};

document.addEventListener('click', (ev) => {
  const el = ev.target;
  if (el.closest('.aligned-col') && !el.closest('.v-radio')) {
    el.closest('.aligned-col')
      .querySelector('.v-radio')
      .dispatchEvent(new Event('click'));
  } else if (el.closest('.radiorow') && !el.closest('.v-radio')) {
    el.closest('.radiorow')
      .querySelector('.v-radio')
      .dispatchEvent(new Event('click'));
  }
});
</script>

<style>
.gameVis {
  width: 7em;
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;
}
.gameVis h3 {
  font-size: 2em;
}
#donationAmtDisplay {
  height: 7rem;
}
.bonusBtn {
  margin: 2rem 0.5rem 0 0.5rem;
}
.bonusIcon {
  display: block !important;
  text-align: center;
  color: rgb(108, 99, 255) !important;
  font-size: 5rem !important;
  margin-bottom: 2rem;
}
.aligned-col {
  display: flex;
  flex-direction: column;
  justify-content: space-between;
  padding: 0 3rem;
  cursor: pointer;
  transition: opacity 0.2s ease-in-out;
}
.radiorow {
  display: flex;
  align-items: center;
  flex-wrap: nowrap;
  margin-bottom: 1em;
  justify-content: flex-start;
  cursor: pointer;
}
.radiorow div {
  flex-grow: 1;
}
.radiorow p,
.radiorow .v-radio {
  margin-bottom: 0 !important;
}
.radiorow .v-radio {
  flex-grow: 0;
}

.nostyle:hover .aligned-col {
  opacity: 0.5;
}
.aligned-col:hover {
  opacity: 1 !important;
}

.aligned-col:last-child {
  border-left: solid 1px #ccc;
}
.nostyle .v-input--radio-group__input {
  display: flex !important;
  flex: 1 1 auto !important;
  justify-content: center;
  align-items: stretch;
  flex-wrap: nowrap;
  margin: 0;
  padding: 0;
  flex-direction: row;
}
.smalltext {
  font-size: 0.8em;
  display: block;
  opacity: 0.7;
}
</style>
