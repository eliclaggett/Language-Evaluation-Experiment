initStep = stepFactory.createStep('InitStep')

initStep.run = {
  println 'initStep.run'
  
  if (Param.initStepTask) {
    Param.initStepTask.cancel()
  }

  if (!Param.playerTimers.isEmpty()) {
    Param.playerTimers.each { key, timer -> timer.cancel() }
  }

  g.V.each { v ->
    v.showReady = true
  }

  def timer = new Timer()
  //Param.initTask = timer.runAfter(45 * 1000, { // 45 seconds
    //calculateGroups()
  //})

// if (Param.numGroupingFinished == numPlayers && numPlayers >= numDesiredPlayers) {
//   // All players finished grouping and we have the desired number of players
// }
// calculateGroups();
}

initStep.done = {
  println 'initStep.done'
}
