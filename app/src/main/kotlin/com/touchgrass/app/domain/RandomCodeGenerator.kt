package com.touchgrass.app.domain

import kotlin.random.Random

/**
 * Generates the 30-character alphanumeric string the user must re-type to pass the
 * [FrictionMode.RandomCode] gate (spec §3.1.D).
 *
 * Alphabet excludes the visual confusables `0/O` and `1/I/l` for kindness to humans, and uses
 * uppercase only (we case-insensitive compare on input).
 */
object RandomCodeGenerator {
    const val CODE_LENGTH: Int = 30

    private val ALPHABET: List<Char> =
        (('A'..'Z') + ('2'..'9')).filter { it != 'O' && it != 'I' && it != 'L' }

    fun next(random: Random = Random.Default): String =
        buildString(CODE_LENGTH) {
            repeat(CODE_LENGTH) { append(ALPHABET[random.nextInt(ALPHABET.size)]) }
        }
}
