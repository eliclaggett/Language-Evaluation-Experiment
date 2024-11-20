<!--
Filename: CheapTalk.vue
Author: Elijah Claggett
Description: Main chat step of the experiment
-->
<template>
  <v-layout fill-height column justify-start align-center class="mt-4">
    <v-row class="flex-grow-0">
      <v-col align="center">
        <h2>{{ remainingTime }}</h2>
      </v-col>
    </v-row>

    <v-alert outlined type="error" v-if="player.profane"
      >Do not send profane messages. Doing so will result in being blacklisted
      from AMT.</v-alert
    >
    <v-alert
      outlined
      type="warning"
      v-if="msgTimer <= player.timeout - player.timeoutWarning && msgTimer > 0"
      >Inactivity warning. You will be removed from the study if you do not send
      a message in {{ msgTimer }} seconds.</v-alert
    >
    <v-alert outlined type="error" v-if="msgTimer == 0"
      >Inactivity warning. You are about to be removed from this study due to
      inactivity.</v-alert
    >
    <v-row dense class="col-sm-12 justify-center">
      <v-col lg="3" sm="6">
        <ChatWindow
          :player="player"
          :nlp="nlp"
          @onMsgSend="handleMsgSend"
          @onType="handleType"
          :participants="participants"
          msgSource="messagesChat"
        />
        <v-layout justify-center column align-center class="pa-6">
          <div class="text-center">
            <p class="text-center red--text d-none" id="reportConfirmText">
              Are you sure? This will end the task.
            </p>
            <v-btn
              @click="clickCancel"
              outlined
              color="#555"
              class="d-none"
              id="cancelBtn"
              >Cancel</v-btn
            >
            <v-btn @click="clickReport" outlined color="error" id="reportBtn"
              >Report Partner</v-btn
            >
          </div>
        </v-layout>
      </v-col>
      <v-col lg="3" sm="6" class="pa-6">
        <h2>Note:</h2>
        <p>
          <!-- Inactivity warning (Unused) -->
          <v-alert outlined type="warning" v-if="msgTimer <= 25 && msgTimer > 0"
            >Inactivity warning. You will be removed from the study if you do
            not send a message in {{ msgTimer }} seconds.</v-alert
          >
          <v-alert outlined type="error" v-if="msgTimer == 0"
            >Inactivity warning. You are about to be removed from this study due
            to inactivity.</v-alert
          >
        </p>
        <p class="card">
          Talk continuously with your partner. It's okay if the conversation
          derails, but please do not end prematurely.
        </p>
        <p class="card">
          Messages have a 140 character limit. Please split longer messages into
          multiple shorter ones.
        </p>
      </v-col>
    </v-row>
  </v-layout>
</template>

<script>
import ChatWindow from '../../components/ChatWindow';
import * as Filter from 'bad-words';

// Profanity filter
var filter = new Filter();
var removeWords = [
  'screw',
  'knob',
  'wop*',
  'poop',
  'penis',
  'screwing',
  'breasts',
  'porn',
  'semen',
  'sex',
];
filter.removeWords(...removeWords);

export default {
  name: 'CheapTalkStep',
  components: { ChatWindow },
  props: {
    player: Object,
    nlp: Object,
  },
  mounted() {},
  methods: {
    clickReport() {
      // Clicking the report button prompts to confirm the action before ending the experiment
      if (!this.reportConfirmed) {
        document.querySelector('#reportBtn').innerText = 'Confirm Report';
      } else {
        window.Breadboard.send('clickReport');
        document.querySelector('#reportBtn').innerText = 'Report Partner';
      }
      this.reportConfirmed = !this.reportConfirmed;
      document.querySelector('#reportConfirmText').classList.toggle('d-none');
      document.querySelector('#cancelBtn').classList.toggle('d-none');
    },
    clickCancel() {
      // Cancel the report
      this.reportConfirmed = false;
      document.querySelector('#reportConfirmText').classList.add('d-none');
      document.querySelector('#cancelBtn').classList.toggle('d-none');
      document.querySelector('#reportBtn').innerText = 'Report Partner';
    },
    handleMsgSend(message) {
      // Check for profanity before sending a message
      let profane = filter.isProfane(message.content);
      if (profane) {
        window.Breadboard.send('profane', { message: JSON.stringify(message) });
      } else {
        window.Breadboard.send('chat', { message: JSON.stringify(message) });
        // Track the deleted characters
        window.Breadboard.send('charsSinceLastChat', {
          message: this.charsSinceLastMsg,
        });
        this.charsSinceLastMsg = '';
      }

      this.lastMsgTime = new Date();
    },
    handleType(data) {
      // Track the deleted characters
      if (data.data) {
        this.charsSinceLastMsg += data.data;
      }
    },
  },
  data: () => {
    return {
      report: '',
      reportSuccess: false,
      reportConfirmed: false,
      lastMsgTime: '',
      charsSinceLastMsg: '',
    };
  },
  computed: {
    participants() {
      let neighbor = this.player.neighborNodes[0];
      return [
        {
          id: this.player.chatId,
          name: 'Me',
          group: this.player.groupId,
          groupName: this.player.groupName,
        },
        {
          id: neighbor.chatId,
          name: 'Partner',
          group: neighbor.groupId,
          groupName: neighbor.groupName,
        },
        { id: -1, name: 'Chatbot' },
      ];
    },
    remainingTime() {
      // Calculate remaining time until next step
      var remainingText = ' until switching topics';
      var startTime = 0;
      var totalTime = 0;
      if (this.player.cooperationDiscussionStartTime) {
        startTime = this.player.cooperationDiscussionStartTime;
        totalTime = this.player.cooperationDiscussionTime;
        remainingText = ' remaining';
      } else if (this.player.chatStartTime) {
        startTime = this.player.chatStartTime;
        totalTime = this.player.chatTime;
      }

      if (startTime) {
        var curTime = Date.now() + this.player.utcOffset;
        var endTime = (startTime + totalTime * 60) * 1000;

        var diff = (endTime - curTime) / 1000;
        if (diff < 60) {
          return 'less than 1 min' + remainingText;
        } else if (diff < 90) {
          return '1 min' + remainingText;
        } else {
          return Math.round(diff / 60) + ' mins' + remainingText;
        }
      } else {
        return '';
      }
    },
    msgTimer() {
      // Inactivity timer (Unused)
      return 999;
    },
  },
  watch: {
    player(player) {
      // Track inactivity
      if (player.chatStartTime && this.lastMsgTime == '') {
        this.lastMsgTime = new Date(player.chatStartTime * 1000);
      }
    },
  },
};
</script>

<style lang="scss">
.participant-label {
  margin-left: 1rem;
}

.participant-label p {
  margin: 0;
}

#cancelBtn {
  margin-right: 1em;
}

.participant-label p:first-child {
  font-weight: bold;
}

.chat-participants .player-container:not(:last-child):after {
  top: -0.25rem;
}
</style>
