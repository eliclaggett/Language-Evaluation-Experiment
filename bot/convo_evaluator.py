#
# Filename: convo_evaluator.py
# Author: Elijah Claggett
#
# Description:
# This file is a Python web server used for evaluating participants' innate communication styles
#

# Imports
from transformers import logging
from flashtext import KeywordProcessor
from SLOR import calc_acceptability_metrics
from google.cloud import dialogflow
from websockets.server import serve
import websockets
import asyncio
import pathlib
import ssl
from transformers import AutoTokenizer, AutoModelForSequenceClassification, TextClassificationPipeline
import numpy as np
from datetime import datetime, timedelta
import json
import time
import sys
from dotenv import load_dotenv, find_dotenv
import os

PORT = '9090'

load_dotenv(find_dotenv())

fspath = os.getenv('EXPERIMENT_DIR')
if (os.getenv('DEPLOYMENT') == 'prod'):
    fspath = os.getenv('PROD_EXPERIMENT_DIR')

DIALOGFLOW_PROJECT_ID = os.getenv('DIALOGFLOW_PROJECT_ID')
SERVER_DOMAIN = os.getenv('SERVER_DOMAIN')

# Helper
class obj:
    # constructor
    def __init__(self, dict1):
        self.__dict__.update(dict1)

connected = set()
participantMsgs = {}

logging.set_verbosity_error()

# For each participant, calculate a communication score
# Pass in a list of all of their chat messages
# Score dimensions:
# - Willingness to communicate
# - Fluency
# - Ability to express opinion
# - Ability to ask a question
# - Friendliness
# - Typing pattern

agreement_db = ["true", "agree", "agrees", "Agreed", "overall_point", "understandable", "agreeing", "general_point", "main_point", "personal_opinion", "reiterate", "fair_statement", "opinion", "sentiment", "fair_point", "total_agreement", "complete_agreement", "agree-", "general_premise", "just_my_opinion", "overall_sentiment", "fair_assessment", "valid_point", "good_argument", "fair", "obviously", "last_point", "admit", "just_my_personal_opinion", "underlying_point", "other_points", "just_opinion", "certainly", "just_an_opinion", "fair_argument", "only_my_opinion",
                "fair_criticism", "most_reasonable_people", "concur", "Fair_point", "full_agreement", "decent_point", "aggree", "assessment",  "Understandable", "personal_view", "reasonable_point", "honest", "minority_opinion", "only_point", "first_point", "honestly", "own_personal_opinion", "suggesting", "just_a_personal_opinion", "first_two_points", "solid_point", "last_statement", "totally", "concede", "solid_argument", "larger_point", "great_argument", "convinced", "basic_point", "general_consensus", "suppose", "strong_opinion", "regard", "popular_opinion", 'correct', 'yes', 'yep']

keyword_processor_agree = KeywordProcessor(case_sensitive=False)

keyword_processor_agree.add_keywords_from_list(
    [word.replace('_', ' ') for word in agreement_db])

def count_agree(text):
    return len(keyword_processor_agree.extract_keywords(text))

def grade_communication(id, msgs):
    """

        :param list messages: first input
        :returns: 
            - msg_count - Number of messages
            - char_count - Number of characters
            - total_slor_score - Average SLOR score
            - total_express_opinion_score - Best express opinion score
            - total_ask_question_score - Best ask question score
            - total_politeness_score - Total politness scores
    """
    # Willingness to communicate
    # - Measured by length and number of messages

    messages = []
    for msg_raw in msgs:
        msg = json.loads(msg_raw)
        messages.append(msg)

    # Initialize other scores to 0
    total_slor_score = 0
    agree_count = 0
    char_count = 0
    msg_count = 0
    conversation_turns = 0
    uses_allcaps = True

    last_chatter = -1
    for msg in messages:
        # Fluency
        # - Measured by SLOR Acceptability score
        if (msg['participantId'] != -1 and msg['participantId'] != 999 and last_chatter == -1):
            last_chatter = msg['participantId']
            conversation_turns += 1

        if (last_chatter != msg['participantId'] and last_chatter != -1):
            last_chatter = msg['participantId']
            conversation_turns += 1

        if (msg['participantId'] == -1 or msg['participantId'] == 999):
            continue

        if (msg['content'].upper() != msg['content']):
            uses_allcaps = False

        ppl, slor = calc_acceptability_metrics(msg['content'])
        if (slor != 0):
            total_slor_score += slor

        agree_count += count_agree(msg['content'])
        char_count += len(msg['content'])
        msg_count += 1

    total_slor_score /= msg_count

    agree_per_turn = agree_count / conversation_turns
    chars_per_turn = char_count / conversation_turns

    print(agree_per_turn, total_slor_score, chars_per_turn)
    score = 0.6518 * agree_per_turn + 1.0264 * total_slor_score

    # if (uses_allcaps):
    #     score = -2

    return score

# Create a table where each row is a player and each column is their various communication scores
# Then, predict high and low mutual cooperation using each of the score parameters

# id, scores, mutual_cooperation
player_grades = []

def detect_intent_texts(project_id, session_id, texts, language_code='en-us'):
    """Returns the result of detect intent with texts as inputs.

    Using the same `session_id` between requests allows continuation
    of the conversation."""

    session_client = dialogflow.SessionsClient()

    session = session_client.session_path(project_id, session_id)
    # print("Session path: {}\n".format(session))

    for text in texts:
        text_input = dialogflow.TextInput(
            text=text, language_code=language_code)

        query_input = dialogflow.QueryInput(text=text_input)

        response = session_client.detect_intent(
            request={"session": session, "query_input": query_input}
        )

        # print("=" * 20)
        # print("Query text: {}".format(response.query_result.query_text))
        # print(
        #     "Detected intent: {} (confidence: {})\n".format(
        #         response.query_result.intent.display_name,
        #         response.query_result.intent_detection_confidence,
        #     )
        # )
        # print("Fulfillment text: {}\n".format(response.query_result.fulfillment_text))
        return response.query_result.intent.display_name, response.query_result.fulfillment_text

# Main program
conversation_states = [
    'greeting',
    'question_partner',
    'answer',
    'farewell'
]

# Important: For reply suggestions only, we have the following two-step exchanges:
# Greeting
# Anger management
# Encouragement
# Farewell
# Only use the first of these steps if the intervention mode is chatbot

f = open(f'{fspath}/texts.json', "r")
textsData = json.loads(f.read())
f.close()

suggestions = textsData['suggestions']
botSuggestions = textsData['botSuggestions']

# Convert to object bc I like those more
suggestions = json.loads(json.dumps(suggestions), object_hook=obj)
botSuggestions = json.loads(json.dumps(botSuggestions), object_hook=obj)

# NLP
if (os.path.isdir(f'{fspath}/bot/models/vira-dialog-act-classification')):
    tokenizer = AutoTokenizer.from_pretrained(
        f"{fspath}/bot/models/vira-dialog-act-classification/dialog_act_model", local_files_only=True)
else:
    tokenizer = AutoTokenizer.from_pretrained(
        'ibm/roberta-base-vira-dialog-acts-live')

if (os.path.isdir(f'{fspath}/bot/shards')):
    model = AutoModelForSequenceClassification.from_pretrained(
        f"{fspath}/bot/shards", local_files_only=True)
else:
    model = AutoModelForSequenceClassification.from_pretrained(
        'ibm/roberta-base-vira-dialog-acts-live')
    os.mkdir(f'{fspath}/bot/shards')
    model.save_pretrained(f"{fspath}/bot/shards", max_shard_size="200MB")

pipeline = TextClassificationPipeline(
    model=model, tokenizer=tokenizer, top_k=None)

# TODO: Delete the following three lines!!
# tokenizer = None
# model = None
# pipeline = None

label_map = {
    'LABEL_0': 'greeting',
    'LABEL_1': 'farewell',
    'LABEL_2': 'negative_reaction',
    'LABEL_3': 'positive_reaction',
    'LABEL_4': 'concern',
    'LABEL_5': 'query',
    'LABEL_6': 'other'
}

def analyzeMsg(msg):
    results = pipeline([msg])
    class_scores = np.array([[dataclass['score']
                            for dataclass in result] for result in results])
    class_labels = np.array([[dataclass['label']
                            for dataclass in result] for result in results])[0]
    pred = np.argmax(class_scores, axis=1)[0]

    # predicted label & confidence
    return (label_map[class_labels[pred]], class_scores[pred])

# Also suggest messages for cases when a participant is aggravated or unresponsive

class Participant:
    def __init__(self, id):
        self.id = id

        self.last_msg_time = datetime.now()
        self.num_msgs = 0
        self.targeted_anger = 0
        self.qr_cycle = 0
        self.num_queries = 0

        self.saw = {}
        for suggestion, _ in suggestions.__dict__.items():
            self.saw[suggestion] = False

    def registerMessage(self, msg):
        self.num_msgs += 1
        self.last_msg_time = datetime.now()

class Pair:
    def __init__(self, p1: Participant, p2: Participant, mode: str):
        self.p1 = p1
        self.p2 = p2
        self.state = None
        self.mode = mode

        self.requestedEncouragement = False
        self.requestedFarewell = False
        self.usedAngerManagement = False

        # Order is subject to change depending on how the pair is being recalled
        self.participant = p1
        self.partner = p2
        # TODO: make this the real end time of the chat
        self.chat_end_time = datetime.now() + timedelta(minutes=10)

    def hasParticipant(self, id: int):
        return self.p1.id == id or self.p2.id == id

    def numMsgs(self):
        return self.p1.num_msgs + self.p2.num_msgs

pairs = []

def getPair(id):
    global pairs
    for pair in pairs:
        if pair.hasParticipant(id):
            if (pair.p1.id == id):
                pair.participant = pair.p1
                pair.partner = pair.p2
            else:
                pair.participant = pair.p2
                pair.partner = pair.p1

            return pair
    return None

# Create pair
def createPair(id1, id2, mode):
    global pairs
    pairs.append(Pair(Participant(id1), Participant(id2), mode))

def defuseSituation(thisPair):
    if not thisPair:
        return None, None, None, None

    thisPair.usedAngerManagement = True

    if (thisPair.mode != 'bot'):
        return [
            thisPair.participant.id,
            thisPair.partner.id
        ], [
            suggestions.anger_management[0],
            suggestions.anger_management[1]
        ], [
            None,
            suggestions.anger_management[0]
        ], [
            'anger_management0',
            'anger_management1'
        ]
    else:
        return thisPair.participant.id, botSuggestions.anger_management, False, 'anger_management'

# Process message from partner
def deliverSuggestion(pair, isPartner, msg=None, dt=None):

    participant = pair.partner if isPartner else pair.participant
    partner = pair.partner if not isPartner else pair.participant

    # Suggestion index (0 for all p1s, 1 for all p2s)
    sIdx = int(participant.id == pair.p2.id)

    # Process current message if there is any
    act = None
    confidence = 0
    subject = None
    dir_obj = None
    if msg is not None:
        act, confidence = analyzeMsg(msg)
        print(act, confidence)
        # subject, dir_obj = getSentenceParts(msg) # TODO: Split into sentences

        participant.registerMessage(msg)

        # if ((act == 'negative_reaction' or act == 'concern') and ((subject and subject.lower() == 'you') or (dir_obj and dir_obj.lower() == 'you'))):
        #     participant.targeted_anger += 1

        if act == 'query' or act == 'concern':
            participant.qr_cycle += 1
            participant.num_queries += 1

        if partner.qr_cycle % 2 == 1 and (act == 'negative_reaction' or act == 'positive_reaction' or act == 'concern'):
            partner.qr_cycle += 1
        print('Participant {} QR Cycle: {}'.format(
            participant.id, participant.qr_cycle))
        print('Participant {} QR Cycle: {}'.format(
            partner.id, partner.qr_cycle))

    # Order of priority:
    # 1) make good first impression
    # 2) defuse anger if there is any
    # 3) address other problems (e.g. unresponsive partner)
    # 4) respond appropriately to the last message
    # 5) make good last impression

    #  Disable because it's not working well
    # if (participant.targeted_anger > 1 and not pair.usedAngerManagement): # TODO: make this real
    #     # Suggest anger defusion message
    #     # 0 is suggested for the angry participant, 1 is suggested for the offender
    #     print('Defusing situation...')
    #     return defuseSituation(pair)

    currentTime = datetime.now()

    # In all other cases we need to analyze the dialog act
    if msg is not None:

        # Active listening logic
        if partner.qr_cycle % 2 == 0 and partner.qr_cycle > 0 and (act == 'negative_reaction' or act == 'positive_reaction' or act == 'concern'):
            # A QR cycle is complete, suggest active listen
            if (partner.qr_cycle == 2 and not partner.saw['active_listen1']):
                partner.saw['active_listen1'] = True
                print('Sending active listening 1 to {}'.format(partner.id))
                return partner.id, suggestions.active_listen1[sIdx], None, 'active_listen1' + str(sIdx)
            elif (partner.qr_cycle == 4 and not partner.saw['active_listen2']):
                partner.saw['active_listen2'] = True
                print('Sending active listening 2 to {}'.format(partner.id))
                return partner.id, suggestions.active_listen2[sIdx], None, 'active_listen2' + str(sIdx)

        # Express interest logic
        # If this participant doesn't ask questions, force them to
        if participant.num_queries <= 1 and pair.chat_end_time - currentTime < timedelta(minutes=5) and not participant.saw['express_interest']:
            participant.saw['express_interest'] = True
            print('Sending express interest to {}'.format(participant.id))
            return participant.id, suggestions.express_interest[sIdx], None, 'express_interest' + str(sIdx)

        # Gratitude logic
        if not partner.saw['gratitude'] and (act == 'negative_reaction' or act == 'positive_reaction' or act == 'concern') and isPartner and pair.chat_end_time - currentTime < timedelta(minutes=5):
            partner.saw['gratitude'] = True
            print('Sending gratitude to {}'.format(participant.id))
            return partner.id, suggestions.gratitude[sIdx], None, 'gratitude' + str(sIdx)

    return None, None, None, None

# Process requests
# try:
pSocks = {}

def registerParticipantSock(id, sock):
    global pSocks
    pSocks[id] = sock

async def handleInput(websocket):
    global pSocks
    connected.add(websocket)
    try:
        async for message in websocket:
            global participantMsgs
            output = {'success': False}
            sent = False
            data = json.loads(message, object_hook=obj)
            if data.command == 'ping':
                output['success'] = True
            elif data.command == 'resetPairs':
                global pairs
                # global participantMsgs
                pairs = []
                participantMsgs = {}
                pSocks = {}
                print('Resetting pairs...')
                output['success'] = True
            elif data.command == 'registerParticipant':
                # global participantMsgs
                registerParticipantSock(data.id, websocket)
                if (data.id not in participantMsgs.keys()):
                    participantMsgs[data.id] = []
                output['success'] = True
            elif data.command == 'createPair':
                thisPair = getPair(data.id1)
                if (thisPair == None):
                    print('Creating pair for {}, {}...'.format(
                        data.id1, data.id2))
                    createPair(data.id1, data.id2, data.mode)
                registerParticipantSock(data.id1, websocket)

                if thisPair and thisPair.state != 'greeting' and thisPair.numMsgs() == 0 and data.id1 in pSocks and data.id2 in pSocks:
                    thisPair.state = 'greeting'
                    thisPair.p1.saw['greeting'] = True
                    thisPair.p2.saw['greeting'] = True

                    if (thisPair.mode != 'bot' and thisPair.mode != 'none'):
                        await pSocks[data.id1].send(json.dumps({'suggestion': suggestions.greeting[0], 'id': 'greeting0'}))
                        await pSocks[data.id2].send(json.dumps({'suggestion': suggestions.greeting[1], 'id': 'greeting1', 'requiresPreceding': suggestions.greeting[0]}))
                    else:
                        await pSocks[data.id1].send(json.dumps({'suggestion': botSuggestions.greeting, 'id': 'greeting'}))

                output['success'] = True
            elif data.command == 'parseMsg':
                print('Generating suggestion for {}...'.format(data.id))
                pair = getPair(data.id)
                if pair:
                    id, suggestion, preReq, suggestionId = deliverSuggestion(
                        pair, data.isPartner, data.msg)
                    sent = True

                    if (id is not None and isinstance(id, list)):
                        for idx in range(len(id)):
                            if (id[idx] in pSocks):
                                output['suggestion'] = suggestion[idx]
                                if preReq[idx] is not None:
                                    output['requiresPreceding'] = preReq[idx]
                                output['success'] = True
                                output['id'] = suggestionId[idx]
                                await pSocks[id[idx]].send(json.dumps(output))
                    elif (id is not None and id in pSocks):
                        output['suggestion'] = suggestion
                        if preReq is not None:
                            output['requiresPreceding'] = preReq
                        output['success'] = True
                        output['id'] = suggestionId
                        await pSocks[id].send(json.dumps(output))
            elif data.command == 'requestEncouragement':
                # We request encouragement from the client side because we want to send the suggestion even if both players are quiet
                thisPair = getPair(data.id1)
                if thisPair and not thisPair.requestedEncouragement:
                    thisPair.requestedEncouragement = True
                    if data.id1 in pSocks and data.id2 in pSocks:
                        if (thisPair.mode != 'bot'):
                            await pSocks[data.id1].send(json.dumps({'suggestion': suggestions.encouragement[0], 'id': 'encouragement0'}))
                            await pSocks[data.id2].send(json.dumps({'suggestion': suggestions.encouragement[1], 'id': 'encouragement1', 'requiresPreceding': suggestions.encouragement[0]}))
                        else:
                            await pSocks[data.id1].send(json.dumps({'suggestion': botSuggestions.encouragement, 'id': 'encouragement'}))

            elif data.command == 'requestFarewell':
                # We request farewell from the client side because we want to send the suggestion even if both players are quiet
                thisPair = getPair(data.id1)
                if thisPair and not thisPair.requestedFarewell:
                    thisPair.requestedFarewell = True
                    if data.id1 in pSocks and data.id2 in pSocks:
                        if (thisPair.mode != 'bot'):
                            await pSocks[data.id1].send(json.dumps({'suggestion': suggestions.farewell[0], 'id': 'farewell0'}))
                            await pSocks[data.id2].send(json.dumps({'suggestion': suggestions.farewell[1], 'id': 'farewell1'}))
                        else:
                            await pSocks[data.id1].send(json.dumps({'suggestion': botSuggestions.farewell, 'id': 'farewell'}))
            elif data.command == 'parseEvalMsg':
                # global participantMsgs
                step, text = detect_intent_texts(
                    DIALOGFLOW_PROJECT_ID, data.sessionId, [data.msg])
                participantMsgs[data.id].append(data.msg)
                await pSocks[data.id].send(json.dumps({'reply': text, 'id': step}))
            elif data.command == 'scoreEvaluation':
                # global participantMsgs
                # print(participantMsgs[data.id])
                score = grade_communication(data.id, data.msgs)
                await pSocks[data.id].send(json.dumps({'score': score}))
            if (not sent):
                await websocket.send(json.dumps(output))
    except websockets.exceptions.ConnectionClosedError:
        print('Connection forcibly closed')
    finally:
        connected.remove(websocket)

async def main():

    if (os.getenv('DEPLOYMENT', default='prod') == 'prod'):
        ssl_context = ssl.SSLContext(ssl.PROTOCOL_TLS_SERVER)
        ssl_context.load_cert_chain(
            f'/etc/letsencrypt/live/{SERVER_DOMAIN}/fullchain.pem', keyfile=f'/etc/letsencrypt/live/{SERVER_DOMAIN}/privkey.pem')
        async with serve(handleInput, "", PORT, ssl=ssl_context):
            print('Running secure websocket server on port {}...'.format(PORT))
            await asyncio.Future()  # run forever
    else:
        async with serve(handleInput, "", PORT):
            print('Running websocket server on port {}...'.format(PORT))
            await asyncio.Future()  # run forever

if __name__ == '__main__':
    try:
        asyncio.run(main())
    except KeyboardInterrupt:

        print('\nBye bye...')
