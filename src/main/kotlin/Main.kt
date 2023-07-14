import kotlin.system.exitProcess

fun main(args: Array<String>) {
    if (args.isEmpty() || args.size > 1) {
        println("Wrong JSON file path")
        exitProcess(0)
    }
    val fileString = Utils.readFile(args.first())
    val json = Utils.parseJson(fileString)

    if (!Utils.validateJson(json)) {
        println("JSON doesn't contain all required keys: private_key, client_email, token_uri")
        exitProcess(0)
    }
    val privateKey = json[Constants.PRIVATE_KEY].toString().replace("\"", "")
    val issuer = json[Constants.CLIENT_EMAIL].toString().replace("\"", "")
    val audience = json[Constants.TOKEN_URI].toString().replace("\"", "")

    // converting private key from PKCS#8 to PKCS#1
    val convertedKey = Utils.convertKey(privateKey)

    val jwt = Utils.createJWT(
        issuer = issuer,
        audience = audience,
        scope = Constants.FIREBASE_OAUTH_URL,
        privateKey = convertedKey
    )

    val accessToken = Utils.getAccessToken(jwt)
    val parsed = Utils.parseJson(accessToken)
    if (parsed["access_token"] != null) {
        println(parsed["access_token"].toString().replace("\"", ""))
    } else {
        println(parsed)
    }
}
