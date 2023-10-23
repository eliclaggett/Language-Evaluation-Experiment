/*
 * Filename: FunctionsChat.groovy
 * Author: Elijah Claggett
 * Date: October 19, 2023
 *
 * Description:
 * This Groovy script defines auxilliary administrative functions used to control the Breadboard experiment.
 */

import java.time.Instant

def generateEvaluatorMessage(text) {
  // Send a message from the chatbot "evaluator"
  // content, participantId, type (text), timestamp (iso 8601 -05:00), uploaded, viewed, myself
  def msgTemplate = [
                participantId: -1,
                content: '',
                timestamp: '',
                uploaded: true,
                viewed: false,
                myself: false,
                type: 'text'
            ]
  msgTemplate.content = text
  msgTemplate.timestamp = Instant.now().toString()
  return Param.jsonGen.toJson(msgTemplate)
}
def generatePartnerMessage(text) {
  // Send a message from the chatbot "partner"
  // content, participantId, type (text), timestamp (iso 8601 -05:00), uploaded, viewed, myself
  def msgTemplate = [
                participantId: 999,
                content: '',
                timestamp: '',
                uploaded: true,
                viewed: false,
                myself: false,
                type: 'text'
            ]
  msgTemplate.content = text
  msgTemplate.timestamp = Instant.now().toString()
  return Param.jsonGen.toJson(msgTemplate)
}

// End the experiment if one of the conversation partners becomes inactive
def endExperimentChatTimeout(v) {
  v.qualified = false
  v.finished = true
  v.gameStep = 'timeout'
  if (Param.talkTimers[v.chatId]) { Param.talkTimers[v.chatId].cancel() }
  def bonus = 0
  def reason = 'chat timeout'
  def comments = false
  v.submit = g.getSubmitForm(v, bonus, reason, Param.sandbox, comments)
  v.neighbors.each { n ->
    n.qualified = false
    n.finished = true
    if (Param.talkTimers[n.chatId]) { Param.talkTimers[n.chatId].cancel() }
    n.gameStep = 'timeout'
    bonus = 0
    n.submit = g.getSubmitForm(n, bonus, reason, Param.sandbox, comments)
    a.addEvent('end_experiment', [
      data: Param.jsonGen.toJson([
              playerId: n.chatId,
            ])
      ])
  }
}

// Stagger sending multiple chatbot "evaluator" messages
// Delay in seconds
sendEvaluatorMessagesWithDelay = { player, step, msgs, delay ->
  def timer = new Timer()

  timer.scheduleAtFixedRate({
    if (step == 'chat') {
      player.messagesChat.add(generateEvaluatorMessage(msgs[0]))
    } else {
      player.messagesEvaluation.add(generateEvaluatorMessage(msgs[0]))
    }

    msgs = msgs.tail()
    if (msgs.size == 0) {
      timer.cancel()
      timer.purge()
    }
  } as TimerTask, delay * 1000, delay * 1000)
}

// Stagger sending multiple chatbot "partner" messages
// Delay in seconds
sendPartnerMessagesWithDelay = { player, step, msgs, delay ->
  def timer = new Timer()

  timer.scheduleAtFixedRate({
    if (step == 'chat') {
      player.messagesChat.add(generatePartnerMessage(msgs[0]))
    } else {
      player.messagesEvaluation.add(generatePartnerMessage(msgs[0]))
    }

    msgs = msgs.tail()
    if (msgs.size == 0) {
      timer.cancel()
      timer.purge()
    }
  } as TimerTask, delay * 1000, delay * 1000)
}

// Schedule the next pre-evaluation chatbot message
scheduleEvalMessage = { player, delay ->
  if (player.gameStep != 'preEval') { return }

  def timer = new Timer()

  if (Param.evalMessageTimers[player.chatId] != null) {
    Param.evalMessageTimers[player.chatId].cancel()
    Param.evalMessageTimers[player.chatId] = null
  }

  Param.evalMessageTimers[player.chatId] = timer.runAfter((delay * 1000) as int) {
    if (player.evaluationStep <= 7) {
      player.messagesEvaluation.add(generatePartnerMessage(Param.messagesEvaluation[2][player.evaluationStep as int]))
      player.lastEvalMessageReceived = player.evaluationStep
      player.evaluationStep += 1
      scheduleEvalMessage(player, 60)
    }
  }
}

// Introduce the main chat topics
startTopicDiscussion = { player ->
  def chatStartTime = Instant.now().epochSecond
  player.chatStartTime = chatStartTime

  // Schedule prompts
  sendEvaluatorMessagesWithDelay(player, 'chat', [
      'Welcome to the chat room! Please discuss your opinions about the following news story.',
      Param.customExamples[player.topic]
      ], 1)
  
  sendEvaluatorMessagesWithDelay(player, 'chat', [
    Param.customPrompts[player.topic]
    ], 60 * Param.topicDelay)

  sendEvaluatorMessagesWithDelay(player, 'chat', [
    Param.customFollowups[player.topic]
    ], 60 * (Param.topicDelay + Param.followupDelay))

  player.neighbors.each { v ->
    v.chatStartTime = chatStartTime

    // Schedule prompts for partner
    sendEvaluatorMessagesWithDelay(v, 'chat', [
      'Welcome to the chat room! Please discuss your opinions about the following news story.',
      Param.customExamples[player.topic]
      ], 1)
  
    sendEvaluatorMessagesWithDelay(v, 'chat', [
      Param.customPrompts[player.topic]
      ], 60 * Param.topicDelay)

    sendEvaluatorMessagesWithDelay(v, 'chat', [
      Param.customFollowups[player.topic]
      ], 60 * (Param.topicDelay + Param.followupDelay))
  }
}

// Introduce the discussion about cooperation options
startCooperationDiscussion = { player ->
  def currentSecond = Instant.now().epochSecond
  def timer = new Timer()

  player.cooperationDiscussionStartTime = currentSecond

  Param.cooperationDiscussionTimers[player.chatId] = timer.runAfter((Param.cooperationDiscussionTime * 60 * 1000) as int) {
    if (!player.qualified) {
      return;
    }
    player.gameStep = 'survey'
    player.surveyStartTime = Instant.now().epochSecond

    player.neighbors.each { v ->
      v.gameStep = 'survey'
      v.surveyStartTime = Instant.now().epochSecond
      Param.surveyTimers[v.chatId] = timer.runAfter((Param.surveyTime * 60 * 1000) as int) {
        forceFinishSurvey(v)
      }
    }

    Param.surveyTimers[player.chatId] = timer.runAfter((Param.surveyTime * 60 * 1000) as int) {
      forceFinishSurvey(player)
    }
  }

  sendEvaluatorMessagesWithDelay(player, 'chat', [
    'Times up! Thank you for discussing your opinions.',
    'Now, you have a few minutes to discuss which bonus option you will pick.',
    'There are two choices. The default bonus will add $' + Param.completionBonus + ' to the base pay for this HIT. You may also choose the double bonus option to receive $' + Param.mutualCompletionBonus +' instead.',
    "But, if you select the double bonus option and your partner doesn't, you will only get the base pay."
    ], 2)

  player.neighbors.each { v ->
    v.cooperationDiscussionStartTime = currentSecond
    sendEvaluatorMessagesWithDelay(v, 'chat', [
    'Times up! Thank you for discussing your opinions.',
    'Now, you have a few minutes to discuss which bonus option you will pick.',
    'There are two choices. The default bonus will add $' + Param.completionBonus + ' to the base pay for this HIT. You may also choose the double bonus option to receive $' + Param.mutualCompletionBonus +' instead.',
    "But, if you select the double bonus option and your partner doesn't, you will only get the base pay."
    ], 2)
  }
}

// Start chat step
startMainChat = { player ->
  a.addEvent('startChat', [
        data: Param.jsonGen.toJson([
                playerId: player.chatId
                ])
        ])

  // Start timer when both partners have clicked continue
  def readyToStart = true
  player.neighbors.each { v ->
        if (v.gameStep != 'mainChat') { readyToStart = false }
  }

  if (readyToStart) {
    startTopicDiscussion(player)

    def timer = new Timer()
    Param.talkTimers[player.chatId] = timer.runAfter((Param.chatTime * 60 * 1000) as int) {
      if (player.qualified) {
        startCooperationDiscussion(player)
      }
    }
  }
  
  player.gameStep = 'mainChat'
}