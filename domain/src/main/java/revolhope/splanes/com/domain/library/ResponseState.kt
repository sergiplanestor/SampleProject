package revolhope.splanes.com.domain.library

sealed class ResponseState<RES> {
    data class Success<RES>(val data: RES) : ResponseState<RES>()
    data class Error<RES>(val error: Exception, var description: String? = null) : ResponseState<RES>()
}