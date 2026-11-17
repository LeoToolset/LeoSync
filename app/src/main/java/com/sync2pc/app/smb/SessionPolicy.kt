package com.sync2pc.app.smb

/** Der PC hat die Anmeldung nur als Gast oder anonym angenommen, obwohl ein Benutzer konfiguriert ist (SYNC-20261114-334, Punkt 6) */
class SmbGuestSessionException : Exception(com.sync2pc.app.l10n.str(com.sync2pc.app.R.string.err_friendly_guest_session))

/** Reine Entscheidung zur Sitzungsart, ohne Android (getestet in SessionPolicyTest) */
object SessionPolicy {
    /**
     * Gast- oder anonyme Sitzung ablehnen? Nur wenn für das Profil ein Benutzername gesetzt ist; Profile ohne Benutzernamen
     * (bewusster Gastzugang) bleiben erlaubt.
     */
    fun rejectGuest(userConfigured: Boolean, isGuest: Boolean, isAnonymous: Boolean): Boolean =
        userConfigured && (isGuest || isAnonymous)
}