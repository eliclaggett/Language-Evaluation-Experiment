<!--
Filename: ChatWindow.vue
Author: Elijah Claggett
Description: Chatbox UI Element
-->
<template>
  <Chat
    :participants="chatParticipants"
    :myself="myself"
    :messages="messages"
    @onMessageSubmit="onMessageSubmit"
    @onType="onType"
    :placeholder="placeholder"
    :colors="colors"
    :profile-picture-config="profilePictureConfig"
    :borderStyle="borderStyle"
    :hideCloseButton="hideCloseButton"
    :send-images="false"
    :submitIconSize="submitIconSize"
    :scrollBottom="{ messageSent: false, messageReceived: false }"
    class="elevation-3"
  >
    <template v-slot:footer>
      <div id="new-msg-alert" @click="scrollToBottom" class="hide-alert">
        New Message&nbsp;&darr;
      </div>
      <span id="character-counter">1 / 140</span>
      <v-slide-y-reverse-transition origin="bottom center 0">
        <Suggestion
          :player="player"
          :popup="true"
          v-if="showSuggestion"
          :msg="nlp.suggestion"
          :id="nlp.id"
          @edit="editSuggestion"
          @cancel="cancelSuggestion"
          @send="sendSuggestion"
        />
      </v-slide-y-reverse-transition>
    </template>
  </Chat>
</template>

<script>
import { Chat } from 'vue-quick-chat';
import Suggestion from './Suggestion';
import 'vue-quick-chat/dist/vue-quick-chat.css';
import colors from '../plugins/colors';

export default {
  name: 'ChatWindow',

  components: {
    Chat,
    Suggestion,
  },
  props: {
    player: Object,
    participants: Array,
    msgSource: String,
    nlp: Object,
  },

  data: () => ({
    // Related to reply suggestions
    showSuggestion: false,
    waitingForPreReq: false,
    requestedEncouragement: false,
    requestedFarewell: false,
    preReq: '',

    chatEndTime: new Date(),
    lastMsgReceived: new Date(),

    // UI settings
    placeholder: 'Type your message here',
    startedScrolling: false,
    colors: {
      header: {
        bg: '#fff',
        text: '#000000de',
      },
      message: {
        others: {
          bg: '#fff',
          text: '#000',
        },
        myself: {
          bg: colors.secondary,
          text: '#fff',
        },
        messagesDisplay: {
          bg: '#eeeeee',
        },
      },
      submitIcon: '#5a5a5a',
    },
    borderStyle: {
      topLeft: '4px',
      topRight: '4px',
      bottomLeft: '4px',
      bottomRight: '4px',
    },
    hideCloseButton: true,
    profilePictureConfig: {
      others: false,
      myself: false,
    },
    submitIconSize: 25,
    displayHeader: false,
    timestampConfig: {
      format: 'HH:mm',
      relative: false,
    },
    messages: [],
    messageCount: 0,
  }),
  methods: {
    loadMoreMessages() {
      // Do nothing
    },
    scrollToBottom() {
      // Scroll to the latest chat message
      let el = document.querySelector('.container-message-display');
      el.scrollTop = el.scrollHeight;
      el.dispatchEvent(new CustomEvent('scroll'));
      document.querySelector('#new-msg-alert').className = 'hide-alert';
    },
    conditionalScrollToBottom() {
      // Scroll to the latest chat message (unless participant is scrolling)
      let el = document.querySelector('.container-message-display');
      this.isScrolling = el.scrollHeight - el.scrollTop != el.clientHeight;

      if (!this.isScrolling) {
        el.scrollTop = el.scrollHeight - el.clientHeight;
        el.dispatchEvent(new CustomEvent('scroll'));
      }
    },

    onMessageSubmit: function (message) {
      // Reset character counter
      document.querySelector('#character-counter').innerText = '0 / 140';

      // Send the message to be processed by the NLP server
      let suggestionRequest = {
        command: 'parseMsg',
        id: this.player.chatId,
        isPartner: false,
        msg: message.content,
      };
      if (
        this.player.gameStep == 'mainChat' &&
        this.player.interventionMode != 'none'
      ) {
        window.nlpServer.send(JSON.stringify(suggestionRequest));
      }

      // Reset reply suggestions
      this.showSuggestion = false;

      // Send message
      this.$emit('onMsgSend', message);
    },
    onType(data) {
      // Update character counter
      let dataLen = document.querySelector('.message-input').innerText.length;
      document.querySelector('#character-counter').innerText =
        dataLen + ' / 140';

      this.$emit('onType', data);
    },

    // Reply suggestion functions
    editSuggestion() {
      this.showSuggestion = false;
      document.querySelector('.message-input').innerText = this.nlp.suggestion;
      document.querySelector('.message-input').focus();
      this.onType(this.nlp.suggestion);
    },
    cancelSuggestion() {
      this.showSuggestion = false;
    },
    sendSuggestion() {
      this.showSuggestion = false;

      let msg = {
        participantId: this.player.chatId,
        content: this.nlp.suggestion,
        timestamp: new Date(),
      };

      this.$emit('onMsgSend', msg);
    },
  },

  computed: {
    chatParticipants() {
      return [...this.participants, { id: -999, name: '' }];
    },
    myself() {
      return {
        name: 'Me',
        id: this.player.chatId,
      };
    },
  },
  watch: {
    player(val) {
      // Keep track of messages
      const msgList = [];

      if (!val[this.msgSource]) {
        this.messages = [];
      } else {
        for (let message of val[this.msgSource]) {
          msgList.push(JSON.parse(message));
        }

        this.messages = msgList;

        if (msgList.length != this.messageCount && msgList.length > 0) {
          // Got a new message!

          this.lastMsgReceived = new Date();

          // Some reply suggestions have prerequisite messages
          // Is this one of them?
          if (
            this.player.interventionMode != 'bot' &&
            this.player.interventionMode != 'none' &&
            this.waitingForPreReq &&
            this.messages[this.messages.length - 1].content == this.preReq
          ) {
            setTimeout(() => {
              this.showSuggestion = true;
              this.waitingForPreReq = false;
              this.preReq = '';

              this.$nextTick(function () {
                document.querySelector('.container-message-display').style[
                  'padding-bottom'
                ] =
                  document.querySelector('#suggestion').offsetHeight -
                  70 +
                  'px';
                this.scrollToBottom();
              });
            }, 1000);
          }

          this.messageCount = msgList.length;

          // Scroll to the latest message
          let el = document.querySelector('.container-message-display');
          this.isScrolling = el.scrollHeight - el.scrollTop != el.clientHeight;

          if (this.isScrolling) {
            document.querySelector('#new-msg-alert').className = '';
          }

          this.$nextTick(function () {
            this.conditionalScrollToBottom();

            // Hide special characters?
            let tmpElem = document.createElement('textarea');
            document
              .querySelectorAll('.message-text p:not(:first-child)')
              .forEach((el) => {
                tmpElem.innerHTML = el.innerHTML;
                el.innerHTML = tmpElem.value;
              });
            tmpElem.remove();

            // Special UI for chatbot messages
            document
              .querySelectorAll('.other-message-body .message-username')
              .forEach((el) => {
                if (el.innerText == 'Chatbot') {
                  el.closest('.message-text').classList.add('evaluator');
                }
              });
          });
        }
      }
    },
    nlp(val) {
      // Reply suggestions
      if (val.suggestion) {
        if ('requiresPreceding' in val) {
          this.waitingForPreReq = true;
          this.preReq = val.requiresPreceding;
        } else {
          let suggestionDelay =
            val.id == 'greeting0' ||
            val.id == 'greeting1' ||
            val.id == 'greeting'
              ? 15000
              : 1000;

          if (this.player.interventionMode == 'bot') {
            // Automatically send the reply suggestion as a chatbot message
            let msg = {
              participantId: -1,
              content: this.nlp.suggestion,
              timestamp: new Date(),
            };

            setTimeout(() => {
              this.$emit('onMsgSend', msg);
            }, suggestionDelay);
          } else if (this.player.interventionMode != 'none') {
            // Automatically send the reply suggestion if the timer elapses
            setTimeout(() => {
              this.showSuggestion = true;

              this.$nextTick(function () {
                document.querySelector('.container-message-display').style[
                  'padding-bottom'
                ] =
                  document.querySelector('#suggestion').offsetHeight -
                  70 +
                  'px';
                this.scrollToBottom();
              });
            }, suggestionDelay);
          }
        }
      }
    },
  },
  mounted() {
    // Custom UI listing all chat participants
    let messageContainer = document.querySelector('.container-message-display');
    let joinMsgs = '';
    for (let participant of this.participants) {
      let groupName = '';
      let participantName = participant.name;
      if (participantName == 'Me') {
        participantName = 'You';
      }
      joinMsgs +=
        '<div class="message-container"><div class="message-text join-message"><p>' +
        participantName +
        ' ' +
        groupName +
        'joined the chat.</p></div></div>';
    }
    messageContainer.insertAdjacentHTML('afterbegin', joinMsgs);

    // Scroll to the bottom of the chat
    let cmd = document.querySelector('.container-message-display');
    cmd.scrollTop = cmd.scrollHeight;
    cmd.dispatchEvent(new CustomEvent('scroll'));

    // Show new message alert
    document
      .querySelector('.container-message-display')
      .addEventListener('scroll', function (ev) {
        let el = ev.target;
        this.isScrolling = el.scrollHeight - el.scrollTop != el.clientHeight;
        if (!this.isScrolling) {
          document.querySelector('#new-msg-alert').className = 'hide-alert';
        }
      });

    // Don't prevent keyboard shortcuts
    document
      .querySelector('.message-input')
      .addEventListener('keydown', (ev) => {
        const excludedKeys = [
          'Backspace',
          'Delete',
          'Meta',
          'Control',
          'Shift',
          'Alt',
        ];
        if (
          ev.target.innerText.length >= 140 &&
          !excludedKeys.includes(ev.key)
        ) {
          ev.preventDefault();
        }
      });

    // Timer for requesting encouragement and farewell
    let suggestionRequestInterval = setInterval(() => {
      if (this.player.gameStep != 'mainChat') {
        return;
      }

      let currentTime = new Date();

      // Send encouragement to partner if 2 silent minutes pass
      if (
        !this.requestedEncouragement &&
        this.player.interventionMode != 'none'
      ) {
        if (currentTime - this.lastMsgReceived > 1 * 60 * 1000) {
          this.requestedEncouragement = true;
          window.nlpServer.send(
            JSON.stringify({
              command: 'requestEncouragement',
              id1: this.player.chatId,
              id2: this.player.neighborNodes[0].chatId,
            })
          );
        }
      }

      // Send farewell if 1 minute left
      if (!this.requestedFarewell && this.player.interventionMode != 'none') {
        let endTime = new Intl.DateTimeFormat('en-US', {
          timeStyle: 'short',
        }).format(
          new Date(
            (this.player.chatTime * 60 + this.player.chatStartTime) * 1000
          )
        );

        if (endTime - currentTime < 2 * 60 * 1000) {
          this.requestedFarewell = true;
          window.nlpServer.send(
            JSON.stringify({
              command: 'requestFarewell',
              id1: this.player.chatId,
              id2: this.player.neighborNodes[0].chatId,
            })
          );
        }
      }
    }, 5000);
    suggestionRequestInterval;
  },
};
</script>

<style>
/* vue-quick-chat css fix */
.quick-chat-container {
  height: 30em;
}

.quick-chat-container .container-message-display .message-container {
  flex-wrap: nowrap;
}

.container-send-message {
  display: flex;
}

.container-send-message svg {
  vertical-align: bottom;
}

.container-send-message span {
  line-height: 1em;
}

/* custom chat styles */
.quick-chat-container .header-container {
  display: none;
}

.quick-chat-container .container-message-display .message-text > p {
  font-size: 0.95rem;
  line-height: 1.5em;
}

.join-message {
  padding-left: 1em;
  color: #868686;
}

.container-message-display {
  padding-top: 0.5em;
}

.evaluator {
  background: #8b8b8b !important;
  color: #fff !important;
}

/* new message alert */
#character-counter {
  position: absolute;
  bottom: 0.25rem;
  right: 1.4rem;
  font-size: 0.6rem;
  color: #999;
  z-index: 0;
}

.quick-chat-container .container-message-manager {
  padding: 2.5rem 1rem;
}

.quick-chat-container {
  position: relative;
}

#new-msg-alert {
  position: absolute;
  bottom: 4.5rem;
  left: 0.5rem;
  padding: 0.5em;
  font-size: 0.85em;
  z-index: 5;
  background-color: rgba(238, 238, 238, 0.8);
  color: #ff5252;
  border: solid 1px #ff5252;
  border-radius: 4px;
  cursor: pointer;
}

.hide-alert {
  display: none;
}

/* reply suggestions */
.suggestions {
  padding: 3px;
  margin: 3px;
  position: relative;
}

.suggestions::before {
  content: 'Suggested replies:';
  position: absolute;
  top: -1.5rem;
  left: 0.75rem;
  font-size: 0.9em;
  color: #555;
}
</style>
