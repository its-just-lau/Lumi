package com.lumi.app.core.storage.db

import com.google.gson.Gson
import kotlinx.coroutines.CancellationException
import java.io.IOException

/**
 * Caché local (Room) de las listas que se consultan a la API.
 *
 * Funciona en modo "primero la red": si la consulta funciona, se guarda una copia; si falla
 * por falta de conexión, se devuelve la última copia guardada para que la app siga siendo útil.
 * Los errores del servidor (401, 404, 422...) nunca se tapan con la caché.
 */
class CacheStore(
    private val dao: CacheDao,
    private val gson: Gson = Gson()
) {

    suspend fun <T : Any> cached(
        collection: String,
        type: Class<T>,
        idOf: (T) -> String,
        fetch: suspend () -> Result<List<T>>
    ): Result<List<T>> {
        val result = fetch()

        result.onSuccess { items ->
            try {
                dao.replace(
                    collection,
                    items.map { CacheItem(collection, idOf(it), gson.toJson(it)) }
                )
            } catch (exception: CancellationException) {
                throw exception
            } catch (exception: Exception) {
                // Si no se puede guardar la copia, la app sigue con los datos de la red.
            }
        }

        val error = result.exceptionOrNull()

        if (error is IOException) {
            val saved = load(collection, type)
            if (saved.isNotEmpty()) return Result.success(saved)
        }

        return result
    }

    private suspend fun <T : Any> load(collection: String, type: Class<T>): List<T> {
        return try {
            dao.getJson(collection).mapNotNull { json ->
                try {
                    gson.fromJson(json, type)
                } catch (exception: Exception) {
                    null
                }
            }
        } catch (exception: CancellationException) {
            throw exception
        } catch (exception: Exception) {
            emptyList()
        }
    }

    /** Borra todo lo guardado (al cerrar sesión o iniciar con otra cuenta). */
    suspend fun clearAll() {
        try {
            dao.clearAll()
        } catch (exception: CancellationException) {
            throw exception
        } catch (exception: Exception) {
            // Nada que hacer: la base se vuelve a llenar en la siguiente consulta.
        }
    }
}
