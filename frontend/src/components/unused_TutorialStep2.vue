<template>
  <div>
    <h2>Tutorial {{ step }} / {{ maxSteps }}</h2>
    <div v-if="step === 1">
      <p>
        When the timer runs out, we will group participants with shared
        {{ valuesOrPreferences }} together and you will be randomly paired with
        someone from any one of the groups. Then, you will be given
        {{ chatTime }} to exchange messages with your partner and learn as much
        about their {{ discussionTopic }} as possible before writing a report
        about their perspective. While you write the report, you will be
        provided the chat history for reference. Remember that sufficiently long
        and accurate reports will be rewarded with a {{ bonusString }} bonus.
        <br /><br />
        Note: Refrain from using offensive language and providing personally
        identifiable information. If necessary, you can end the experiment by
        clicking the "Report Partner" button below the chat window:
        <v-img
          :src="require(`../assets/report_btn.png`)"
          max-width="150"
        ></v-img>
      </p>
    </div>
    <div v-if="step === 7" class="text--primary">
      <p>
        After finishing the survey, you will receive
        {{ player.basePay | money }} for completing the task. A sufficient
        report will earn you an additional {{ player.completionBonus | money }}.
        When your earnings are calculate and the "Submit HIT" button is
        displayed, follow the instructions on screen and promptly click "Submit
        HIT" when appropriate.
      </p>
    </div>
    <v-btn @click="clickBack" :disabled="step < 2">Back</v-btn>
    <!-- <v-spacer /> -->
    <v-btn @click="clickNext" v-if="step < maxSteps">Next</v-btn>
    <v-btn @click="clickDone" v-else>Finish</v-btn>
  </div>
</template>

<script>
export default {
  name: 'TutorialBox',

  props: {
    player: Object,
    step: Number,
  },

  methods: {
    clickNext: function () {
      this.$emit('next');
    },
    clickBack: function () {
      this.$emit('back');
    },
    clickDone: function () {
      this.$emit('done');
    },
  },
  computed: {
    bonusString() {
      return this.player.defaultBonus.toFixed(2).toString();
    },
    completionString() {
      return this.player.completionBonus.toFixed(2).toString();
    },
    maxSteps() {
      return 7;
    },
    discussionTopic() {
      return this.player.groupingType == 'value'
        ? 'the cause of poverty'
        : 'what makes a painting beautiful';
    },
    assessmentText() {
      return this.player.groupingType == 'value'
        ? 'attitude assessment'
        : 'personality assessment';
    },
    assessmentTextDetailed() {
      return this.player.groupingType == 'value'
        ? 'attitude assessment about the cause of poverty'
        : 'preference assessment of art styles';
    },
    groupingDescription() {
      return this.player.groupingType == 'value'
        ? 'statements you agree with more'
        : 'paintings you like better';
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
};
</script>

<style>
.center {
  display: block;
  margin-left: auto;
  margin-right: auto;
}
</style>
