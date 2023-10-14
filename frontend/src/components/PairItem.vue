<template>
  <v-hover v-slot="{ hover }" ref="myhover">
    <v-card
      rounded="lg"
      color="#EEEEEE"
      :class="`elevation-${hover ? 24 : 6}`"
      class="d-flex flex-column justify-space-between align-center pa-10 transition-swing"
      @click="chooseA"
      ref="thiscard"
    >
      <v-img
        v-if="itemType == 'image'"
        :src="require(`../assets/${itemValue}`)"
        height="20rem"
        contain
      ></v-img>
      <span class="text-center" v-if="itemType == 'text'">{{ itemValue }}</span>
      <v-radio-group v-model="radioGroup">
        <v-radio
          readonly
          active-class="0"
          :off-icon="hover ? '$radioOn' : '$radioOff'"
        ></v-radio>
      </v-radio-group>
    </v-card>
  </v-hover>
</template>

<script>
export default {
  name: 'PairItem',
  props: ['itemValue', 'itemType', 'itemIdx'],
  data() {
    return {
      radioGroup: 0,
    };
  },
  methods: {
    chooseA: function () {
      this.$emit('chooseItem');
    },
  },
  computed: {
    imageSrc() {
      return '../assets/' + this.imageName;
    },
  },
  filters: {
    nbsp(val) {
      return val.replace(' ', '\u00a0');
    },
  },
};
</script>
<style>
.v-card {
  background: #fff !important;
}
.v-card:before {
  background: none;
}
.v-card .v-input--selection-controls__input {
  margin: 0;
}
</style>
