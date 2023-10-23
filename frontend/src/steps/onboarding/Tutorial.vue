<!-- General tutorial for the experiment -->
<template>
  <v-col class="pt-4 center" cols="10" md="6">
    <h1 class="text-center">Tutorial</h1>
    <div v-if="step === 1" class="text--primary">
      <p class="pt-4">
        As you read through this short tutorial, please answer the three comprehension
        questions that look like this:
      </p>
      <v-alert color="primary" icon="mdi-exclamation-thick" type="success">If you do not answer all four questions
        correctly, we must ask you to return the HIT. In response to your feedback, we have rewritten the questions to
        make them more clear.</v-alert>
      <v-radio-group v-model="q1">
        <h3>
          Do you need to answer comprehension questions about this tutorial?
        </h3>
        <v-radio key="q1-1" label="Yes" value="1" />
        <v-radio key="q1-2" label="No" value="2" />
      </v-radio-group>
    </div>
    <div v-if="step === 2">
      <p class="pt-4">
        This HIT is split into two sections. Within each section there are a
        couple of tasks. You have a fixed amount of time to complete each
        section.
      </p>
      <h3>Section 1) Preparation</h3>
      <p class="pt-4">
        Since this HIT involves communication with a partner, we will do a
        practice session with a chatbot then survey your beliefs before
        assigning a partner. Your progress through Section 1 is displayed above.
      </p>
      <h3>Section 2) Main Task</h3>
      <p class="pt-4" v-if="player.platform == 'mturk'">
        We are studying how people communicate and perceive each other's
        beliefs. In this task, you will be paired with another Amazon Mechanical
        Turker and you will discuss with them about an assigned topic.
      </p>
      <p>
        Then, you will write a paragraph-long report
        <b>about your partner's</b> opinion on the conversation topic.
      </p>
      <p>
        You will be given a {{ player.completionBonus | money }}-{{
          player.mutualCompletionBonus | money
        }}
        bonus for completing this task.
      </p>
      <p>
        If we recruit an odd number of participants, we may not be able to
        partner you. If so, we will stop the HIT prematurely and give you the
        base pay.
      </p>
      <v-radio-group v-model="q2">
        <h3>What does the main task involve?</h3>
        <v-radio v-for="a in q2Answers" :key="a.value + '-q2'" :label="a.label" :value="a.value" />
      </v-radio-group>
      <br />
    </div>
    <div v-if="step === 3" class="text--primary">
      <v-col class="center" xl="4" lg="6" md="10" sm="6">
        <v-img :src="require(`../assets/undraw_forms_re_pkrt.svg`)"></v-img>
      </v-col>
      <h2 class="text-center">How we'll make pairs</h2>
      <p class="pt-4">
        Using the results of the survey you will take shortly, we will group
        participants based on their beliefs. People in different groups will have different beliefs. People in the same
        group will have similar beliefs. After creating groups, we'll form some pairs within
        each group and some pairs between groups. After you are assigned a
        partner, you will have {{ player.chatTime }} minutes to discuss your
        thoughts.
      </p>
      <v-radio-group v-model="q3">
        <h3>If my partner is from a different group, they likely have...</h3>
        <v-radio v-for="a in q3Answers" :key="a.value + '-q3'" :label="a.label" :value="a.value" />
      </v-radio-group>
      <p>
        The survey consists of seven statements which you will react to using
        the buttons below each statement. Please answer each question carefully
        because the next steps of the task will depend on your previous answers.
        You may click anywhere on the option you like best to select it:
      </p>
      <v-col class="center" cols="4">
        <PairItem itemValue="Example button" itemType="text" @chooseItem="chooseItem(0)" />
      </v-col>
      <p class="mt-8 mb-6 text-center">
        After making a selection, you will automatically move to the next
        question.
      </p>
    </div>
    <div v-if="step === 4" class="text--primary">
      <v-col class="center" xl="4" lg="6" md="10" sm="6">
        <v-img :src="require(`../assets/undraw_treasure_of-9-i.svg`)"></v-img>
      </v-col>
      <h2 class="text-center">Completion Bonus</h2>
      <p class="pt-4">
        There are two bonus options for completing this HIT which will be added
        to the base pay of {{ player.basePay | money }}. After chatting with
        your partner about the assigned topic, you will have
        {{ player.cooperationDiscussionTime }} minutes to discuss which bonus
        option you'd like.
      </p>
      <h3>Default Bonus</h3>
      <p>
        The default completion bonus is {{ player.completionBonus | money }}.
      </p>
      <h3>Extra Bonus</h3>
      <p>
        If you and your partner agree to the extra bonus option, your completion
        bonus will be {{ player.mutualCompletionBonus | money }}.
        But if only one of you selects this option, that person would receive no bonus.
      </p>
      <v-radio-group v-model="q4">
        <h3>
          What is the total pay of this HIT if you and your partner select the
          extra bonus? (total pay = base pay + bonus)
        </h3>
        <v-radio v-for="a in q4Answers" :key="a.value + '-q4'" :label="a.label" :value="a.value" />
      </v-radio-group>
      <v-alert type="success" outlined v-if="passCheck">Correct! Let's get started.</v-alert>
      <v-alert type="error" outlined v-if="showHint">Oops! That's not right. Please review the explanation and try
        again.</v-alert>
      <br />
    </div>
    <div class="text-center">
      <v-btn @click="clickBack" :disabled="step < 2" class="mr-2">Back</v-btn>
      <v-btn @click="clickNext" v-if="step < maxSteps" :disabled="nextDisabled">Next</v-btn>
      <v-btn @click="clickDone" v-else :disabled="nextDisabled">Continue</v-btn>
    </div>
    <p class="warningtext" v-if="nextDisabled">
      Please answer all the questions on this page
    </p>
  </v-col>
</template>

<script>
/* global Breadboard */
import PairItem from '../components/PairItem';

const shuffleArray = (array) => {
  for (let i = array.length - 1; i > 0; i--) {
    const j = Math.floor(Math.random() * (i + 1));
    const temp = array[i];
    array[i] = array[j];
    array[j] = temp;
  }
};

export default {
  name: 'TutorialStep',
  props: {
    player: Object,
  },

  components: {
    PairItem,
  },

  data() {
    return {
      // Tutorial steps
      step: 1,
      passCheck: false,
      showHint: false, // Prolific: Instead of failing a participant for answering incorrectly, show a hint and allow them to answer again
      maxSteps: 4,
      q1: '1',
      q2: '-1',
      q3: '-1',
      q4: '-1',
      q2Answers: [
        { label: 'A practice chat session and a survey', value: 1 },
        {
          label:
            'Discussion with a partner and writing a report about their opinions',
          value: 2,
        },
        { label: 'None of these options', value: 3 },
      ],
      q3Answers: [
        { label: 'Similar opinions to me', value: 1 },
        { label: 'Different opinions from me', value: 2 },
      ],
      q4Order: [0, 1, 2, 3],
    };
  },

  methods: {
    clickNext() {
      this.step++;
      document.body.scrollTop = 0; // For Safari
      document.documentElement.scrollTop = 0;
    },
    clickBack() {
      this.step--;
    },
    checkUnderstanding() {
      const data = [this.q1, this.q2, this.q3, this.q4];
      Breadboard.send('checkUnderstanding', data);
    },
    clickDone() {
      this.checkUnderstanding();
      Breadboard.send('completeTutorial');
    },
    chooseItem(ev) {
      ev;
    },
    chooseTrueItem(ev) {
      if (ev == 0) {
        this.passCheck = true;
        this.showHint = false;
      } else {
        if (this.player.platform == 'mturk') {
          Breadboard.send('failCheckUnderstanding');
        } else {
          this.showHint = true;
        }
      }
    },
  },

  computed: {
    q4Answers() {
      let basePay = this.player.basePay.toLocaleString('en-US', {
        style: 'currency',
        currency: 'USD',
      });
      let completionBonus = this.player.mutualCompletionBonus.toLocaleString(
        'en-US',
        { style: 'currency', currency: 'USD' }
      );
      let total = (
        this.player.basePay + this.player.mutualCompletionBonus
      ).toLocaleString('en-US', { style: 'currency', currency: 'USD' });
      let answers = [
        { label: basePay, value: 1 },
        { label: completionBonus, value: 2 },
        { label: total, value: 3 },
        { label: '$0.00', value: 4 },
      ];

      const sortedAnswers = [
        answers[this.q4Order[0]],
        answers[this.q4Order[1]],
        answers[this.q4Order[2]],
        answers[this.q4Order[3]],
      ];

      return sortedAnswers;
    },
    nextDisabled() {
      if (this.step == 1) return this.q1 == '-1';
      if (this.step == 2) return this.q2 == '-1';
      if (this.step == 3) return this.q3 == '-1';
      if (this.step == 4) return this.q4 == '-1';
      return false;
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

  created() {
    shuffleArray(this.q2Answers);
    shuffleArray(this.q3Answers);
    shuffleArray(this.q4Order);
  },
};
</script>

<style>
.warningtext {
  margin-top: 1em;
  color: rgb(198, 52, 52);
  opacity: 0.7;
  text-align: center;
}
</style>
