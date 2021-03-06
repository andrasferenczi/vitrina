package stoyck.vitrina.di.app

import android.content.Context
import android.net.Uri
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import dagger.Module
import dagger.Provides
import stoyck.vitrina.R
import stoyck.vitrina.VitrinaApplication
import stoyck.vitrina.util.gson.UriDeserializer
import stoyck.vitrina.util.gson.UriSerializer
import javax.inject.Named
import javax.inject.Singleton

/**
 * Not actually needed, just in case
 */
@Module
class ApplicationModule(
    private val app: VitrinaApplication
) {

    @Provides
    @Singleton
    fun provideApp(): VitrinaApplication = app

    @Provides
    @Singleton
    fun provideContext(): Context = app

    @Provides
    @Named("reddit_client_id")
    fun redditClientId(context: Context): String =
        context.getString(R.string.reddit_client_id)

    @Provides
    @Named("package_name")
    fun packageName(context: Context): String = context.packageName

    @Provides
    fun gson(): Gson {
        return GsonBuilder()
            .registerTypeAdapter(Uri::class.java, UriSerializer())
            .registerTypeAdapter(Uri::class.java, UriDeserializer())
            .create()
    }

}