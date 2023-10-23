<!-- Experiment consent form -->
<template>
  <v-col class="push-10 center" cols="10" md="6">
    <div class="text--primary">
      <h1 class="text-center">Online Consent (STUDY2021_00000280)</h1>
      <h3 class="pt-4 pb-4">
        Please read the following information. When you give your consent, you
        can proceed to the task.
        <span v-if="player.platform == 'prolific'">In this consent form, we refer to the current study as a Human
          Intelligence Task (HIT).</span>
      </h3>
      <p>
        This game is part of a research study conducted by Carnegie Mellon
        University and is funded by the National Science Foundation.
      </p>
      <strong>Purpose</strong>
      <p>
        The purpose of the research is to examine how people communicate and
        cooperate with others.
      </p>
      <strong>Procedures</strong>
      <p>
        You will be expected to work on this task with other players. This task
        consists of two stages. In the first stage, you will be expected to
        communicate with one or more other players regarding assessments on
        real-world topics. In the second stage, you will be expected to play a
        decision-making game with the same counterparts.
      </p>
      <p>
        Tutorial: before the task, you will complete a tutorial on how to do the
        task. After the tutorial, you will be asked a few questions about your
        understanding of the game. If you do not answer the questions correctly,
        you will still receive the base pay of {{ basePay | money }}, but you
        are not eligible to join the task and the HIT again.
      </p>
      <p>
        For those participants who are eligible to participate in the actual
        game (due to answering the tutorial questions correctly), we may inform
        you that we cannot use you for the game at that moment either because A)
        we have more people than we need for a group at that time, or B) there
        are not enough eligible participants to form a group at that time, so
        the game will not happen then. Participants in both A and B will be paid
        the base pay (as mentioned above) and may accept the HIT again in the
        future.
      </p>
      <p>
        Whether you fail the comprehension test or are not found a match, your
        non-participation in the actual game will not result in a negative
        rating on {{ player.platform | platform }}.
      </p>
      <strong>Participant requirements</strong>
      <p>
        Participation in this study is limited to individuals age 18 and older.
        Only those located in the US at the time of their participation are
        allowed to participate in this study.
      </p>
      <strong>Risks</strong>
      <p>
        We prohibit participants to send unkind or inappropriate messages during
        this task. When you send such messages, we will report your violation to
        {{ player.platform | platform }}. Although we conduct a careful
        screening process, you might receive unkind or inappropriate messages
        from another participant.
      </p>
      <strong>Benefits</strong>
      <p>
        You may learn about how to communicate with others. Also, the knowledge
        received may be useful to others and to the scientific community by
        clarifying how people communicate and cooperate with each other.
      </p>
      <strong>Compensation & Costs</strong>
      <p>
        You will be compensated the base pay of {{ basePay | money }} for
        beginning the study and completing the initial tutorial section. If you
        are deemed eligible to participate in the actual game (by answering the
        tutorial questions correctly), and you complete the game, you will also
        receive a completion bonus of {{ completionBonus | money }}. In
        addition, those who participate in the game may earn a bonus up to
        {{ performanceBonus | money }} in a performance bonus based on the
        decisions they make while playing the game.
      </p>
      <p>There will be no cost to you if you participate in this study.</p>
      <strong>Confidentiality</strong>
      <p>
        The data captured for the research does not include any personally
        identifiable information about you. The sponsor of this research, the
        National Science Foundation, may have access to the research record. The
        study will utilize {{ player.platform | platform }} to conduct this
        research. These companies are not owned by CMU. The companies will have
        access to the research data that you produce and any identifiable
        information that you share with the company while using its product.
        Please note that Carnegie Mellon does not control the Terms and
        Conditions of the company or how they will use any information that they
        collect.
      </p>
      <strong>Future use of information</strong>
      <p>
        We may use the anonymous data for our future research studies, or we may
        distribute the data to other researchers for their research studies. We
        would do this without getting additional informed consent from you (or
        your legally authorized representative). Sharing of data with other
        researchers will only be done in such a manner that you will not be
        identified.
      </p>
      <strong>Right to ask questions & Contact Information</strong>
      <p>
        If you have any questions about this study, you should feel free to ask
        them by contacting the Principal Investigator now at Hirokazu Shirado
        (shirado@cmu.edu). If you have questions later, desire additional
        information, or wish to withdraw your participation please contact the
        Principal Investigator by e-mail in accordance with the contact
        information listed above.
      </p>
      <p>
        If you have questions pertaining to your rights as a research
        participant; or to report concerns to this study, you should contact the
        Office of Research integrity and Compliance at Carnegie Mellon
        University. Email: irb-review@andrew.cmu.edu . Phone: 412-268-4721.
      </p>
      <strong>Voluntary participation</strong>
      <p>
        Your participation in this research is voluntary. You may discontinue
        participation at any time during the research activity. You may print a
        copy of this consent form for your records.
      </p>

      <v-radio-group v-for="q in questions" v-bind:key="q.key" :label="q.label" v-model="q.model">
        <template v-slot:label>
          <h3 class="mb-0">{{ q.label }}</h3>
        </template>
        <v-radio v-bind:key="q.key + '-' + o.value" v-for="o in q.options" :label="o.label" :value="o.value"></v-radio>
      </v-radio-group>
      <!-- <v-btn :disabled="!formValid" @click="submitForm">Next</v-btn> -->
    </div>
    <div class="text-center">
      <v-btn @click="clickDone">Start Practice Session</v-btn>
    </div>
  </v-col>
</template>

<script>
/* global Breadboard */
export default {
  name: 'ConsentStep',
  props: {
    player: Object,
  },

  components: {},

  data() {
    return {
      // Consent form questions
      q1: '',
      q2: '',
      q3: '',
      q4: '',
      questions: [
        {
          label: 'I am age 18 or older. ',
          key: 'q1',
          model: this.q1,
          options: [
            {
              label: 'Yes',
              value: 'yes',
            },
            {
              label: 'No',
              value: 'no',
            },
          ],
        },
        {
          label: 'I have read and understand the information above.',
          key: 'q2',
          model: this.q2,
          options: [
            {
              label: 'Yes',
              value: 'yes',
            },
            {
              label: 'No',
              value: 'no',
            },
          ],
        },
        {
          label:
            'I have reviewed the eligibility requirements listed in the Participant Requirements section of this consent form and certify that I am eligible to participate in this research, to the best of my knowledge.',
          key: 'q3',
          model: this.q3,
          options: [
            {
              label: 'Yes',
              value: 'yes',
            },
            {
              label: 'No',
              value: 'no',
            },
          ],
        },
        {
          label:
            'I want to participate in this research and continue with the game.',
          key: 'q4',
          model: this.q4,
          options: [
            {
              label: 'Yes',
              value: 'yes',
            },
            {
              label: 'No',
              value: 'no',
            },
          ],
        },
      ],
    };
  },

  methods: {
    validateConsentForm() {
      const data = {};
      this.questions.forEach((q) => {
        data[q.key] = q.model;
      });

      Breadboard.send('consent', data);
    },
    clickDone() {
      this.validateConsentForm();
    },
  },

  computed: {
    basePay() {
      return this.player.platform == 'prolific'
        ? this.player.prolificTask1Pay
        : this.player.basePay;
    },
    completionBonus() {
      return this.player.platform == 'prolific'
        ? this.player.completionBonus + this.player.prolificTask2Pay
        : this.player.completionBonus;
    },
    performanceBonus() {
      return (
        this.player.completionBonus * this.player.multiplier -
        this.player.completionBonus
      );
    },
  },
  filters: {
    money(val) {
      return val.toLocaleString('en-US', {
        style: 'currency',
        currency: 'USD',
      });
    },
    platform(val) {
      return val == 'mturk' ? 'Amazon Mechanical Turk' : 'Prolific';
    },
  }
};
</script>

<style></style>
