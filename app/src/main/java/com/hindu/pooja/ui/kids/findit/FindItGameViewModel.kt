package com.hindu.pooja.ui.kids.findit

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hindu.pooja.ui.kids.findit.model.HiddenObject
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class FindItGameViewModel : ViewModel() {

    private val _timeRemaining = MutableStateFlow(0)
    val timeRemaining: StateFlow<Int> = _timeRemaining

    private val _foundObjects = MutableStateFlow<List<String>>(emptyList())
    val foundObjects: StateFlow<List<String>> = _foundObjects

    private val _currentXp = MutableStateFlow(0)
    val currentXp: StateFlow<Int> = _currentXp
    val levelTargetXp = 100 // You can set dynamically per level if needed

    fun startGame(totalSeconds: Int) {
        viewModelScope.launch {
            for (i in totalSeconds downTo 0) {
                _timeRemaining.value = i
                delay(1000)
            }
            // Handle timeout if needed
        }
    }

    fun isObjectFound(name: String): Boolean {
        return _foundObjects.value.contains(name)
    }

    fun markObjectFound(name: String) {
        if (!isObjectFound(name)) {
            _foundObjects.value = _foundObjects.value + name
            _currentXp.value += 10
        }
    }

    fun allObjectsFound(objects: List<HiddenObject>): Boolean {
        return objects.all { isObjectFound(it.name) }
    }

    fun finishGame(levelName: String) {
        // You can save to Firebase/local or update leaderboard
    }
}
