package com.github.devlotfi.cipherbox.openapi.apis

import com.github.devlotfi.cipherbox.openapi.infrastructure.CollectionFormats.*
import retrofit2.http.*
import retrofit2.Response
import okhttp3.RequestBody
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

import com.github.devlotfi.cipherbox.openapi.models.ErrorResponse
import com.github.devlotfi.cipherbox.openapi.models.Todo

interface TodosApi {
    /**
     * GET todos/{id}
     * Get a todo by id
     * Returns a single todo, or an error if the id is invalid or unknown.
     * Responses:
     *  - 200: The requested todo
     *  - 400: The id is not an integer
     *  - 404: No todo with this id
     *
     * @param id Todo id (integer)
     * @return [Todo]
     */
    @GET("todos/{id}")
    suspend fun getTodo(@Path("id") id: kotlin.String): Response<Todo>

    /**
     * GET todos
     * List all todos
     * Returns every todo currently stored in memory.
     * Responses:
     *  - 200: All todos
     *
     * @return [kotlin.collections.List<Todo>]
     */
    @GET("todos")
    suspend fun listTodos(): Response<kotlin.collections.List<Todo>>

}
