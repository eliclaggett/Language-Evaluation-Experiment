<template>
  <div>
    <v-container
      v-if="player.groupingComplete === true && player.isWaiting === true"
    >
      <v-row justify="center" class="pa-16">
        <v-col align="center" style="position: relative">
          <div v-if="false && step === 1 && !player.showReady">
            <h1 class="mb-2">Questionnaire Results</h1>
            <p v-if="player.groupingType == 'value'">
              Your answers indicate that you believe global warming
              <b>{{ preferenceHeadline }}</b> an important problem.
            </p>
            <p v-if="player.groupingType == 'artificial'">
              Your answers indicate <b>{{ preferenceHeadline }}</b> paintings.
            </p>
            <!-- a preference for X -->
            <p></p>
            <p>Below is a chart of your answers.</p>
            <div class="center">
              <Pie
                :chart-options="chartOptions"
                :chart-data="chartData"
                chart-id="prefPieChart"
                :height="chartHeight"
                :width="chartWidth"
              />
            </div>
            <div id="descriptionContainer" class="d-none">
              <br />
              <v-card id="description">
                <v-card-actions>
                  <v-spacer></v-spacer>
                  <v-btn icon @click="toggleDescription"
                    ><v-icon>mdi-close-circle-outline</v-icon></v-btn
                  >
                </v-card-actions>
                <p>
                  These labels are based on
                  <a
                    href="https://climatecommunication.yale.edu/publications/climate-change-in-the-american-mind-beliefs-attitudes-december-2022/"
                    target="__blank"
                    >research</a
                  >
                  conducted by Yale University.<br /><br />
                </p>
                <ul>
                  <li>
                    <b>High concern: </b>Global warming is an issue that we must
                    actively work to solve.
                  </li>
                  <li>
                    <b>Low concern: </b> Global warming should not be the focus
                    of political policy.
                  </li>
                  <!-- <li><b>Cultural: </b> People living in poverty stay that way because they would rather live off of the government assistance than work.</li> -->
                </ul>
              </v-card>
              <br /><br />
            </div>
            <a
              href="#"
              @click="toggleDescription"
              id="showDescription"
              class="d-block pa-10"
              v-if="player.groupingType == 'value'"
              >What do these mean?</a
            >
            <v-layout justify-center>
              <v-btn @click="goNext">Next</v-btn>
            </v-layout>
          </div>

          <!-- Alternate first step -->
          <div v-if="step == 1 && !player.showReady">
            <h1 v-if="!player.showReady" class="text-center">
              Almost there! Prepare for the next task
            </h1>
            <p class="pt-4 text-left">
              <span v-if="!player.showReady"
                >When the timer runs out, you will be assigned a partner and
                enter a chatroom with them.</span
              >
              <span v-if="player.showReady"
                >You have been assigned a group and a partner. Click "I'm Ready"
                to start the task.</span
              >
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
              <v-alert
                color="primary"
                icon="mdi-star"
                type="success"
                class="d-none"
              >
                Remember that sufficiently detailed reports about your partner
                will be rewarded with a
                <strong>{{ player.completionBonus | money }}</strong> bonus.
              </v-alert>
            </p>

            <div v-if="player.interventionMode == 'reply'">
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
              <v-img
                :src="require(`../assets/report_btn.png`)"
                max-width="150"
                class="ma-4"
              ></v-img>
            </p>
          </div>
          <div v-if="step === 2 || player.showReady" class="text-left">
            <h1 v-if="!player.showReady" class="text-center">
              Please wait for the timer to elapse.
            </h1>
            <h1 v-if="player.showReady">Task Start</h1>
            <div>
              <span v-if="!player.showReady"
                >Within 30 seconds of the timer expiring, please click the "I'm
                Ready" button that will appear. You may need to refresh the page
                to see the button.</span
              >
              <p v-if="player.showReady">
                Please click the ready button immediately to start the task. If
                you do not see it, refresh the page:<br /><br />
                <v-btn
                  @click="readyForGrouping"
                  id="readyBtn"
                  block
                  x-large
                  color="primary"
                  >I'm Ready</v-btn
                >
                <v-alert type="success" id="readyMsg" class="d-none"
                  >Waiting for all participants to be ready...</v-alert
                >
              </p>
            </div>
          </div>
        </v-col>
      </v-row>
    </v-container>
    <v-expand-transition>
      <v-container
        v-if="
          player.groupingComplete === true &&
          player.isWaiting === false &&
          showSplash
        "
        id="groupAssignedMsg"
        align-start
        justify-start
        align-self="start"
      >
        <div class="center">
          <p class="text-center">
            We finished assigning groups and pairs based on your questionnaire
            answers. People in the same group are likely to have shared values.
          </p>
          <v-row justify="center"
            ><h1 class="center text-center pa-4">
              Your partner is from {{ otherGroup }} group.
            </h1></v-row
          >
          <v-row justify="center">
            <v-img
              :src="require(`../assets/vs0.png`)"
              v-if="player.neighborNodes[0].groupId != player.groupId"
              id="groupTypeIcon"
            ></v-img>
            <v-img
              :src="require(`../assets/shake0.png`)"
              v-if="player.neighborNodes[0].groupId == player.groupId"
              id="groupTypeIcon"
              class="sameGroup"
            ></v-img>
          </v-row>
          <!-- <v-row justify="center">
        <div id="groupContainer" style="max-width: 80%">
          <v-img :src="require(`../assets/group-bg.svg`)"></v-img>
          <div id="groupInner">
            <div id="partnerline"></div>
            <div :class="`ingroup group${player.groupId}`">
              <div class="center-circle"></div>
              <p>Me</p>
            </div>
            <div :class="`outgroup group${player.neighborNodes[0].groupId}`">
              <div class="center-circle"></div>
              <p>Partner</p>
            </div>
          </div>
          <div class="groupList">
            <h4>Groups created:</h4>
            <ul>
              <li><div class="dot group0"></div> Violet Group</li>
              <li><div class="dot group1"></div> Orange Group</li>
            </ul>
          </div>
        </div>
      </v-row> -->
          <!-- <v-row justify="center" class="col-sm-10 center" v-if="myPrefs.length > 0">
          <p class="col-sm-12 center text-center">Here is a selection of you and your partner's responses:</p>
          <div class="col-sm-6">
            <h3 class="text-center">My Responses</h3><br>
            <p class="card" v-for="answer in myPrefs" :key="answer">{{answer}}</p>
          </div>
          <div class="col-sm-6 border-left">
            <h3 class="text-center">Their Responses</h3><br>
            <p class="card" v-for="answer in partnerPrefs" :key="answer">{{answer}}</p>
          </div>
        </v-row> -->
          <v-row justify="center" class="col-sm-8 center"
            ><p>
              Next, you will chat with your partner about an assigned topic.
            </p></v-row
          >
          <v-row justify="center" class="pb-4"
            ><v-btn @click="goToChat">Continue to Chat</v-btn></v-row
          >
        </div>
      </v-container>
    </v-expand-transition>
    <!-- <v-expand-transition>
    <v-container v-if="player.groupingComplete===true && player.isWaiting===false && showSplash" id="groupAssignedMsg" fill-height align-start justify-start align-self="start">
      <div class="center">
      <v-row justify="center"><h1 class="center text-center">You are now part of {{player.groupName}}!</h1></v-row>
      <v-row justify="center">
        <div :class="`ingroup group${player.groupId}`">
            <div class="center-circle">
              <div class="group-circle c1" v-if="player.neighborNodes[0].groupName == player.groupName"></div>
              <div class="group-circle c2"></div>
              <div class="group-circle c3"></div>
              <div class="group-circle c4"></div>
              <div class="group-circle c5"></div>
            </div>
          </div>
          
        <div :class="`outgroup group${player.neighborNodes[0].groupId}`" v-if="player.neighborNodes[0].groupName != player.groupName">
          <div class="center-circle">
            <div class="group-circle c1"></div>
            <div class="group-circle c2"></div>
            <div class="group-circle c3"></div>
            <div class="group-circle c4"></div>
            <div class="group-circle c5"></div>
          </div>
        </div>
      </v-row>
      <v-row justify="center" class="push-10"><p class="center text-center">You have been assigned a partner from <strong>{{player.neighborNodes[0].groupName}}</strong>.
        <br>Next, chat with your partner about {{discussionTopic}}.</p></v-row>
      <v-row justify="center"><v-btn @click="goToChat">Continue to Chat</v-btn></v-row>
      </div>
    </v-container>
  </v-expand-transition> -->
  </div>
</template>

<script>
/* global Breadboard */
// import PairwiseCompare from "../components/PairwiseCompare";
import { Pie } from 'vue-chartjs';
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
  components: { Pie, SuggestionComponent },
  props: {
    player: Object,
  },
  filters: {
    nbspPartner(val) {
      return 'Your partner is a member of ' + val.replace(' ', '&nbsp;');
    },
    nbspYou(val) {
      return 'You are a member of ' + val.replace(' ', '&nbsp;');
    },
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
    toggleDescription: function () {
      document
        .querySelector('#descriptionContainer')
        .classList.toggle('d-none');
      document.querySelector('#showDescription').classList.toggle('d-none');
      document.querySelector('#showDescription').classList.toggle('d-block');
    },
    readyForGrouping() {
      Breadboard.send('readyForGrouping', {});
      document.querySelector('#readyBtn').classList.add('d-none');
      document.querySelector('#readyMsg').classList.remove('d-none');
    },
    goToChat() {
      this.showSplash = false;
      // document.querySelector('#partnerline').style.display = 'none';
      // setTimeout(() => {
      Breadboard.send('startCheapTalk', {});
      // }, 500);
    },
    goNext() {
      this.step++;
      document.body.scrollTop = 0; // For Safari
      document.documentElement.scrollTop = 0;
    },
    setLeft(data) {
      return data;
    },
    drawLine() {
      if (
        !document.querySelector('.ingroup') ||
        !document.querySelector('#partnerline')
      ) {
        return;
      }

      // let withinGroups = this.player.groupId == this.player.neighborNodes[0].groupId;
      let ocRect = document
        .querySelector('.outgroup .center-circle')
        .getBoundingClientRect();

      let icRect = document
        .querySelector('.ingroup .center-circle')
        .getBoundingClientRect();
      // var body = document.body;
      // var docEl = document.documentElement;

      // var scrollTop = window.pageYOffset || docEl.scrollTop || body.scrollTop;
      // var scrollLeft = window.pageXOffset || docEl.scrollLeft || body.scrollLeft;
      // var clientTop = docEl.clientTop || body.clientTop || 0;
      // var clientLeft = docEl.clientLeft || body.clientLeft || 0;

      let line = document.querySelector('#partnerline');
      // let angle = Math.tan(Math.abs(ocRect.x - icRect.x) / Math.abs(ocRect.y - icRect.y));
      let dist = Math.sqrt(
        Math.pow(ocRect.x - icRect.x, 2) + Math.pow(ocRect.y - icRect.y, 2)
      );

      line.style.width = dist - 2 + 'px';

      // line.style.top = 'calc(' + (icRect.y + scrollTop - clientTop ) + 'px + 2em)';
      // line.style.left = 'calc('+(icRect.x + scrollLeft - clientLeft) + 'px + 2em)';
      // line.style.transform = 'rotate('+(Math.PI/2 - angle)+'rad)';
    },
  },
  data() {
    return {
      slideIdx: 0,
      showSplash: true,
      groupingComplete: false,
      chartOptions: {
        responsive: false,
        maintainAspectRatio: false,
        plugins: {
          legend: {
            display: true,
            position: 'bottom',
            onClick: () => {},
          },
        },
      },
      chartHeight: 800,
      chartWidth: 400,
      preferenceHeadline: '',
      preferenceSummary: '',
      myPrefs: {},
      partnerPrefs: {},
      step: 1,
    };
  },
  computed: {
    slide() {
      return this.player.groupingQuestionRandomized[this.slideIdx];
    },
    order() {
      return this.player.groupingQuestionOrderRandomized[this.slideIdx];
    },
    chartData() {
      if (this.player.groupingType == 'value') {
        return {
          labels: ['High Concern', 'Low Concern'],
          datasets: [
            {
              data: this.player.groupingFactors,
              label: 'Level of concern about global warming',
              backgroundColor: ['#6C63FF', '#FFAD7D'],
            },
          ],
        };
      } else {
        return {
          labels: ['Pop Art', 'Abstract', 'Representational'],
          datasets: [
            {
              data: this.player.groupingFactors,
              label: 'Artists',
              backgroundColor: ['#6C63FF', '#FFAD7D', '#34B3B2'],
            },
          ],
        };
      }
    },
    discussionTopic() {
      return this.player.groupingType === 'artificial'
        ? 'what makes a painting beautiful'
        : 'global warming';
    },
    prompt() {
      return this.player.groupingType == 'value'
        ? 'How do you feel about the concept of "global warming"? Regarding it, what do you think U.S. policy should be?'
        : 'What makes a painting beautiful? How do you decide what is beautiful?';
    },
    valuesOrPreferences() {
      return this.player.groupingType == 'value' ? 'values' : 'preferences';
    },
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

        // Animation
        setTimeout(() => {
          document.querySelectorAll('.group-circle').forEach((el) => {
            el.classList.add('animate');
          });
          setTimeout(() => {
            this.drawLine();
          }, 100);
        }, 200);

        // Set and show grouping preferences
        let myPrefs = val.groupingPreferences;
        let partnerPrefs = val.neighborNodes[0].groupingPreferences;
        let betweenGroup = val.groupId != val.neighborNodes[0].groupId;

        this.myPrefs = [];
        this.partnerPrefs = [];

        for (idx in myPrefs) {
          if (
            (betweenGroup && myPrefs[idx] == partnerPrefs[idx]) ||
            (!betweenGroup && myPrefs[idx] != partnerPrefs[idx])
          ) {
            myPrefs[idx] = null;
            partnerPrefs[idx] = null;
          }
          // else if (this.myPrefs.length < 3) {
          //   this.myPrefs.push(window.texts.surveyPairs[idx][myPrefs[idx]]);
          //   this.partnerPrefs.push(window.texts[idx][partnerPrefs[idx]]);
          // }
        }
      }

      // Compute grouping preferences
      // var sortedFactors = []
      var factorOrder = [];
      var factorDifferences = [];
      var groupingFactors = [...this.player.groupingFactors];
      var factorToType = { 0: 'pop art', 1: 'abstract', 2: 'representational' };
      if (this.player.groupingType == 'value') {
        // Liberal, Conservative
        factorToType = { 0: 'is', 1: 'is not' };
      }
      // var factorSum = groupingFactors[0] + groupingFactors[1] + groupingFactors[2];

      // Get the ordered list of factors and difference between each factor
      // var factorCount = 3;
      for (let i = 0; i < this.player.groupingFactors.length; i++) {
        var idx = groupingFactors.indexOf(Math.max(...groupingFactors));
        factorOrder.push(idx);
        groupingFactors[idx] = Number.MIN_SAFE_INTEGER;
      }
      groupingFactors = [...this.player.groupingFactors];

      factorDifferences.push(
        groupingFactors[factorOrder[1]] / groupingFactors[factorOrder[0]]
      );

      if (this.player.groupingType == 'artificial') {
        factorDifferences.push(
          groupingFactors[factorOrder[2]] / groupingFactors[factorOrder[1]]
        );
        if (factorDifferences[0] + factorDifferences[1] > 1.8) {
          this.preferenceHeadline =
            'you equally like Abstract, Pop Art, and Representational';
        } else if (factorDifferences[0] > 0.9) {
          this.preferenceHeadline =
            'a preference for both ' +
            factorToType[factorOrder[0]] +
            ' and ' +
            factorToType[factorOrder[1]];
        } else {
          this.preferenceHeadline =
            'a preference for ' + factorToType[factorOrder[0]];
        }
      } else {
        if (factorDifferences[0] == 1) {
          this.preferenceHeadline = 'multiple factors';
        } else {
          this.preferenceHeadline = factorToType[factorOrder[0]];
        }
      }

      let returnString = '';

      // for(let i = 0; i < this.player.surveyFactors.length; i++) {
      //   let pairIdx = this.player.groupingPreferences[i];
      //   let answerFactors = (this.player.groupingType == 'value') ? this.player.surveyFactors[i][pairIdx] : this.player.imageFactors[i][pairIdx];
      //   sortedFactors.push(
      //     {
      //       'key': i,
      //       'factors': answerFactors,
      //       'prompt': this.player.surveyPairs[i][pairIdx],
      //       'image': this.player.imagePairs[i][pairIdx],
      //       'sort_type': factorOrder.indexOf(answerFactors.indexOf(Math.max(...answerFactors))),
      //       'sort_strength': Math.max(...answerFactors)
      //     }
      //   );
      // }

      // sortedFactors.sort((a,b) => (a.sort_type - b.sort_type || b.sort_strength - a.sort_strength));

      // var normalizedPlayerFactors = [...this.player.groupingFactors];
      // normalizedPlayerFactors = normalizedPlayerFactors.map( el => el / Math.max(...normalizedPlayerFactors) );

      // var returnString = '';
      // if (this.player.groupingType == 'value') {
      //   returnString += '<ol>';
      // }
      // for (let i = 0; i < sortedFactors.length; i++) {
      //   var disagreementSum = normalizedPlayerFactors.map( (el, idx) => Math.abs(el-sortedFactors[i]['factors'][idx]) ).reduce((a,b) => a+b, 0);
      //   var agreement = (1 - (disagreementSum / factorCount)) * 100;
      //   if (this.player.groupingType == 'value') {
      //   returnString += '<li>'+ sortedFactors[i]['prompt'] + ' (' + agreement.toLocaleString('en-US', {maximumSignificantDigits: 2}) + '% match)</li> ';
      //   } else {
      //     returnString += '<div class="'+((agreement > 50) ? 'majority': 'test')+'"><v-img src="../assets/'+sortedFactors[i]['image']+'" width="8rem" height="8rem" contain></v-img>'+ ' ' + agreement.toLocaleString('en-US', {maximumSignificantDigits: 2}) + '% match</div>';
      //   }
      // }
      // if (this.player.groupingType == 'value') {
      //   returnString += '</ol>';
      // }
      this.preferenceSummary = returnString;
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
  /* position: absolute; */
  /* top: 0; */
  /* left: 0; */
  /* width: 100%; */
  /* height: 100%; */
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
  -webkit-animation: myOrbit 4s ease-in-out forwards; /* Chrome, Safari 5 */
  -moz-animation: myOrbit 4s ease-in-out forwards; /* Firefox 5-15 */
  -o-animation: myOrbit 4s ease-in-out forwards; /* Opera 12+ */
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
  -webkit-animation: myOrbit 4s ease-in-out forwards; /* Chrome, Safari 5 */
  -moz-animation: myOrbit 4s ease-in-out forwards; /* Firefox 5-15 */
  -o-animation: myOrbit 4s ease-in-out forwards; /* Opera 12+ */
  animation: myOrbit 4s ease-in-out forwards; /* Chrome, Firefox 16+, 
                                                      IE 10+, Safari 5 */
}
.c2.animate {
  -webkit-animation: myOrbit1 4s ease-in-out forwards; /* Chrome, Safari 5 */
  -moz-animation: myOrbit1 4s ease-in-out forwards; /* Firefox 5-15 */
  -o-animation: myOrbit1 4s ease-in-out forwards; /* Opera 12+ */
  animation: myOrbit1 4s ease-in-out forwards; /* Chrome, Firefox 16+, 
                                                      IE 10+, Safari 5 */
}
.c3.animate {
  -webkit-animation: myOrbit2 4s ease-in-out forwards; /* Chrome, Safari 5 */
  -moz-animation: myOrbit2 4s ease-in-out forwards; /* Firefox 5-15 */
  -o-animation: myOrbit2 4s ease-in-out forwards; /* Opera 12+ */
  animation: myOrbit2 4s ease-in-out forwards; /* Chrome, Firefox 16+, 
                                                      IE 10+, Safari 5 */
}
.c4.animate {
  -webkit-animation: myOrbit3 4s ease-in-out forwards; /* Chrome, Safari 5 */
  -moz-animation: myOrbit3 4s ease-in-out forwards; /* Firefox 5-15 */
  -o-animation: myOrbit3 4s ease-in-out forwards; /* Opera 12+ */
  animation: myOrbit3 4s ease-in-out forwards; /* Chrome, Firefox 16+, 
                                                      IE 10+, Safari 5 */
}
.c5.animate {
  -webkit-animation: myOrbit4 4s ease-in-out forwards; /* Chrome, Safari 5 */
  -moz-animation: myOrbit4 4s ease-in-out forwards; /* Firefox 5-15 */
  -o-animation: myOrbit4 4s ease-in-out forwards; /* Opera 12+ */
  animation: myOrbit4 4s ease-in-out forwards; /* Chrome, Firefox 16+, 
                                                      IE 10+, Safari 5 */
}

.group-circles {
  -webkit-animation: myOrbit 4s ease-in-out forwards; /* Chrome, Safari 5 */
  -moz-animation: myOrbit 4s ease-in-out forwards; /* Firefox 5-15 */
  -o-animation: myOrbit 4s ease-in-out forwards; /* Opera 12+ */
  animation: myOrbit 4s ease-in-out forwards; /* Chrome, Firefox 16+, 
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
