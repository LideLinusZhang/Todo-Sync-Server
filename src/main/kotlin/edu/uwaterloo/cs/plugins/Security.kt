package edu.uwaterloo.cs.plugins

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import edu.uwaterloo.cs.data.DataFactory
import edu.uwaterloo.cs.data.User
import edu.uwaterloo.cs.data.Users
import io.ktor.client.*
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.not

fun Application.configureSecurity(httpClient: HttpClient) {
    install(Authentication) {
        digest("auth-digest") {
            realm = edu.uwaterloo.cs.todo.lib.realm
            digestProvider { userName, _ ->
                DataFactory.transaction {
                    User.find { Users.name eq userName and not(Users.byOAuth) }.firstOrNull()?.hashedPassword
                }
            }
        }
        oauth("auth-oauth-google") {
            urlProvider = { "http://localhost:8080/user/callback" }
            providerLookup = {
                OAuthServerSettings.OAuth2ServerSettings(
                    name = "google",
                    authorizeUrl = "https://accounts.google.com/o/oauth2/v2/auth",
                    accessTokenUrl = "https://oauth2.googleapis.com/token",
                    requestMethod = HttpMethod.Post,
                    clientId = System.getenv("GOOGLE_CLIENT_ID"),
                    clientSecret = System.getenv("GOOGLE_CLIENT_SECRET"),
                    defaultScopes = listOf("https://www.googleapis.com/auth/userinfo.profile"),
                    extraAuthParameters = listOf("access_type" to "offline")
                )
            }
            client = httpClient
        }
        jwt("auth-jwt") {
            realm = edu.uwaterloo.cs.todo.lib.realm
            verifier(JWT.require(Algorithm.HMAC256(System.getenv("JWT_SECRET"))).build())
            validate { credential ->
                if (!credential.payload.getClaim("username").asString().isNullOrEmpty()) {
                    JWTPrincipal(credential.payload)
                } else {
                    null
                }
            }
        }
    }
}
