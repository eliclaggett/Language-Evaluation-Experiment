<template>
  <v-col cols="6">
    <v-card
      height="38vh"
      rounded="lg"
      color="#EEEEEE"
      elevation="0"
      class="pa-2"
    >
      <draggable
        :group="{ name: 'items', put: false }"
        :sort="false"
        draggable=".item"
        :animation="200"
        ghost-class="moving-card"
      >
        <v-card
          v-for="item in items"
          :key="item.id"
          class="item"
          color="#FFFFFF"
          elevated-2
          style="padding: 1vw; margin: 1.2vw; height: 9vh"
          :style="{ 'border-left': '1.75vw solid' + item.color }"
        >
          {{ item.text }}
        </v-card>
      </draggable>
    </v-card>
  </v-col>
</template>

<script>
export default {
  name: 'Numbers',

  components: {
    draggable: window.vuedraggable,
  },

  props: {
    items: Array,
  },
};
</script>
