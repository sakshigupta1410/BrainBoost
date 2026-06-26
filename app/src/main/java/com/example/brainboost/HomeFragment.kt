package com.example.brainboost

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.fragment.app.Fragment

class HomeFragment : Fragment() {

    private lateinit var cardMemoryFlip: View
    private lateinit var cardSpeedTap: View
    private lateinit var cardMathQuiz: View
    private lateinit var cardWordScramble: View
    private lateinit var cardTicTacToe: View
    private lateinit var cardCodeQuiz: LinearLayout
    private lateinit var cardPuzzleGame: LinearLayout
    private lateinit var cardWordSearch: LinearLayout
    private lateinit var card2048: LinearLayout
    private lateinit var cardMissingNumber: LinearLayout
    private lateinit var cardSudoku: LinearLayout
    private lateinit var cardMazeGame: View
    private lateinit var cardChess: LinearLayout
    private lateinit var cardScienceDetective: View
    private lateinit var cardMindMolecule: View
    private lateinit var cardFoodChain: View

    @SuppressLint("MissingInflatedId")
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        val view = inflater.inflate(R.layout.fragment_home, container, false)

        // Initialize cards
        cardMemoryFlip = view.findViewById(R.id.cardMemoryFlip)
        cardSpeedTap = view.findViewById(R.id.cardSpeedTap)
        cardMathQuiz = view.findViewById(R.id.cardMathQuiz)
        cardWordScramble = view.findViewById(R.id.cardWordScramble)
        cardTicTacToe = view.findViewById(R.id.cardTicTacToe)
        cardCodeQuiz = view.findViewById(R.id.cardCodeQuiz)
        cardPuzzleGame = view.findViewById(R.id.cardPuzzleGame)
        cardWordSearch = view.findViewById(R.id.cardWordSearch)
        card2048 = view.findViewById(R.id.card2048)
        cardMissingNumber = view.findViewById(R.id.cardMissingNumber)
        cardSudoku = view.findViewById(R.id.cardSudoku)
        cardMazeGame = view.findViewById(R.id.cardMazeGame)
        cardChess = view.findViewById(R.id.cardChess)
        cardScienceDetective = view.findViewById(R.id.cardScienceDetective)
        cardMindMolecule = view.findViewById(R.id.cardMindMolecule)
        cardFoodChain = view.findViewById(R.id.cardFoodChain)

        setupListeners()

        return view
    }

    private fun setupListeners() {

        cardMemoryFlip.setOnClickListener {
            animateCard(it)
            startActivity(Intent(requireContext(), MemoryCardFlipActivity::class.java))
        }



        cardMathQuiz.setOnClickListener {
            animateCard(it)
            startActivity(Intent(requireContext(), MathBrainQuizActivity::class.java))
        }

        cardSpeedTap.setOnClickListener {
            animateCard(it)
            startActivity(Intent(requireContext(), SpeedTapActivity::class.java))
        }

        cardWordScramble.setOnClickListener {
            animateCard(it)
            startActivity(Intent(requireContext(), WordScrambleActivity::class.java))
        }

        cardTicTacToe.setOnClickListener {
            animateCard(it)
            startActivity(Intent(requireContext(), TicTacToeActivity::class.java))
        }

        cardCodeQuiz.setOnClickListener {
            animateCard(it)
            startActivity(Intent(requireContext(), CodeQuizActivity::class.java))
        }

        cardPuzzleGame.setOnClickListener {
            animateCard(it)
            startActivity(Intent(requireContext(), PuzzleMenuActivity::class.java))
        }

        cardWordSearch.setOnClickListener {
            animateCard(it)
            startActivity(Intent(requireContext(), WordSearchMenuActivity::class.java))
        }

        card2048.setOnClickListener {
            animateCard(it)
            startActivity(Intent(requireContext(), Game2048Activity::class.java))
        }

        cardMazeGame.setOnClickListener {
            animateCard(it)
            startActivity(Intent(requireContext(), MazeGameActivity::class.java))
        }

        cardChess.setOnClickListener {
            animateCard(it)
            startActivity(Intent(requireContext(), ChessActivity::class.java))
        }

        cardMissingNumber.setOnClickListener {
            animateCard(it)
            startActivity(Intent(requireContext(), MissingNumberActivity::class.java))
        }

        cardSudoku.setOnClickListener {
            animateCard(it)
            startActivity(Intent(requireContext(), SudokuActivity::class.java))
        }

        cardScienceDetective.setOnClickListener {
            animateCard(it)
            startActivity(Intent(requireContext(), ScienceDetectiveActivity::class.java))
        }

        cardMindMolecule.setOnClickListener {
            animateCard(it)
            startActivity(Intent(requireContext(), MindOfMoleculeActivity::class.java))
        }

        cardFoodChain.setOnClickListener {
            animateCard(it)
            startActivity(Intent(requireContext(), FoodChainActivity::class.java))
        }
    }

    // Smooth click animation
    private fun animateCard(view: View) {
        view.animate()
            .scaleX(0.92f)
            .scaleY(0.92f)
            .setDuration(120)
            .withEndAction {
                view.animate()
                    .scaleX(1f)
                    .scaleY(1f)
                    .setDuration(120)
                    .start()
            }
            .start()
    }
}
