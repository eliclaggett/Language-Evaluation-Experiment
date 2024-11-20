#
# Filename: SLOR.py
# Author: Elijah Claggett
#
# Description:
# This file defines helper functions for calculating the SLOR score of text as defined in:
# https://arxiv.org/abs/1809.08731
#

# Imports
import math
from flair.embeddings import FlairEmbeddings

# get language model
language_model = FlairEmbeddings('news-forward', has_decoder=True).lm

def calc_perplexity(sentence):
    if len(sentence) == 1:
        sentence_perplexity = language_model.calculate_perplexity(
            sentence + ' ')
    else:
        sentence_perplexity = language_model.calculate_perplexity(sentence)
    return sentence_perplexity

def calc_token_perplexities(token_lst):
    total_token_counter = 0
    token_perplexity_sum = 0
    for token in token_lst:
        if len(token) == 1:
            token_len = 2
            token_perplexity = calc_perplexity(token + ' ')
        else:
            token_len = len(token)
            token_perplexity = calc_perplexity(token)
        total_token_counter += token_len
        token_perplexity_sum += token_len*math.log(token_perplexity)
    return token_perplexity_sum, total_token_counter

# Function to calculate average perplexity and SLOR for a given string
# Note: it also calculates the individual SLOR values per generation and returns a list of them
def calc_acceptability_metrics(sentence):
    """

      :param list lines: first input
      :returns: 
          - total_perplexity - first output
          - total_SLOR - second output
    """
    total_perplexity = 0
    total_SLOR = 0
    SLOR_lst = []

    if (sentence.upper() == sentence):
        sentence = sentence[0] + sentence[1:].lower()

    lines_clean = [sentence.strip().split('\t')]

    for line in lines_clean:
        if len(line[0]) == 0 or len(line[0].split()) == 0:
            print("Error input line: ", line[0])
        else:
            perplexity = calc_perplexity(line[0])
            total_perplexity += perplexity

            tokens = line[0].split()
            tokens_perplexity, tokens_len = calc_token_perplexities(tokens)
            SLOR_line = -math.log(perplexity) + tokens_perplexity / tokens_len
            total_SLOR += SLOR_line

    return total_perplexity, total_SLOR
