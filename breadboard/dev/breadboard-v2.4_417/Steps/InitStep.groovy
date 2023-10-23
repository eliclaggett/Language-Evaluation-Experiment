/*
 * Filename: InitStep.groovy
 * Author: Elijah Claggett
 * Date: October 19, 2023
 *
 * Description:
 * This script is required for Breadboard. It executes when the waiting period is over for the experiment and the main task should begin.
 */

initStep = stepFactory.createStep('InitStep')

initStep.run = {
  println 'initStep.run'
  
  // Cancel initStep timer
  if (Param.initStepTask) {
    Param.initStepTask.cancel()
  }

  // Cancel all player timers (showing the waiting room time remaining)
  if (!Param.playerTimers.isEmpty()) {
    Param.playerTimers.each { key, timer -> timer.cancel() }
  }

  // Show the ready button
  g.V.each { v ->
    v.showReady = true
  }

  // Give participants 45 seconds to press ready
  def timer = new Timer()
  Param.initTask = timer.runAfter(45 * 1000, { // 45 seconds
    calculateGroups()
  })
}

initStep.done = {
  println 'initStep.done'
}
