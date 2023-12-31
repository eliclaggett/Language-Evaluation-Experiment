/*
 * Filename: FunctionsGrouping.groovy
 * Author: Elijah Claggett
 * Date: October 19, 2023
 *
 * Description:
 * This Groovy script defines functions used to assign participants "identity groups" and make pairs.
 */

import java.util.*

// Groovy will cast this to the wrong type; must initialize as a LinkedHashMap
// We also can't define them with a type on the left side of equals or else Groovy will scope them incorrectly
playerPassEval = new LinkedHashMap<Integer,Integer>()

// Calculate median
def median(data) {
  def copy = data.sort()
  def middle = data.size().intdiv(2)
  // you can omit the return in groovy for the last statement
  data.size() % 2 ? copy[middle] : (copy[middle - 1] + copy[middle]) / 2
}

// Set a participant as leftover if we couldn't assign a partner
def setIsLeftover(chatId) {
  def player = g.V.find { v-> v.chatId == chatId }
  player.qualified = false
  player.finished = true
  def bonus = 0
  def reason = 'leftover'
  def comments = false
  player.submit = g.getSubmitForm(player, bonus, reason, Param.sandbox, comments)
  player.gameStep = 'leftover'
  a.addEvent('leftover', [
      data: Param.jsonGen.toJson([
            playerId: player.chatId
          ])
      ])
  Param.numPlayers--
}

// Find the index and value of the max element in a list
// Prefer stronger opinions
// If multiple answers have the same opinion difference, pick randomly
def maxWithIdx(data, exclude = []) {
  def val = Integer.MIN_VALUE
  def idx = -1

  if (data.getClass() == java.util.LinkedHashMap) {
    // LinkedHashMap
    for (entry in data) {
      if (exclude.contains(entry.key)) {
        continue
      }

      if (entry.value >= val) {
        val = entry.value
        idx = entry.key
      }
    }
  } else if (data) {
    // ArrayList
    for (int i = 0; i < data.size(); i++) {
      if (exclude.contains(i)) {
        continue
      }

      if (data[i] >= val) {
        val = data[i]
        idx = i
      }
    }
  }

  return [idx, val]
}

// Find the index and value of the min element in a list
// Prefer stronger opinions
// If multiple answers have the same opinion difference, pick randomly
def minWithIdx(data, exclude = []) {
  def val = Integer.MAX_VALUE
  def idx = -1

  if (data.getClass() == java.util.LinkedHashMap) {
    // LinkedHashMap
    for (entry in data) {
      if (exclude.contains(entry.key)) {
        continue
      }

      if (entry.value <= val) {
        val = entry.value
        idx = entry.key
      }
    }
  } else if (data) {
    // ArrayList
    for (int i = 0; i < data.size(); i++) {
      if (exclude.contains(i)) {
        continue
      }

      if (data[i] <= val) {
        val = data[i]
        idx = i
      }
    }
  }

  return [idx, val]
}

// Find the topic that these two players differ by exactly "diff" on
// (i.e. diff=0 is maximum agreement, diff=6 is maximum disagreement)
def ensureBestTopic(answers, playerKey, partnerKey, diff) {
  def p1Answers = answers[playerKey]
  def p2Answers = answers[partnerKey]

  def eligibleTopics = []
  def eligibleTopicOpinionStrengths = []
  for (int i = 0; i < p1Answers.size(); i++) {
    def thisDiff = Math.abs(p1Answers[i] - p2Answers[i])

    if (thisDiff == diff) {
      // Only allow participants on the agree side of the spectrum to be matched with participants on the disagree side
      if (
        (p1Answers[i] < 3 && p2Answers[i] > 3) ||
        (p1Answers[i] > 3 && p2Answers[i] < 3)
        ) {
        eligibleTopics.add(i)
        eligibleTopicOpinionStrengths.add(Math.abs(p1Answers[i] - 2) + Math.abs(p2Answers[i] - 2))
      } else if (
        Param.platform == 'prolific' &&
        (
          (p1Answers[i] < 2 && p2Answers[i] > 2) ||
          (p1Answers[i] > 2 && p2Answers[i] < 2)
        )
      ) {
        // For Prolific, allow out-group pairs with weak opinion differences
        eligibleTopics.add(i)
        eligibleTopicOpinionStrengths.add(Math.abs(p1Answers[i] - 2) + Math.abs(p2Answers[i] - 2))
      } else if (
        (diff == 0 || diff == 1) &&
        (
          (p1Answers[i] < 3 && p2Answers[i] < 3) ||
          (p1Answers[i] > 3 && p2Answers[i] > 3)
        )
      ) {
        // When we allow in-group pairs, only make in-group pairs that have some opinion
        eligibleTopics.add(i)
        eligibleTopicOpinionStrengths.add(Math.abs(p1Answers[i] - 2) + Math.abs(p2Answers[i] - 2))
      }
    }
  }

  (idx, maxOpinionStrength) = maxWithIdx(eligibleTopicOpinionStrengths)

  if (idx == -1) {
    return [-1, -1]
  } else {
    // Randomly pick from topics if there is a tie
    def countWithSameStrength = 0
    for (int i = 0; i < eligibleTopicOpinionStrengths.size(); i++) {
      if (eligibleTopicOpinionStrengths[i] == maxOpinionStrength) {
        countWithSameStrength++
      }
    }
    def useThisTopic = (int) Math.round(Math.random() * countWithSameStrength)
    def maxIdx = 0
    for (int i = 0; i < eligibleTopicOpinionStrengths.size(); i++) {
      if (eligibleTopicOpinionStrengths[i] == maxOpinionStrength) {
        if (maxIdx == useThisTopic) {
          idx = i
        }
        maxIdx++
      }
    }

    return [eligibleTopics[idx], maxOpinionStrength]
  }
}

// Find a partner that has an opinion difference of exactly "diff" with this participant on any topic
// (i.e. diff=0 is maximum agreement, diff=6 is maximum disagreement)
def ensureBestPartner(answers, diffs, playerKey, diff, exclude) {
  def eligiblePartners = []
  def eligiblePartnerTopics = []
  def eligiblePartnerOpinionStrengths = []
  for (partner in diffs[playerKey]) {
    if (exclude.contains(partner.key)) {
      continue
    }
    if (diffs[playerKey][partner.key].contains(diff)) {
      if (Param.samplingType == 'within' && (playerPassEval[playerKey] != playerPassEval[partner.key])) {
        continue
      } else if (Param.samplingType == 'between' && playerPassEval[playerKey] == playerPassEval[partner.key]) {
        continue
      }
      // else if (Param.samplingType == 'random') {
        // Do nothing.
      // }

      // This player is eligible
      def (topic, strength) = ensureBestTopic(answers, playerKey, partner.key, diff)
      if (topic != -1) {
        eligiblePartners.add(partner.key)
        eligiblePartnerTopics.add(topic)
        eligiblePartnerOpinionStrengths.add(strength)
      }
    }
  }

  (idx, maxOpinionStrength) = maxWithIdx(eligiblePartnerOpinionStrengths)

  if (idx == -1) {
    return [-1, -1]
  } else {
    return [eligiblePartners[idx], eligiblePartnerTopics[idx]]
  }
}

// Must initialize these as a Java map otherwise Groovy doesn't interpret them correctly
// We also can't define them with a type on the left side of equals or else Groovy will scope them incorrectly
pairs = new LinkedHashMap<Integer,Integer>()
playerGroups = new LinkedHashMap<Integer,Integer>() // 0=ingroup, 1=outgroup
pairTopics = new LinkedHashMap<Integer,Integer>()

playerAnswers = new LinkedHashMap<Integer,ArrayList<Integer>>()
answerDiffs = new LinkedHashMap<Integer,LinkedHashMap<Integer,ArrayList<Integer>>>()
pairwiseMaxDiffs = new LinkedHashMap<Integer,LinkedHashMap<Integer,Integer>>()
pairwiseMinDiffs = new LinkedHashMap<Integer,LinkedHashMap<Integer,Integer>>()

def makeOutGroupPairs(dist, pId) {
  // Ensure this is an optimal pair and optimal topic
  def (mxPartner, topic) = ensureBestPartner(playerAnswers, answerDiffs, pId, dist, pairs.keySet())

  if (mxPartner == -1) { return false }

  pairs[pId] = mxPartner
  pairs[mxPartner] = pId
  playerGroups[pId] = 0
  playerGroups[mxPartner] = 1

  pairTopics[pId] = topic
  pairTopics[mxPartner] = topic
  return true
}
def makeInGroupPairs(dist, pId) {
  // Ensure this is an optimal pair
  def (mnPartner, topic) = ensureBestPartner(playerAnswers, answerDiffs, pId, dist, pairs.keySet())

  if (mnPartner == -1) { return false }

  pairs[pId] = mnPartner
  pairs[mnPartner] = pId
  playerGroups[pId] = 0
  playerGroups[mnPartner] = 0

  pairTopics[pId] = topic
  pairTopics[mnPartner] = topic
  return true
}
def makeRemaingRandomPairs() {
  for (entry in pairwiseMinDiffs) {
    if (pairs.containsKey(entry.key)) {
      continue
    }

    def (mnPartner, mn) = minWithIdx(pairwiseMinDiffs[entry.key], pairs.keySet())

    if (mnPartner == -1) {
      setIsLeftover(entry.key)
      continue
    }

    def (topic, _) = ensureBestTopic(playerAnswers, entry.key, mnPartner, 2)

    pairs[entry.key] = mnPartner
    pairs[mnPartner] = entry.key

    def thisPairType = (int) Math.round(Math.random())
    playerGroups[entry.key] = 0
    playerGroups[mnPartner] = 1

    pairTopics[entry.key] = topic
    pairTopics[mnPartner] = topic
  }
}

// Executed each time a participant submits an answer to the intial survey
answerInitialSurvey = { player, data ->
  def idx = data[0]
  def answer = data[1]

  player.initalSurveyAnswers[idx] = answer

  a.addEvent('answerInitialSurvey', [
    data: Param.jsonGen.toJson([
            idx: idx,
            answer: answer,
            playerId: player.chatId
          ])
    ])
}

// Executed when a participant finishes the initial survey
completeInitialSurvey = { thisPlayer, key ->
  thisPlayer.groupingComplete = true
  a.addEvent('completedInitialSurvey', [
    data: Param.jsonGen.toJson([
            playerId: thisPlayer.chatId
          ])
    ])
  thisPlayer.gameStep = 'tutorial2'
  Param.numGroupingFinished++
}

// Executed after the experiment waiting time elapses
calculateGroups = {
  g.V.each { player ->
    if (!player.qualified && !player.finished) {
      player.finished = true
      player.gameStep = 'notReady'
      def bonus = 0
      def reason = 'notReady'
      def comments = false
      player.submit = g.getSubmitForm(player, bonus, reason, Param.sandbox, comments)

      a.addEvent('notReady', [
          data: Param.jsonGen.toJson([
                playerId: player.chatId
              ])
          ])
      Param.numPlayers--
    }
  }

  // Store all player answers for easy computation
  g.V.each { player ->
    if (player.qualified) {
      def p = player.initalSurveyAnswers
      playerPassEval[player.chatId] = player.passEval
      playerAnswers[player.chatId] = [p[0], p[1], p[2], p[3], p[4], p[5], p[6]]
    }
  }

  // Optimize pairs in descending order of priority
  // If we have a difference of 4, make an outgroup pair
  // If we have a difference of 0, and the answers are 0 or 4, make an ingroup pair
  // If we have a difference >  2, make an outgroup pair
  // If we have a difference of 1, make an ingroup pair
  // Otherwise (difference == 2), make random pair

  // Calculate pairwise answer distances
  for (entry in playerAnswers) {
    answerDiffs[entry.key] = new LinkedHashMap<Integer,ArrayList<Integer>>()
    for (partnerEntry in playerAnswers) {
      if (partnerEntry.key != entry.key && partnerEntry.value) {
        answerDiffs[entry.key][partnerEntry.key] = new ArrayList<Integer>()
        for (int i; i < partnerEntry.value.size(); i++) {
          answerDiffs[entry.key][partnerEntry.key].add(Math.abs(playerAnswers[partnerEntry.key][i] - playerAnswers[entry.key][i]))
        }
      }
    }
  }

  // Select pairwise min and max answer distances
  for (entry in answerDiffs) {
    pairwiseMaxDiffs[entry.key] = new LinkedHashMap<Integer,Integer>()
    pairwiseMinDiffs[entry.key] = new LinkedHashMap<Integer,Integer>()
    for (partnerEntry in answerDiffs[entry.key]) {
      def (argmx, mx) = maxWithIdx(answerDiffs[entry.key][partnerEntry.key])
      def (argmn, mn) = minWithIdx(answerDiffs[entry.key][partnerEntry.key])

      pairwiseMaxDiffs[entry.key][partnerEntry.key] = mx
      pairwiseMinDiffs[entry.key][partnerEntry.key] = mn
    }
  }

  for (entry in answerDiffs) {

    if (pairs.containsKey(entry.key)) { continue }

    def pairType = (int) Math.round(Math.random())

    if (pairType == 1) {
      def success = makeOutGroupPairs(6, entry.key)
      if (!success) { success = makeOutGroupPairs(5, entry.key) }
      if (!success) { success = makeOutGroupPairs(4, entry.key) }
      if (!success) { success = makeOutGroupPairs(3, entry.key) }

      if (!success) {
        pairType = 0
        success = makeInGroupPairs(0, entry.key)
      }
      if (!success) { success = makeInGroupPairs(1, entry.key) }
      println('Player: ' + entry.key + ' Type: ' + pairType + ' Success: ' + success)
    } else {
      def success = makeInGroupPairs(0, entry.key)
      if (!success) { success = makeInGroupPairs(1, entry.key) }

      if (!success) {
        pairType = 1
        success = makeInGroupPairs(6, entry.key)
      }
      if (!success) { success = makeOutGroupPairs(5, entry.key) }
      if (!success) { success = makeOutGroupPairs(4, entry.key) }
      if (!success) { success = makeOutGroupPairs(3, entry.key) }
      println('Player: ' + entry.key + ' Type: ' + pairType + ' Success: ' + success)
    }
  }

  // Setup pairs
  def edges = []
  g.V.each { player ->
    if (player.qualified) {
      def partner = g.V.find { v-> v.chatId == pairs[player.chatId] }
      if (partner != null) {
        def playerGroup = playerGroups[player.chatId]

        player.topic = pairTopics[player.chatId]
        a.addEvent('setTopic', [
          data: Param.jsonGen.toJson([
              playerId: player.chatId,
              topicId: player.topic
            ])
          ])

        // In-group pairs will both be playerGroup==0
        // Out-group pairs have one partner playerGroup==0 and one partner playerGroup==1
        if (playerGroup == 0) {
          // In group
          player.groupName = 'Violet Group'
          player.groupImage = 'group0.png'
          player.groupLabel = 'V'
          player.groupColor = 'violet'
          player.groupId = 0
        } else {
          // Out group
          player.groupName = 'Orange Group'
          player.groupImage = 'group1.png'
          player.groupLabel = 'Y'
          player.groupColor = 'yellow'
          player.groupId = 1
        }

        a.addEvent('setGroup', [
          data: Param.jsonGen.toJson([
              playerId: player.chatId,
              groupId: player.groupId
            ])
          ])

        if (!edges.contains(player) && !edges.contains(partner)) {
          println('Creating social tie for ' + player.chatId + ' and ' + partner.chatId)
          g.addEdge(player, partner)
          edges.add(player)
          edges.add(partner)
        }

        a.addEvent('setNeighbor', [
          data: Param.jsonGen.toJson([
              playerId: player.chatId,
              neighborId: partner.chatId
            ])
          ])

        partner.neighborNodes.add(['chatId': player.chatId, 'groupName': player.groupName, 'groupImage': player.groupImage, 'groupLabel': player.groupLabel, 'groupColor': player.groupColor, 'groupId': player.groupId,  'initalSurveyAnswers': player.initalSurveyAnswers, 'groupingFactors': player.groupingFactors])
      }
    }
  }


  // If a participant hasn't been assigned a conversation topic by now, they have been unable to be paired
  // End the experiment for those participants 
  g.V.each { player ->
    if (player.qualified) {
      if (player.topic == -1) {
        setIsLeftover(player.chatId)
      } else {
        player.isWaiting = false
      }
    }
  }

  // 30 second timer automatically move to chat
  def timer = new Timer()
  timer.runAfter(30 * 1000) {
    g.V.each { player ->
      if (player.gameStep == 'tutorial2') {
        player.gameStep = 'mainChat'
        startMainChat(player)
      }
    }
  }
}
