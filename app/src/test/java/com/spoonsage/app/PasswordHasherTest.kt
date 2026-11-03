package com.spoonsage.app

import com.spoonsage.app.util.PasswordHasher
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class PasswordHasherTest {

    @Test
    fun correctPasswordVerifies() {
        val salt = PasswordHasher.newSalt()
        val hash = PasswordHasher.hash("Secret123", salt)
        assertTrue(PasswordHasher.verify("Secret123", salt, hash))
    }

    @Test
    fun wrongPasswordFails() {
        val salt = PasswordHasher.newSalt()
        val hash = PasswordHasher.hash("Secret123", salt)
        assertFalse(PasswordHasher.verify("secret123", salt, hash))
    }

    @Test
    fun hashIsNotThePlainPassword() {
        val hash = PasswordHasher.hash("Secret123", PasswordHasher.newSalt())
        assertFalse(hash.contains("Secret123"))
        assertEquals(64, hash.length)
    }

    @Test
    fun samePasswordDifferentSaltGivesDifferentHash() {
        val a = PasswordHasher.hash("Secret123", PasswordHasher.newSalt())
        val b = PasswordHasher.hash("Secret123", PasswordHasher.newSalt())
        assertNotEquals(a, b)
    }
}
