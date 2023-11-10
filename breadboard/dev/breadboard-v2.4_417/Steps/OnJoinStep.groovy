/*
 * Filename: OnJoinStep.groovy
 * Author: Elijah Claggett
 * Date: October 19, 2023
 *
 * Description:
 * This is the main Groovy script defining the experimental procedure.
 * This script is required for Breadboard. It executes when an experiment participant joins the session.
 */

import java.time.Instant
import java.text.SimpleDateFormat
import groovy.json.JsonSlurper
import java.net.URI
import java.net.URISyntaxException
import java.util.Map
import org.java_websocket.client.WebSocketClient
import org.java_websocket.drafts.Draft
import org.java_websocket.handshake.ServerHandshake

// Minimal websocket class
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

// Start pre-evaluation step
def startPreEval(player) {
  player.gameStep = 'preEval'
  msgs = [
    "Welcome to our study! This task simply involves exchanging opinions about a specific topic with a partner.",
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
  
  // Initialize player attributes
  player.gameStep = 'recaptcha'
  player.interventionMode = Param.interventionMode
  player.samplingMode = Param.samplingMode
  player.rejectedSuggestions = []
  player.chatId = Param.chatIdCounter
  player.gameStep = 'preEval'
  player.stepEvaluation = 0
  player.basePay = Param.basePay
  player.completionBonus = Param.completionBonus
  player.mutualCompletionBonus = Param.mutualCompletionBonus
  player.prolificTask1Pay = Param.prolificTask1Pay
  player.prolificTask2Pay = Param.prolificTask2Pay
  player.prolificDefectBonus = Param.prolificDefectBonus
  player.prolificCooperateBonus = Param.prolificCooperateBonus
  player.timeoutWarning = Param.timeoutWarning
  player.timeout = Param.timeout
  player.groupingComplete = false
  player.surveyFactors = Param.surveyFactors
  player.imageFactors = Param.imageFactors
  player.profane = false
  player.neighborNodes = []
  player.displayNeighborNodes = true
  player.isWaiting = true
  player.qualified = false
  player.imagePairs = Param.imagePairs
  player.topic = -1
  player.partnerReport = ''
  player.chatStartTime = ''
  player.lastChatTime = ''
  player.surveyStartTime = ''
  player.cooperationDiscussionStartTime = ''
  player.cooperationDiscussionTime = Param.cooperationDiscussionTime
  player.cooperationStartTime = ''
  player.chatEndTime = ''
  player.utcOffset = 0
  player.chatTime = Param.chatTime
  player.surveyTime = Param.surveyTime
  player.cooperationTime = Param.cooperationTime
  player.submit = ''
  player.finished = false
  player.showReady = false
  player.groupingType = (Param.groupingMethod == true) ? 'value' : 'artificial'
  player.initalSurveyAnswers = []
  player.groupingFactors = [0, 0, 0]
  player.groupId = -1
  player.groupName = ''
  player.groupImage = ''
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
  player.prolificCompletionCodes = Param.prolificCompletionCodes
  player.whichCompletionCode = 'task2_partnerDefect'
  player.waitingTime = Param.waitingTime
  player.evaluationStep = ''
  player.readyToScoreEvaluation = false
  player.evalScore = -1
  player.passEval = false
  player.lastEvalMessageReceived = -1
  player.bonusOption = -1
  player.partnerBonusOption = -1
  player.tutorialCorrectAnswers = [1,2,2,3]

  // Record player id
  a.addEvent('setPlayerId', [
    data: Param.jsonGen.toJson([
        playerId: player.chatId,
        amtId: player.id,
        amtUserId: playerId
      ])
    ])

  def date = new Date()
  def currentTime = date.getTime()
  def dateFormatter = new SimpleDateFormat('MM/dd/yyyy HH:mm:ss')
  def dateString = dateFormatter.format(date)
  if (!Param.uiTest) {
    if (Param.startAt == null) {
      // First player
      // Record experiment parameters
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

      // Automatically start main task after waiting time elapses
      def timer = new Timer()
      Param.initStepTask = timer.runAfter(Param.waitingTime * 60 * 1000) {
        initStep.run()
      }

      // Open a websocket connection with the NLP server if there is an experimental intervention
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

    // Handle if participants join after the main task starts (should never happen)
    if (Param.startAt - currentTime < 0) {
      player.gameStep = 'tooLate'
      return
    }

    // Waiting room text 
    def timerText = 'The main task will start in:'
    if (Param.platform == 'prolific') {
      timerText = 'The next task will start in:'
    }
    Param.playerTimers[player.chatId] = g.addTimer('player': player, time: ((Param.startAt - currentTime) / 1000).toInteger(), type: 'time', appearance: 'info', timerText: timerText, direction: 'down', result: { })
  }

  // Register a participant's Prolific details stored in URL parameters
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

  // Executed when a participant submits reCAPTCHA
  player.on('verifyRecaptcha', { v, data ->
    verifyRecaptcha(v, data)
  })

  // Executed only in the 'mcq' participant pre-evaluation/sampling mode
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

  // Executed when a participant finishes the tutorial
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

  // Executed when a participant submits the consent form
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

    if (!v.finished) {
      if (Param.samplingMode == 'all') {
        judgeHuman(v.chatId, 1)
      } else {
        startPreEval(v)
      }      
    }
  })

  // Executed when a participant completes the main tutorial (record event happening)
  player.on('completeTutorial', { v, data ->
    a.addEvent('completedTutorial', [
          data: Param.jsonGen.toJson([
                  playerId: v.chatId
                  ])
      ])
  });

  // Executed when a participant receives a reply suggestion (record event happening)
  player.on('receiveSuggestion', { v, data ->
    a.addEvent('receiveSuggestion', [
          data: Param.jsonGen.toJson([
                  playerId: v.chatId
                  ])
      ])
  });

  // Executed when a participant accepts a reply suggestion (record event happening)
  player.on('acceptSuggestion', { v, data ->
    a.addEvent('acceptSuggestion', [
          data: Param.jsonGen.toJson([
                  playerId: v.chatId,
                  suggestionId: data.id
                  ])
      ])
  });

  // Executed when a participant edits a reply suggestion (record event happening)
  player.on('editSuggestion', { v, data ->
    a.addEvent('editSuggestion', [
          data: Param.jsonGen.toJson([
                  playerId: v.chatId,
                  suggestionId: data.id
                  ])
      ])
  });

  // Executed when a participant cancels a reply suggestion (record event happening)
  player.on('cancelSuggestion', { v, data ->
    v.rejectedSuggestions.push(data.id)
    a.addEvent('cancelSuggestion', [
          data: Param.jsonGen.toJson([
                  playerId: v.chatId,
                  suggestionId: data.id
                  ])
      ])
  })

  // Executed when a participant makes a cooperation decision
  player.on('completeCooperationDecision', { v, data ->
    v.gameStep = 'gradePartnerAnswer'

    v.bonusOption = data['option'] as int

    if (Param.cooperationDecisionTimers[v.chatId]) {
      Param.cooperationDecisionTimers[v.chatId].cancel()
    }

    // Check if partner is also finished
    def neighborsFinished = true
    v.neighbors.each { n ->
      if (n.bonusOption == -1) {
        neighborsFinished = false
      }
      n.partnerBonusOption = v.bonusOption
    }

    // Determine final bonus based on participant and partner's coordinated decision
    if (v.bonusOption as int == 1 || neighborsFinished) {
      
      def allPartnersAgree = true
      v.neighbors.each { n ->
        if (n.bonusOption as int != 2) {
          allPartnersAgree = false
        }
      }

      def bonus = 0
      if (v.bonusOption as int == 1) {
        v.whichCompletionCode = 'task2_defect'
        bonus = Param.completionBonus
      } else if (v.bonusOption as int == 2 && allPartnersAgree) {
        // Both partners must select option two to receive the extra bonus
        v.whichCompletionCode = 'task2_cooperate'
        bonus = Param.mutualCompletionBonus
      }

      // Calculate final bonus and allow participant to submit the HIT
      bonus = Math.ceil(bonus * 100) / 100
      def reason = 'completed'
      def comments = false
      v.submit = g.getSubmitForm(v, bonus, reason, Param.sandbox, comments)

      // Store final bonus of chat partners and allow them to submit the HIT
      v.neighbors.each { n ->
        def neighborBonus = 0
        if (n.bonusOption as int == 1) {
          neighborBonus = Param.completionBonus
        } else if (n.bonusOption as int == 2 && allPartnersAgree) {
          neighborBonus = Param.mutualCompletionBonus
          n.whichCompletionCode = 'task2_cooperate'
        }

        neighborBonus = Math.ceil(neighborBonus * 100) / 100
        n.submit = g.getSubmitForm(n, neighborBonus, reason, Param.sandbox, comments)
      }
    }

    // Record that the cooperation decision was made
    a.addEvent('completedCooperationDecision', [
          data: Param.jsonGen.toJson([
                  playerId: v.chatId,
                  bonusOption: v.bonusOption
                ])
      ])
  })

  // Executed when a participant completes the follow-up survey
  player.on('completeSurvey', { v, data ->
    if (Param.surveyTimers[v.chatId]) {
      Param.surveyTimers[v.chatId].cancel()
    }

    // Set timer for cooperation decision
    def timer = new Timer()
    v.cooperationStartTime = Instant.now().epochSecond
    Param.cooperationDecisionTimers[v.chatId] = timer.runAfter((Param.cooperationTime * 60 * 1000) as int) {
      endHIT(v)
    }

    // Share survey answers with partner so they can peer review
    v.gameStep = 'game'
    v.neighbors.each { n ->
      n.partnerReport = data
    }

    // Record event happening
    a.addEvent('completedSurvey', [
          data: Param.jsonGen.toJson([
                  playerId: v.chatId] + data
                )
      ])
  })

  // Executed when fake loading process is over
  player.on('doneFakeLoading', { v, data ->
    v.doneFakeLoading = true
  })

  // Executed when a participant peer-reviews their partners follow-up survey answers
  player.on('reportPartnerAnswer', { v, data ->
    a.addEvent('reportPartnerAnswer', [
          data: Param.jsonGen.toJson([
                  playerId: v.chatId] + data
                )
      ])
    v.gameStep = 'end'
  })

  // Executed every time a participant selects an answer in the initial survey
  player.on('answerInitialSurvey', { v, data ->
    answerInitialSurvey(v, data)
  })

  // Executed when a participant finishes the initial survey
  player.on('completeInitialSurvey', { v, data ->
    completeInitialSurvey(v, data)
  })

  // Executed when a participants finishes the initial survey and main task tutorial
  player.on('readyForMainTask', { v, data ->
    v.qualified = true
    Param.numReadyForMainTask++
    if (Param.numReadyForMainTask == Param.numPlayers) {
      if (Param.initTask) {
        Param.initTask.cancel()
      }

      // Group and pair all participants
      calculateGroups()
    }
  })

  // Set a pair's chat end time based on their start time
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

  // Executed when participants move to the main chat
  player.on('startMainChat', { v, data ->
    startMainChat(v)
  })

  // Executed if a participant attempts to use a bad word
  player.on('profane', { v, data ->
    a.addEvent('usedBadWord', [
          data: Param.jsonGen.toJson([
                  playerId: v.chatId]
                )
      ])
    if (v.profane) {
      // If you already received a warning, it's over
      endExperimentProfanity(v)
    }
    v.profane = true // This is your one warning!
  })

  // Executed if a participant confirms that they want to report their partner
  player.on('clickReport', { v, data ->
    a.addEvent('reportPartner', [
          data: Param.jsonGen.toJson([
                  playerId: v.chatId]
                )
      ])
 
    endExperimentProfanity(v)
  })

  // Executed when a participant is inactive past the limit
  player.on('chatTimeout', { v, data ->
    if (v.qualified) {
      a.addEvent('chatTimeout', [
            data: Param.jsonGen.toJson([
                    playerId: v.chatId]
                  )
        ])
      endExperimentChatTimeout(v)
    }
  })

  // Executed when a participant sends a message during the pre-evaluation step
  player.on('sendMessageEvaluation', { v, data ->
    
    // Record the message that the participant sent
    v.messagesEvaluation.add(data.message)

    a.addEvent('evaluationMessage', [
            data: Param.jsonGen.toJson([
                    playerId: v.chatId,
                    data: data.message
                  ])
    ])

    if (Param.evalMessageTimers[v.chatId] != null) {
      Param.evalMessageTimers[v.chatId].cancel()
      Param.evalMessageTimers[v.chatId] = null
    } 
    
    // Send the next reply from the chatbot or score the pre-evaluation
    if (v.evaluationStep < Param.messagesEvaluation[2].size()) {
      scheduleEvalMessage(v, 5)
    } else {
      v.readyToScoreEvaluation = true
    }
  });

  // Executed when we use an external service like the NLP server to send pre-evaluation messages
  // The Vue.JS application communicates with the external service then gives us the reply to send
  player.on('preEvalReply', { v, data ->
    if (v.evaluationStep != 'Q4') {
      v.messagesEvaluation.add(generateEvaluatorMessage(data.msg))
      v.evaluationStep = data.id

      a.addEvent('evaluationReply', [
              data: Param.jsonGen.toJson([
                      playerId: v.chatId,
                      data: data.msg
                    ])
      ])
    }
  });

  // Executed when the participant receives a score for their pre-evaluation messages
  player.on('evalScore', { v, data ->
    v.evalScore = data.score
    v.passEval = v.evalScore >= 0.6

    a.addEvent('evalScore', [
            data: Param.jsonGen.toJson([
                    playerId: v.chatId,
                    data: data.score
                  ])
    ])

    // Move all participants on to the next step (do not fail them based on the score)
    judgeHuman(v.chatId, 1)
  });

  // Executed when a participant sends a message in the main chat
  player.on('chat', { v, data ->
    
    // Add message to both player and neighbor list
    v.messagesChat.add(data.message)
    println(data.message)

    v.lastChatTime = Instant.now().epochSecond

    v.messageReceived = false

    v.neighbors.each { neighbor ->
      // toggle messageReceived so that the slot becomes visible for partner
      neighbor.messageReceived = true
      neighbor.messagesChat.add(data.message)
    }
    a.addEvent('chatMessage', [
          data: Param.jsonGen.toJson([
                  playerId: v.chatId,
                  data: data.message
                ])
      ])
  }
  )

  // Executed when a participant sends a message in the main chat
  // We record their deleted characters
  player.on('charsSinceLastChat', { v, data ->
    a.addEvent('charsSinceLastChat', [
          data: Param.jsonGen.toJson([
                  playerId: v.chatId,
                  data: data.message
                ])
      ])
  })

  // Executed when a participant submits the HIT
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

  // Executed when a participant submits feedback at the end of the experiment
  player.on('submitFeedback', { v, data ->
    a.addEvent('submitFeedback', [
          data: Param.jsonGen.toJson([
                  playerId: v.chatId,
                  data: data.feedback
                ])
      ])
  })


  // Start the experiment with the pre-evaluation
  // startPreEval(player)
  player.gameStep = 'recaptcha'
  Param.numPlayers++
  Param.chatIdCounter++
}

onJoinStep.done = {
  println 'onJoinStep.done'
}
