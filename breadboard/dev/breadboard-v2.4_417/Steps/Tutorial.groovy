startTutorial = { player ->
  a.addEvent('startedTutorial', [
    data: Param.jsonGen.toJson([
            playerId: player.chatId
          ])
    ])
  player.gameStep = 'tutorial'
}
