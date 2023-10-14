<template>
  <v-app id="app">
    <v-app-bar
      dense
      app
      :class="
        player.neighborNodes && player.neighborNodes.length > 0
          ? 'no-shadow'
          : ''
      "
    >
      <v-spacer />
      <v-app-bar-title>Communication Task</v-app-bar-title>
      <v-spacer />
    </v-app-bar>
    <div
      id="partnerList"
      v-if="
        player.displayNeighborNodes &&
        player.neighborNodes &&
        player.neighborNodes.length > 0
      "
    >
      <span v-if="player.neighborNodes[0].groupId != player.groupId"
        >Your partner is from a different group</span
      >
      <span v-if="player.neighborNodes[0].groupId == player.groupId"
        >Your partner is from the same group</span
      >
      <!-- <div class="player-container">
          <div class="player-icon" :class="`group${player.groupId}`"></div>
          Me
        </div>
        <div class="player-container" v-for="neighbor in player.neighborNodes" :key="neighbor.chatId">
          <div class="player-icon" :class="`group${neighbor.groupId}`"></div>
          Partner
        </div> -->
    </div>
    <v-main v-if="!browserCompatible">
      <h2 class="pa-10 text-center">
        Your browser is incompatible. Please return this HIT.
      </h2>
    </v-main>
    <v-main :class="mainClass" v-else-if="browserCompatible">
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
      <RecaptchaStep
        v-if="player.gameStep === 'recaptcha' && loading === false"
        :player="player"
      />
      <ConsentStep
        v-if="player.gameStep === 'consent' && loading === false"
        :player="player"
      />
      <PreEvalStep
        v-if="player.gameStep === 'verification' && loading === false"
        :player="player"
        :nlp="nlp"
      />
      <TutorialStep
        v-if="player.gameStep === 'tutorial' && loading === false"
        :player="player"
      />
      <GroupingStep
        v-if="player.gameStep === 'grouping' && loading === false"
        :player="player"
      />
      <Tutorial2Step
        v-if="player.gameStep === 'tutorial2' && loading === false"
        :player="player"
      />
      <CheapTalkStep
        v-if="player.gameStep === 'cheaptalk' && loading === false"
        :player="player"
        :nlp="nlp"
      />
      <GameStep
        v-if="player.gameStep === 'game' && loading === false"
        :player="player"
      />
      <SurveyStep
        v-if="player.gameStep === 'survey' && loading === false"
        :player="player"
      />
      <PartnerAnswerStep
        v-if="player.gameStep === 'partnerAnswer' && loading === false"
        :player="player"
      />
      <EndStep
        v-if="player.gameStep === 'end' && loading === false"
        :player="player"
      />
      <ReturnStep
        v-else-if="
          player.gameStep === 'failedCaptcha' ||
          player.gameStep === 'failedConsent' ||
          player.gameStep === 'leftover' ||
          player.gameStep === 'reported' ||
          player.gameStep == 'notReady' ||
          player.gameStep == 'tooLate' ||
          player.gameStep == 'failedEvaluation' ||
          player.gameStep == 'timeout' ||
          player.gameStep == 'failCheckUnderstanding'
        "
        :player="player"
      />
    </v-main>
  </v-app>
</template>

<script>
/* global Breadboard */
import RecaptchaStep from './steps/Recaptcha.vue';
import ConsentStep from './steps/Consent.vue';
import PreEvalStep from './steps/PreEval.vue';
// import Consent from "./steps/Consent.vue";
import TutorialStep from './steps/Tutorial.vue';
import GroupingStep from './steps/Grouping.vue';
import Tutorial2Step from './steps/Tutorial2.vue';
import CheapTalkStep from './steps/CheapTalk.vue';
import GameStep from './steps/CooperationGame.vue';
import SurveyStep from './steps/Survey.vue';
import PartnerAnswerStep from './steps/PartnerAnswer.vue';
import EndStep from './steps/End.vue';
import ReturnStep from './steps/Return';
import { PlayerTimers } from '@human-nature-lab/breadboard-client';

window.texts = {
  INSTALL:
    'Copy this object to frontend/App.vue, it is automatically read by Breadboard',

  messagesEvaluation: [
    [
      'How many times in the last year did you suffer a fatal heart attack?',
      'Thank you. Next, please enter the name of this HIT.',
      'Lastly, why did we ask you the first question?',
    ],
    [
      'How many times in the last year did you suffer a fatal heart attack?',
      'Thank you. Next, please enter the name of this HIT.',
      'Lastly, please enter this text exactly: "I&nbsp;am&nbsp;comfortable&nbsp;beginning&nbsp;this&nbsp;task."',
    ],
    [
      "Hey, how's it going?",
      "I'm doing well myself. First of all, I think the fear around AI's capabilities is overblown.",
      "Instead of being fearful, I actually think it's cool that AI can help edit photos and text to make them look better to us. What do you think?",
      'Why do you feel that way?',
      "I see. People are so afraid that AI has all these problems like replacing human jobs and creating unrealistic beauty standards but I think that's always been happening. Nothing new.",
      "I do understand that the speed and scale of AI's effect on society is different than before. It's definitely messed up that you can't tell whether something is real or not.",
      "It's especially bad that ChatGPT straight up lies to people sometimes.",
      "Anyways, I'm hopeful we can figure out as a society how to fix AI's problems. It was nice chatting with you!",
    ],
  ],
  surveyPairs: [
    [
      'Global warming is a fact',
      'There is too much conflicting evidence about climate change to know whether it is actually happening',
    ],
    [
      'Global warming is mostly caused by human activities',
      'Global warming is a naturally occurring phenomenon',
    ],
    [
      'I am very worried about global warming',
      'The media is often too alarmist about issues like global warming',
    ],
    [
      'Utilities should be required to produce renewable energy even if it raises costs',
      'The US should expand natural gas drilling to create jobs and reduce costs',
    ],
    [
      'People should make changes like recycling and reducing meat consumption to help the climate',
      'Personal life changes will never be enough to solve global warming',
    ],
    [
      'At some point, all cars will be electric',
      'Electric cars can be worse for the planet than gas cars',
    ],
  ],
  likertQuestions: [
    'I would want my kids to be taught evolution as a fact of biology',
    'My second amendment right to bear arms should be protected',
    'I support funding the military',
    'Our children are being indoctrinated at school with LGBT messaging',
    'I would pay higher taxes to support climate change research',
    'Restrictions to stop the spread of COVID-19 went too far',
    'I want stricter immigration requirements into the U.S.',
  ],
  customExamples: [
    'David Campbell, a teacher in Orange Park, FL admitted to teaching inaccurate simplifications about evolution as long as he could convince religious students that evolution is real. What do you think about this story?',
    'The parents of a Michigan gunman who killed four high school students in 2021 are now being tried for involuntary manslaughter because they purchased the weapon for their son. What do you think about this story?',
    'Lockheed Martin, which has spent the past 20 years developing a new F-35 fighter jet, is projected to receive over 1.7 trillion dollars for aircraft that continue to malfunction and face delays. Some believe it will never be ready to fly. What are your reactions to hearing this?',
    'People are outraged at the finding that public schools in New York City have spent over $200,000 doing drag queen story hours which have been suspected of “grooming” children. What are your reactions to recent stories like these?',
    'In response to the U.S. government continuing to increase restrictions on fossil fuel use, former Energy Secretary Rick Perry has released a statement saying that these skyrocketing energy prices are "killing America." What do you think?',
    'Regarding the U.S\'. response to the COVID-19 pandemic, Supreme Court Justice Neil Gorsuch said we "experienced the greatest intrusions on civil liberties in the peacetime history of this country." Do you think COVID-19 restrictions went too far?',
    'Representative Elise Stefanik has stated that the U.S. is "sacrificing America\'s children\'s safety and happiness to prioritize the needs of illegals" referring to the influx of migrants into New York City. Do you share this opinion?',
  ],
  customExampleSources: [
    'https://www.nytimes.com/2008/08/24/education/24evolution.html',
    'https://www.nbcnews.com/news/us-news/oxford-michigan-school-shooters-parents-will-stand-trial-rcna76357',
    'https://www.nytimes.com/2019/08/21/magazine/f35-joint-strike-fighter-program.html',
    'https://www.ny1.com/nyc/all-boroughs/education/2022/06/16/queens-councilmember-calls-drag-queen-story-hours-in-schools--grooming-',
    'https://www.foxnews.com/media/democrats-killing-america-rick-perry',
    'https://www.foxnews.com/politics/gorsuch-gives-scathing-overview-covid-era-fear-desire-safety-powerful-forces',
    'https://www.foxnews.com/politics/ny-house-republicans-blast-dems-states-migrant-crisis-packing-school-gyms-absolutely-unacceptable',
  ],
  customPrompts: [
    'Now, exchange your opinions on the way evolution should be taught in schools. Would you want your kids learning it?',
    'Now, exchange your opinions on the degree that gun rights should be protected or abolished.',
    'Now, exchange your opinions on how much the military should be funded. How much of your tax dollars would you be willing to provide?',
    'Now, exchange your opinions on the inclusion of gay and transgender stories in public schools. How would you feel if your children had access to these?',
    'Now, exchange your feelings about the severity of global warming.',
    'Now, exchange your opinions on the way the COVID-19 pandemic was handled.',
    'Now, discuss how you feel immigration affects society.',
  ],
  customFollowups: [
    'Should teachers be punished for choosing to teach creationism as an alternative explanation?',
    'What do you think decreasing access to guns will do to society?',
    "Is there meaning to having the strongest military if it doesn't get involved in world conflicts?",
    'Should teachers and books in public schools be banned from discussing sexuality and gender?',
    'How much can economic prosperity be sacrificed to prevent climate change?',
    'Do you think the CDC is a trustworthy source of information?',
    'How would your opinions change if immigrants were given more social support services like food stamps and medicare?',
  ],
  suggestions: {
    greeting: ['How are you doing?', "I'm good, and you?"],
    active_listen1: ['I hear you', 'I understand what you mean'],
    active_listen2: ['I see!', 'Gotcha!'],
    gratitude: ['Thank you for sharing!', 'Thanks for your input'],
    farewell: [
      'I appreciate you chatting with me!',
      "You've been a great partner",
    ],
    express_interest: [
      'What do you think?',
      'Is there anything else you could say about this topic?',
    ],
    anger_management: [
      "Apologies for the negativity. Let's do the best we can at this task!",
      "Yeah, we're a team",
    ],
    encouragement: [
      "I think it's better if we keep chatting, What do you want to talk about next?",
      "Sorry for being quiet, I'm still here!",
    ],
  },
  botSuggestions: {
    greeting: 'Hi, how are you both doing?',
    anger_management:
      'Hi everyone, remember that our task is to understand each other. Do you mind trying to resolve your conflicts?',
    encouragement:
      "Thanks for your participation so far. It's better if we keep chatting. What do you wanna talk about next?",
    farewell:
      "I appreciate your participation in this chat! You've both been great. See ya!",
  },
};

export default {
  name: 'App',
  components: {
    PlayerTimers,
    RecaptchaStep,
    ConsentStep,
    PreEvalStep,
    // ConsentStep,
    TutorialStep,
    GroupingStep,
    Tutorial2Step,
    CheapTalkStep,
    GameStep,
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
      if (this.player.nlpPort === undefined) {
        return;
      }

      let suggestionURL = '';
      if (window.location.protocol === 'http:') {
        suggestionURL =
          'ws://' + window.location.hostname + ':' + this.player.nlpPort;
      } else {
        suggestionURL =
          'wss://' + window.location.hostname + ':' + this.player.nlpPort;
      }

      window.suggestionBox = new WebSocket(suggestionURL);

      window.suggestionBox.onopen = () => {
        this.connectedToNLP = true;

        window.nlpInterval = setInterval(() => {
          if (window.suggestionBox instanceof WebSocket) {
            window.suggestionBox.send('{"command": "ping"}');
          } else {
            clearInterval(window.nlpInterval);
          }
        }, 20000);

        if (this.player.gameStep == 'cheaptalk') {
          let req = {
            command: 'createPair',
            id1: this.player.chatId,
            id2: this.player.neighborNodes[0].chatId,
            mode: this.player.interventionMode,
          };
          window.suggestionBox.send(JSON.stringify(req));
        } else {
          let req = {
            command: 'registerParticipant',
            id: this.player.chatId,
          };
          window.suggestionBox.send(JSON.stringify(req));
        }
      };

      window.suggestionBox.onclose = () => {
        this.connectedToNLP = false;
        this.connectToNLP();
      };

      window.suggestionBox.onmessage = (event) => {
        // Reply is in event.data
        let nlpReply = JSON.parse(event.data);

        if (nlpReply.suggestion) {
          // Just in case
          if (!('id' in nlpReply)) {
            nlpReply.id = '';
          }

          window.Breadboard.send('receiveSuggestion', { id: nlpReply.id });
          this.nlp = nlpReply;
        } else if (nlpReply.reply) {
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
          window.Breadboard.send('evalScore', { score: nlpReply.score });
        }
      };
    },

    updatePlayer(player) {
      if (this.loading) {
        this.playerState = player.gameStep;
        this.loading = false;

        // Register the prolific ID when they join
        if (player.gameStep == 'recaptcha') {
          console.log('Registering prolific:');
          console.log({
            prolificId: window.prolificId,
            studyId: window.studyId,
            sessionId: window.sessionId,
          });
          Breadboard.send('registerProlific', {
            prolificId: window.prolificId,
            studyId: window.studyId,
            sessionId: window.sessionId,
          });
        }
      }

      if (
        this.player &&
        this.player.readyToScoreEvaluation == false &&
        player.readyToScoreEvaluation == true
      ) {
        let req = {
          command: 'scoreEvaluation',
          id: this.player.chatId,
          msgs: this.player.messagesEvaluation,
        };

        if (window.suggestionBox) {
          window.suggestionBox.send(JSON.stringify(req));
        }
      }

      this.player = player;
      // Connect to suggested reply server
      if (!window.suggestionBox) {
        this.connectToNLP();
      } else {
        this.connectedToNLP = true;
      }

      // Handle player step change
      if (this.playerState != player.gameStep) {
        if (
          player.gameStep == 'cheaptalk' &&
          player.interventionMode != 'none'
        ) {
          // Register partner with suggestionBox
          console.log('creating pair in nlp');
          let req = {
            command: 'createPair',
            id1: this.player.chatId,
            id2: this.player.neighborNodes[0].chatId,
            mode: this.player.interventionMode,
          };
          let inter = setInterval(() => {
            if (window.suggestionBox instanceof WebSocket) {
              window.suggestionBox.send(JSON.stringify(req));
              console.log('Registering pair with suggestionBox...');
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
      let thisClass = 'progress-top center mt-4';
      if (
        !['tutorial', 'consent', 'verification', 'grouping'].includes(
          this.player.gameStep
        )
      ) {
        thisClass = 'd-none';
      }
      return thisClass;
    },
    displaySec2Progress() {
      let thisClass = 'progress-top center';
      if (!['cheaptalk', 'game', 'survey'].includes(this.player.gameStep)) {
        thisClass = 'd-none';
      }
      return thisClass;
    },
    pi1() {
      return this.player.gameStep == 'tutorial' ||
        this.player.gameStep == 'consent'
        ? 'active progress-itm'
        : 'progress-itm';
    },
    pi2() {
      return this.player.gameStep == 'verification'
        ? 'active progress-itm'
        : 'progress-itm';
    },
    pi3() {
      return this.player.gameStep == 'grouping'
        ? 'active progress-itm'
        : 'progress-itm';
    },
    mi1() {
      return this.player.gameStep == 'cheaptalk' ||
        this.player.gameStep == 'consent'
        ? 'active progress-itm'
        : 'progress-itm';
    },
    mi2() {
      return this.player.gameStep == 'survey' ||
        this.player.gameStep == 'consent'
        ? 'active progress-itm'
        : 'progress-itm';
    },
    mi3() {
      return this.player.gameStep == 'game' || this.player.gameStep == 'consent'
        ? 'active progress-itm'
        : 'progress-itm';
    },
  },
  mounted() {
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
