<!-- Chatbot/MCQ Pre-evaluation -->
<!-- Four pre-evaluation (sampling) modes: chatbot, chatbot2, slor, mcq -->
<template>
  <v-col class="pt-6 center" cols="10" md="6">
    <h1 class="text-center">Pre-Evaluation</h1>
    <div v-if="player.samplingMode == 'chatbot' ||
      player.samplingMode == 'chatbot2' ||
      player.samplingMode == 'slor'
      ">
      <p class="text-center">
        Please complete this practice session with a chatbot partner. You will
        automatically move to the next step once you send enough messages.
      </p>
      <div>
        <v-alert outlined type="error" :class="allcaps">Please do not type in all caps.</v-alert>
        <v-alert type="success" id="verificationMsg" v-if="false">Submitted successfully! Please wait while we verify your
          answer</v-alert>
        <v-form id="verificationForm">
          <ChatWindow :player="player" :nlp="nlp" @onMsgSend="handleMsgSend" :participants="participants"
            msgSource="messagesEvaluation" />
        </v-form>
      </div>
    </div>
    <div v-else>
      <p class="text-center">
        This task requires communicating with a partner. Before starting the
        task, please read this example chat interaction and answer the questions
        below. In this chat, like the chat you will do later in the experiment,
        there are no instructions to debate.
      </p>
      <div id="chat-log" v-html="messageLog"></div>
      <v-radio-group v-for="q in questions" v-bind:key="q.key" :label="q.label" v-model="q.model">
        <template v-slot:label>
          <h3>{{ q.label }}</h3>
        </template>
        <v-radio v-bind:key="q.key + '-' + o.value" v-for="o in q.options" :label="o.label" :value="o.value"></v-radio>
      </v-radio-group>
      <div class="text-center">
        <v-btn @click="clickDone">Submit Answers</v-btn>
      </div>
    </div>
  </v-col>
</template>

<script>
import ChatWindow from '../components/ChatWindow.vue';

export default {
  name: 'PreEvalStep',
  components: { ChatWindow },
  props: {
    player: Object,
    nlp: Object,
  },
  data() {
    return {
      evalMessages: [
        {
          participantId: 1,
          content: 'I think the economy has gone down the drain.',
        },
        { participantId: 2, content: 'I do too but why do you think so?' },
        { participantId: 1, content: 'I think everything is worse now' },
        {
          participantId: 2,
          content:
            'We are supposed to debate.. so you need to explain what you mean. I think it went down the drain too but things are starting to get better.',
        },
      ],
      allcaps: 'd-none',
      q1: '',
      q2: '',
      questions: [
        {
          label: 'What does Participant 1 think?',
          key: 'q1',
          model: this.q1,
          options: [
            {
              label: 'Things are starting to get better',
              value: 'a1',
            },
            {
              label: 'No opinion',
              value: 'a2',
            },
            {
              label: 'They change their mind',
              value: 'a3',
            },
            {
              label: 'Everything is worse now',
              value: 'a4',
            },
          ],
        },
        {
          label: 'What is happening in this chat?',
          key: 'q2',
          model: this.q2,
          options: [
            {
              label:
                'The participants need to debate, but one of them is being unclear',
              value: 'a1',
            },
            {
              label:
                'The participants are discussing their opinions about travel',
              value: 'a2',
            },
            {
              label: "There's not enough information to decide",
              value: 'a3',
            },
            {
              label:
                "The participants don't need to debate. They misunderstood the instructions.",
              value: 'a4',
            },
          ],
        },
      ],
    };
  },

  computed: {
    participants() {
      // List of chat participants
      return [
        { id: -1, name: 'Chatbot', profilePicture: 'red' },
        { id: 999, name: 'Partner', profilePicture: 'red' },
        { id: this.player.chatId, name: 'Me', profilePicture: 'blue' },
      ];
    },
    messageLog() {
      // Message log for non-interactive pre-evaluation
      let html = '';
      if (this.evalMessages.length > 0) {
        for (let msg of this.evalMessages) {
          let isPartner = msg.participantId == 2;
          let playerName = 'Participant 1';
          if (msg.participantId == -1) playerName = 'Evaluator';
          if (isPartner) playerName = 'Participant 2';
          html +=
            '<div class="' +
            (isPartner ? 'partner' : '') +
            '"><p>' +
            playerName +
            '</p><p>' +
            msg.content +
            '</p></div>';
        }
      }
      return html;
    },
  },

  methods: {
    handleMsgSend(message) {
      // Do interactive pre-evaluation
      if (message.content == message.content.toUpperCase()) {
        this.allcaps = '';
      } else {
        this.allcaps = 'd-none';
      }
      window.Breadboard.send('sendMessageEvaluation', {
        message: JSON.stringify(message),
      });
    },
    validateMCQ() {
      // Do non-interactive pre-evaluation
      const data = {};
      this.questions.forEach((q) => {
        data[q.key] = q.model;
      });

      window.Breadboard.send('humanCheck', data);
    },
    clickDone() {
      // The "Done" button only appears for non-interactive pre-evaluation
      this.validateMCQ();
    },
  },
  mounted() {
    // Scroll to top of page
    window.scrollTo(0, 0);
  },
};
</script>
