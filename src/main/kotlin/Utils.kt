import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import org.bouncycastle.asn1.pkcs.PrivateKeyInfo
import org.bouncycastle.openssl.PEMKeyPair
import org.bouncycastle.openssl.PEMParser
import org.bouncycastle.openssl.jcajce.JcaPEMKeyConverter
import org.bouncycastle.openssl.jcajce.JcaPEMWriter
import org.bouncycastle.util.io.pem.PemObject
import org.bouncycastle.util.io.pem.PemObjectGenerator
import java.io.*
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import java.nio.file.Files
import java.nio.file.Paths
import java.security.interfaces.RSAKey
import java.util.Date
import kotlin.system.exitProcess

object Utils {
    private val client = HttpClient.newHttpClient()

    fun convertKey(privateKey: String): String {
        try {
            val pemParser = PEMParser(StringReader(privateKey.replace("\\n", "\n")))
            val pkInfo = pemParser.readObject() as PrivateKeyInfo
            pemParser.close()

            val pkcs1ASN1Encodable = pkInfo.parsePrivateKey()
            val privateKeyPkcs1ASN1 = pkcs1ASN1Encodable.toASN1Primitive()

            val stringWriter = StringWriter()
            val jcaPEMWriter = JcaPEMWriter(stringWriter)
            jcaPEMWriter.writeObject(
                PemObject("RSA PRIVATE KEY", privateKeyPkcs1ASN1.encoded)
                        as PemObjectGenerator
            )
            jcaPEMWriter.close()
            return stringWriter.toString()
        } catch (e: IllegalArgumentException) {
            e.printStackTrace()
            exitProcess(1)
        }
    }

    fun parseJson(json: String): JsonObject {
        try {
            val jsonElement = JsonParser.parseString(json)
            return jsonElement.asJsonObject
        } catch (e: Throwable) {
            e.printStackTrace()
            exitProcess(1)
        }
    }

    fun validateJson(json: JsonObject) : Boolean {
        return !(json[Constants.PRIVATE_KEY] == null || json[Constants.TOKEN_URI] == null
                || json[Constants.CLIENT_EMAIL] == null)
    }

    fun readFile(path: String): String {
        try {
            val data = Files.readAllBytes(Paths.get(path))
            return String(data)
        } catch (e: Throwable) {
            e.printStackTrace()
            exitProcess(1)
        }
    }

    fun createJWT(issuer: String, audience: String, privateKey: String, scope: String): String {
        try {
            val rsaKey = convertToRSAKey(privateKey)
            val algorithm = Algorithm.RSA256(rsaKey)
            return JWT.create()
                .withClaim("iss", issuer)
                .withClaim("scope", scope)
                .withClaim("aud", audience)
                .withIssuedAt(Date())
                .withExpiresAt(Date(System.currentTimeMillis() + 3600000))
                .sign(algorithm)
        } catch (e: Throwable) {
            e.printStackTrace()
            exitProcess(1)
        }
    }

    private fun convertToRSAKey(privateKey: String): RSAKey {
        try {
            val pemParser = PEMParser(StringReader(privateKey))
            val converter = JcaPEMKeyConverter()
            val parsedObject = pemParser.readObject()
            val kp = converter.getKeyPair(parsedObject as PEMKeyPair)
            return kp.private as RSAKey
        } catch (e: Throwable) {
            e.printStackTrace()
            exitProcess(1)
        }
    }

    fun getAccessToken(jwt: String): String {
        try {
            val body = "{\"grant_type\":\"urn:ietf:params:oauth:grant-type:jwt-bearer\",\"assertion\":\"${jwt}\"}"
            val request = HttpRequest.newBuilder()
                .uri(URI.create(Constants.GOOGLE_OAUTH_URL))
                .POST(HttpRequest.BodyPublishers.ofString(body))
                .build()
            val response = client.send(request, HttpResponse.BodyHandlers.ofString())
            if(response.statusCode() != 200){
                println(response)
                println(response.body())
                exitProcess(0)
            }
            return response.body()
        } catch (e: Throwable) {
            e.printStackTrace()
            exitProcess(1)
        }
    }
}