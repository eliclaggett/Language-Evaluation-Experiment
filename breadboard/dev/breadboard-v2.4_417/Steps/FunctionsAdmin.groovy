/*
 * Filename: FunctionsAdmin.groovy
 * Author: Elijah Claggett
 * Date: October 19, 2023
 *
 * Description:
 * This Groovy script defines auxilliary administrative functions used to control the Breadboard experiment.
 */

// Judge whether the participant passed the pre-evaluation or not
def judgeHuman(id, pass) {
  g.V.each { v ->
    if (v.chatId == id) {
      a.addEvent('submitHumanCheck', [
          data: Param.jsonGen.toJson([
                  playerId: v.chatId
                ])
          ])
      if (pass == 1) {
        v.passedHumanCheck = true
        v.gameStep = 'grouping'
        a.addEvent('passedEvaluation', [
          data: Param.jsonGen.toJson([
                  playerId: v.chatId,
                  reason: 'pre-evaluation'
                ])
          ])
      } else if (pass == 0) {
        v.gameStep = 'failedEvaluation'
        v.finished = true
        Param.numPlayers--
        a.addEvent('failedEvaluation', [
          data: Param.jsonGen.toJson([
                  playerId: v.chatId,
                  reason: 'pre-evaluation'
                ])
          ])
      }
    }
  }
}

// End the experiment due to profanity
def endExperimentProfanity(v) {
  v.qualified = false
  v.finished = true
  v.gameStep = 'reported'
  if (Param.talkTimers[v.chatId]) { Param.talkTimers[v.chatId].cancel() }
  def bonus = 0
  def reason = 'reported language'
  def comments = false
  v.submit = g.getSubmitForm(v, bonus, reason, Param.sandbox, comments)
  a.addEvent('profane', [
    data: Param.jsonGen.toJson([
            playerId: v.chatId,
          ])
    ])
  v.neighbors.each { n ->
    n.qualified = false
    n.finished = true
    if (Param.talkTimers[n.chatId]) { Param.talkTimers[n.chatId].cancel() }
    n.gameStep = 'reported'
    bonus = 0
    n.submit = g.getSubmitForm(n, bonus, reason, Param.sandbox, comments)
    a.addEvent('profane', [
      data: Param.jsonGen.toJson([
              playerId: n.chatId,
            ])
      ])
  }
}

// Force finishing the survey after the timer elapses
def forceFinishSurvey(v) {
  // Temporarily set the partner report to blank
  v.neighbors.each{ n ->
    n.partnerReport = 'blank'
  }
 
 // Add event to show that survey was forcefully submitted
  a.addEvent('forceFinishSurvey', [
      data: Param.jsonGen.toJson([
              playerId: v.chatId,
            ])
      ])

  // Try to automatically submit the unfinished form
  v.forceSubmitSurvey = true;

  // In case the player dropped network conection, automatically end the HIT after the donation time runs out
  v.cooperationStartTime = Instant.now().epochSecond
  def timer = new Timer()
  Param.cooperationDecisionTimers[v.chatId] = timer.runAfter((Param.cooperationTime * 60 * 1000) as int) {
    endHIT(v)
  }
}

// General function to end the experiment
def endHIT(v) {
  if (v.bonusOption == -1) {
    v.bonusOption = 1
  }

  v.gameStep = 'gradePartnerAnswer'
  a.addEvent('forceFinishGame', [
  data: Param.jsonGen.toJson([
          playerId: v.chatId,
        ])
  ])
  def neighborsFinished = true
  def allPartnersAgree = true

  v.neighbors.each { neighbor ->
    neighbor.partnerBonusOption = 1
    if (neighbor.bonusOption == -1) {
      neighborsFinished = false
    }
    if (neighbor.bonusOption as int != 2) {
      allPartnersAgree = false
    }
  }

  if (neighborsFinished) {
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
}
