<!-- Initial survey for determining a participant's stance toward various topics -->
<template>
  <div>
    <v-container v-if="player.groupingComplete === false">
      <v-row class="pa-16">
        <h1 class="center text-center" v-if="player.groupingType === 'artificial'">
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
          <LikertQuestion :prompt="window.texts.likertQuestions[slide]" @chooseItem="chooseItem($event)" />
        </v-col>
      </v-row>
    </v-container>
  </div>
</template>

<script>
/* global Breadboard */
import LikertQuestion from '../../components/LikertQuestion.vue';

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
    chooseItem: function (data) {
      // Record participant's choice
      Breadboard.send('answerInitialSurvey', [this.slide, data]);
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
      } else {
        Breadboard.send('completeInitialSurvey');
      }
    },
    drawLine() {
      // Grouping complete animation
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
      let dist = Math.sqrt(
        Math.pow(ocRect.x - icRect.x, 2) + Math.pow(ocRect.y - icRect.y, 2)
      );

      line.style.width = dist - 2 + 'px';

      line.style.top =
        'calc(' + (icRect.y + scrollTop - clientTop) + 'px + 2em)';
      line.style.left =
        'calc(' + (icRect.x + scrollLeft - clientLeft) + 'px + 2em)';
    },
  },
  data() {
    return {
      slideIdx: 0,
      groupingComplete: false
    };
  },
  computed: {
    slide() {
      return this.player.groupingQuestionRandomized[this.slideIdx];
    },
    order() {
      return this.player.groupingQuestionOrderRandomized[this.slideIdx];
    }
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
    },
  },
};
</script>

<style>
#groupAssignedMsg {
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
  /* background: url('../assets/group0.png'); */
  background-size: cover;
  background-position: center;
}

.group1 .group-circle {
  background: teal;
}

.group1 .center-circle {
  /* background: url('../assets/group1.png'); */
  background-size: cover;
  background-position: center;
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
