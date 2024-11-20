<!--
Filename: LikertQuestion.vue
Author: Elijah Claggett
Description: Likert-scale survey question UI Element
-->
<template>
  <v-container class="likert">
    <v-row align="center">
      <h2 class="center text-center">{{ prompt }}</h2>
    </v-row>
    <v-row align="center" :class="thisClass">
      <v-col cols="1"></v-col>
      <v-col cols="2">
        <PairItem
          itemValue="Strongly disagree"
          itemType="text"
          itemIdx="1"
          @chooseItem="chooseItem(0)"
        />
      </v-col>
      <v-col cols="2">
        <PairItem
          itemValue="Disagree"
          itemType="text"
          itemIdx="1"
          @chooseItem="chooseItem(1)"
        />
      </v-col>
      <v-col cols="2">
        <PairItem
          itemValue="Somewhat disagree"
          itemType="text"
          itemIdx="1"
          @chooseItem="chooseItem(2)"
        />
      </v-col>
      <v-col cols="2">
        <PairItem
          itemValue="Neutral"
          itemType="text"
          itemIdx="1"
          @chooseItem="chooseItem(3)"
        />
      </v-col>
      <v-col cols="2">
        <PairItem
          itemValue="Somewhat agree"
          itemType="text"
          itemIdx="1"
          @chooseItem="chooseItem(4)"
        />
      </v-col>
      <v-col cols="2">
        <PairItem
          itemValue="Agree"
          itemType="text"
          itemIdx="1"
          @chooseItem="chooseItem(5)"
        />
      </v-col>
      <v-col cols="2">
        <PairItem
          itemValue="Strongly agree"
          itemType="text"
          itemIdx="1"
          @chooseItem="chooseItem(6)"
        />
      </v-col>
      <v-col cols="1"></v-col>
    </v-row>
  </v-container>
</template>

<script>
import PairItem from './PairItem.vue';

export default {
  name: 'LikertQuestion',

  props: {
    prompt: String,
  },

  components: {
    PairItem,
  },

  data: () => ({
    enforcingClickDelay: false,
  }),

  computed: {
    thisClass() {
      return this.enforcingClickDelay
        ? 'mt-12 disabled questionRow'
        : 'mt-12 questionRow';
    },
  },

  methods: {
    chooseItem: function (item) {
      if (!this.enforcingClickDelay) {
        this.$emit('chooseItem', item);
      }

      // Force a delay before the next question can be answered
      this.enforcingClickDelay = true;
      setTimeout(() => {
        this.enforcingClickDelay = false;
      }, 1000);
    },
  },
};
</script>
<style>
.questionRow.align-center {
  align-items: stretch !important;
  flex-wrap: nowrap;
}

.questionRow.align-center .col {
  padding: 0 0.1rem;
  flex-shrink: 1;
}

.questionRow .v-card {
  height: 100%;
  padding: 2em 0.5em !important;
  font-size: 0.9rem;
}

.mt-12.disabled .v-card {
  opacity: 0.3;
  cursor: initial;
  pointer-events: none;
}
</style>
