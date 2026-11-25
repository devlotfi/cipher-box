package com.github.devlotfi.cipherbox.shared.screens.login

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavKey
import com.github.devlotfi.cipherbox.shared.navigation.LoginNavigationKeys
import com.github.devlotfi.cipherbox.shared.provider.LocalLoginDb
import com.github.devlotfi.cipherbox.shared.utils.RoomFlowData
import kotlinx.coroutines.flow.map
import java.math.BigDecimal
import java.math.MathContext

// ───────────────────────── Screen ─────────────────────────

private enum class Kind { Number, Operator, Function, Equals }

private data class Key(val label: String, val kind: Kind)

private val keys = listOf(
  listOf(
    Key("AC", Kind.Function),
    Key("⌫", Kind.Function),
    Key("%", Kind.Function),
    Key("÷", Kind.Operator)
  ),
  listOf(
    Key("7", Kind.Number),
    Key("8", Kind.Number),
    Key("9", Kind.Number),
    Key("×", Kind.Operator)
  ),
  listOf(
    Key("4", Kind.Number),
    Key("5", Kind.Number),
    Key("6", Kind.Number),
    Key("−", Kind.Operator)
  ),
  listOf(
    Key("1", Kind.Number),
    Key("2", Kind.Number),
    Key("3", Kind.Number),
    Key("+", Kind.Operator)
  ),
  listOf(
    Key("±", Kind.Number),
    Key("0", Kind.Number),
    Key(".", Kind.Number),
    Key("=", Kind.Equals)
  ),
)

@Composable
fun CalculatorCamouflageScreen(
  modifier: Modifier = Modifier,
  backStack: NavBackStack<NavKey>
) {
  val localLoginDb = LocalLoginDb.current
  val loginDb = requireNotNull(localLoginDb.loginDbManager.db)
  var expression by rememberSaveable { mutableStateOf("") }
  var justEvaluated by rememberSaveable { mutableStateOf(false) }
  val preview = remember(expression) { if (justEvaluated) "" else previewOf(expression) }

  val loginDataFlow = remember {
    loginDb.loginDataDao().observe().map { RoomFlowData(loading = false, data = it) }
  }
  val loginDataFlowData by loginDataFlow.collectAsStateWithLifecycle(
    initialValue = RoomFlowData(
      loading = true
    )
  )

  SideEffect(expression) {
    if (expression == loginDataFlowData.data?.camouflagePin) {
      backStack.clear()
      backStack.add(LoginNavigationKeys.Pin)
    }
  }

  fun onKey(label: String) {
    when (label) {
      "AC" -> {
        expression = ""; justEvaluated = false
      }

      "⌫" -> {
        expression = if (justEvaluated || expression == "Error") "" else expression.dropLast(1)
        justEvaluated = false
      }

      "=" -> {
        if (expression.isNotEmpty() && expression != "Error") {
          val result = evaluate(expression)
          expression = result ?: "Error"
          justEvaluated = true
        }
      }

      else -> {
        if (expression == "Error") expression = ""
        val startsFresh = justEvaluated
        justEvaluated = false
        expression = applyInput(expression, label, startsFresh)
      }
    }
  }

  Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.surface) {
    Column(
      modifier = Modifier
        .fillMaxSize()
        .statusBarsPadding()
        .navigationBarsPadding()
        .padding(horizontal = 16.dp, vertical = 12.dp),
      verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
      Display(
        expression = expression,
        preview = preview,
        modifier = Modifier
          .weight(0.8f)
          .fillMaxWidth()
      )
      Keypad(
        onKey = ::onKey,
        modifier = Modifier
          .weight(1.5f)
          .fillMaxWidth()
      )
    }
  }
}

@Composable
private fun Display(expression: String, preview: String, modifier: Modifier = Modifier) {
  val scroll = rememberScrollState()
  LaunchedEffect(expression) { scroll.scrollTo(scroll.maxValue) }

  val mainSize = when {
    expression.length > 14 -> 36.sp
    expression.length > 9 -> 48.sp
    else -> 64.sp
  }

  Column(
    modifier = modifier.padding(horizontal = 8.dp),
    horizontalAlignment = Alignment.End,
    verticalArrangement = Arrangement.Bottom
  ) {
    Text(
      text = expression.ifEmpty { "0" },
      modifier = Modifier
        .fillMaxWidth()
        .horizontalScroll(scroll),
      style = MaterialTheme.typography.displayLarge.copy(
        fontSize = mainSize,
        fontWeight = FontWeight.Light
      ),
      color = if (expression.isEmpty())
        MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
      else MaterialTheme.colorScheme.onSurface,
      textAlign = TextAlign.End,
      maxLines = 1,
      softWrap = false
    )
    Text(
      text = preview.ifEmpty { " " },
      style = MaterialTheme.typography.headlineMedium,
      color = MaterialTheme.colorScheme.onSurfaceVariant,
      maxLines = 1,
      modifier = Modifier.padding(top = 4.dp)
    )
  }
}

// ───────────────────────── Keypad ─────────────────────────

@Composable
private fun Keypad(onKey: (String) -> Unit, modifier: Modifier = Modifier) {
  Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(10.dp)) {
    keys.forEach { row ->
      Row(
        modifier = Modifier
          .weight(1f)
          .fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
      ) {
        row.forEach { key ->
          CalcButton(
            key = key,
            onClick = { onKey(key.label) },
            modifier = Modifier
              .weight(1f)
              .fillMaxSize()
          )
        }
      }
    }
  }
}

@Composable
private fun CalcButton(key: Key, onClick: () -> Unit, modifier: Modifier = Modifier) {
  val scheme = MaterialTheme.colorScheme
  val (container, content) = when (key.kind) {
    Kind.Number -> scheme.surfaceContainerHigh to scheme.onSurface
    Kind.Operator -> scheme.secondaryContainer to scheme.onSecondaryContainer
    Kind.Function -> scheme.tertiaryContainer to scheme.onTertiaryContainer
    Kind.Equals -> scheme.primary to scheme.onPrimary
  }

  Box(
    modifier = modifier
      .clip(RoundedCornerShape(28.dp))
      .background(container)
      .clickable(onClick = onClick),
    contentAlignment = Alignment.Center
  ) {
    Text(
      text = key.label,
      color = content,
      fontSize = 28.sp,
      fontWeight = if (key.kind == Kind.Number) FontWeight.Normal else FontWeight.Medium
    )
  }
}

// ───────────────────────── Input handling ─────────────────────────

private val operators = setOf('+', '−', '×', '÷')

private fun applyInput(current: String, label: String, startsFresh: Boolean): String {
  val c = label.first()
  // After "=", typing a digit starts a new calculation; an operator continues from the result.
  val expr = if (startsFresh && (c.isDigit() || c == '.')) "" else current

  return when {
    c.isDigit() -> expr + c

    c == '.' -> {
      val segment = expr.takeLastWhile { it !in operators && it != '%' }
      when {
        '.' in segment -> expr
        segment.isEmpty() -> expr + "0."
        else -> "$expr."
      }
    }

    c == '±' -> toggleSign(expr)

    c == '%' -> if (expr.isNotEmpty() && (expr.last()
        .isDigit() || expr.last() == '%')
    ) "$expr%" else expr

    c in operators -> when {
      expr.isEmpty() -> if (c == '−') "−" else expr
      expr == "−" -> expr
      expr.last() in operators -> {
        // Replace a trailing operator (but keep a leading minus intact).
        val trimmed = expr.dropLast(1)
        if (trimmed.isEmpty()) (if (c == '−') "−" else expr) else trimmed + c
      }

      else -> expr + c
    }

    else -> expr
  }
}

private fun toggleSign(expr: String): String {
  if (expr.isEmpty()) return "−"
  val idx = expr.indexOfLast { it in operators }
  return when {
    idx == -1 -> if (expr.startsWith("−")) expr.drop(1) else "−$expr"
    idx == expr.lastIndex -> expr // trailing operator: nothing to toggle
    expr[idx] == '−' && (idx == 0 || expr[idx - 1] in operators) ->
      expr.removeRange(idx, idx + 1) // unary minus → positive
    else -> expr.substring(0, idx + 1) + "−" + expr.substring(idx + 1)
  }
}

// ───────────────────────── Evaluation ─────────────────────────

/** Live result shown under the expression; blank when it would just repeat the input. */
private fun previewOf(expression: String): String {
  if (expression.isEmpty() || expression == "Error") return ""
  if (expression.none { it in operators || it == '%' }) return ""
  if (expression.count { it == '−' } == 1 && expression.startsWith("−") &&
    expression.drop(1).none { it in operators || it == '%' }
  ) return ""
  return evaluate(expression) ?: ""
}

/** Returns formatted result, or null if the expression is invalid / divides by zero. */
private fun evaluate(raw: String): String? {
  val expr = raw.trimEnd { it in operators }
  if (expr.isEmpty()) return null
  return try {
    val value = Parser(expr).parse()
    if (value.isNaN() || value.isInfinite()) null else format(value)
  } catch (e: Exception) {
    null
  }
}

private fun format(value: Double): String {
  if (value == 0.0) return "0"
  val bd = BigDecimal(value).round(MathContext(12)).stripTrailingZeros()
  val abs = bd.abs()
  return if (abs >= BigDecimal("1E12") || abs < BigDecimal("1E-9")) {
    bd.toString().replace("E+", "E").replace('-', '−')
  } else {
    bd.toPlainString().replace('-', '−')
  }
}

/**
 * Recursive-descent parser:
 *   expr    = term (('+' | '−') term)*
 *   term    = unary (('×' | '÷') unary)*
 *   unary   = '−' unary | postfix
 *   postfix = number '%'*
 */
private class Parser(private val s: String) {
  private var pos = 0

  fun parse(): Double {
    val v = expr()
    if (pos != s.length) error("Unexpected character")
    return v
  }

  private fun peek(): Char? = s.getOrNull(pos)

  private fun expr(): Double {
    var v = term()
    while (true) {
      when (peek()) {
        '+' -> {
          pos++; v += term()
        }

        '−' -> {
          pos++; v -= term()
        }

        else -> return v
      }
    }
  }

  private fun term(): Double {
    var v = unary()
    while (true) {
      when (peek()) {
        '×' -> {
          pos++; v *= unary()
        }

        '÷' -> {
          pos++
          val d = unary()
          if (d == 0.0) error("Divide by zero")
          v /= d
        }

        else -> return v
      }
    }
  }

  private fun unary(): Double {
    if (peek() == '−') {
      pos++; return -unary()
    }
    return postfix()
  }

  private fun postfix(): Double {
    var v = number()
    while (peek() == '%') {
      pos++; v /= 100.0
    }
    return v
  }

  private fun number(): Double {
    val start = pos
    while (peek()?.let { it.isDigit() || it == '.' } == true) pos++
    if (start == pos) error("Number expected")
    return s.substring(start, pos).toDouble()
  }
}