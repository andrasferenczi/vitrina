package stoyck.vitrina.muzei.ext

import android.content.ContentProviderOperation
import android.content.Context
import android.content.OperationApplicationException
import android.os.RemoteException
import android.util.Log
import com.google.android.apps.muzei.api.provider.Artwork
import com.google.android.apps.muzei.api.provider.ProviderClient
import com.google.android.apps.muzei.api.provider.ProviderContract
import stoyck.vitrina.BuildConfig

fun Context.retrieveVitrinaProviderClient(): ProviderClient {
    return ProviderContract
        .getProviderClient(
            applicationContext,
            BuildConfig.VITRINA_AUTHORITY
        )
}

fun ProviderClient.readArtworks(context: Context): List<Artwork> {
    return context.contentResolver
        .query(contentUri, null, null, null, null)
        .use { cursor ->

            if (cursor == null) {
                return@use emptyList<Artwork>()
            }

            val result = mutableListOf<Artwork>()

            cursor.moveToFirst()

            while (!cursor.isAfterLast) {
                val artwork = Artwork.fromCursor(cursor)
                result.add(artwork)
                cursor.moveToNext()
            }

            return@use result
        }
}

fun ProviderClient.removeArtworks(context: Context, vararg artworks: Artwork) {
    return removeArtworksById(context, *artworks.map { it.id }.toLongArray())
}

fun ProviderClient.removeArtworksById(context: Context, vararg artworks: Long) {
    val contentResolver = context.contentResolver

    val operations = artworks
        .map { artwork ->
            ContentProviderOperation.newDelete(contentUri)
                .withSelection(
                    "${ProviderContract.Artwork._ID} = ?",
                    arrayOf(artwork.toString())
                ).build()
        }


    try {
        val results =
            contentResolver.applyBatch(BuildConfig.VITRINA_AUTHORITY, ArrayList(operations))

        if (BuildConfig.DEBUG) {
            results.forEach {
                Log.v("VitrinaProviderClient", it.toString())
            }
        }
    } catch (ignored: OperationApplicationException) {
    } catch (ignored: RemoteException) {
    }
}

/**
 * Why this is wrong:
 *
 * https://github.com/romannurik/muzei/issues/689#issuecomment-687668168
 */
@Deprecated("Do not remove old artworks automatically")
fun ProviderClient.pruneOldArtworks(context: Context, maxCount: Int) {
    val artworks = readArtworks(context).sortedByDescending { it.dateAdded }

    val artworkCountToRemove = artworks.size - maxCount

    if (artworkCountToRemove <= 0) {
        return
    }

    val idsToRemove = artworks.takeLast(artworkCountToRemove)
    removeArtworks(context, *idsToRemove.toTypedArray())
}