package com.spoonsage.app

import com.spoonsage.app.util.Validators
import com.spoonsage.app.util.Validators.Error
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class ValidatorsTest {

    @Test fun validEmail() = assertNull(Validators.email("given@example.co.za"))
    @Test fun emptyEmail() = assertEquals(Error.EMPTY, Validators.email("  "))
    @Test fun badEmail() = assertEquals(Error.INVALID_EMAIL, Validators.email("given@"))

    @Test fun shortPassword() = assertEquals(Error.SHORT_PASSWORD, Validators.password("12345"))
    @Test fun okPassword() = assertNull(Validators.password("123456"))
    @Test fun mismatch() = assertEquals(Error.PASSWORD_MISMATCH, Validators.confirmPassword("abcdef", "abcdeg"))

    @Test fun numberNotANumber() = assertEquals(Error.NOT_A_NUMBER, Validators.number("abc", 0.0, 10.0))
    @Test fun numberOutOfRange() = assertEquals(Error.OUT_OF_RANGE, Validators.number("400", 25.0, 300.0))
    @Test fun numberCommaDecimal() = assertNull(Validators.number("1,5", 0.0, 10.0))
    @Test fun optionalBlankNumber() = assertNull(Validators.number("", 0.0, 10.0, optional = true))
    @Test fun parseComma() = assertEquals(1.5, Validators.toDouble("1,5"), 0.0001)
    @Test fun parseGarbageFallsBack() = assertEquals(0, Validators.toInt("x"))

    @Test fun validTime() = assertNull(Validators.time("07:30"))
    @Test fun invalidTime() = assertEquals(Error.INVALID_TIME, Validators.time("25:00"))
}
