// Original task
import groovy.json.JsonOutput;
import java.time.Instant;
import groovy.json.JsonSlurper;

Param = [
  platform: 'mturk', // "mturk" or "prolific"
  completionCode: 'C1CUDUAH',
  mode: 'dev', // "dev" or "prod"
  samplingMode: 'slor', // "mcq", "chatbot", "chatbot2", "slor", "all"
  samplingType: 'within', // "within", "between", "random"
  prioritizeType: 'ingroup',// "ingroup" or "outgroup"
  interventionMode: 'none', // "reply", "none", or "bot"
  waitingTime: 12, // in minutes, must be >= join time and tutorial time in Breadboard (4 min join time + 8 min pre-eval time)
  chatTime: 3, // in minutes
  topicDelay: 1,
  followupDelay: 1,
  cooperationDiscussionTime: 2,
  surveyTime: 5, // in minutes
  donationTime: 3,
  
  basePay: 3,
  prolificTask1Pay: 2,
  prolificTask2Pay: 2,
  completionBonus: 2,
  mutualCompletionBonus: 4,
  multiplier: 1.5,

  sandbox: true,
  timeoutWarning: 40,
  timeout: 120,
  groupingMethod: true, // Do not change
  nlpPort: '9090',

  numPlayers: 0,
  chatIdCounter: 0,
  numReadyForGrouping: 0,
  numGroupingFinished: 0,
  jsonGen: JsonOutput,
  initTask: null,
  initStepTask: null,
  playerTimers: [:],
  evalTimers: [:],
  evalMessageTimers: [:],
  talkTimers: [:],
  surveyTimers: [:],
  donationTimers: [:],
  cooperationDiscussionTimers: [:],
  startAt: null,
  verificationFile: null,

  uiTest: false,

  imagePairs: [
  ['p1_pottery.jpeg', 'a2_red.jpeg'],
  ['r2_castle.jpeg', 'a1_kvii.jpeg'],
  ['p3_parents.jpeg', 'r3_daughters.jpeg'],
  ['a4_yellow.jpeg', 'r1_fighting.jpeg'],
  ['p2_waterlilies.jpeg', 'a3_untitled.jpeg'],
  ['p4_interior.jpeg', 'r4_bay.jpeg']
],
imageFactors: [
  [[1, 0, 0], [0, 1, 0]],
  [[0, 0, 1], [0, 1, 0]],
  [[1, 0, 0], [0, 0, 1]],
  [[0, 1, 0], [0, 0, 1]],
  [[1, 0, 0], [0, 1, 0]],
  [[1, 0, 0], [0, 0, 1]]
],

// [Liberal, Conservative]
surveyFactors: [
  [[1, 0], [0, 1]],
  [[1, 0], [0, 1]],
  [[1, 0], [0, 1]],
  [[1, 0], [0, 1]],
  [[1, 0], [0, 1]],
  [[1, 0], [0, 1]]
],
prompts: [
  'What makes a painting beautiful? How do you decide what is beautiful?',
  'How do you feel about the concept of "global warming"? Regarding it, what do you think U.S. policy should be?'
],
EXPERIMENT_DIR: '/Users/eclagget/Code/smart-reply-coordination/main'
]



// Start code written by ChatGPT because I can't import the library to parse dotenv files
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public static Map<String, String> parseDotenvFile(String filePath) {
    Map<String, String> dotenvMap = new HashMap<>();

    try {
        BufferedReader reader = new BufferedReader(new FileReader(filePath))
        String line;

        while ((line = reader.readLine()) != null) {
            // Skip comments and empty lines
            if (line.trim().isEmpty() || line.trim().startsWith("#")) {
                continue;
            }

            // Split the line into key and value
            String[] parts = line.split("=", 2);
            if (parts.length == 2) {
                String key = parts[0].trim();
                String value = parts[1].trim();

                // Remove quotes around the value if present
                if (value.startsWith("\"") && value.endsWith("\"")) {
                    value = value.substring(1, value.length() - 1);
                }

                dotenvMap.put(key, value);
            }
        }
    } catch (IOException e) {
        e.printStackTrace();
    }

    return dotenvMap;
}
// End code written by ChatGPT

Map<String, String> dotenvMap = parseDotenvFile(Param.EXPERIMENT_DIR);
Param.dotenvMap = dotenvMap
Param.RECAPTCHA_SECRET = Param.dotenvMap.get('RECAPTCHA_SECRET')

def jsonSlurper = new JsonSlurper();
def mergeMaps(Map input, Map merge) {
    merge.each { k, v ->
        if (v instanceof Map)
            mergeMaps(input[k], v)
        else
            input[k] = v
    }
}

if (Param.mode == 'prod') {
  def texts = jsonSlurper.parseText(new File('/home/ubuntu/eli/texts.json').text);
  Param = Param + texts;
} else {
  def texts = jsonSlurper.parseText(new File('/Users/eclagget/Code/smart-reply-coordination/main/texts.json').text);
  Param = Param + texts;
}

// Initialized empty
// def numPlayers = 0
// def numReadyForGrouping = 0
// Param.numGroupingFinished = 0
// Param.initTask = null
// Param.playerTimers = []
// Param.talkTimers = []
// Param.startAt = null
// Param.verificationFile = null

// Pop art, abstract, representational

// Personal deficiency, stigma (culture of poverty), structural

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

def generateEvaluatorMessage(text) {
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
  v.donationStartTime = Instant.now().epochSecond
  def timer = new Timer()
  Param.donationTimers[v.chatId] = timer.runAfter((Param.donationTime * 60 * 1000) as int) {
    endHIT(v)
  }
}


def endHIT(v) {
  
  if (v.bonusOption == -1) {
    v.bonusOption = 1
  }

  v.gameStep = 'partnerAnswer'
  a.addEvent('forceFinishGame', [
  data: Param.jsonGen.toJson([
          playerId: v.chatId,
        ])
  ])
  def neighborsFinished = true
  def allPartnersAgree = true
  // def sharedPotTotal = v.donated

  // v.neighbors.each { neighbor ->
  //   neighbor.received = 0
  //   sharedPotTotal += neighbor.donated
  //   if (neighbor.donated == -1) {
  //     neighborsFinished = false
  //   }
  // }
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
