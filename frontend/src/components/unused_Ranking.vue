<template>
  <v-main class="main">
    <v-col class="ma-0 pa-0">
      <v-container ma-0 fill-height style="background: #ffffff; width: 100%">
        <v-card
          max-height="75vh"
          rounded="lg"
          color="#EEEEEE"
          elevation="0"
          class="pa-2"
        >
          <div align="center">
            <v-card
              shaped
              color="#FFFFFF"
              outlined
              style="
                border: 1px solid #e15759;
                padding: 10px;
                margin: 10px;
                max-width: 60%;
              "
            >
              {{ rankingTitle }}
            </v-card>
          </div>

          <v-card
            v-for="item in rankingItems"
            :key="item.id"
            class="item"
            color="#FFFFFF"
            elevation="0"
            style="padding: 8px; margin: 10px; height: 9vh"
            :style="{ 'border-left': '1.75vw solid' + item.color }"
          >
            {{ item.text }}
          </v-card>
        </v-card>
      </v-container>
    </v-col>
  </v-main>
</template>

<script>
export default {
  name: 'Ranking',

  props: {
    rankingItems: Array,
    rankingTitle: String,
  },
};
</script>

<style>
.item {
  color: '#FFFFFF';
  padding: 8px;
  margin: 10px;
  height: 9vh;
  width: 19vw;
  font-size: 0.88em;
}

.moving-card {
  opacity: 0.5;
  background: #f7fafc;
  border: 1px solid #4299e1;
}

#choiceLabel {
  padding: 0;
  margin-top: 0;
  margin-bottom: 0;
}

.left {
  margin-left: 12vw;
}

.right {
  margin-left: 35vw;
}

.container {
  justify-content: center;
}
</style>
