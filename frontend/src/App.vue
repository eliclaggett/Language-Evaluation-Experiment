<!-- Main app -->
<template>
  <v-app id="app">
    <!-- Top bar -->
    <v-app-bar dense app :class="player.neighborNodes && player.neighborNodes.length > 0
      ? 'no-shadow'
      : ''
      ">
      <v-spacer />
      <v-app-bar-title>Communication Task</v-app-bar-title>
      <v-spacer />
    </v-app-bar>
    <div id="partnerList" v-if="player.displayNeighborNodes &&
      player.neighborNodes &&
      player.neighborNodes.length > 0
      ">
      <span v-if="player.neighborNodes[0].groupId != player.groupId">Your partner is from a different group</span>
      <span v-if="player.neighborNodes[0].groupId == player.groupId">Your partner is from the same group</span>
    </div>
    <v-main v-if="!browserCompatible">
      <h2 class="pa-10 text-center">
        Your browser is incompatible. Please return the study.
      </h2>
    </v-main>

    <!-- Main app -->
    <v-main :class="mainClass" v-else-if="browserCompatible">
      <!-- Experiment progress -->
      <v-progress-linear v-if="loading" indeterminate />
      <PlayerTimers :player="player" v-if="loading === false" />
      <v-col :class="displaySec1Progress" cols="10" md="6">
        <div :class="pi1">
          <div class="number">1&#12297;</div>
          <div class="description">
            <strong>Tutorial</strong>
            <span>~3 min</span>
          </div>
        </div>
        <div :class="pi2">
          <div class="number">2&#12297;</div>
          <div class="description">
            <strong>Practice Session</strong>
            <span>~5 min</span>
          </div>
        </div>
        <div :class="pi3">
          <div class="number">3&#12297;</div>
          <div class="description">
            <strong>Survey</strong>
            <span>~1 min</span>
          </div>
        </div>
      </v-col>
      <v-col :class="displaySec2Progress" cols="10" md="6">
        <div :class="mi1">
          <div class="number">1&#12297;</div>
          <div class="description">
            <strong>Chat</strong>
            <span>12 min</span>
          </div>
        </div>
        <div :class="mi2">
          <div class="number">2&#12297;</div>
          <div class="description">
            <strong>Write Report</strong>
            <span>5 min</span>
          </div>
        </div>
        <div :class="mi3">
          <div class="number">3&#12297;</div>
          <div class="description">
            <strong>Confirm Bonus</strong>
            <span>1 min</span>
          </div>
        </div>
      </v-col>

      <!-- Experiment steps -->
      <RecaptchaStep v-if="player.gameStep === 'recaptcha' && loading === false" :player="player" />
      <ConsentStep v-if="player.gameStep === 'consent' && loading === false" :player="player" />
      <PreEvalStep v-if="player.gameStep === 'preEval' && loading === false" :player="player" :nlp="nlp" />
      <TutorialStep v-if="player.gameStep === 'tutorial' && loading === false" :player="player" />
      <GroupingStep v-if="player.gameStep === 'grouping' && loading === false" :player="player" />
      <Tutorial2Step v-if="player.gameStep === 'tutorial2' && loading === false" :player="player" />
      <CheapTalkStep v-if="player.gameStep === 'mainChat' && loading === false" :player="player" :nlp="nlp" />
      <CooperationStep v-if="player.gameStep === 'game' && loading === false" :player="player" />
      <SurveyStep v-if="player.gameStep === 'survey' && loading === false" :player="player" />
      <PartnerAnswerStep v-if="player.gameStep === 'gradePartnerAnswer' && loading === false" :player="player" />
      <EndStep v-if="player.gameStep === 'end' && loading === false" :player="player" />
      <ReturnStep v-else-if="player.gameStep === 'failedCaptcha' ||
        player.gameStep === 'failedConsent' ||
        player.gameStep === 'leftover' ||
        player.gameStep === 'reported' ||
        player.gameStep == 'notReady' ||
        player.gameStep == 'tooLate' ||
        player.gameStep == 'failedEvaluation' ||
        player.gameStep == 'timeout' ||
        player.gameStep == 'failCheckUnderstanding'
        " :player="player" />
    </v-main>
  </v-app>
</template>

<script>
/* global Breadboard */

// Experiment steps
import RecaptchaStep from './steps/onboarding/Recaptcha.vue';
import ConsentStep from './steps/onboarding/Consent.vue';
import PreEvalStep from './steps/onboarding/PreEval.vue';
import TutorialStep from './steps/onboarding/Tutorial.vue';
import GroupingStep from './steps/onboarding/Grouping.vue';
import Tutorial2Step from './steps/onboarding/Tutorial2.vue';

import CheapTalkStep from './steps/main_task/CheapTalk.vue';
import CooperationStep from './steps/main_task/CooperationDecision.vue';

import SurveyStep from './steps/final/Survey.vue';
import PartnerAnswerStep from './steps/final/PartnerAnswer.vue';
import EndStep from './steps/final/End.vue';
import ReturnStep from './steps/final/Return.vue';

// External imports
import { PlayerTimers } from '@human-nature-lab/breadboard-client';

export default {
  name: 'Experiment_Platform',
  components: {
    PlayerTimers,
    RecaptchaStep,
    ConsentStep,
    PreEvalStep,
    TutorialStep,
    GroupingStep,
    Tutorial2Step,
    CheapTalkStep,
    CooperationStep,
    SurveyStep,
    PartnerAnswerStep,
    EndStep,
    ReturnStep,
  },
  data() {
    return {
      loading: true,
      player: {},
      nlp: {},
      playerState: '',
      connectedToNLP: false,
      sentEvalScoreRequest: false,
    };
  },

  created() {
    const queryString = window.location.search;
    const urlParams = new URLSearchParams(queryString);
    window.prolificId = urlParams.get('PROLIFIC_PID');
    window.studyId = urlParams.get('STUDY_ID');
    window.sessionId = urlParams.get('SESSION_ID');

    Breadboard.on('player', this.updatePlayer);
  },

  beforeDestroy() {
    Breadboard.off('player', this.updatePlayer);
  },

  methods: {
    connectToNLP() {
      // Establish a separate connection with an NLP server that analyzes message content
      console.log('Attempting to establish connection with NLP server');
      if (this.player.nlpPort === undefined) {
        console.log('NLP: Port is undefined');
        return;
      }

      let nlpServerURL = '';
      if (window.location.protocol === 'http:') {
        nlpServerURL =
          'ws://' + window.location.hostname + ':' + this.player.nlpPort;
      } else {
        nlpServerURL =
          'wss://' + window.location.hostname + ':' + this.player.nlpPort;
      }
      console.log('NLP server URL: ' + nlpServerURL)

      window.nlpServer = new WebSocket(nlpServerURL);

      window.nlpServer.onopen = () => {
        // Yay! Successful connection
        this.connectedToNLP = true;
        console.log('Connection successfully established with NLP server');
        // Occassionally ping the server to prevent the connection from closing
        window.nlpInterval = setInterval(() => {
          if (window.nlpServer instanceof WebSocket) {
            window.nlpServer.send('{"command": "ping"}');
          } else {
            clearInterval(window.nlpInterval);
          }
        }, 20000);

        // Register the participant's ID and their partner's ID with the server
        if (this.player.gameStep == 'mainChat') {
          let req = {
            command: 'createPair',
            id1: this.player.chatId,
            id2: this.player.neighborNodes[0].chatId,
            mode: this.player.interventionMode,
          };
          window.nlpServer.send(JSON.stringify(req));
        } else {
          let req = {
            command: 'registerParticipant',
            id: this.player.chatId,
          };
          window.nlpServer.send(JSON.stringify(req));
        }
      };

      window.nlpServer.onclose = () => {
        this.connectedToNLP = false;
        this.connectToNLP(); // Automatically reconnect
      };

      window.nlpServer.onmessage = (event) => {
        // We received a message from the NLP server
        // Reply is in event.data
        let nlpReply = JSON.parse(event.data);

        if (nlpReply.suggestion) {
          // The message is a reply suggestion
          // Just in case
          if (!('id' in nlpReply)) {
            nlpReply.id = '';
          }

          window.Breadboard.send('receiveSuggestion', { id: nlpReply.id });
          this.nlp = nlpReply;
        } else if (nlpReply.reply) {
          // The message is a chatbot reply
          // Just in case
          if (!('id' in nlpReply)) {
            nlpReply.id = '';
          }

          window.Breadboard.send('preEvalReply', {
            id: nlpReply.id,
            msg: nlpReply.reply,
          });
          this.nlp = nlpReply;
        } else if (nlpReply.score) {
          // The messages is the language style evaluation score of the participant
          window.Breadboard.send('evalScore', { score: nlpReply.score });
        }
      };
    },

    updatePlayer(player) {
      // Breadboard function that runs when a participant's attributes are updated
      if (this.loading) {
        this.playerState = player.gameStep;
        this.loading = false;

        // Register the prolific ID when they join
        if (player.gameStep == 'recaptcha') {
          Breadboard.send('registerProlific', {
            prolificId: window.prolificId,
            studyId: window.studyId,
            sessionId: window.sessionId,
          });
        }
      }

      this.player = player;
      if (
        this.player &&
        this.player.passEval == false &&
        this.player.readyToScoreEvaluation == true &&
        this.sentEvalScoreRequest == false
      ) {
        this.sentEvalScoreRequest = true;
        // This participant is now ready to be scored, send a message to the NLP server
        let req = {
          command: 'scoreEvaluation',
          id: this.player.chatId,
          msgs: this.player.messagesEvaluation,
        };

        if (window.nlpServer) {
          window.nlpServer.send(JSON.stringify(req));
        }
      }

      
      // Connect to the NLP server
      if (!this.connectedToNLP) {
        this.connectToNLP();
      } else {
        this.connectedToNLP = true;
      }

      if (this.playerState != player.gameStep) {
        // Handle experiment step change
        if (
          player.gameStep == 'mainChat' &&
          player.interventionMode != 'none'
        ) {
          // Register partner with NLP server
          let req = {
            command: 'createPair',
            id1: this.player.chatId,
            id2: this.player.neighborNodes[0].chatId,
            mode: this.player.interventionMode,
          };
          let inter = setInterval(() => {
            if (window.nlpServer instanceof WebSocket) {
              window.nlpServer.send(JSON.stringify(req));
              clearInterval(inter);
            }
          }, 200);
        }
      }
      this.playerState = player.gameStep;
    },
  },

  computed: {
    browserCompatible() {
      // Detect older browsers which may cause errors for participants
      return Promise && 'any' in Promise;
    },
    mainClass() {
      // Conditional UI styles according to experiment step and NLP server connection status
      let cls = '';
      if (!this.connectedToNLP) {
        if (!(this.player && this.player.interventionMode == 'none')) {
          cls += 'notConnected ';
        }
      }
      if (this.player.neighborNodes && this.player.neighborNodes.length > 0) {
        cls += 'push-down ';
      }
      return cls;
    },
    displaySec1Progress() {
      // Display progress bar for experiment stage 1
      let thisClass = 'progress-top center mt-4';
      if (
        !['tutorial', 'consent', 'preEval', 'grouping'].includes(
          this.player.gameStep
        )
      ) {
        thisClass = 'd-none';
      }
      return thisClass;
    },
    displaySec2Progress() {
      // Display progress bar for experiment stage 3
      let thisClass = 'progress-top center';
      if (!['mainChat', 'game', 'survey'].includes(this.player.gameStep)) {
        thisClass = 'd-none';
      }
      return thisClass;
    },
    pi1() {
      // Conditional styling for progress bar item (stage 1)
      return this.player.gameStep == 'tutorial' ||
        this.player.gameStep == 'consent'
        ? 'active progress-itm'
        : 'progress-itm';
    },
    pi2() {
      // Conditional styling for progress bar item (stage 1)
      return this.player.gameStep == 'preEval'
        ? 'active progress-itm'
        : 'progress-itm';
    },
    pi3() {
      // Conditional styling for progress bar item (stage 1)
      return this.player.gameStep == 'grouping'
        ? 'active progress-itm'
        : 'progress-itm';
    },
    mi1() {
      // Conditional styling for progress bar item (stage 3)
      return this.player.gameStep == 'mainChat' ||
        this.player.gameStep == 'consent'
        ? 'active progress-itm'
        : 'progress-itm';
    },
    mi2() {
      // Conditional styling for progress bar item (stage 3)
      return this.player.gameStep == 'survey' ||
        this.player.gameStep == 'consent'
        ? 'active progress-itm'
        : 'progress-itm';
    },
    mi3() {
      // Conditional styling for progress bar item (stage 3)
      return this.player.gameStep == 'game' || this.player.gameStep == 'consent'
        ? 'active progress-itm'
        : 'progress-itm';
    },
  },
  mounted() {
    // Load variables from json file (shared with Breadboard and NLP server)
    const texts = require("@/assets/texts.json");
    window.texts = texts;

    // Load external scripts
    let srcs = ['https://use.fontawesome.com/releases/v5.15.4/js/all.js'];
    srcs.forEach((script) => {
      let tag = document.createElement('script');
      tag.setAttribute('src', script);
      tag.setAttribute('defer', 'defer');
      document.head.appendChild(tag);
    });
  },
};
</script>

<style>
:root {
  --bg-color: #fff;
  --text-color: #444;
  --theme-primary: #6c63ff;
  --theme-secondary: #3a34b3;
  --theme-accent: #ffad7d;
  --theme-accent2: #34b3b2;
}

@import url('https://fonts.googleapis.com/css2?family=Poppins:ital,wght@0,200;0,400;0,600;1,400&family=Quicksand:wght@300;400;600&family=Roboto:wght@400;500&display=swap');

/* Fix Breadboard CSS */
.v-application .v-progress-linear .info {
  background-color: var(--theme-primary) !important;
}

.notConnected {
  cursor: initial !important;
  position: relative;
}

.notConnected::after {
  position: fixed;
  top: 0;
  left: 0;
  width: 100vw;
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  text-align: center;
  content: 'Reconnecting to server... Please refresh the page if the connection takes longer than a few seconds.';
  background: rgba(255, 255, 255, 0.9);
  backdrop-filter: blur(1em);
}

.v-btn {
  text-transform: none;
  letter-spacing: normal;
  font-size: 0.95rem !important;
}

.v-input--radio-group__input h3 {
  margin-bottom: 1rem;
}

/* Custom CSS */
.v-application {
  font-family: 'Poppins', Helvetica, sans-serif;
  background: var(--bg-color);
  color: var(--text-color);
}

h1,
h2,
h3,
h4,
p,
span,
a,
canvas,
div {
  user-select: none;
}

.center {
  margin-left: auto;
  margin-right: auto;
}

.labeled-icon {
  display: flex;
  justify-content: center;
  align-items: center;
  margin: 1em 0;
}

.labeled-icon .v-image {
  border-radius: 100%;
  max-width: 2em;
  height: 2em;
  box-sizing: border-box;
  margin-right: 0.5em;
}

.quick-chat-container div {
  user-select: text !important;
  -webkit-user-select: text !important;
}

.theme--light.v-label,
.text--primary {
  color: var(--text-color);
}

.no-shadow {
  box-shadow: none !important;
}

.push-10 {
  padding-top: 4rem;
}

.push-down {
  margin-top: 4rem;
}

.v-app-bar.v-app-bar--fixed {
  z-index: 6;
}

.v-toolbar {
  background: var(--bg-color) !important;
}

.theme--light.v-btn.v-btn--has-bg {
  background: var(--bg-color);
}

#partnerList {
  position: fixed;
  top: 48px;
  left: 0;
  width: 100%;
  background: var(--bg-color);
  height: 2.5rem;
  color: #333;
  display: flex;
  justify-content: center;
  align-items: center;
  z-index: 5;
  text-align: center;
  box-shadow: 0px 2px 4px -1px rgba(0, 0, 0, 0.2),
    0px 4px 5px 0px rgba(0, 0, 0, 0.14), 0px 1px 10px 0px rgba(0, 0, 0, 0.12);
  margin-bottom: 40rem;
  /* padding: 1rem; */
}

.player-container {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 0 1rem;
  position: relative;
  font-size: 1rem;
  text-align: center;
}

.player-container.large .player-icon {
  font-size: 5em;
}

.player-container:not(:last-child):after {
  content: '';
  position: absolute;
  top: 0.5rem;
  right: 0;
  width: 1px;
  height: 2rem;
  background: #ccc;
}

.player-icon {
  border-radius: 100%;
  background: #ccc;
  display: flex;
  height: 0.75em;
  width: 0.75em;
  flex-grow: 0;
  justify-content: center;
  align-items: center;
  font-size: 2em;
  overflow: hidden;
  color: #fff;
}

.player-icon.group0 {
  background: url('./assets/group0.png');
  background-size: cover;
  background-position: center;
}

.player-icon.group1 {
  background: url('./assets/group1.png');
  background-size: cover;
  background-position: center;
}

.v-subheader {
  height: auto;
}

.block-spam {
  display: none;
}

.completioncode {
  display: block;
  font-size: 2em;
  font-weight: bold;
  user-select: text;
  text-align: center;
}

.progress-top {
  display: flex;
  font-size: 0.85em;
  justify-content: center;
}

.progress-itm {
  display: flex;
  margin: 0 1em;
  align-items: center;
  white-space: nowrap;
  opacity: 0.7;
}

.number {
  font-size: 3em;
  font-weight: bold;
  opacity: 0.3;
  flex-grow: 0;
  margin-right: -0.5em;
}

.progress-itm .description {
  display: flex;
  flex-direction: column;
  flex-grow: 1;
}

.progress-itm.active,
.progress-itm.active .number {
  opacity: 1;
}

.progress-itm.active {
  border-bottom: solid 2px rgb(108, 99, 255);
}

button[type='submit'] {
  box-shadow: 0px 3px 1px -2px rgba(0, 0, 0, 0.2),
    0px 2px 2px 0px rgba(0, 0, 0, 0.14), 0px 1px 5px 0px rgba(0, 0, 0, 0.12);
  align-items: center;
  border-radius: 4px;
  display: inline-flex;
  flex: 0 0 auto;
  justify-content: center;
  outline: 0;
  position: relative;
  text-decoration: none;
  transition-duration: 0.28s;
  transition-property: box-shadow, transform, opacity;
  transition-timing-function: cubic-bezier(0.4, 0, 0.2, 1);
  -webkit-user-select: none;
  -moz-user-select: none;
  -ms-user-select: none;
  user-select: none;
  vertical-align: middle;
  white-space: nowrap;
}
</style>
