package org.example.app

import android.animation.LayoutTransition
import android.app.Activity
import android.graphics.Color
import android.os.Bundle
import android.text.SpannableStringBuilder
import android.view.View
import android.view.animation.AccelerateDecelerateInterpolator
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView


/**
 * PUBLIC_INTERFACE
 * MainActivity is the entry point for the Simple Calculator.
 * It presents a minimalist calculator UI with a result display, operation buttons, and a numeric keypad.
 * Supports addition and subtraction with basic input validation.
 */
class MainActivity : Activity() {

    // UI elements
    private lateinit var displayText: TextView
    private lateinit var opAdd: Button
    private lateinit var opSub: Button
    private lateinit var equals: Button
    private lateinit var clear: Button
    private lateinit var numButtons: List<Button>

    // Calculator state
    private var currentInput: StringBuilder = StringBuilder()
    private var pendingOperator: Char? = null
    private var accumulator: Long? = null
    private var justEvaluated: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Initialize views
        displayText = findViewById(R.id.displayText)
        opAdd = findViewById(R.id.btnOpAdd)
        opSub = findViewById(R.id.btnOpSub)
        equals = findViewById(R.id.btnEquals)
        clear = findViewById(R.id.btnClear)

        numButtons = listOf(
            findViewById(R.id.btn0),
            findViewById(R.id.btn1),
            findViewById(R.id.btn2),
            findViewById(R.id.btn3),
            findViewById(R.id.btn4),
            findViewById(R.id.btn5),
            findViewById(R.id.btn6),
            findViewById(R.id.btn7),
            findViewById(R.id.btn8),
            findViewById(R.id.btn9)
        )

        // Subtle layout transitions on container level if present
        val root = findViewById<LinearLayout>(R.id.rootContainer)
        root?.layoutTransition = LayoutTransition().apply {
            setDuration(180)
            setInterpolator(LayoutTransition.CHANGING, AccelerateDecelerateInterpolator())
        }

        setUpStyling()
        setUpListeners()
        resetState()
    }

    private fun setUpStyling() {
        // Elevations and rounded feel through stateListAnimator where available
        fun elevate(v: View) {
            v.elevation = 6f
        }
        numButtons.forEach { elevate(it) }
        elevate(opAdd)
        elevate(opSub)
        elevate(equals)
        elevate(clear)
        elevate(displayText)
    }

    private fun setUpListeners() {
        // Number buttons
        numButtons.forEachIndexed { value, button ->
            button.setOnClickListener {
                handleDigit(value)
            }
        }

        // Operators
        opAdd.setOnClickListener { handleOperator('+') }
        opSub.setOnClickListener { handleOperator('-') }

        // Actions
        equals.setOnClickListener { evaluate() }
        clear.setOnClickListener { resetState(animated = true) }
    }

    /**
     * PUBLIC_INTERFACE
     * Resets the calculator state and updates the display.
     */
    private fun resetState(animated: Boolean = false) {
        currentInput.clear()
        pendingOperator = null
        accumulator = null
        justEvaluated = false
        updateDisplay("0", animated)
    }

    private fun updateDisplay(text: String, animated: Boolean = false) {
        if (animated) {
            displayText.alpha = 0.6f
            displayText.animate()
                .alpha(1f)
                .setDuration(150)
                .setInterpolator(AccelerateDecelerateInterpolator())
                .start()
        }
        displayText.text = text
    }

    private fun handleDigit(digit: Int) {
        // Basic input validation and control flow
        if (justEvaluated) {
            // Starting a new entry after equals wipes previous result
            currentInput.clear()
            pendingOperator = null
            accumulator = null
            justEvaluated = false
        }

        // Avoid multiple leading zeros
        if (currentInput.isEmpty() && digit == 0) {
            updateDisplay("0", animated = true)
            currentInput.append('0') // Single leading zero allowed
            return
        }
        // Replace single '0' with actual digit
        if (currentInput.length == 1 && currentInput[0] == '0') {
            currentInput.clear()
        }

        currentInput.append(digit.toString())
        updateDisplay(currentInput.toString(), animated = true)
    }

    private fun parseCurrentInput(): Long? {
        if (currentInput.isEmpty()) return null
        return try {
            currentInput.toString().toLong()
        } catch (_: NumberFormatException) {
            null
        }
    }

    private fun applyPendingOperator(operand: Long) {
        if (accumulator == null) {
            accumulator = operand
        } else {
            val op = pendingOperator
            accumulator = when (op) {
                '+' -> (accumulator ?: 0) + operand
                '-' -> (accumulator ?: 0) - operand
                else -> accumulator
            }
        }
    }

    private fun handleOperator(op: Char) {
        // If user taps operator without number, use accumulator or 0
        val inputVal = parseCurrentInput()
        if (inputVal != null) {
            applyPendingOperator(inputVal)
            currentInput.clear()
            updateDisplay(accumulator.toString(), animated = true)
        } else if (accumulator == null) {
            // No number yet, default to 0 then set operator
            accumulator = 0
            updateDisplay("0", animated = true)
        }
        pendingOperator = op
        justEvaluated = false
        emphasizeOperator(op)
    }

    private fun emphasizeOperator(op: Char) {
        // Simple visual emphasis by setting selected state-like background tint
        val blue = Color.parseColor("#2563EB")
        val amber = Color.parseColor("#F59E0B")
        val reset = Color.parseColor("#FFFFFF")

        opAdd.setBackgroundColor(if (op == '+') blue else reset)
        opAdd.setTextColor(if (op == '+') Color.WHITE else Color.parseColor("#111827"))
        opSub.setBackgroundColor(if (op == '-') amber else reset)
        opSub.setTextColor(if (op == '-') Color.WHITE else Color.parseColor("#111827"))
    }

    /**
     * PUBLIC_INTERFACE
     * Evaluate the current expression: accumulator (op) currentInput.
     * Shows the result and resets operator state to allow chaining.
     */
    private fun evaluate() {
        val inputVal = parseCurrentInput()
        if (inputVal == null && accumulator == null) {
            // Nothing to evaluate
            updateDisplay("0", animated = true)
            return
        }
        if (inputVal != null) {
            applyPendingOperator(inputVal)
        }
        pendingOperator = null
        currentInput.clear()
        val result = accumulator ?: 0
        updateDisplay(result.toString(), animated = true)
        justEvaluated = true

        // Fade out operator emphasis
        opAdd.setBackgroundColor(Color.parseColor("#FFFFFF"))
        opAdd.setTextColor(Color.parseColor("#111827"))
        opSub.setBackgroundColor(Color.parseColor("#FFFFFF"))
        opSub.setTextColor(Color.parseColor("#111827"))
    }
}
