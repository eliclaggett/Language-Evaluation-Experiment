@Grab('com.squareup.okhttp3:okhttp:3.5.0')
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import okhttp3.Response
import okhttp3.FormBody
import org.json.JSONObject
import java.time.Instant

OkHttpClient client = new OkHttpClient()

startRecaptcha = { player ->
    player.gameStep = "recaptcha"
}

verifyRecaptcha = { player, data ->

    def systemTime = Instant.now().epochSecond * 1000;
    def userTime = data['time'];
    player.utcOffset = systemTime - userTime;

    RequestBody body = new FormBody.Builder()
                .add("secret", Param.RECAPTCHA_SECRET)
                .add("response", data['response'])
                .build()
    Request request = new Request.Builder()
            .url("https://www.google.com/recaptcha/api/siteverify")
            .post(body)
            .build()
    Response response = client.newCall(request).execute()
    def responseString = response.body().string()
    JSONObject obj = new JSONObject(responseString)
    def captcha = obj.get("success")
    if (captcha) {
        player.gameStep = 'tutorial'
        a.addEvent("passedRecaptcha", [
          data: Param.jsonGen.toJson([
                  playerId: player.chatId
                  ])
          ])
    } else {
        player.gameStep = "failedCaptcha"
        Param.numPlayers--
        player.qualified = false
        player.finished = true
        a.addEvent("failedRecaptcha", [
          data: Param.jsonGen.toJson([
                  playerId: player.chatId,
                  reason: "recaptcha"
                  ])
          ])
    }
}
