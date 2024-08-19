package com.example.unscramble.ui

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.example.unscramble.data.GameUiState
import com.example.unscramble.data.MAX_NO_OF_WORDS
import com.example.unscramble.data.SCORE_INCREASE
import com.example.unscramble.data.allWords
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class GameViewModel: ViewModel() {

    // Game UI state
// Backing property to avoid state updates from other classes
    private val _uiState = MutableStateFlow(GameUiState())
    val uiState: StateFlow<GameUiState> = _uiState.asStateFlow()


    //set words used in game
    private var usedWords: MutableSet<String> = mutableSetOf()
    private lateinit var currentWord: String
    private fun pickRandomWordAndShuffle(): String{
        // Continue picking up a new random word until you get one that hasn't been used before
        currentWord = allWords.random()
        if(usedWords.contains(currentWord)){
            return pickRandomWordAndShuffle()
        }
        else {
            usedWords.add(currentWord)
            return shuffleCurrentWord(currentWord)
        }
    }



    private fun shuffleCurrentWord(word: String): String{
        val tempWord = word.toCharArray()
        //scramble word
        tempWord.shuffle()
        while (String(tempWord).equals(word)){  //chance of, shuffle word= original word, to avoid that while is used
            tempWord.shuffle()
        }
        return (String(tempWord))
    }

    fun resetGame(){
        usedWords.clear()
        _uiState.value = GameUiState(currentScrambleWord = pickRandomWordAndShuffle())
    }

    init {
        resetGame()
    }

    var userGuess by mutableStateOf("")
    fun updateUserGuess(guessWord: String){
        userGuess = guessWord
    }

    fun checkUserGuess(){
        if(userGuess.equals(currentWord, ignoreCase = true)){
        //user guess is correct,increaseScore
            //call updateGameState for next round
            val updateScore = _uiState.value.score.plus(SCORE_INCREASE)
            updateGameState(updateScore)
        }
        else{
            //user guess word is wrong
            _uiState.update {currentState ->
                currentState.copy(isGuessedWordWrong = true)
            }
        }
        //reset user guess
        updateUserGuess("")
    }
    private fun updateGameState(updateScore: Int) {
        if (usedWords.size == MAX_NO_OF_WORDS) {
            _uiState.update { currentState ->
                currentState.copy(
                    isGuessedWordWrong = false,
                    score = updateScore,
                    isGameOver = true
                )
            }
        } else {
            _uiState.update { currentState ->
                currentState.copy(
                    isGuessedWordWrong = false,
                    currentScrambleWord = pickRandomWordAndShuffle(),
                    score = updateScore,
                    currentWordCount = currentState.currentWordCount.inc()
                )
            }
        }
    }

    fun skipWord(){
        updateGameState(_uiState.value.score)
        updateUserGuess("")
    }
}
