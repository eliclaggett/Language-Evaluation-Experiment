/*
 * Filename: 0Globals.groovy
 * Author: Elijah Claggett
 * Date: October 19, 2023
 *
 * Description:
 * This Groovy script defines the global variables and experiment parameters used by the Breadboard backend.
 */

import groovy.json.JsonOutput;
import java.time.Instant;
import groovy.json.JsonSlurper;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

Param = [
  platform: 'mturk', // "mturk" or "prolific"
  completionCode: 'C1CUDUAH',
  mode: 'dev', // "dev" or "prod"
  samplingMode: 'slor', // "mcq", "chatbot", "chatbot2", "slor", "all"
  samplingType: 'within', // "within", "between", "random"
  prioritizeType: 'ingroup',// "ingroup" or "outgroup"
  interventionMode: 'none', // "reply", "none", or "bot"
  
  // Experiment timing in minutes
  waitingTime: 12, // must be >= join time and tutorial time in Breadboard (4 min join time + 8 min pre-eval time)
  chatTime: 3,  // time allotted to chat step
  topicDelay: 1, // time after chat starts that the topic is introduced
  followupDelay: 1, // time after topic is introduced that the followup prompt is sent
  cooperationDiscussionTime: 2, // time after the chat that is alotted to discuss cooperation decision
  surveyTime: 5, // time allotted to follow-up survey
  cooperationTime: 3, // time alloteted to cooperation decision
  
  // Experiment payment in dollars
  basePay: 3,
  prolificTask1Pay: 2,
  prolificTask2Pay: 2,
  completionBonus: 2,
  mutualCompletionBonus: 4,
  multiplier: 1.5,

  sandbox: true, // AMT sandbox mode
  timeoutWarning: 40, // time in seconds to show an inactivity warning
  timeout: 120, // time in seconds to disqualify a participant for inactivity
  groupingMethod: true, // true = 'value-based grouping', false = 'minimal group paradigm'
  nlpPort: '9090',

  // Initialized empty
  numPlayers: 0,
  chatIdCounter: 0,
  numReadyForMainTask: 0,
  numGroupingFinished: 0,
  jsonGen: JsonOutput,
  initTask: null,
  initStepTask: null,
  playerTimers: [:],
  evalTimers: [:],
  evalMessageTimers: [:],
  talkTimers: [:],
  surveyTimers: [:],
  cooperationDecisionTimers: [:],
  cooperationDiscussionTimers: [:],
  startAt: null,
  verificationFile: null,

  uiTest: false,

  // Pairwise comparison of paintings to make minimal-group paradigm pairs
  imagePairs: [
    ['p1_pottery.jpeg', 'a2_red.jpeg'],
    ['r2_castle.jpeg', 'a1_kvii.jpeg'],
    ['p3_parents.jpeg', 'r3_daughters.jpeg'],
    ['a4_yellow.jpeg', 'r1_fighting.jpeg'],
    ['p2_waterlilies.jpeg', 'a3_untitled.jpeg'],
    ['p4_interior.jpeg', 'r4_bay.jpeg']
  ],

  // Each image is one of three styles
  imageFactors: [
    [[1, 0, 0], [0, 1, 0]],
    [[0, 0, 1], [0, 1, 0]],
    [[1, 0, 0], [0, 0, 1]],
    [[0, 1, 0], [0, 0, 1]],
    [[1, 0, 0], [0, 1, 0]],
    [[1, 0, 0], [0, 0, 1]]
  ],

  // Each survey item is one of two political leanings
  // [Liberal, Conservative]
  surveyFactors: [
    [[1, 0], [0, 1]],
    [[1, 0], [0, 1]],
    [[1, 0], [0, 1]],
    [[1, 0], [0, 1]],
    [[1, 0], [0, 1]],
    [[1, 0], [0, 1]]
  ],

  // Minimal group paradigm and value-based prompts
  prompts: [
    'What makes a painting beautiful? How do you decide what is beautiful?',
    'How do you feel about the concept of "global warming"? Regarding it, what do you think U.S. policy should be?'
  ]
]

public static Map<String, String> findClosestDotenv() {
  Map<String, String> dotenvMap = new HashMap<>();
  boolean foundEnv = false;
  
  // Starting at current directory, traverse up until we find a dotenv file
  String dir = System.getProperty('user.dir');
  while (!foundEnv) {
    File folder = new File(dir);
    File[] listOfFiles = folder.listFiles();
    for (int i = 0; i < listOfFiles.length; i++) {
      if (listOfFiles[i].getName() == '.env') {
        foundEnv = true;
        break;
      }
    }

    if (!foundEnv) {
      dir = dir.substring(0, dir.lastIndexOf('/'));
    }
  }

  // Read dotenv file
  try {
    BufferedReader reader = new BufferedReader(new FileReader(dir + '/.env'))
    String line;

    while ((line = reader.readLine()) != null) {
      // Skip comments and empty lines
      if (line.trim().isEmpty() || line.trim().startsWith('#')) {
        continue;
      }

      // Split into key and value
      String[] parts = line.split('=', 2);
      if (parts.length == 2) {
        String key = parts[0].trim();
        String value = parts[1].trim();

        // Remove quotes
        if (value.startsWith('"') && value.endsWith('"')) {
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

// Read environment variables from dotenv
Map<String, String> dotenvMap = findClosestDotenv()
Param.dotenvMap = dotenvMap
Param.RECAPTCHA_SECRET = Param.dotenvMap.get('RECAPTCHA_SECRET')

// Read global variables from JSON
def jsonSlurper = new JsonSlurper();
if (Param.mode == 'prod') {
  def texts = jsonSlurper.parseText(new File('/home/ubuntu/eli/texts.json').text);
  Param = Param + texts;
} else {
  def texts = jsonSlurper.parseText(new File('/Users/eclagget/Code/smart-reply-coordination/main/texts.json').text);
  Param = Param + texts;
}