/*
 * Filename: OnLeaveStep.groovy
 * Author: Elijah Claggett
 * Date: October 19, 2023
 *
 * Description:
 * This script is required for Breadboard. It executes when an experiment participant exits the session.
 */
onLeaveStep = stepFactory.createNoUserActionStep()

onLeaveStep.run = {
  println 'onLeaveStep.run'
}
onLeaveStep.done = {
  println 'onLeaveStep.done'
}
