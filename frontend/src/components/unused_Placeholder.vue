<template>
  <v-card
    height="83vh"
    width="21.5vw"
    rounded="lg"
    color="#EEEEEE"
    elevation="0"
    class="pa-2"
    style="margin-left: 4vw"
  >
    <div align="center">
      <v-card
        shaped
        color="#FFFFFF"
        outlined
        style="
          border: 1px solid #e15759;
          padding: 1vw;
          margin: 1vw;
          max-width: 60%;
        "
      >
        Your Initial Rank
      </v-card>
    </div>
    <div class="rankedItemContainer">
      <div class="placeholders">
        <v-card
          color="#EEEEEE"
          style="
            padding: 1vw;
            margin: 1.2vw;
            height: 9vh;
            box-shadow: inset 0 0 15px #dddddd;
          "
        ></v-card>
        <v-card
          color="#EEEEEE"
          style="
            padding: 1vw;
            margin: 1.2vw;
            height: 9vh;
            box-shadow: inset 0 0 15px #dddddd;
          "
        ></v-card>
        <v-card
          color="#EEEEEE"
          style="
            padding: 1vw;
            margin: 1.2vw;
            height: 9vh;
            box-shadow: inset 0 0 15px #dddddd;
          "
        ></v-card>
        <v-card
          color="#EEEEEE"
          style="
            padding: 1vw;
            margin: 1.2vw;
            height: 9vh;
            box-shadow: inset 0 0 15px #dddddd;
          "
        ></v-card>
        <v-card
          color="#EEEEEE"
          style="
            padding: 1vw;
            margin: 1.2vw;
            height: 9vh;
            box-shadow: inset 0 0 15px #dddddd;
          "
        ></v-card>
        <v-card
          color="#EEEEEE"
          style="
            padding: 1vw;
            margin: 1.2vw;
            height: 9vh;
            box-shadow: inset 0 0 15px #dddddd;
          "
        ></v-card>
      </div>
      <draggable
        class="dragArea"
        group="items"
        draggable=".item"
        :animation="200"
        ghost-class="moving-card"
        @add="onAdd"
        @update="onMove"
      >
        <v-card
          v-for="item in rankedItems"
          :key="item.id"
          class="item"
          color="#FFFFFF"
          elevated-2
          style="padding: 1vw; margin: 1.2vh; height: 9vh"
          :style="{ 'border-left': '1.75vw solid' + item.color }"
        >
          {{ item.text }}
        </v-card>
      </draggable>
    </div>
  </v-card>
</template>

<script>
export default {
  name: 'Placeholder',

  components: {
    draggable: window.vuedraggable,
  },

  props: {
    player: Object,
    ranking: Array,
    rankedItems: Array,
  },

  methods: {
    onAdd: function (data) {
      let playeritems = this.player.items;
      let itemNum = 0;
      for (let i = 0; i < playeritems.length; i++) {
        if (data.item.innerHTML.trim() === playeritems[i].text) itemNum = i;
      }
      this.ranking.splice(data.newIndex, 0, itemNum);
    },
    onMove: function (data) {
      //update ranking
      const x = this.ranking[data.oldIndex];
      this.ranking.splice(data.oldIndex, 1);
      this.ranking.splice(data.newIndex, 0, x);
    },
  },
};
</script>
