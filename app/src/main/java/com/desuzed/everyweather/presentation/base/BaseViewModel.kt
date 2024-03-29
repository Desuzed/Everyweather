package com.desuzed.everyweather.presentation.base

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

abstract class BaseViewModel<S, A, I : UserInteraction>(initState: S) : ViewModel() {
    private val _state = MutableStateFlow(initState)
    val state: StateFlow<S> = _state.asStateFlow()

    private val _action = MutableSharedFlow<A>()
    val action: SharedFlow<A> = _action.asSharedFlow()

    protected fun setState(reducer: S.() -> S) {
        _state.value = reducer(_state.value)
    }

    protected fun setAction(action: A) {
        viewModelScope.launch {
            _action.emit(action)
        }
    }

    protected fun launch(block: suspend CoroutineScope.() -> Unit) {
        viewModelScope.launch {
            block()
        }
    }

    open fun onUserInteraction(interaction: I) {}

    inline fun <T> collect(source: Flow<T>, crossinline consumer: suspend (T) -> Unit) {
        viewModelScope.launch {
            source.collect {
                consumer(it)
            }
        }
    }
}