<!--
Filename: PairwiseCompare.vue
Author: Elijah Claggett
Description: Binary Choice Input UI Element
-->
<template>
  <v-container>
    <v-row align="center">
      <v-col cols="6">
        <PairItem
          :itemValue="left"
          :itemType="itemType"
          itemIdx="1"
          @chooseItem="chooseItem(0)"
        />
      </v-col>

      <v-col cols="6">
        <PairItem
          :itemValue="right"
          :itemType="itemType"
          itemIdx="2"
          @chooseItem="chooseItem(1)"
        />
      </v-col>
    </v-row>
  </v-container>
</template>

<script>
import PairItem from './PairItem';

export default {
  name: 'PairwiseCompare',

  props: {
    player: Object,
    option1: String,
    option2: String,
    order: Number,
    itemType: String,
  },

  components: {
    PairItem,
  },
  computed: {
    left() {
      return this.order == 0 ? this.option1 : this.option2;
    },
    right() {
      return this.order == 0 ? this.option2 : this.option1;
    },
  },

  methods: {
    chooseItem: function (item) {
      let realItem =
        (this.order == 0 && item == 0) || (this.order == 1 && item == 1)
          ? 0
          : 1;
      this.$emit('chooseItem', realItem);
    },
  },
};
</script>

<style></style>
