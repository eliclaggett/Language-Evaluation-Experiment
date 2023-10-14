<template>
  <div>
    <v-container
      class="center text-center pt-10"
      v-if="!clickedContinue && player.groupingComplete === false"
    >
  


      <h1>Done!</h1>
      <p class="pt-4 pb-4">
        <!-- Next, please complete a short survey so we can assign you a suitable
        partner. -->
        Thanks so much your time! That's all I need :)
      </p>
      <!-- <v-btn @click="clickContinue">Continue</v-btn> -->
    </v-container>
    <v-container v-if="clickedContinue && player.groupingComplete === false">
      <v-row class="pa-16">
        <h1
          class="center text-center"
          v-if="player.groupingType === 'artificial'"
        >
          Click on the painting you like better ({{
            slideIdx + 1
          }}&nbsp;/&nbsp;{{ player.imagePairs.length }})
        </h1>
        <h1 class="center text-center" v-if="player.groupingType === 'value'">
          How do you feel about this statement? ({{
            slideIdx + 1
          }}&nbsp;/&nbsp;{{ window.texts.likertQuestions.length }})
        </h1>
      </v-row>
      <v-row>
        <v-col cols="12" class="center">
          <LikertQuestion
            :prompt="window.texts.likertQuestions[slide]"
            @chooseItem="chooseItem($event)"
          />
        </v-col>
      </v-row>
      <!-- <v-row>
        <v-col xl="8" class="center">
        <PairwiseCompare v-if="player.groupingType==='artificial'" :player="player"
          :option1="player.imagePairs[slide][0]" :option2="player.imagePairs[slide][1]" itemType="image" :order="order"
          @chooseItem="chooseItem($event)" />
        <PairwiseCompare v-if="player.groupingType==='value'" :player="player" :option1="player.surveyPairs[slide][0]"
          :option2="player.surveyPairs[slide][1]" itemType="text" :order="order" @chooseItem="chooseItem($event)" />
        </v-col>
      </v-row> -->
    </v-container>
  </div>
</template>

<script>
/* global Breadboard */
// import PairwiseCompare from "../components/PairwiseCompare";
import LikertQuestion from '../components/LikertQuestion.vue';

export default {
  name: 'GroupingStep',
  components: { LikertQuestion },
  props: {
    player: Object,
  },
  mounted() {
    window.addEventListener('resize', this.drawLine);
  },
  methods: {
    clickContinue() {
      this.clickedContinue = true;
    },
    chooseItem: function (data) {
      Breadboard.send('chooseGroupingPreference', [this.slide, data]);
      if (
        this.player.groupingType === 'artificial' &&
        this.slideIdx < this.player.imagePairs.length - 1
      )
        this.slideIdx++;
      else if (
        this.player.groupingType === 'value' &&
        this.slideIdx < window.texts.likertQuestions.length - 1
      ) {
        this.slideIdx++;
        console.log(this.slideIdx);
        console.log(window.texts.likertQuestions.length);
      } else {
        console.log('complete grouping');
        Breadboard.send('completeGrouping');
      }
    },
    setLeft(data) {
      return data;
    },
    drawLine() {
      if (!document.querySelector('.ingroup')) {
        return;
      }

      let withinGroups =
        this.player.groupId == this.player.neighborNodes[0].groupId;
      let ocRect = withinGroups
        ? document.querySelector('.ingroup .c1').getBoundingClientRect()
        : document
            .querySelector('.outgroup .center-circle')
            .getBoundingClientRect();

      let icRect = document
        .querySelector('.ingroup .center-circle')
        .getBoundingClientRect();
      var body = document.body;
      var docEl = document.documentElement;

      var scrollTop = window.pageYOffset || docEl.scrollTop || body.scrollTop;
      var scrollLeft =
        window.pageXOffset || docEl.scrollLeft || body.scrollLeft;
      var clientTop = docEl.clientTop || body.clientTop || 0;
      var clientLeft = docEl.clientLeft || body.clientLeft || 0;

      let line = document.querySelector('#partnerline');
      // let angle = Math.tan(Math.abs(ocRect.x - icRect.x) / Math.abs(ocRect.y - icRect.y));
      let dist = Math.sqrt(
        Math.pow(ocRect.x - icRect.x, 2) + Math.pow(ocRect.y - icRect.y, 2)
      );

      line.style.width = dist - 2 + 'px';

      line.style.top =
        'calc(' + (icRect.y + scrollTop - clientTop) + 'px + 2em)';
      line.style.left =
        'calc(' + (icRect.x + scrollLeft - clientLeft) + 'px + 2em)';
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
      clickedContinue: false,
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
          labels: ['Individual', 'Cultural', 'Societal'],
          datasets: [
            {
              data: this.player.groupingFactors,
              label: 'Factors causing poverty',
              backgroundColor: ['#66CCEE', '#EE6677', '#CCBB44'],
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
              backgroundColor: ['#66CCEE', '#EE6677', '#CCBB44'],
            },
          ],
        };
      }
    },
    discussionTopic() {
      return this.player.groupingType === 'artificial'
        ? 'what makes a painting beautiful'
        : 'the cause of poverty';
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
          }, 4000);
        }, 200);
      }

      // Compute grouping preferences
      // var sortedFactors = []
      var factorOrder = [];
      var factorDifferences = [];
      var groupingFactors = [...this.player.groupingFactors];
      var factorToType = { 0: 'pop art', 1: 'abstract', 2: 'representational' };
      if (this.player.groupingType == 'value') {
        // Personal deficiency, stigma (culture of poverty), structural
        factorToType = {
          0: 'personal deficiency',
          1: 'a culture of poverty',
          2: 'societal failure',
        };
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
      factorDifferences.push(
        groupingFactors[factorOrder[2]] / groupingFactors[factorOrder[1]]
      );

      if (this.player.groupingType == 'artificial') {
        if (factorDifferences[0] + factorDifferences[1] > 1.8) {
          this.preferenceHeadline = 'Abstract, Pop Art, and Representational';
        } else if (factorDifferences[0] > 0.9) {
          this.preferenceHeadline =
            'You prefer both ' +
            factorToType[factorOrder[0]] +
            ' and ' +
            factorToType[factorOrder[1]] +
            ' paintings';
        } else {
          this.preferenceHeadline =
            'You prefer ' + factorToType[factorOrder[0]] + ' paintings';
        }
      } else {
        if (factorDifferences[0] + factorDifferences[1] > 1.8) {
          this.preferenceHeadline =
            'We estimate that you attribute diverse causes to poverty has diverse causes.';
        } else if (factorDifferences[0] > 0.9) {
          this.preferenceHeadline =
            'You believe both that' +
            factorToType[factorOrder[0]] +
            ' and ' +
            factorToType[factorOrder[1]] +
            ' cause poverty';
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
}
.group0 .group-circle {
  background: green;
}
.group0 .center-circle {
  background: url('../assets/group0.png');
  background-size: cover;
  background-position: center;
}
.group1 .group-circle {
  background: teal;
}
.group1 .center-circle {
  background: url('../assets/group1.png');
  background-size: cover;
  background-position: center;
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
