package com.sync2pc.app.ui

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import com.sync2pc.app.R

/** Statussymbole oben rechts auf der Profilkachel (SYNC-20261112-285) */
enum class ProfileIcon(@DrawableRes val drawable: Int, @StringRes val description: Int) {
    PC(R.drawable.ic_pc_known, R.string.home_profile_pc_known),
    ENCRYPTED(R.drawable.ic_encrypted, R.string.icon_encrypted),
}

/** Welche Symbole die Kopfzeile der Profilkachel zeigt (rein, getestet in ProfileStatusIconsTest) */
object ProfileStatusIcons {
    /**
     * Nur die zutreffenden Symbole, immer in der Reihenfolge PC, Verschlüsselt (von links nach rechts); nicht
     * zutreffende entfallen ohne Lücke, die Gruppe steht am rechten Rand. Das WLAN-Symbol entfällt (SYNC-20261115-382).
     * [pcKnown]: die PC-Kennung ist erkannt (PC-Erkennung per Kennung und gespeicherte Kennung bekannt); [encrypted]: die letzte
     * Anmeldung war verschlüsselt.
     */
    fun visible(pcKnown: Boolean, encrypted: Boolean): List<ProfileIcon> = buildList {
        if (pcKnown) add(ProfileIcon.PC)
        if (encrypted) add(ProfileIcon.ENCRYPTED)
    }
}
