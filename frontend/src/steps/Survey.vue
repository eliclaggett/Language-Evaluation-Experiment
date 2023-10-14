<template>
  <v-container class="cols-sm-6 push-10" justify-center>
    <v-row class="survey">
      <v-col cols="8">
        <h2>Task Summary</h2>
        <p class="ma-0">{{ remainingTime }} to finish all steps.</p>

        <div v-if="step === 1">
          <p>
            Please answer these questions in at least a few sentences. (1/2)
          </p>
          <v-form>
            <div class="followup-reminder">
              <h3>You were asked this follow-up question during the chat:</h3>
              <v-card>
                <v-card-text>{{ followupQuestion }}</v-card-text>
              </v-card>
            </div>

            <h3>
              What was your partner's answer to this question?
              <small>(required)</small>
            </h3>
            <v-textarea
              v-model="summary"
              label=""
              auto-grow
              outlined
            ></v-textarea>
            <h3>
              What is your partner's opinion about the conversation topic as a
              whole? <small>(required)</small>
            </h3>
            <v-textarea
              v-model="reasoning"
              label=""
              auto-grow
              outlined
            ></v-textarea>
            <h3>
              How would you rate your feelings toward your partner?
              <small>(required)</small>
            </h3>
            <div class="slider-container">
              <v-slider
                vertical
                v-model="rating"
                class="thermometer"
                step="1"
                min="0"
                max="6"
                length="500"
              ></v-slider>
              <div class="slider-labels">
                <span :class="bold6" @click="moveSlider(6)"
                  >Very warm - I wish I knew this person in real life</span
                >
                <span :class="bold5" @click="moveSlider(5)">Warm</span>
                <span :class="bold4" @click="moveSlider(4)">Slightly warm</span>
                <span :class="bold3" @click="moveSlider(3)">No feeling</span>
                <span :class="bold2" @click="moveSlider(2)">Slightly cold</span>
                <span :class="bold1" @click="moveSlider(1)">Cold</span>
                <span :class="bold0" @click="moveSlider(0)"
                  >Very cold - I never want to speak to this person again</span
                >
              </div>
            </div>
            <!-- <v-rating
            hover
            length="5"
            size="64"
            color="secondary"
            background-color="primary"
            @input="addRating($event)"
          ></v-rating> -->
          </v-form>
          <div
            v-if="player.rejectedSuggestions.length > 0"
            class="rejectionExplanation"
          >
            <h3>You rejected some reply suggestions</h3>
            <p>For each one, could you explain why you chose to reject them?</p>
            <div
              v-for="suggestion in player.rejectedSuggestions"
              :key="suggestion"
            >
              <h3>
                Why did you reject "{{ suggestion | suggestionIdToName }}"? What
                suggestion would sound better to you? (required)
              </h3>
              <v-textarea
                v-model="rejected[suggestion]"
                :key="suggestion"
                label=""
                auto-grow
                outlined
              ></v-textarea>
            </div>
          </div>
        </div>
        <div v-if="step === 2">
          <p>Please answer the following questions. (2/2)</p>
          <v-row>
            <v-col cols="6">
              <v-subheader>
                <h3>
                  What is your political affiliation? <small>(required)</small>
                </h3>
              </v-subheader>
            </v-col>
            <v-col cols="6">
              <v-select
                v-model="politics"
                item-text="label"
                item-value="value"
                :items="items_politics"
                label=""
                return-object
              ></v-select>
            </v-col>
          </v-row>
          <v-row>
            <v-col cols="6">
              <v-subheader>
                <h3>What is your gender?</h3>
              </v-subheader>
            </v-col>
            <v-col cols="6">
              <v-select
                v-model="gender"
                item-text="label"
                item-value="value"
                :items="items_gender"
                label=""
                return-object
              ></v-select>
            </v-col>
          </v-row>
          <v-row>
            <v-col cols="6">
              <v-subheader>
                <h3>Which category includes your age?</h3>
              </v-subheader>
            </v-col>
            <v-col cols="6">
              <v-select
                v-model="age"
                item-text="label"
                item-value="value"
                :items="items_age"
                label=""
                return-object
              ></v-select>
            </v-col>
          </v-row>
          <v-row>
            <v-col cols="6">
              <v-subheader>
                <h3>How would you identify your race and ethnicity?</h3>
              </v-subheader>
            </v-col>
            <v-col cols="6">
              <v-select
                v-model="race"
                item-text="label"
                item-value="value"
                :items="items_race"
                label=""
                return-object
              ></v-select>
            </v-col>
          </v-row>

          <v-row>
            <v-col cols="6">
              <v-subheader>
                <h3>
                  What is the highest grade of school or year of college you
                  completed?
                </h3>
              </v-subheader>
            </v-col>
            <v-col cols="6">
              <v-select
                v-model="education"
                item-text="label"
                item-value="value"
                :items="items_education"
                label=""
                return-object
              ></v-select>
            </v-col>
          </v-row>

          <v-row>
            <v-col cols="6">
              <v-subheader>
                <h3>
                  About what wage and salary did you receive in the last year
                  (including any type of income such as pension)?
                </h3>
              </v-subheader>
            </v-col>
            <v-col cols="6">
              <v-select
                v-model="income"
                item-text="label"
                item-value="value"
                :items="items_income"
                label=""
                return-object
              ></v-select>
            </v-col>
          </v-row>
        </div>
        <div class="push-10">
          <v-btn @click="clickBack" :disabled="step < 2" class="mr-2"
            >Back</v-btn
          >
          <v-btn
            @click="clickNext"
            v-if="step < maxSteps"
            :disabled="!formValid"
            >Next</v-btn
          >
          <v-btn v-else @click="submitSurvey" :disabled="!formValid"
            >Next</v-btn
          >
        </div>
      </v-col>
      <v-col cols="4">
        <h2>Chat Log</h2>
        <div id="chat-log" v-html="messageLog"></div>
      </v-col>
    </v-row>
  </v-container>
</template>

<script>
/* global Breadboard */

export default {
  name: 'SurveyStep',
  filters: {
    partnerName(val) {
      if (this.player.chatId == val) {
        return 'Me: ';
      } else {
        return 'Partner: ';
      }
    },
    suggestionIdToName(val) {
      let key = val.substring(0, val.length - 1);
      console.log(window.texts.suggestionskey);
      let idx = parseInt(val.substring(val.length - 1));

      if (key in window.texts.suggestions) {
        return window.texts.suggestions[key][idx];
      } else if (key in window.texts.botSuggestions) {
        return window.texts.botSuggestions[key];
      }

      return 'Unknown';
    },
  },
  data() {
    return {
      maxSteps: 2,
      step: 1,
      rating: 3,
      summary: undefined,
      reasoning: undefined,
      partnerRating: -1,
      rejected: {
        greeting0: '',
        greeting1: '',
        encouragement0: '',
        encouragement1: '',
        express_interest0: '',
        express_interest1: '',
        active_listen00: '',
        active_listen01: '',
        active_listen10: '',
        active_listen11: '',
        gratitude0: '',
        gratitude1: '',
        farewell0: '',
        farewell1: '',
        anger_management0: '',
        anger_management1: '',
      },
      politics: { label: undefined, value: undefined },
      gender: { label: undefined, value: undefined },
      age: { label: undefined, value: undefined },
      race: { label: undefined, value: undefined },
      education: { label: undefined, value: undefined },
      income: { label: undefined, value: undefined },
      items_politics: [
        { label: 'Left', value: 'left' },
        { label: 'Left-leaning', value: 'left-leaning' },
        { label: 'Center', value: 'center' },
        { label: 'Right-leaning', value: 'right-leaning' },
        { label: 'Right', value: 'right' },
        { label: 'Other', value: 'other' },
        { label: 'Decline to answer', value: 'no_answer_politics' },
      ],
      items_gender: [
        { label: 'Female', value: 'female' },
        { label: 'Male', value: 'male' },
        { label: 'Non-binary', value: 'non-binary' },
        { label: 'Other', value: 'other' },
        { label: 'Decline to answer', value: 'no_answer_gender' },
      ],
      items_age: [
        { label: '18-29', value: '18-29' },
        { label: '30-39', value: '30-39' },
        { label: '40-49', value: '40-49' },
        { label: '50-59', value: '50-59' },
        { label: '60 or older', value: 'over60' },
        { label: 'Decline to answer', value: 'no_answer_age' },
      ],
      items_race: [
        { label: 'Asian / Pacific Islander', value: 'asian' },
        { label: 'Black / African American', value: 'black' },
        { label: 'Hispanic / Latino', value: 'hispanic' },
        { label: 'White, Caucasian, European; not Hispanic', value: 'white' },
        { label: 'American Indian / Native American', value: 'native' },
        { label: 'Multiple ethnicities', value: 'multi' },
        { label: 'Other', value: 'other_ethnicity' },
        { label: 'Decline to answer', value: 'no_answer_race' },
      ],
      items_education: [
        { label: 'High school or less', value: 'high_school' },
        { label: 'Some college (1-3 years)', value: 'some_college' },
        { label: 'College graduate (Bachelors)', value: 'college' },
        { label: 'Masters', value: 'masters' },
        { label: 'Above Masters degree', value: 'gt_masters' },
        { label: 'Other', value: 'other_education' },
        { label: 'Decline to answer', value: 'no_answer_education' },
      ],
      items_income: [
        { label: 'Less than $20,000', value: 'less_20000' },
        { label: '$20,000 - $34,999', value: '20000_34999' },
        { label: '$35,000 - $49,999', value: '35000_49999' },
        { label: '$50,000 - $74,999', value: '50000_74999' },
        { label: '$75,000 - $99,999', value: '75000_99999' },
        { label: 'Over $100,000', value: '100000_or_greater' },
        { label: 'Decline to answer', value: 'no_answer_income' },
      ],
    };
  },

  computed: {
    bold0() {
      return this.rating == '0' ? 'bold' : '';
    },
    bold1() {
      return this.rating == '1' ? 'bold' : '';
    },
    bold2() {
      return this.rating == '2' ? 'bold' : '';
    },
    bold3() {
      return this.rating == '3' ? 'bold' : '';
    },
    bold4() {
      return this.rating == '4' ? 'bold' : '';
    },
    bold5() {
      return this.rating == '5' ? 'bold' : '';
    },
    bold6() {
      return this.rating == '6' ? 'bold' : '';
    },
    followupQuestion() {
      return window.texts.customFollowups[parseInt(this.player.topic)];
    },
    formValid() {
      let valid = true;
      if (this.step == 1) {
        if (this.summary === undefined || this.reasoning == undefined) {
          valid = false;
        }
      } else if (this.step == 2) {
        if (this.politics.value === undefined) {
          valid = false;
        }
      }
      return valid;
    },
    messageLog() {
      let html = '';
      if (this.player.messagesChat.length > 0) {
        for (let rawMsg of this.player.messagesChat) {
          let msg = JSON.parse(rawMsg);
          let isPartner = msg.participantId != this.player.chatId;
          let playerName = 'Partner';
          if (msg.participantId == -1) playerName = 'Evaluator';
          if (msg.participantId == this.player.chatId) playerName = 'Me';
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
    remainingTime() {
      if (this.player.surveyStartTime) {
        var curTime = Date.now() + this.player.utcOffset;
        var endTime =
          (this.player.surveyStartTime + this.player.surveyTime * 60) * 1000;

        var diff = (endTime - curTime) / 1000;
        if (diff < 60) {
          return '<1 min remaining';
        } else if (diff < 90) {
          return '1 min remaining';
        } else {
          return Math.round(diff / 60) + ' mins remaining';
        }
        // var endTime = new Intl.DateTimeFormat('en-US', { timeStyle: 'short' }).format((new Date((this.player.chatTime * 60 + this.player.chatStartTime) * 1000)));
        // return 'until ' + endTime;
      } else {
        return '';
      }
    },
  },

  props: {
    player: Object,
  },

  methods: {
    clickNext() {
      this.step++;
    },
    myCounter(data) {
      let numWords = data ? data.split(' ').length : 0;
      let remaining = 15 - numWords;
      return remaining > 0
        ? 'Write at least ' + remaining + ' more words.'
        : '';
    },
    clickBack() {
      this.step--;
    },
    submitSurvey() {
      const data = {};
      data['summary'] = this.summary;
      data['reasoning'] = this.reasoning;
      data['partnerRating'] = this.rating;
      data['gender'] = this.gender.value;
      data['age'] = this.age.value;
      data['race'] = this.race.value;
      data['education'] = this.education.value;
      data['income'] = this.income.value;
      data['politics'] = this.politics.value;
      data['rejected'] = this.rejected;

      Breadboard.send('completeSurvey', data);
    },
    addRating(value) {
      this.partnerRating = value;
    },
    moveSlider(value) {
      this.rating = value;
    },
  },
  watch: {
    player(player) {
      if (player.forceSubmitSurvey) {
        this.submitSurvey();
      }
    },
  },
};
</script>
<style>
.followup-reminder {
  margin-bottom: 22px;
}
.followup-reminder h3 {
  margin-bottom: 0.5em;
}
/* Thermometer */
.slider-container {
  display: flex;
  margin-left: 1em;
  margin-top: 1em;
  margin-bottom: 1em;
  height: 16em;
  justify-content: stretch;
  align-items: stretch;
  align-content: stretch;
}
.bold {
  font-weight: bold;
}
.slider-labels {
  display: flex;
  flex-direction: column;
  justify-content: space-between;
  text-align: left;
  width: auto;
  flex-grow: 0;
  margin-left: 1.5em;
  /* height: calc(100% - 1em); */
}
.slider-labels span {
  cursor: pointer;
}
.thermometer {
  width: auto;
  flex-grow: 0;
}
.thermometer .v-slider--vertical,
.thermometer .v-input__slot {
  height: 100%;
}
.thermometer .v-input__slot {
  padding: 0.75em 0;
}
.v-slider--vertical .v-slider__track-container {
  width: 0.5em;
  border-radius: 0.5em;
  background: linear-gradient(rgb(255, 112, 112), rgb(85, 146, 255)) !important;
}
.v-application .thermometer .primary.v-slider__track-background,
.v-application .thermometer .v-slider__track-fill {
  background-color: rgba(0, 0, 0, 0) !important;
}
.thermometer .v-slider .v-slider__track-container:after {
  content: '';
  width: 1em;
  height: 1em;
  border-radius: 1em;
  background-color: rgb(85, 146, 255);
  position: absolute;
  z-index: 5;
  bottom: -0.5em;
  left: -0.25em;
}
.thermometer .v-slider .v-slider__thumb.primary {
  background-color: rgba(0, 0, 0, 0.6) !important;
  width: 1.2em;
  height: 1.2em;
  left: -0.6em;
}
.v-slider__thumb:before {
  left: -0.6em;
  top: -0.6em;
  z-index: 0;
  /* display: none; */
}

/* Chat log */
#chat-log {
  background: #eee;
  border-radius: 4px;
  padding: 1em;
  overflow-y: scroll;
  flex-grow: 1;
  height: 100%;
  max-height: 20em;
}
#chat-log::-webkit-scrollbar {
  width: 12px;
}

#chat-log::-webkit-scrollbar-track {
  background: #ddd;
}

#chat-log::-webkit-scrollbar-thumb {
  border-radius: 10px;
  background: #ccc;
}

#chat-log .partner p {
  color: #333;
}
#chat-log p {
  margin: 0.25em 0;
  color: rgb(0, 0, 206);
}
#chat-log p:first-child {
  color: #747474;
  text-transform: uppercase;
  font-size: 0.85em;
}
#chat-log p:last-child {
  margin-bottom: 1rem;
}
</style>
