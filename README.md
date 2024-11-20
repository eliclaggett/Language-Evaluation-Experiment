<h1 align="center">
  <br>
  <img src="https://raw.githubusercontent.com/eliclaggett/Language-Evaluation-Experiment/refs/heads/main/assets/lang-eval-1.svg" width="300"></a>
  <br>
  Algorithmic Pairing for Better Conversations
</h1>

<h4 align="center">A large-scale experiment evaluating human prosociality.</h4>

<p align="center">
  <a href="#research-paper">Research Paper</a> •
  <a href="#how-to-use">How to Use</a> •
  <a href="#download">Download</a> •
  <a href="#authors">Authors</a> •
  <a href="#license">License</a>
</p>

<!-- ![screenshot]() -->

## Research Paper

> **Note:**
> This repository is the companion code to our research paper appearing in CSCW'25. We do not recommend deploying this code for general use.

Guided by the conversational grounding theory, we analyzed the linguistic features of 123 interpersonal dyadic discussions (2,809 messages) using this online chat system and developed a classifier to identify individuals who use the communication style that promotes trust development. We then conducted a randomized controlled experiment with 530 human subjects in 265 pairs to measure the effect of assigning discussion partners based on the classifier’s assessment of participants' prior interactions with a chatbot. **Our results show that algorithmically assigned pairs exhibit significantly higher trust in their conversation partners than random pairs, irrespective of opinion similarity.**

For more details, please see our full paper:

**Claggett, E.**, Shirado, H. "Making Pairs That Cooperate: AI Evaluation of Trust in Human Conversations" _Proceedings of the 2025 ACM Conference on Computer Supported Cooperative Work and Social Computing._

## How to Use

This experiment was deployed using Yale's [Breadboard](https://breadboard.yale.edu) platform for studying human social behavior online. Installing this software is a prerequisite to running this code.

### Installation Steps

1. Install Python 3, Pyenv, and Pyenv-Virtualenv
2. Install Breadboard
3. (optional) Install NGINX to run this experiment on Prolific

Finally, clone this repository and run the `start.sh` script to get started.

## Download

There is no single executable for this web application, so please follow installation steps above and have fun!

## Authors

<p align="center">
  <span>
    <img src="/img1.png" width="100" />
    <strong>Eli Claggett</strong>
  </span>
</p>

## License

MIT