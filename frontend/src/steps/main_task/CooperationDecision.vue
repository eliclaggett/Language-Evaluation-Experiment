<!-- Main Task: Cooperation Decision -->
<template>
  <v-col class="push-10 center" xl="6" lg="8" md="8">
    <div v-if="!player.doneFakeLoading" class="text-center center overflow-hidden">
      <!-- Fake loading message to "verify the participant's responses" -->
      <h2 class="text-center">Verification in progress...</h2>
      <v-progress-linear indeterminate class="ma-10 center text-center" />
      <p class="text-center">
        {{ loadingText }}
      </p>
    </div>
    <div v-if="player.doneFakeLoading">
      <!-- Done fake loading, show the bonus options -->
      <h1 class="text-center mb-4">You're eligible for a bonus!</h1>
      <p class="text-center">Please select an option below.</p>
      <v-layout style="margin-top: 1.5em; align-items: stretch" justify-center class="col-sm-8 center">
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
                <span class="smalltext">* If you select this but your partner does not, you will
                  receive no bonus.</span>
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
  name: 'CooperationStep',

  props: {
    player: Object,
  },

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

  filters: {
    money(val) {
      return val.toLocaleString('en-US', {
        style: 'currency',
        currency: 'USD',
      });
    },
  },

  data: () => ({
    loadingText: 'Waiting for review', // Dynamic loading message
    startFakeLoading: false,
    bonusOption: '1', // Default bonus option == 1
  }),

  computed: {
    remainingTime() {
      if (this.player.cooperationStartTime) {
        var curTime = Date.now() + this.player.utcOffset;
        var endTime =
          (this.player.cooperationStartTime + this.player.cooperationTime * 60) *
          1000;

        var diff = (endTime - curTime) / 1000;
        return Math.round(diff) + ' seconds remaining';
      } else {
        return '';
      }
    },
  },

  methods: {
    selectBonusOption() {
      document.body.scrollTop = 0; // For Safari
      document.documentElement.scrollTop = 0;
      Breadboard.send('completeCooperationDecision', { option: this.bonusOption });
    },
  },
};

document.addEventListener('click', (ev) => {
  // Allow clicking anywhere in the general area to select the radio button
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
