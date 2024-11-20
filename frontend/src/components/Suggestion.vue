<!--
Filename: Suggestion.vue
Author: Elijah Claggett
Description: Reply Suggestion UI Element
-->
<template>
  <div id="suggestion">
    <div class="suggestion-body">
      <v-btn icon v-on:click="cancel($event)" color="#bbb">
        <v-icon>mdi-close</v-icon>
      </v-btn>
      <div class="suggestion-msg" v-on:click="edit($event)">{{ msg }}</div>
      <v-btn icon color="#bbb" v-on:click="send($event)">
        <v-icon>mdi-send</v-icon>
      </v-btn>
    </div>
  </div>
</template>

<script>
export default {
  name: 'SuggestionComponent',
  props: {
    msg: String,
    id: String,
    player: Object,
    popup: Boolean,
    freeze: Boolean,
  },
  data() {
    return {
      radioGroup: 0,
      initiateSendTime: new Date(),
      sendTime: 10,
      animationInterval: 200,
      timeRemaining: 0,
    };
  },
  methods: {
    edit: function () {
      if (window.myInterval) clearInterval(window.myInterval); // Cancel auto-send
      if (!this.freeze) {
        window.Breadboard.send('editSuggestion', { id: this.id });
        this.$emit('edit');
      }
    },
    cancel: function () {
      if (window.myInterval) clearInterval(window.myInterval); // Cancel auto-send
      if (!this.freeze) {
        window.Breadboard.send('cancelSuggestion', { id: this.id });
        this.$emit('cancel');
      }
    },
    send: function (auto = false) {
      if (window.myInterval) clearInterval(window.myInterval); // Cancel auto-send

      if (!this.freeze) {
        window.Breadboard.send('acceptSuggestion', { id: this.id, auto: auto });
        this.$emit('send');
      }
    },
  },
};
</script>
<style lang="scss">
#suggestion {
  width: 100%;
  height: auto;
  background: #f5f5f5;
  z-index: 5;
  position: absolute;
  bottom: 5em;
  border-radius: 1em 1em 0 0;
  padding: 0em;
  font-size: 0.8em;
  color: #555;
}
#suggestion .header {
  font-size: 0.85em;
  display: none;
}
#suggestion .header strong {
  display: block;
  font-weight: normal;
  text-transform: capitalize;
}
#suggestion .header span {
  color: #999;
}
#suggestion .suggestion-body {
  display: flex;
  width: 100%;
  cursor: initial;
  padding: 0em 1.5em;
  margin-bottom: 0;
  transition: background-color 0.2s;
  background: none;
  align-items: stretch;
  justify-content: space-between;
  overflow: hidden;
}
#suggestion .suggestion-body button {
  align-self: center;
}
#suggestion .suggestion-msg {
  box-shadow: 0 0 1em rgba(0, 0, 0, 0.05);
  border-width: 0 1px 0 1px;
  display: flex;
  align-items: center;
  padding: 0 1em;
}
#suggestion .footer {
  display: flex;
  justify-content: center;
  display: none;
}
.progress-btn {
  position: relative;
  width: 40px;
  margin-left: 1em;
}
.progress-btn button {
  z-index: 1;
  text-align: center;
}
.progress-btn .v-progress-circular {
  position: absolute;
  z-index: 0;
  top: -2px;
  left: -3px;
  width: 100%;
  display: block;
  padding: 1em;
  opacity: 0.8;
}
.progress-btn .v-progress-circular__underlay {
  stroke: white;
}
</style>
