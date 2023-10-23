<!-- Short follow-up tutorial and waiting screen for the main task -->
<template>
  <div>
    <v-container v-if="player.groupingComplete === true && player.isWaiting === true">
      <v-row justify="center" class="pa-16">
        <v-col align="center" style="position: relative">
          <div v-if="step == 1 && !player.showReady">
            <!-- Short tutorial of the main stage of the experiment -->
            <h1 v-if="!player.showReady" class="text-center">
              Almost there! Prepare for the next task
            </h1>
            <p class="pt-4 text-left">
              <span v-if="!player.showReady">When the timer runs out, you will be assigned a partner and
                enter a chatroom with them.</span>
              <span v-if="player.showReady">You have been assigned a group and a partner. Click "I'm Ready"
                to start the task.</span>
            </p>
            <h3 class="text-left">
              Discussion About Assigned Topic ({{ player.chatTime }} min):
            </h3>
            <p class="text-left">
              You will be given {{ player.chatTime }} minutes to work with your
              partner and discuss each of your opinions about a specific topic.
              Try to understand how and why your partner formed their opinion by
              asking questions and comparing their answers to your own.
            </p>
            <h3 class="text-left">
              Discussion About Bonus Option ({{
                player.cooperationDiscussionTime
              }}
              min):
            </h3>
            <p class="text-left">
              Then, you will have {{ player.cooperationDiscussionTime }} minutes
              to decide on a bonus option with your partner. After that, you
              will write a report about the conversation.
              <v-alert color="primary" icon="mdi-star" type="success" class="d-none">
                Remember that sufficiently detailed reports about your partner
                will be rewarded with a
                <strong>{{ player.completionBonus | money }}</strong> bonus.
              </v-alert>
            </p>

            <div v-if="player.interventionMode == 'reply'">
              <!-- Describe the features of the chat -->
              <h2>Chat features</h2>
              <p class="text-left">
                You may occasionally receive message suggestions in the chat.
                These will popup above the text field. Feel free to accept or
                decline these suggestions.
              </p>
              <ul>
                <li>
                  <strong>Cancel</strong> a suggestion by pressing the "X"
                  button.
                </li>
                <li>
                  <strong>Edit</strong> a suggestion by clicking anywhere on the
                  suggestion text.
                </li>
                <li>
                  <strong>Accept</strong> the suggestion by clicking the send
                  button.
                </li>
              </ul>
              <div class="col-sm-4 example-suggestion">
                <SuggestionComponent msg="Example suggestion" freeze />
              </div>
            </div>

            <p class="text-left">
              Note: Refrain from using offensive language. If your partner is
              abusive or unresponive, you can end the task by clicking the
              "Report Partner" button below the chat window:
              <v-img :src="require(`../assets/report_btn.png`)" max-width="150" class="ma-4"></v-img>
            </p>
          </div>

          <div v-if="step === 2 || player.showReady" class="text-left">
            <!-- Wait for all participants to finish initial survey (and wait for timer to expire) -->
            <h1 v-if="!player.showReady" class="text-center">
              Please wait for the timer to elapse.
            </h1>
            <h1 v-if="player.showReady">Task Start</h1>
            <div>
              <span v-if="!player.showReady">Within 30 seconds of the timer expiring, please click the "I'm
                Ready" button that will appear. You may need to refresh the page
                to see the button.</span>
              <p v-if="player.showReady">
                Please click the ready button immediately to start the task. If
                you do not see it, refresh the page:<br /><br />
                <v-btn @click="readyForMainTask" id="readyBtn" block x-large color="primary">I'm Ready</v-btn>
                <v-alert type="success" id="readyMsg" class="d-none">Waiting for all participants to be ready...</v-alert>
              </p>
            </div>
          </div>
        </v-col>
      </v-row>
    </v-container>
    <v-expand-transition>
      <v-container v-if="player.groupingComplete === true &&
        player.isWaiting === false &&
        showSplash
        " id="groupAssignedMsg" align-start justify-start align-self="start">
        <!-- Timer elapsed! Let's move to the chat now -->
        <div class="center">
          <p class="text-center">
            We finished assigning groups and pairs based on your questionnaire
            answers. People in the same group are likely to have shared values.
          </p>
          <v-row justify="center">
            <h1 class="center text-center pa-4">
              Your partner is from {{ otherGroup }} group.
            </h1>
          </v-row>
          <v-row justify="center">
            <v-img :src="require(`../assets/vs0.png`)" v-if="player.neighborNodes[0].groupId != player.groupId"
              id="groupTypeIcon"></v-img>
            <v-img :src="require(`../assets/shake0.png`)" v-if="player.neighborNodes[0].groupId == player.groupId"
              id="groupTypeIcon" class="sameGroup"></v-img>
          </v-row>
          <v-row justify="center" class="col-sm-8 center">
            <p>
              Next, you will chat with your partner about an assigned topic.
            </p>
          </v-row>
          <v-row justify="center" class="pb-4"><v-btn @click="goToChat">Continue to Chat</v-btn></v-row>
        </div>
      </v-container>
    </v-expand-transition>
  </div>
</template>

<script>
/* global Breadboard */
import {
  Chart as ChartJS,
  Title,
  Tooltip,
  Legend,
  CategoryScale,
  ArcElement,
} from 'chart.js';
import SuggestionComponent from '../components/Suggestion';

ChartJS.register(Title, Tooltip, Legend, ArcElement, CategoryScale);

export default {
  name: 'Tutorial2Step',
  components: { SuggestionComponent },
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
  mounted() {
    window.addEventListener('resize', this.drawLine);
  },
  methods: {
    readyForMainTask() {
      Breadboard.send('readyForMainTask', {});
      document.querySelector('#readyBtn').classList.add('d-none');
      document.querySelector('#readyMsg').classList.remove('d-none');
    },
    goToChat() {
      this.showSplash = false;
      Breadboard.send('startMainChat', {});
    },
    drawLine() {
      if (
        !document.querySelector('.ingroup') ||
        !document.querySelector('#partnerline')
      ) {
        return;
      }

      let ocRect = document
        .querySelector('.outgroup .center-circle')
        .getBoundingClientRect();

      let icRect = document
        .querySelector('.ingroup .center-circle')
        .getBoundingClientRect();

      let line = document.querySelector('#partnerline');
      let dist = Math.sqrt(
        Math.pow(ocRect.x - icRect.x, 2) + Math.pow(ocRect.y - icRect.y, 2)
      );

      line.style.width = dist - 2 + 'px';
    },
  },
  data() {
    return {
      slideIdx: 0,
      showSplash: true,
      groupingComplete: false,
      step: 1
    };
  },
  computed: {
    otherGroup() {
      return this.player.groupId == this.player.neighborNodes[0].groupId
        ? 'the same'
        : 'a different';
    },
  },
  watch: {
    player(val) {
      // Grouping complete animation
      if (
        val.groupingComplete &&
        val.isWaiting == false &&
        !this.groupingComplete
      ) {
        this.groupingComplete = true;
        setTimeout(() => {
          document.querySelectorAll('.group-circle').forEach((el) => {
            el.classList.add('animate');
          });
          setTimeout(() => {
            this.drawLine();
          }, 100);
        }, 200);
      }
    },
  },
};
</script>

<style>
#groupTypeIcon {
  max-width: 512px;
  width: 80%;
}

#groupTypeIcon.sameGroup {
  margin: 3em 0;
  max-width: 400px;
}

.example-suggestion {
  position: relative;
  display: block;
  min-width: 14em;
}

.example-suggestion #suggestion {
  position: unset;
}

.card {
  border-radius: 4px;
  padding: 1em;
  /* box-shadow: 0 3px 1px -2px rgba(0,0,0,.2),0 2px 2px 0 rgba(0,0,0,.14),0 1px 5px 0 rgba(0,0,0,.12); */
  border: solid 1px #eee;
  font-size: 0.92rem;
  color: #555;
}

.border-left {
  border-left: solid 1px #eee;
}

#readyBtn {
  font-weight: bold;
}

#groupContainer {
  position: relative;
  height: 10em;
  width: 80%;
}

#groupContainer p {
  margin-top: 0.5em;
}

#groupInner {
  display: flex;
  justify-content: center;
  align-items: center;
}

.groupList ul {
  padding: 0;
  margin: 0;
}

.groupList ul li {
  list-style: none;
  display: flex;
  align-items: center;
  padding: 0;
  margin: 0;
}

.dot {
  width: 1em;
  height: 1em;
  background-color: #000;
  border-radius: 1em;
  display: inline-block;
  vertical-align: middle;
  margin-right: 0.25em;
}

.dot.group0 {
  background-color: rgb(80, 27, 140);
}

.dot.group1 {
  background-color: rgb(216, 119, 1);
}

.ingroup .center-circle {
  border: solid 5px gold;
  /* box-shadow: 0 0 5px 5px gold; */
  box-sizing: content-box;
}

#groupInner .ingroup,
#groupInner .outgroup {
  width: 9em;
}

a {
  color: #999 !important;
}

#description {
  width: 100%;
  text-align: left;
  padding: 2.5em 1em 1em 1em;
}

#description .v-card__actions {
  position: absolute;
  width: 100%;
  left: 0;
  top: 0;
}

#groupAssignedMsg {
  /* z-index: 5; */
  /* background: #f5f5f5; */
  position: absolute;
  top: 0;
  margin-top: 2rem;
  align-content: center;
  width: 100%;
  max-width: none;
}

.majority {
  box-shadow: 0 0 5px blue;
}

/* Circle */
#partnerline {
  position: absolute;
  width: 0;
  height: 3px;
  background: #ccc;
  transform-origin: 0 0;
  z-index: 1;
  transition: width 2s ease-in-out;
  top: calc(50% - 1.5em);
}

.ingroup,
.outgroup {
  position: relative;
  width: 50%;
  height: 15em;
  display: flex;
  justify-content: center;
  align-items: center;
  z-index: 2;
  /* flex-wrap: wrap; */
  flex-direction: column;
}

.group0 .group-circle {
  background: green;
}

.group0 .center-circle {
  background: url('../assets/group0.png'), rgb(80, 27, 140);
  background-size: 3em;
  background-repeat: no-repeat;
  background-position: top;
}

.group1 .group-circle {
  background: teal;
}

.group1 .center-circle {
  background: url('../assets/group1.png'), rgb(216, 119, 1);
  background-size: 3em;
  background-repeat: no-repeat;
  background-position: top;
}

.group-circle.animate {
  -webkit-animation: myOrbit 4s ease-in-out forwards;
  /* Chrome, Safari 5 */
  -moz-animation: myOrbit 4s ease-in-out forwards;
  /* Firefox 5-15 */
  -o-animation: myOrbit 4s ease-in-out forwards;
  /* Opera 12+ */
  animation: myOrbit 4s ease-in-out forwards;
}

.group-circle {
  position: absolute;
  width: 2em;
  height: 2em;
  border-radius: 100%;
  top: 1em;
  left: 1em;
}

.center-circle {
  position: relative;
  width: 4em;
  height: 4em;
  border-radius: 100%;
}

.c1.animate {
  -webkit-animation: myOrbit 4s ease-in-out forwards;
  /* Chrome, Safari 5 */
  -moz-animation: myOrbit 4s ease-in-out forwards;
  /* Firefox 5-15 */
  -o-animation: myOrbit 4s ease-in-out forwards;
  /* Opera 12+ */
  animation: myOrbit 4s ease-in-out forwards;
  /* Chrome, Firefox 16+, 
                                                      IE 10+, Safari 5 */
}

.c2.animate {
  -webkit-animation: myOrbit1 4s ease-in-out forwards;
  /* Chrome, Safari 5 */
  -moz-animation: myOrbit1 4s ease-in-out forwards;
  /* Firefox 5-15 */
  -o-animation: myOrbit1 4s ease-in-out forwards;
  /* Opera 12+ */
  animation: myOrbit1 4s ease-in-out forwards;
  /* Chrome, Firefox 16+, 
                                                      IE 10+, Safari 5 */
}

.c3.animate {
  -webkit-animation: myOrbit2 4s ease-in-out forwards;
  /* Chrome, Safari 5 */
  -moz-animation: myOrbit2 4s ease-in-out forwards;
  /* Firefox 5-15 */
  -o-animation: myOrbit2 4s ease-in-out forwards;
  /* Opera 12+ */
  animation: myOrbit2 4s ease-in-out forwards;
  /* Chrome, Firefox 16+, 
                                                      IE 10+, Safari 5 */
}

.c4.animate {
  -webkit-animation: myOrbit3 4s ease-in-out forwards;
  /* Chrome, Safari 5 */
  -moz-animation: myOrbit3 4s ease-in-out forwards;
  /* Firefox 5-15 */
  -o-animation: myOrbit3 4s ease-in-out forwards;
  /* Opera 12+ */
  animation: myOrbit3 4s ease-in-out forwards;
  /* Chrome, Firefox 16+, 
                                                      IE 10+, Safari 5 */
}

.c5.animate {
  -webkit-animation: myOrbit4 4s ease-in-out forwards;
  /* Chrome, Safari 5 */
  -moz-animation: myOrbit4 4s ease-in-out forwards;
  /* Firefox 5-15 */
  -o-animation: myOrbit4 4s ease-in-out forwards;
  /* Opera 12+ */
  animation: myOrbit4 4s ease-in-out forwards;
  /* Chrome, Firefox 16+, 
                                                      IE 10+, Safari 5 */
}

.group-circles {
  -webkit-animation: myOrbit 4s ease-in-out forwards;
  /* Chrome, Safari 5 */
  -moz-animation: myOrbit 4s ease-in-out forwards;
  /* Firefox 5-15 */
  -o-animation: myOrbit 4s ease-in-out forwards;
  /* Opera 12+ */
  animation: myOrbit 4s ease-in-out forwards;
  /* Chrome, Firefox 16+, 
                                                      IE 10+, Safari 5 */
}

@-webkit-keyframes myOrbit {
  from {
    -webkit-transform: rotate(0deg) translateX(5em);
  }

  to {
    -webkit-transform: rotate(360deg) translateX(5em);
  }
}

@-moz-keyframes myOrbit {
  from {
    -moz-transform: rotate(0deg) translateX(5em);
  }

  to {
    -moz-transform: rotate(360deg) translateX(5em);
  }
}

@-o-keyframes myOrbit {
  from {
    -o-transform: rotate(0deg) translateX(5em);
  }

  to {
    -o-transform: rotate(360deg) translateX(5em);
  }
}

@keyframes myOrbit {
  from {
    transform: rotate(0deg) translateX(5em);
  }

  to {
    transform: rotate(360deg) translateX(5em);
  }
}

@-webkit-keyframes myOrbit1 {
  from {
    -webkit-transform: rotate(0deg) translateX(5em);
  }

  to {
    -webkit-transform: rotate(288deg) translateX(5em);
  }
}

@-moz-keyframes myOrbit1 {
  from {
    -moz-transform: rotate(0deg) translateX(5em);
  }

  to {
    -moz-transform: rotate(288deg) translateX(5em);
  }
}

@-o-keyframes myOrbit1 {
  from {
    -o-transform: rotate(0deg) translateX(5em);
  }

  to {
    -o-transform: rotate(288deg) translateX(5em);
  }
}

@keyframes myOrbit1 {
  from {
    transform: rotate(0deg) translateX(5em);
  }

  to {
    transform: rotate(288deg) translateX(5em);
  }
}

@-webkit-keyframes myOrbit1 {
  from {
    -webkit-transform: rotate(0deg) translateX(5em);
  }

  to {
    -webkit-transform: rotate(288deg) translateX(5em);
  }
}

@-moz-keyframes myOrbit1 {
  from {
    -moz-transform: rotate(0deg) translateX(5em);
  }

  to {
    -moz-transform: rotate(288deg) translateX(5em);
  }
}

@-o-keyframes myOrbit1 {
  from {
    -o-transform: rotate(0deg) translateX(5em);
  }

  to {
    -o-transform: rotate(288deg) translateX(5em);
  }
}

@keyframes myOrbit1 {
  from {
    transform: rotate(0deg) translateX(5em);
  }

  to {
    transform: rotate(288deg) translateX(5em);
  }
}

@-webkit-keyframes myOrbit2 {
  from {
    -webkit-transform: rotate(0deg) translateX(5em);
  }

  to {
    -webkit-transform: rotate(216deg) translateX(5em);
  }
}

@-moz-keyframes myOrbit2 {
  from {
    -moz-transform: rotate(0deg) translateX(5em);
  }

  to {
    -moz-transform: rotate(216deg) translateX(5em);
  }
}

@-o-keyframes myOrbit2 {
  from {
    -o-transform: rotate(0deg) translateX(5em);
  }

  to {
    -o-transform: rotate(216deg) translateX(5em);
  }
}

@keyframes myOrbit2 {
  from {
    transform: rotate(0deg) translateX(5em);
  }

  to {
    transform: rotate(216deg) translateX(5em);
  }
}

@-webkit-keyframes myOrbit3 {
  from {
    -webkit-transform: rotate(0deg) translateX(5em);
  }

  to {
    -webkit-transform: rotate(144deg) translateX(5em);
  }
}

@-moz-keyframes myOrbit3 {
  from {
    -moz-transform: rotate(0deg) translateX(5em);
  }

  to {
    -moz-transform: rotate(144deg) translateX(5em);
  }
}

@-o-keyframes myOrbit3 {
  from {
    -o-transform: rotate(0deg) translateX(5em);
  }

  to {
    -o-transform: rotate(144deg) translateX(5em);
  }
}

@keyframes myOrbit3 {
  from {
    transform: rotate(0deg) translateX(5em);
  }

  to {
    transform: rotate(144deg) translateX(5em);
  }
}

@-webkit-keyframes myOrbit4 {
  from {
    -webkit-transform: rotate(0deg) translateX(5em);
  }

  to {
    -webkit-transform: rotate(72deg) translateX(5em);
  }
}

@-moz-keyframes myOrbit4 {
  from {
    -moz-transform: rotate(0deg) translateX(5em);
  }

  to {
    -moz-transform: rotate(72deg) translateX(5em);
  }
}

@-o-keyframes myOrbit4 {
  from {
    -o-transform: rotate(0deg) translateX(5em);
  }

  to {
    -o-transform: rotate(72deg) translateX(5em);
  }
}

@keyframes myOrbit4 {
  from {
    transform: rotate(0deg) translateX(5em);
  }

  to {
    transform: rotate(72deg) translateX(5em);
  }
}
</style>
