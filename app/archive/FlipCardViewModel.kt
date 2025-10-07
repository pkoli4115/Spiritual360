package com.hindu.pooja.archive

class FlipCardViewModel(app: Application) : AndroidViewModel(app) {

    private val repo = KandaRepository(app.applicationContext)
    private val _title = MutableStateFlow("")
    val title: StateFlow<String> = _title

    private val _lessons = MutableStateFlow<List<Lesson>>(emptyList())
    val lessons: StateFlow<List<Lesson>> = _lessons

    private val _currentLesson = MutableStateFlow(0) // index
    val currentLesson: StateFlow<Int> = _currentLesson

    private val _currentCard = MutableStateFlow(0)   // paragraph card index
    val currentCard: StateFlow<Int> = _currentCard

    // Use your existing TTS helper/sanitizer
    private val tts by lazy { TtsHelper(app.applicationContext) }
    private val sanitizer = TtsSanitizer()

    fun load(kanda: KandaJsonLoader.Kanda) {
        viewModelScope.launch {
            _lessons.value = repo.getLessons(kanda)
            _title.value = repo.getTitle(kanda)
            _currentLesson.value = 0
            _currentCard.value = 0
        }
    }

    fun nextCard(cardsInLesson: Int) {
        if (_currentCard.value + 1 < cardsInLesson) {
            _currentCard.value += 1
        }
    }

    fun prevCard() {
        if (_currentCard.value > 0) {
            _currentCard.value -= 1
        }
    }

    fun nextLesson() {
        val idx = _currentLesson.value
        val max = _lessons.value.lastIndex
        if (idx < max) {
            _currentLesson.value = idx + 1
            _currentCard.value = 0
        }
    }

    fun prevLesson() {
        val idx = _currentLesson.value
        if (idx > 0) {
            _currentLesson.value = idx - 1
            _currentCard.value = 0
        }
    }

    fun speak(text: String) {
        val clean = sanitizer.sanitize(text)
        tts.speak(clean, lang = "te")
    }

    fun stopTts() {
        tts.stop()
    }

    override fun onCleared() {
        super.onCleared()
        tts.shutdown()
    }
}