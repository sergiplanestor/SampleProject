package revolhope.splanes.com.presentation.common.base

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import revolhope.splanes.com.domain.library.ResponseState
import kotlin.coroutines.CoroutineContext

abstract class BaseViewModel : ViewModel(), CoroutineScope {

    val loaderState: LiveData<Boolean> get() = _loaderState
    private val _loaderState = MutableLiveData<Boolean>()

    val onErrorState: LiveData<Boolean> get() = _onErrorState
    private val _onErrorState = MutableLiveData<Boolean>()

    private val job = Job()
    /**
     * The context of this scope.
     * Context is encapsulated by the scope and used for implementation of coroutine builders that are extensions on the scope.
     * Accessing this property in general code is not recommended for any purposes except accessing the [Job] instance for advanced usages.
     *
     * By convention, should contain an instance of a [job][Job] to enforce structured concurrency.
     */
    override val coroutineContext: CoroutineContext
        get() = job + Dispatchers.Main

    /**
     * Launch given action in background ([Dispatchers.IO]).
     * Also notify loader states.
     *
     * @param action [suspend] action to perform in background.
     */
    fun <T> launchAsync(
        action: suspend () -> ResponseState<T>,
        onSuccess: (T) -> Unit,
        isPostError: Boolean = true
    ) {
        launch {
            withContext(Dispatchers.IO) {
                _loaderState.postValue(true)
                val res = action.invoke()
                _loaderState.postValue(false)
                res
            }.also { handleResponseState(it, isPostError)?.let(onSuccess) }
        }
    }

    /**
     * Manage response state (@see [ResponseState]) and unwrap it. Generic parameter [T] represent
     * [ResponseState] wrapped object.
     * @param state [ResponseState] object to be manage.
     * @param isPostError [Boolean] indicating if in case of error, this should be notified or
     * should be ignored.
     *
     * @return Nullable [T] object.
     */
    private fun <T> handleResponseState(state: ResponseState<T>, isPostError: Boolean = true) : T? {
        return when (state) {
            is ResponseState.Success -> state.data
            is ResponseState.Error -> {
                if (isPostError) _onErrorState.postValue(true)
                null
            }
        }
    }
}