package com.sync2pc.app.data

import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Base64
import java.security.KeyStore
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec

/** Gespeichertes Passwort nicht mehr lesbar (Schlüssel weg/ungültig, Chiffretext beschädigt): nie mit leerem Passwort anmelden (R-SMB5) */
class CredentialUnreadableException(cause: Throwable? = null) : Exception(com.sync2pc.app.l10n.str(com.sync2pc.app.R.string.err_friendly_credential_unreadable), cause)

/** Keystore gerade nicht verfügbar (z. B. kurz nach dem Start): vorübergehend, der nächste Auslöser versucht es erneut */
class CredentialUnavailableException(cause: Throwable? = null) : Exception(com.sync2pc.app.l10n.str(com.sync2pc.app.R.string.err_friendly_credential_unavailable), cause)

/**
 * Chiffretext beim Speichern/Testen im Profileditor (SYNC-20261115-377): War das gespeicherte Passwort nicht lesbar und wurde
 * kein neues eingegeben, bleibt der alte Chiffretext unverändert. So meldet der Verbindungstest «Passwort nicht lesbar» (wie der
 * Lauf, [CredentialUnreadableException]) statt sich mit leerem Passwort anzumelden, und Speichern löscht nichts.
 */
object PasswordKeep {
    fun encFor(originalEnc: String, unreadable: Boolean, typed: String, encrypt: (String) -> String): String =
        if (unreadable && typed.isEmpty()) originalEnc else encrypt(typed)
}

/** Ergebnis von [CredentialCrypto.decryptResult] */
sealed class DecryptResult {
    data class Ok(val plain: String) : DecryptResult()
    /** Chiffretext nicht leer, aber nicht entschlüsselbar */
    object Unreadable : DecryptResult()
    /** übrige Keystore-Fehler */
    object Unavailable : DecryptResult()
}

/** Verschlüsselt SMB-Passwörter mit einem nicht exportierbaren Schlüssel im Android-Keystore. */
object CredentialCrypto {
    private const val ALIAS = "sync2pc_credentials"
    private const val TRANSFORMATION = "AES/GCM/NoPadding"

    /** Nur für instrumentierte Tests (SYNC-20261111-207, S5): anderer Schlüsselname, damit der echte Schlüssel nie angefasst wird */
    @Volatile internal var alias: String = ALIAS

    private fun key(): SecretKey {
        val ks = KeyStore.getInstance("AndroidKeyStore").apply { load(null) }
        (ks.getKey(alias, null) as? SecretKey)?.let { return it }
        val gen = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, "AndroidKeyStore")
        gen.init(
            KeyGenParameterSpec.Builder(alias, KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT)
                .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
                .setKeySize(256)
                .build()
        )
        return gen.generateKey()
    }

    fun encrypt(plain: String): String {
        if (plain.isEmpty()) return ""
        val cipher = Cipher.getInstance(TRANSFORMATION)
        cipher.init(Cipher.ENCRYPT_MODE, key())
        val ct = cipher.doFinal(plain.toByteArray(Charsets.UTF_8))
        return Base64.encodeToString(cipher.iv + ct, Base64.NO_WRAP)
    }

    /**
     * Wie [decrypt], unterscheidet aber «nicht lesbar» von «gerade nicht verfügbar» (R-SMB5, SYNC-20261114-334). Erzeugt nie einen
     * neuen Schlüssel: fehlt er, ist ein vorhandener Chiffretext nicht lesbar. Leer = Ok("").
     */
    fun decryptResult(enc: String): DecryptResult {
        if (enc.isEmpty()) return DecryptResult.Ok("")
        return try {
            val all = Base64.decode(enc, Base64.NO_WRAP)
            if (all.size <= 12) return DecryptResult.Unreadable
            val ks = KeyStore.getInstance("AndroidKeyStore").apply { load(null) }
            val key = ks.getKey(alias, null) as? SecretKey ?: return DecryptResult.Unreadable
            val cipher = Cipher.getInstance(TRANSFORMATION)
            cipher.init(Cipher.DECRYPT_MODE, key, GCMParameterSpec(128, all.copyOfRange(0, 12)))
            DecryptResult.Ok(String(cipher.doFinal(all.copyOfRange(12, all.size)), Charsets.UTF_8))
        } catch (e: Exception) {
            if (classify(e)) DecryptResult.Unreadable else DecryptResult.Unavailable
        }
    }

    /** true = «nicht lesbar» (Schlüssel ungültig/falsch, Chiffretext beschädigt), false = «nicht verfügbar» (übrige Keystore-Fehler) */
    internal fun classify(e: Throwable): Boolean =
        e is javax.crypto.BadPaddingException || e is javax.crypto.IllegalBlockSizeException ||
            e is java.security.UnrecoverableKeyException || e is IllegalArgumentException ||
            e is java.security.InvalidKeyException && e.javaClass.name.contains("KeyPermanentlyInvalidated") ||
            e.javaClass.name.contains("KeyPermanentlyInvalidatedException")

    fun decrypt(enc: String): String {
        if (enc.isEmpty()) return ""
        return try {
            val all = Base64.decode(enc, Base64.NO_WRAP)
            val iv = all.copyOfRange(0, 12)
            val ct = all.copyOfRange(12, all.size)
            val cipher = Cipher.getInstance(TRANSFORMATION)
            cipher.init(Cipher.DECRYPT_MODE, key(), GCMParameterSpec(128, iv))
            String(cipher.doFinal(ct), Charsets.UTF_8)
        } catch (e: Exception) {
            ""
        }
    }
}
