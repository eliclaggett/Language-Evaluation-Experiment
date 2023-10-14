import java.time.Instant
import java.text.SimpleDateFormat
import groovy.json.JsonSlurper
import java.net.URI
import java.net.URISyntaxException
import java.util.Map
import org.java_websocket.client.WebSocketClient
import org.java_websocket.drafts.Draft
import org.java_websocket.handshake.ServerHandshake

public class ExampleClient extends WebSocketClient {

  public ExampleClient(URI serverUri, Draft draft) {
    super(serverUri, draft)
  }

  public ExampleClient(URI serverURI) {
    super(serverURI)
  }

  public ExampleClient(URI serverUri, Map<String, String> httpHeaders) {
    super(serverUri, httpHeaders)
  }

  @Override
  public void onOpen(ServerHandshake handshakedata) {
    send('{"command": "resetPairs"}')
    println('opened connection')
  }

  @Override
  public void onMessage(String message) {
    println('received: ' + message)
  }

  @Override
  public void onClose(int code, String reason, boolean remote) {
    // The close codes are documented in class org.java_websocket.framing.CloseFrame
    println('Connection closed by ' + (remote ? 'remote peer' : 'us') + ' Code: ' + code + ' Reason: ' + reason)
  }

  @Override
  public void onError(Exception ex) {
    ex.printStackTrace()
  }

}

def startPreEval(player) {
  player.gameStep = 'verification'
  msgs = [
    "Welcome to this HIT! This task simply involves exchanging opinions about a specific topic with a partner.",
    "Let's start with a practice session.",
    "First, please discuss how you feel about the recent news about how AI is impacting jobs, education, and society with your partner."
  ]
  sendEvaluatorMessagesWithDelay(player, 'pre-evaluation', msgs, 2)
  player.evaluationStep = 1
  sendPartnerMessagesWithDelay(player, 'pre-evaluation', [Param.messagesEvaluation[2][0]], 10)
  scheduleEvalMessage(player, 75)
}

onJoinStep = stepFactory.createNoUserActionStep()

onJoinStep.run = { playerId ->
  def player = g.getVertex(playerId)
  if (!player) { return }
  println('onJoinStep.run:' + Param.numPlayers)
  // Initialize player variables
  player.interventionMode = Param.interventionMode
  player.samplingMode = Param.samplingMode
  player.rejectedSuggestions = []
  player.chatId = Param.chatIdCounter
  player.gameStep = 'verification'
  player.stepEvaluation = 0
  player.basePay = Param.basePay
  player.completionBonus = Param.completionBonus
  player.mutualCompletionBonus = Param.mutualCompletionBonus
  player.prolificTask1Pay = Param.prolificTask1Pay
  player.prolificTask2Pay = Param.prolificTask2Pay
  player.prolificMaxPay = Param.prolificTask1Pay + Param.prolificTask2Pay + Param.completionBonus * Param.multiplier
  player.multiplier = Param.multiplier
  player.timeoutWarning = Param.timeoutWarning
  player.timeout = Param.timeout
  player.earned = player.basePay
  player.received = -1
  player.donated = -1
  player.chatTurn = false
  player.groupingComplete = false
  player.surveyFactors = Param.surveyFactors
  player.imageFactors = Param.imageFactors
  player.profane = false
  player.neighborNodes = []
  player.displayNeighborNodes = true
  player.isWaiting = true
  player.qualified = false
  // player.surveyPairs = Param.surveyPairs
  player.imagePairs = Param.imagePairs
  // player.likertQuestions = Param.likertQuestions
  // player.customExamples = Param.customExamples
  // player.customFollowups = Param.customFollowups
  // player.customExampleSources = Param.customExampleSources
  player.topic = -1
  player.partnerReport = ''
  player.chatStartTime = ''
  player.lastChatTime = ''
  player.surveyStartTime = ''
  player.cooperationDiscussionStartTime = ''
  player.cooperationDiscussionTime = Param.cooperationDiscussionTime
  player.donationStartTime = ''
  player.chatEndTime = ''
  player.utcOffset = 0
  player.chatTime = Param.chatTime
  player.surveyTime = Param.surveyTime
  player.donationTime = Param.donationTime
  player.submit = ''
  player.finished = false
  player.showReady = false
  player.groupingType = (Param.groupingMethod == true) ? 'value' : 'artificial'
  player.groupingPreferences = []
  player.groupingFactors = [0, 0, 0]
  player.groupId = -1
  player.groupName = ''
  player.groupImage = ''
  player.gstepconfirmed = false
  player.doneFakeLoading = false
  player.passedHumanCheck = false
  player.checkingVerification = false
  player.groupingQuestionRandomized = [0, 1, 2, 3, 4, 5, 6] // Index of each of the 7 questions
  Collections.shuffle(player.groupingQuestionRandomized)
  player.groupingQuestionOrderRandomized = [0, 0, 0, 0, 0, 0, 0] // LR order of each of the 7 questions
  (0..5).each { val ->   player.groupingQuestionOrderRandomized[val] = Math.round(Math.random()) }
  player.messagesChat = []
  player.messagesEvaluation = []
  player.nlpPort = Param.nlpPort
  player.forceSubmitSurvey = false
  player.platform = Param.platform
  player.prolificId = ''
  player.completionCode = Param.completionCode
  player.waitingTime = Param.waitingTime
  player.totalMsgLength = 0
  player.askedForMore = false
  player.evaluationStep = ''
  player.readyToScoreEvaluation = false
  player.evalScore = -1
  player.passEval = false
  player.lastEvalMessageReceived = -1
  player.bonusOption = -1
  player.partnerBonusOption = -1

  a.addEvent('setPlayerId', [
    data: Param.jsonGen.toJson([
        playerId: player.chatId,
        amtId: player.id,
        amtUserId: playerId
      ])
    ])
  // Add timer until grouping automatically begins

  def date = new Date()
  def currentTime = date.getTime()
  def dateFormatter = new SimpleDateFormat('MM/dd/yyyy HH:mm:ss')
  def dateString = dateFormatter.format(date)
  if (!Param.uiTest) {
    if (Param.startAt == null) {
      // First player
      Param.verificationFile = new File('verification.txt')
      Param.verificationFile.append('Start experiment: ' + dateString + '\n')
      Param.startAt = currentTime + (Param.waitingTime * 60 * 1000)
      a.addEvent('gameStartTime', [
        data: Param.startAt])
      a.addEvent('groupingType', [
        data: Param.groupingMethod])
      a.addEvent('interventionMode', [
        data: Param.interventionMode])
      a.addEvent('samplingMode', [
        data: Param.samplingMode])
      a.addEvent('samplingType', [
        data: Param.samplingType])
      a.addEvent('basePay', [
        data: Param.basePay])
      a.addEvent('completionBonus', [
        data: Param.completionBonus])
      a.addEvent('waitTime', [
        data: Param.waitingTime])
      a.addEvent('chatTime', [
        data: Param.chatTime])
      a.addEvent('maxDonation', [
        data: Param.mutualCompletionBonus])
      a.addEvent('sandbox', [
        data: Param.sandbox])

      def timer = new Timer()
      //Param.initStepTask = timer.runAfter(Param.waitingTime * 60 * 1000) {
        //initStep.run()
      //}

      if (Param.interventionMode == 'reply' || Param.interventionMode == 'bot') {
        def socket = null
        if (Param.mode == 'dev') {
          socket = new ExampleClient(new URI('ws://localhost:' + Param.nlpPort))
        } else {
          
          server_domain = Param.dotenvMap.get('SERVER_DOMAIN')
          socket = new ExampleClient(new URI('wss://'+server_domain+':' + Param.nlpPort))
        }
        socket.connect()
      }
    }
    player.gameStart = Param.startAt

    // if (Param.startAt - currentTime < 0) {
    //   player.gameStep = 'tooLate'
    //   return
    // }

    def timerText = 'The main task will start in:'
    if (Param.platform == 'prolific') {
      timerText = 'The next task will start in:'
    }
    Param.playerTimers[player.chatId] = g.addTimer('player': player, time: ((Param.startAt - currentTime) / 1000).toInteger(), type: 'time', appearance: 'info', timerText: timerText, direction: 'down', result: { })
  }

  player.on('registerProlific', { v, data ->
    def prolificId = (data['prolificId']) ? data['prolificId'] : ''
    def sessionId = (data['sessionId']) ? data['sessionId'] : ''
    def studyId = (data['studyId']) ? data['studyId'] : ''

    if (v.prolificId == '') {
      a.addEvent('registerProlific', [
        data: Param.jsonGen.toJson([
                playerId: v.chatId,
                prolificId: prolificId,
                sessionId: sessionId,
                studyId: studyId
              ])
      ])
      v.prolificId = prolificId
    }
  })

  player.on('verifyRecaptcha', { v, data ->
    verifyRecaptcha(v, data)
  })

  player.on('humanCheck', { v, data ->
    if (Param.samplingMode == 'mcq') {
      if (data['q1'] != 'a4' || data['q2'] != 'a4') {
        judgeHuman(v.chatId, 0)
      } else {
        judgeHuman(v.chatId, 1)
      }
    }

    def systemTime = Instant.now().epochSecond * 1000
    def userTime = data['currentTime']
    player.utcOffset = systemTime - userTime
  })

  player.on('checkUnderstanding', { v, data ->
    def passCheck = true;
    if (
      data[0] as int != 1 ||
      data[1] as int != 2 ||
      data[2] as int != 2 ||
      data[3] as int != 3
    ) {
      passCheck = false
    }

    if (passCheck) {
      v.gameStep = 'consent'
      a.addEvent('passedCheckUnderstanding', [
        data: Param.jsonGen.toJson([
                playerId: v.chatId
              ])
      ])
    } else {
      v.gameStep = 'failCheckUnderstanding'
      v.finished = true
      v.qualified = false
      Param.numPlayers--
      a.addEvent('failedCheckUnderstanding', [
        data: Param.jsonGen.toJson([
                playerId: v.chatId,
                answers: data
              ])
      ])
    }
  })

  player.on('consent', { v, data ->
    if (data.q1 == 'yes' && data.q2 == 'yes' && data.q3 == 'yes') {
      a.addEvent('giveConsent', [
          data: Param.jsonGen.toJson([
                  playerId: v.chatId
                ])
        ])
    } else {
      v.gameStep = 'failedConsent'
      v.finished = true
      Param.numPlayers--
      a.addEvent('failedConsent', [
          data: Param.jsonGen.toJson([
                  playerId: v.chatId
                ])
        ])
    }
  })
  player.on('completeConsent', { v, data ->
    if (!v.finished) {
      if (Param.samplingMode == 'all' || Param.platform == 'prolific') {
        judgeHuman(v.chatId, 1)
      } else {
        startPreEval(v)
      }      
    }
  });

  player.on('completeTutorial', { v, data ->
    a.addEvent('completedTutorial', [
          data: Param.jsonGen.toJson([
                  playerId: v.chatId
                  ])
      ])
  });

  player.on('receiveSuggestion', { v, data ->
    a.addEvent('receiveSuggestion', [
          data: Param.jsonGen.toJson([
                  playerId: v.chatId
                  ])
      ])
  });

  player.on('acceptSuggestion', { v, data ->
    a.addEvent('acceptSuggestion', [
          data: Param.jsonGen.toJson([
                  playerId: v.chatId,
                  suggestionId: data.id
                  ])
      ])
  });

  player.on('editSuggestion', { v, data ->
    a.addEvent('editSuggestion', [
          data: Param.jsonGen.toJson([
                  playerId: v.chatId,
                  suggestionId: data.id
                  ])
      ])
  });

  player.on('cancelSuggestion', { v, data ->
    v.rejectedSuggestions.push(data.id)
    a.addEvent('cancelSuggestion', [
          data: Param.jsonGen.toJson([
                  playerId: v.chatId,
                  suggestionId: data.id
                  ])
      ])
  })

  player.on('completeCooperationGame', { v, data ->
    v.gameStep = 'partnerAnswer'
    // v.donated = data['amount']

    v.bonusOption = data['option'] as int

    if (Param.donationTimers[v.chatId]) {
      Param.donationTimers[v.chatId].cancel()
    }

    def neighborsFinished = true
    // def sharedPotTotal = v.donated

    v.neighbors.each { n ->
      // if (n.donated == -1) {
      //   neighborsFinished = false
      // }
      // sharedPotTotal += Math.max(n.donated as float, 0 as float)
      // n.received = data['amount']
      if (n.bonusOption == -1) {
        neighborsFinished = false
      }
      n.partnerBonusOption = v.bonusOption
    }

    // if (neighborsFinished) {
    //   def sharedPotPayout = Math.floor(sharedPotTotal * 75)
    //   sharedPotPayout /= 100
    //   def bonus = v.completionBonus - Math.max(v.donated as float, 0 as float) + sharedPotPayout
    //   bonus = Math.ceil(bonus * 100) / 100
    //   def reason = 'completed'
    //   def comments = false
    //   v.submit = g.getSubmitForm(v, bonus, reason, Param.sandbox, comments)
    //   v.neighbors.each { n ->
    //     bonus = n.completionBonus - Math.max(n.donated as float, 0 as float) + sharedPotPayout
    //     bonus = Math.ceil(bonus * 100) / 100
    //     n.submit = g.getSubmitForm(n, bonus, reason, Param.sandbox, comments)
    //   }
    // }
    if (v.bonusOption as int == 1 || neighborsFinished) {
      
      def allPartnersAgree = true
      v.neighbors.each { n ->
        if (n.bonusOption as int != 2) {
          allPartnersAgree = false
        }
      }

      def bonus = 0
      if (v.bonusOption as int == 1) {
        bonus = Param.completionBonus
      } else if (v.bonusOption as int == 2 && allPartnersAgree) {
        bonus = Param.mutualCompletionBonus
      }

      bonus = Math.ceil(bonus * 100) / 100
      def reason = 'completed'
      def comments = false
      v.submit = g.getSubmitForm(v, bonus, reason, Param.sandbox, comments)

      v.neighbors.each { n ->
        def neighborBonus = 0
        if (n.bonusOption as int == 1) {
          neighborBonus = Param.completionBonus
        } else if (n.bonusOption as int == 2 && allPartnersAgree) {
          neighborBonus = Param.mutualCompletionBonus
        }

        neighborBonus = Math.ceil(neighborBonus * 100) / 100
        n.submit = g.getSubmitForm(n, neighborBonus, reason, Param.sandbox, comments)
      }
    }

    // a.addEvent('completedCooperationGame', [
    //       data: Param.jsonGen.toJson([
    //               playerId: v.chatId,
    //               donationAmount: v.donated
    //             ])
    //   ])
    a.addEvent('completedCooperationGame', [
          data: Param.jsonGen.toJson([
                  playerId: v.chatId,
                  bonusOption: v.bonusOption
                ])
      ])
  })

  player.on('completeSurvey', { v, data ->
    if (Param.surveyTimers[v.chatId]) {
      Param.surveyTimers[v.chatId].cancel()
    }
    def timer = new Timer()
    v.donationStartTime = Instant.now().epochSecond
    Param.donationTimers[v.chatId] = timer.runAfter((Param.donationTime * 60 * 1000) as int) {
      endHIT(v)
    }

    v.gameStep = 'game'
    v.neighbors.each { n ->
      n.partnerReport = data
    }
    // v.displayNeighborNodes = false
    a.addEvent('completedSurvey', [
          data: Param.jsonGen.toJson([
                  playerId: v.chatId] + data
                )
      ])
  })

  player.on('doneFakeLoading', { v, data ->
    v.doneFakeLoading = true
  })

  player.on('reportPartnerAnswer', { v, data ->
    a.addEvent('reportPartnerAnswer', [
          data: Param.jsonGen.toJson([
                  playerId: v.chatId] + data
                )
      ])
    v.gameStep = 'end'
  })

  player.on('chooseGroupingPreference', { v, data ->
    chooseGroupingPreference(v, data)
  })

  player.on('completeGrouping', { v, data ->
    completeGrouping(v, data)
  })
  player.on('readyForGrouping', { v, data ->
    v.qualified = true
    Param.numReadyForGrouping++
    if (Param.numReadyForGrouping == Param.numPlayers) {
      if (Param.initTask) {
        Param.initTask.cancel()
      }
      calculateGroups()
    }
  })

  player.on('setChatEndTime', { v, data ->
    def endTime = 0
    v.neighbors.each { neighbor ->
      if (neighbor.chatEndTime != '') { endTime = neighbor.chatEndTime }
    }

    if (endTime == 0) {
      v.chatEndTime = data.time + (Param.chatTime * 60 * 1000)
    } else {
      v.chatEndTime = endTime
    }
  })

  player.on('startCheapTalk', { v, data ->
    startCheapTalk(v)
  })

  player.on('profane', { v, data ->
    a.addEvent('usedBadWord', [
          data: Param.jsonGen.toJson([
                  playerId: v.chatId]
                )
      ])
    if (v.profane) {
      endExperimentProfanity(v)
    }
    v.profane = true
  })

  player.on('clickReport', { v, data ->
    a.addEvent('reportPartner', [
          data: Param.jsonGen.toJson([
                  playerId: v.chatId]
                )
      ])
    endExperimentProfanity(v)
  })

  player.on('chatTimeout', { v, data ->
    println('Vue chat timeout')
    if (v.qualified) {
      a.addEvent('chatTimeout', [
            data: Param.jsonGen.toJson([
                    playerId: v.chatId]
                  )
        ])
      endExperimentChatTimeout(v)
    }
  })

  player.on('sendMessageEvaluation', { v, data ->
    
    // Record the message that the participant sent
    v.messagesEvaluation.add(data.message)

    a.addEvent('evaluationMessage', [
            data: Param.jsonGen.toJson([
                    playerId: v.chatId,
                    data: data.message
                  ])
    ])

    // Either speed up sending the next message or schedule a new process to send the next message
    if (Param.evalMessageTimers[v.chatId] != null) {
      Param.evalMessageTimers[v.chatId].cancel()
      // Param.evalMessageTimers[v.chatId].purge()
      Param.evalMessageTimers[v.chatId] = null
    } 
    
    if (v.evaluationStep < Param.messagesEvaluation[2].size()) {
      scheduleEvalMessage(v, 5)
    } else {
      v.readyToScoreEvaluation = true
    }
    
    if (v.lastEvalMessageReceived == v.evaluationStep) {
      // v.evaluationStep += 1
    }
    
    
    // if (v.evaluationStep == 'Q1 - yes' || v.evaluationStep == 'Q1 - no - change mind' || v.evaluationStep == 'Q2' || v.evaluationStep == 'Q3') {
    //   def jsonSlurper = new JsonSlurper()
    //   def parsedMsg = jsonSlurper.parseText(data.message)
    //   v.totalMsgLength += parsedMsg.content.size()
    //   if (v.totalMsgLength < 40 && !v.askedForMore) {
    //     v.askedForMore = true
    //     v.messagesEvaluation.add(generateEvaluatorMessage('Can you please explain in at least a few sentences?'))
    //   } else if (v.evaluationStep == 'Q3') {
    //     // Done with pre-evaluation
    //     // Evaluate all messages
    //     v.messagesEvaluation.add(generateEvaluatorMessage('Great! Thanks for your patience. I will pass you on to the next task briefly!'))
    //     v.readyToScoreEvaluation = true
    //   }
    // }
  });

  player.on('preEvalReply', { v, data ->
    if (v.evaluationStep != 'Q4') {
      v.messagesEvaluation.add(generateEvaluatorMessage(data.msg))
      v.evaluationStep = data.id
      v.totalMsgLength = 0
      v.askedForMore = false

      a.addEvent('evaluationReply', [
              data: Param.jsonGen.toJson([
                      playerId: v.chatId,
                      data: data.msg
                    ])
      ])
    }
  });

  player.on('evalScore', { v, data ->
    v.evalScore = data.score
    v.passEval = v.evalScore >= 0.6

    a.addEvent('evalScore', [
            data: Param.jsonGen.toJson([
                    playerId: v.chatId,
                    data: data.score
                  ])
    ])
    judgeHuman(v.chatId, 1)
  });

  player.on('sendMessageEvaluation_old', { v, data ->
    v.messagesEvaluation.add(data.message)

    a.addEvent('evaluationMessage', [
            data: Param.jsonGen.toJson([
                    playerId: v.chatId,
                    data: data.message
                  ])
      ])

    def timer = new Timer()
    def chatbotMode = Param.samplingMode == 'chatbot' ? 0 : 1

    if (v.stepEvaluation == 0) {
      timer.runAfter(2 * 1000) {
        player.messagesEvaluation.add(generateEvaluatorMessage(Param.messagesEvaluation[chatbotMode][1]))
      }
    } else if (v.stepEvaluation == 1) {
      timer.runAfter(2 * 1000) {
        player.messagesEvaluation.add(generateEvaluatorMessage(Param.messagesEvaluation[chatbotMode][2]))
      }
    } else {
      // Check if player's answer is related to bot check, human check, attention check
      def jsonSlurper = new JsonSlurper()
      def allAnswers = '---' + v.chatId + '---\n\n'
      def autoPass = true
      def haveAllAnswers = false
      def idx = 0
      for (msg in player.messagesEvaluation) {
        msgObject = jsonSlurper.parseText(msg)
        if (msgObject.participantId != -1) {
          def thisAnswer = msgObject.content.toLowerCase()
          def thisAnswerCased = msgObject.content
          // AutoPass: Check answer 1
          if (idx == 0) {
            if ( !(thisAnswer ==~ /.*(0|zero|no|never|n't|nt).*/) ) {
              autoPass = false
            }
          }
          // AutoPass: Check answer 2
          if (idx == 1) {
            if ( !(thisAnswer ==~ /.*(hcii|slab|communication|cmu).*/) ) {
              autoPass = false
            }
          }
          // AutoPass: Check answer 3
          if (idx == 2) {
            if ( chatbotMode == 0 && !(thisAnswer ==~ /.*(attention|bot|human|real|person|quality).*/) ) {
              autoPass = false
            } else if (chatbotMode == 1) {
              // def correctAnswer = "I am ready to begin."
              def correctAnswer = 'I am comfortable beginning this task.'
              def numWrongCharacters = correctAnswer.length()
              def j = 0
              for (int i = 0; i < thisAnswer.length(); i++) {
                if ((i == 0 || i == thisAnswer.length() - 1) && thisAnswer[i] == '"') { continue }
                if (i >= correctAnswer.length()) { break }
                if (thisAnswerCased[i] == correctAnswer[j]) { numWrongCharacters-- }
                j++
              }
              if (numWrongCharacters > 1) { autoPass = false }
            }
            haveAllAnswers = true
          }
          allAnswers += msgObject.content + '\n\n'
          idx++
        } else {
        }
      }
      allAnswers += '-------\n'
      v.checkingVerification = true
      if (haveAllAnswers) {
        if (Param.verificationFile && !autoPass) {
          Param.verificationFile.append(allAnswers)
          judgeHuman(v.chatId, 0)
        } else if (autoPass) {
          judgeHuman(v.chatId, 1)
        } else {
          println('ERROR NO VERIFICATION FILE')
        }
      }

      timer.runAfter(1 * 1000) {
        player.messagesEvaluation.add(generateEvaluatorMessage('Thanks. Please wait while we review your answers.'))
      }
    }
    v.stepEvaluation++
  })

  // updating chat messages
  player.on('chat', { v, data ->
    // add message to both player and neighbor list
    v.messagesChat.add(data.message)
    println(data.message)

    v.lastChatTime = Instant.now().epochSecond

    v.messageReceived = false
    // def timeOfChat = Instant.now().epochSecond
    v.neighbors.each { neighbor ->
      //   // toggle messageReceived so that the slot becomes visible for partner
      neighbor.messageReceived = true
      neighbor.messagesChat.add(data.message)

    //   def timeDiff = 0
    //   // check chat timeout time for neighbors
    //   if (neighbor.lastChatTime != '') {
    //     timeDiff = timeOfChat - neighbor.lastChatTime
    //   } else if (neighbor.chatStartTime != '') {
    //     timeDiff = timeOfChat - neighbor.chatStartTime
    //   } else {
    //     timeDiff = 0
    //   }
    //   if (timeDiff > Param.timeout) {
    //     println('sendChat neighbor chat timeout')
    //     endExperimentChatTimeout(neighbor)
    //     return
    //   }
    }
    a.addEvent('chatMessage', [
          data: Param.jsonGen.toJson([
                  playerId: v.chatId,
                  data: data.message
                ])
      ])

    // process message and submit api request for smart suggestions
    /*
    def m = jsonSlurper.parseText(data.message)
    // note: deal with ' in messages?
    get = new URL("http://localhost:105/predict?context="+m.content).openConnection();
    get.setRequestMethod("GET")
    get.setDoOutput(true)
    getRC = get.getResponseCode();
    if (getRC.equals(200)) {
          // update the neighbor's suggestion bar
          v.neighbors.each {neighbor ->
          def response = jsonSlurper.parseText(get.getInputStream().getText())
          println(response)
          // we only want the first three responses
          int counter = 0;
          response.each {
              key, value -> if (counter < 3) {
                // update suggestions array accordingly
                neighbor.sug[counter] = key;
                counter++;
              }
          }
          }
    }
    */
  }
  )

  player.on('charsSinceLastChat', { v, data ->
    a.addEvent('charsSinceLastChat', [
          data: Param.jsonGen.toJson([
                  playerId: v.chatId,
                  data: data.message
                ])
      ])
  })

  player.on('submitHIT', { v, data ->
    a.addEvent('submitHIT', [
          data: Param.jsonGen.toJson([
                  playerId: v.chatId
                ])
      ])
    a.addEvent('submitHITParams', [
          data: Param.jsonGen.toJson(data)
      ])
    v.finished = true
  })

  player.on('submitFeedback', { v, data ->
    a.addEvent('submitFeedback', [
          data: Param.jsonGen.toJson([
                  playerId: v.chatId,
                  data: data.feedback
                ])
      ])
  })

  // --- SMART REPLY ---
  // variables needed for displaying suggestions
  // player.messageTemplate = jsonSlurper.parseText('{"content":"","participantId":1,"uploaded":false,"viewed":false,"type":"text"}');
  // player.messageReceived = false;
  // initial suggestions:
  // player.sug = ["Hello!", "How are you?", "Hi there."];

  // startRecaptcha(player)
  startPreEval(player)
  Param.numPlayers++
  Param.chatIdCounter++
}

onJoinStep.done = {
  println 'onJoinStep.done'
}
