package stoyck.vitrina.network

import android.util.Base64
import androidx.annotation.VisibleForTesting
import stoyck.vitrina.network.data.RedditAbout
import stoyck.vitrina.network.data.RedditAuthorizationResponse
import stoyck.vitrina.network.data.RedditPost
import stoyck.vitrina.ui.suggestion.SubredditSuggestionData
import javax.inject.Inject
import javax.inject.Named
import javax.inject.Singleton

@Singleton
class RedditService @Inject constructor(
    private val publicApi: RedditPublicApi,
    private val oauthApi: RedditOauthApi,
    private val bearerBearerTokenWrapper: BearerTokenWrapper,
    @Named("reddit_client_id")
    private val clientId: String
) {
    private val usernameAndPassword = "$clientId:"

    private val authorization: String =
        "Basic " + Base64.encodeToString(
            usernameAndPassword.toByteArray(),
            Base64.DEFAULT
        ).trim() // remove the goddamn new line at the end


    @VisibleForTesting
    suspend fun retrieveAccessToken(): RedditAuthorizationResponse {
        return publicApi.getAccessToken(authorization = authorization)
    }

    private suspend fun ensureBearerTokenExists() {
        if (bearerBearerTokenWrapper.isTokenValid) {
            return
        }

        val token = retrieveAccessToken()
        bearerBearerTokenWrapper.authorization = token
    }

    suspend fun retrieveSubreddit(subreddit: String): RedditAbout {
        ensureBearerTokenExists()
        val page = oauthApi.getAbout(subreddit = subreddit)
        return page.data
    }

    suspend fun retrievePosts(subreddits: List<String>): List<RedditPost> {
        return retrievePosts(subreddits.joinToString(separator = "+"))
    }

    suspend fun retrievePosts(subreddit: String): List<RedditPost> {
        ensureBearerTokenExists()
        val result = oauthApi.getPosts(
            subreddit = subreddit,
            limit = 100
        )

        return result.data.children.map { it.data }
    }

    suspend fun retrieveHints(
        query: String,
        includeOver18: Boolean = false
    ): List<SubredditSuggestionData> {
        ensureBearerTokenExists()
        val result = oauthApi.searchSubreddits(
            query = query,
            includeOver18 = includeOver18
        )
        return result.subreddits.map {
            SubredditSuggestionData(
                name = it.name,
                subscriberCount = it.subscriberCount
            )
        }
    }

}
