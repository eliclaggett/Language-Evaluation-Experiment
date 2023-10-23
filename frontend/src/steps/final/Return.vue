<!-- Catch-all for task incompletion cases -->
<template>
  <v-container class="col-sm-6 push-10 text-center center">

    <!-- Failed reCAPTCHA -->
    <h3 v-if="player.gameStep === 'failedCaptcha'">
      <span v-if="player.platform == 'mturk'">Unfortunately, you failed the human verification. Please return the
        HIT.</span>
      <span v-if="player.platform == 'prolific'">Unfortunately, you failed the CAPTCHA. Please return this submission on
        Prolific by selecting the "stop without completing" button.</span>
    </h3>

    <!-- Failed consent form -->
    <h3 v-if="player.gameStep === 'failedConsent'">
      <span v-if="player.platform == 'mturk'">We cannot continue without your consent. Please return the HIT.</span>
      <span v-if="player.platform == 'prolific'">As you have indicated that you do not consent to participate in this
        study, please return this submission on Prolific by selecting the "stop
        without completing" button.</span>
    </h3>

    <!-- Failed comprehension test -->
    <div v-if="player.gameStep === 'failCheckUnderstanding'">
      <h3>
        Unfortunately, you answered one of the comprehension questions incorrectly. Please return the HIT.
      </h3>
      <p>
        We understand that accidents happen and we still hope to see you join our future HITs!
      </p>
    </div>

    <!-- We could not assign a suitable conversation partner -->
    <h3 v-else-if="player.gameStep === 'leftover'">
      <span v-if="player.platform == 'mturk'">We recruited too many participants. Please submit the HIT to be
        compensated for your time. We hope to see you again.</span>
      <span v-if="player.platform == 'prolific'">Study Completed</span>
    </h3>
    <p v-if="player.gameStep == 'leftover' && player.platform == 'prolific'">
      Thank you for your patience. We were unable to continue to the paired
      communication step of this study. Please submit the Completion Code below
      when you are ready:<br />
      <strong class="completioncode">{{ player.completionCode }}</strong>
    </p>

    <!-- Someone pressed the "report" button -->
    <div v-else-if="player.gameStep === 'reported'">
      <h3>
        The experiment has been cancelled prematurely because a participant
        clicked the report button.
      </h3>
      <p>
        You will still receive the base pay for this experiment of
        {{ player.basePay | money }}.
      </p>
    </div>

    <!-- Failed to press the "ready" button in time -->
    <h3 v-else-if="player.gameStep === 'notReady' && player.platform == 'prolific'
      ">
      Study Completed
    </h3>
    <p v-if="player.gameStep === 'notReady' && player.platform == 'prolific'">
      Thank you for your patience. We were unable to continue to the paired
      communication step of this study. Please submit the Completion Code below
      when you are ready:<br />
      <strong class="completioncode">{{ player.completionCode }}</strong>
    </p>
    <h3 v-if="player.gameStep === 'notReady' &&
      !player.groupingComplete &&
      player.platform == 'mturk'
      ">
      Unfortunately, the task started before you were able to complete the practice session and survey. Please submit the
      HIT to receive the base pay!
    </h3>
    <h3 v-else-if="player.gameStep === 'notReady' &&
      player.groupingComplete &&
      player.platform == 'mturk'
      ">
      Unfortunately, the task started before you were able to complete the practice session and survey. Please submit the
      HIT to receive the base pay!
    </h3>

    <!-- The participant joined too late -->
    <h3 v-else-if="player.gameStep === 'tooLate' && player.platform == 'mturk'">
      This task has already started. Please return the HIT
    </h3>
    <h3 v-else-if="player.gameStep === 'tooLate' && player.platform == 'prolific'">
      Unfortunately, this time-sensitive study has expired.<br />Please return
      this submission on Prolific by selecting the 'stop without completing'
      button.
    </h3>
    <p v-if="player.gameStep === 'tooLate' && player.platform == 'prolific'">
      <br />We hope to see you join our studies again!
    </p>

    <!-- Failed pre-evaluation -->
    <h3 v-if="player.gameStep === 'failedEvaluation'">
      Unfortunately, we are unable to continue to the HIT.
    </h3>
    <p v-if="player.gameStep === 'failedEvaluation' && player.samplingMode != 'mcq'
      ">
      We appreciate you accepting our HIT, but our chatbot found unusual answers
      to the pre-evaluation questions. Please return this HIT. The qualification
      criteria for this task is unique so please continue to join our other HITs
      in the future.
    </p>
    <p v-if="player.gameStep === 'failedEvaluation' && player.samplingMode == 'mcq'
      ">
      We appreciate you accepting our HIT, but your answers to one or more of
      the pre-evaluation questions were incorrect. Please return this HIT. The
      qualification criteria for this task is unique so please continue to join
      our other HITs in the future.
    </p>

    <!-- Participant became inactive -->
    <p v-if="player.gameStep === 'timeout'">
      Please submit the HIT now. You will receive the base payment of ${{
        player.basePay
      }}.
    </p>

    <!-- Show a submit button if the participant should be paid -->
    <div v-if="player.submit && player.platform == 'mturk'" v-html="player.submit" class="pt-6 pb-6 text-center center">
    </div>

    <!-- Encourage participants to contact us -->
    <p v-if="player.gameStep !== 'failedConsent' &&
      player.gameStep !== 'failedCaptcha' &&
      player.gameStep !== 'notReady' &&
      player.gameStep !== 'tooLate'
      " class="pt-4 selectable">
      Please tell us how to improve this task! Contact us at cmu.hcii.slab<span class="block-spam"
        aria-hidden="true">spam</span>@gmail.com
    </p>
  </v-container>
</template>

<script>
export default {
  name: 'ReturnStep',
  mounted() {

    // Sometimes button styles don't load automatically
    document.querySelectorAll('button').forEach((el) => {
      el.className =
        'v-btn v-btn--is-elevated v-btn--has-bg theme--light v-size--default center';
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
};
</script>
<style></style>
