package io.cloudflight.jems.server.common.validator

import io.cloudflight.jems.api.common.dto.I18nMessage
import io.cloudflight.jems.api.programme.dto.language.SystemLanguage
import io.cloudflight.jems.api.project.dto.InputTranslation
import io.cloudflight.jems.server.UnitTest
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.catchThrowableOfType
import org.junit.jupiter.api.DynamicTest
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestFactory
import java.math.BigDecimal
import java.time.ZonedDateTime


internal class GeneralValidatorDefaultImplTest : UnitTest() {

    private val generalValidator = GeneralValidatorDefaultImpl()

    @Test
    fun `should return correct validation result when input length is not equal to the required length`() {
        val input = "input text"
        val requiredLength = 11
        val validationResult = generalValidator.exactLength(input, requiredLength, "input")

        assertThat(validationResult["input"])
            .isEqualTo(
                I18nMessage(
                    "common.error.field.length",
                    mapOf("actualLength" to input.length.toString(), "requiredLength" to requiredLength.toString())
                )
            )
    }

    @Test
    fun `should return correct validation result when input is equal to given parameter`() {
        val input = "input text"
        val validationResult = generalValidator.notEqualTo(input, input, "input")

        assertThat(validationResult["input"])
            .isEqualTo(
                I18nMessage(
                    "common.error.key.invalid",
                    mapOf("key" to input)
                )
            )
    }

    @Nested
    inner class MaxLength {
        @Test
        fun `should return correct validation result when input length is more than max allowed length`() {
            val input = "input text"
            val maxLength = 3
            val validationResult = generalValidator.maxLength(input, maxLength, "input")

            assertThat(validationResult["input"])
                .isEqualTo(
                    I18nMessage(
                        "common.error.field.max.length",
                        mapOf("actualLength" to input.length.toString(), "requiredLength" to maxLength.toString())
                    )
                )
        }

        @Test
        fun `should return correct validation result when translations input length is more than max allowed length`() {
            val input = mutableSetOf(InputTranslation(SystemLanguage.EN, "translation text"))
            val maxLength = 3

            val validationResult = generalValidator.maxLength(input, maxLength, "input")

            assertThat(validationResult["input.${input.first().language.translationKey}"])
                .isEqualTo(
                    I18nMessage(
                        "common.error.field.max.length",
                        mapOf(
                            "actualLength" to input.first().translation!!.length.toString(),
                            "requiredLength" to maxLength.toString()
                        )
                    )
                )
        }
    }

    @Test
    fun `should return correct validation result when input is blank`() {

        val validationResult = generalValidator.notBlank("  ", "input")
        assertThat(validationResult["input"])
            .isEqualTo(I18nMessage("common.error.field.blank"))
    }

    @Test
    fun `should return correct validation result when input is null`() {

        val validationResult = generalValidator.notNull(null, "input")
        assertThat(validationResult["input"])
            .isEqualTo(I18nMessage("common.error.field.required"))
    }

    @TestFactory
    fun `should return correct validation result when input is null or zero`() =
        listOf(
            null, 0L
        ).map { input ->
            DynamicTest.dynamicTest(
                "should return correct validation result when input is null or zero - (input = ${input})"
            ) {
                val validationResult = generalValidator.notNullOrZero(input, "input")
                assertThat(validationResult["input"])
                    .isEqualTo(I18nMessage("common.error.field.should.not.be.null.or.zero"))
            }
        }

    @Test
    fun `should return correct validation result when input is not null or zero`() {
        val validationResult = generalValidator.nullOrZero(1L, "input")
        assertThat(validationResult["input"])
            .isEqualTo(I18nMessage("common.error.field.should.be.null.or.zero"))
    }

    @Test
    fun `should return correct validation result when input is less than minimum`() {
        val validationResult = generalValidator.minDecimal(BigDecimal.ONE, BigDecimal.TEN, "input")
        assertThat(validationResult["input"])
            .isEqualTo(I18nMessage("common.error.field.min.decimal", mapOf("minValue" to BigDecimal.TEN.toString())))
    }


    @Test
    fun `should return correct validation result when start date is after end date`() {
        val start = ZonedDateTime.now().plusDays(4)
        val end = ZonedDateTime.now()

        val validationResult = generalValidator.startDateBeforeEndDate(start, end, "start", "end")

        assertThat(validationResult["start"])
            .isEqualTo(
                I18nMessage(
                    i18nKey = "common.error.field.start.before.end",
                    i18nArguments = mapOf("endDate" to "end", "startDate" to "start")
                )
            )
        assertThat(validationResult["end"])
            .isEqualTo(
                I18nMessage(
                    i18nKey = "common.error.field.end.after.start",
                    i18nArguments = mapOf("endDate" to "end", "startDate" to "start")
                )
            )
    }

    @Test
    fun `should return correct validation result when start date is in the future`() {
        val date = ZonedDateTime.now().plusDays(4).toLocalDate()

        val validationResult = generalValidator.dateNotInFuture(date, "date")

        assertThat(validationResult["date"])
            .isEqualTo(I18nMessage(i18nKey = "common.error.field.date.is.in.future"))

    }


    @TestFactory
    fun `should return correct validation result when input not matched the regex`() {
        val input = "aa"
        val emailRegex = "^(.+)@(.+)\$"

        val validationResult = generalValidator.matches(input, emailRegex, "email", "error.key")

        assertThat(validationResult["email"])
            .isEqualTo(I18nMessage(i18nKey = "error.key"))

    }

    @Test
    fun `should return correct validation result when input contains more items than max size`() {
        val input = listOf(1, 2, 3, 4)
        val maxSize = 2

        val validationResult = generalValidator.maxSize(input, maxSize, "numbers")

        assertThat(validationResult["numbers"])
            .isEqualTo(
                I18nMessage(
                    i18nKey = "common.error.field.max.size",
                    i18nArguments = mapOf("maxSize" to maxSize.toString())
                )
            )
    }

    @Test
    fun `should return correct validation result when input contains less items than min size`() {
        val input = listOf(1, 2, 3, 4)
        val minSize = 6

        val validationResult = generalValidator.minSize(input, minSize, "numbers")

        assertThat(validationResult["numbers"])
            .isEqualTo(
                I18nMessage(
                    i18nKey = "common.error.field.min.size",
                    i18nArguments = mapOf("minSize" to minSize.toString())
                )
            )
    }

    @Nested
    inner class Digits {
        @Test
        fun `should return correct validation result when input fraction part length is more than max allowed length`() {
            val input = BigDecimal(3.01)
            val maxIntegerLength = 1
            val maxFractionLength = 1
            val validationResult = generalValidator.digits(input, maxIntegerLength, maxFractionLength, "input")

            assertThat(validationResult["input"])
                .isEqualTo(
                    I18nMessage(
                        "common.error.field.digit",
                        mapOf(
                            "maxInteger" to maxIntegerLength.toString(),
                            "maxFraction" to maxFractionLength.toString(),
                        )
                    )
                )
        }


        @Test
        fun `should return correct validation result when input integer part length is more than max allowed length`() {
            val input = BigDecimal(30.0)
            val maxIntegerLength = 1
            val maxFractionLength = 1
            val validationResult = generalValidator.digits(input, maxIntegerLength, maxFractionLength, "input")

            assertThat(validationResult["input"])
                .isEqualTo(
                    I18nMessage(
                        "common.error.field.digit",
                        mapOf(
                            "maxInteger" to maxIntegerLength.toString(),
                            "maxFraction" to maxFractionLength.toString(),
                        )
                    )
                )
        }
    }


    @Nested
    inner class OnlyDigits {
        @Test
        fun `should return empty map as validation result when input contains only digits`() {
            val validationResult = generalValidator.onlyDigits("99046728", "input")

            assertThat(validationResult["input"]).isNull()
        }

        @Test
        fun `should return empty map as validation result when input is null`() {
            val validationResult = generalValidator.onlyDigits(null, "input")

            assertThat(validationResult["input"]).isNull()
        }

        @Test
        fun `should return empty map as validation result when input is blank`() {
            val validationResult = generalValidator.onlyDigits("", "input")

            assertThat(validationResult["input"]).isNull()
        }

        @Test
        fun `should return correct validation result when input contains characters other that digits`() {
            val validationResult = generalValidator.onlyDigits("3823g76", "input")

            assertThat(validationResult["input"])
                .isEqualTo(I18nMessage("common.error.only.digits"))
        }
    }

    @Nested
    inner class NumberBetween {
        @TestFactory
        fun `should return correct validation result when input integer is not in the range`() =
            listOf(
                50, 110
            ).map { input ->
                DynamicTest.dynamicTest(
                    "should return correct validation result when input integer is not in the range - (input = ${input})"
                ) {
                    val minValue = 60
                    val maxValue = 100
                    val validationResult = generalValidator.numberBetween(input, minValue, maxValue, "input")

                    assertThat(validationResult["input"])
                        .isEqualTo(
                            I18nMessage(
                                "common.error.field.number.out.of.range",
                                mapOf(
                                    "number" to "$input",
                                    "min" to "$minValue",
                                    "max" to "$maxValue",
                                )
                            )
                        )
                }
            }

        @TestFactory
        fun `should return correct validation result when input big decimal is not in the range or scale is not correct`() =
            listOf(
                BigDecimal.TEN,
                BigDecimal.valueOf(5999 ,2),
                BigDecimal.valueOf(101),
            ).map { input ->
                DynamicTest.dynamicTest(
                    "should return correct validation result when input big decimal is not in the range - (input = ${input})"
                ) {
                    val minValue = BigDecimal.valueOf(60)
                    val maxValue = BigDecimal.valueOf(100)
                    val validationResult = generalValidator.numberBetween(input, minValue, maxValue,  "input")

                    assertThat(validationResult["input"])
                        .isEqualTo(
                            I18nMessage(
                                "common.error.field.number.out.of.range",
                                mapOf(
                                    "number" to "$input",
                                    "min" to "$minValue",
                                    "max" to "$maxValue",
                                )
                            )
                        )
                }
            }
    }

    @TestFactory
    fun `should return correct validation result when number scale is not valid`() =
        listOf(
            BigDecimal.valueOf(99, 3 ),
            BigDecimal.valueOf(69999, 4)
        ).map { input ->
            DynamicTest.dynamicTest(
                "should return correct validation result when number scale is not valid - (input = ${input})"
            ) {
                val validationResult = generalValidator.scale(input,2,  "input")

                assertThat(validationResult["input"])
                    .isEqualTo(
                        I18nMessage(
                            "common.error.field.number.scale.is.not.valid",
                            mapOf(
                                "number" to "$input",
                                "maxScale" to 2.toString(),
                            )
                        )
                    )
            }
        }

    @Test
    fun `should return correct validation result when currency is not valid`() {
        assertThat(generalValidator.onlyValidCurrencies(setOf("AAA", "AAB", "AAC"), "inputName")["inputName"])
            .isEqualTo(
                I18nMessage(
                    i18nKey = "common.error.currency.code.invalid",
                    i18nArguments = mapOf(
                        "AAA" to "invalid.currency.code",
                        "AAB" to "invalid.currency.code",
                        "AAC" to "invalid.currency.code",
                        "currencyCodes" to "AAA, AAB, AAC",
                    ),
                )
            )
    }

    @Test
    fun `should return correct validation result when currency is valid`() {
        assertThat(generalValidator.onlyValidCurrencies(setOf("EUR", "PLN", "HUF", "SLE"), "inputName")).isEmpty()
    }

    @Test
    fun `should throw AppInputValidationException when there is at least one validation error`() {
        val input = "input text"
        val validInput = "12"
        val maxLength = 3
        val exception = catchThrowableOfType(
            {
                generalValidator.throwIfAnyIsInvalid(
                    generalValidator.maxLength(input, maxLength, "input"),
                    generalValidator.maxLength(validInput, maxLength, "validInput")
                )
            },
            AppInputValidationException::class.java
        )
        assertThat(exception.i18nMessage.i18nKey).isEqualTo("common.error.input.invalid")

        assertThat(exception.formErrors["input"])
            .isEqualTo(
                I18nMessage(
                    "common.error.field.max.length",
                    mapOf("actualLength" to input.length.toString(), "requiredLength" to maxLength.toString())
                )
            )

        assertThat(exception.formErrors["validInput"]).isNull()
    }
}
