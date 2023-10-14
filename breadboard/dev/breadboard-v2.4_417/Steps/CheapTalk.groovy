import java.time.Instant

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


scheduleEvalMessage = { player, delay ->
  if (player.gameStep != 'verification') { return }

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


startTopicDiscussion = { player ->
  def chatStartTime = Instant.now().epochSecond
  player.chatStartTime = chatStartTime
  
  if (true /* Use concrete example for all topics */) {

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

  } else {
    // Schedule prompts (no example)
    sendEvaluatorMessagesWithDelay(player, 'chat', [
        'Welcome to the chat room! Please discuss your opinions about the following topic.',
        Param.customPrompts[player.topic]
        ], 1)

    sendEvaluatorMessagesWithDelay(player, 'chat', [
      Param.customFollowups[player.topic]
      ], 60 * Param.followupDelay)

    player.neighbors.each { v ->
      v.chatStartTime = chatStartTime

      // Schedule prompts for partner (no example)
      sendEvaluatorMessagesWithDelay(v, 'chat', [
        'Welcome to the chat room! Please discuss your opinions about the following topic.',
        Param.customPrompts[player.topic]
        ], 1)

      sendEvaluatorMessagesWithDelay(v, 'chat', [
        Param.customFollowups[player.topic]
        ], 60 * Param.followupDelay)
    }
  }
}

startCooperationDiscussion = { player ->
  def currentSecond = Instant.now().epochSecond
  def timer = new Timer()

  player.cooperationDiscussionStartTime = currentSecond

  Param.cooperationDiscussionTimers[player.chatId] = timer.runAfter((Param.cooperationDiscussionTime * 60 * 1000) as int) {
    if (!player.qualified) {
      return;
    }
    player.gameStep = 'survey'
    // player.displayNeighborNodes = false
    player.surveyStartTime = Instant.now().epochSecond

    player.neighbors.each { v ->
      v.gameStep = 'survey'
      // player.displayNeighborNodes = false
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

startCheapTalk = { player ->
  a.addEvent('startChat', [
        data: Param.jsonGen.toJson([
                playerId: player.chatId
                ])
        ])

  // Start timer when both partners have clicked continue
  def readyToStart = true
  player.neighbors.each { v ->
        if (v.gameStep != 'cheaptalk') { readyToStart = false }
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
  
  player.gameStep = 'cheaptalk'
}

switchRoles = { player ->
  player.chatTurn = !player.chatTurn
}
