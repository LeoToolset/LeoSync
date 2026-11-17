# Privacy Policy – LeoSync

**Last updated:** November 15, 2026  
**Version:** 1.2

This privacy policy explains what data is processed by the LeoSync Android application and how this data is handled.

LeoSync is an application developed under the Leo brand. Its purpose is to synchronize photos and videos between Android devices and users' own network shares without relying on external cloud services.

## 1. Data Controller

The party responsible for data processing is:

- **Developer:** Raffael Baldinger
- **Country:** Switzerland
- **Contact:** leotools@protonmail.com

## 2. Data Processing Principles

LeoSync is designed to minimize the transfer of data to external parties.

Photos, videos, metadata and synchronization information are generally processed locally on the Android device.

Files are transferred directly between the Android device and the SMB network share configured by the user.

LeoSync does not use:

- External cloud storage services
- Advertising networks
- Analytics or tracking services
- External synchronization services
- Advertising or analytics SDKs

No usage statistics or personal data are automatically transmitted to the developer.

LeoSync is distributed and updated exclusively through Google Play; the app itself does not check for updates (see Section 8).

## 3. Photos and Videos

LeoSync requires access to the photos and videos selected by the user for synchronization.

Files are processed locally on the Android device and transferred directly to the configured SMB network share.

No data is transferred through a cloud service operated by LeoSync or its developer.

Depending on the selected synchronization mode, files may also be transferred from the SMB server to the Android device.

Files are stored and managed at the storage locations selected by the user.

## 4. EXIF Metadata and Location Information

LeoSync can read GPS coordinates embedded in the EXIF metadata of photos.

This information is processed locally and compared against an integrated offline database to determine, for example, the country in which a photo was taken.

No external geolocation, mapping or location services are contacted for this purpose.

GPS coordinates are not automatically transmitted to the developer.

When original photos are transferred, their original EXIF metadata may remain part of the transferred files.

### 4.1 No Location Permission, Recognition of the PC

LeoSync does not query the device's location and does not require a location permission for this. The name of the Wi-Fi network (SSID) is not read. LeoSync recognizes the PC by its identifier (SMB server identifier), which is checked before the user name and password are sent.

To save battery on unknown networks, the app stores locally on the device a technical fingerprint of the networks in which the PC was reliably recognized (formed from network details such as the router address, address range and search domain). This fingerprint does not leave the device, contains neither location nor Wi-Fi names, and can be deleted in the profile settings with "Forget".

The permission "Location in photos" (access to GPS data in photos, see Section 4) is requested by the app only when the user chooses country folders for a photo profile, and only after an explanation. The capture location is evaluated exclusively locally on the device, not stored and not transmitted. The permission can be revoked at any time in the Android settings; country folders are then not available.

## 5. SMB Connections and Credentials

To connect to an SMB network share, LeoSync requires connection information provided by the user.

This may include:

- Server address
- Share path
- Username
- Password
- Other connection parameters

SMB passwords are stored securely using the Android Keystore.

Credentials are not transmitted to the developer or external cloud services.

File transfers take place directly between the Android device and the configured SMB server.

The security of the connection also depends on the network configuration and security settings of the respective SMB server.

## 6. Diagnostic and Error Reports

LeoSync provides a feature for exporting technical diagnostic reports.

These reports help investigate errors, identify compatibility issues and improve application stability.

Depending on the implementation and the device, a diagnostic report may contain the following information:

### 6.1 Device and Application Information

- Device manufacturer and model
- Android version and SDK version
- Application version and build information
- Manufacturer-specific operating system version

### 6.2 System Status and Permissions

- Battery level and charging status
- Battery optimization status
- Power-saving and background execution status
- Status of relevant application permissions
- Available folder access permissions

### 6.3 Network and Synchronization Information

- Wi-Fi connection status
- Network type and connection status
- Synchronization settings
- Configured synchronization profiles
- Technical status of SMB connections
- Synchronization history and error states

### 6.4 Technical Logs

- Background jobs and WorkManager status
- Warnings and error messages
- Information about terminated processes
- Synchronization statistics
- Timestamps of relevant events

Diagnostic reports ("Report a problem", "Copy diagnostic data") are pseudonymized and masked before output: user names, IP addresses, computer, share, Wi-Fi, profile and folder names, paths and exact file names are replaced with placeholders (e.g. "Host-1", "WLAN-1"); free text such as error messages is additionally masked by pattern (e.g. IP addresses, email addresses, paths, coordinates). Device model, Android version, settings, counters, year/month folders, standard folders such as DCIM and file extensions are retained. Masking cannot rule out that individual details in unusual text formats remain unmasked.

**SMB passwords are not included in diagnostic reports.**

The exported **history** ("Share history") is excluded from this: it is an export by the user for themselves and may contain IP addresses, server details and file names. It should be reviewed before being passed on.

The most recent app crashes are stored locally on the device with a shortened error trace and appear only in the diagnostic report.

### 6.5 Pseudonymous Installation Identifier

Diagnostic reports may contain a randomly generated installation identifier (UUID).

This UUID:

- Is generated locally on the device.
- Is not derived from hardware or device identifiers.
- Does not directly contain personally identifiable information.
- Is used to associate multiple diagnostic reports from the same app installation.

A new installation identifier is generated when the application is reinstalled.

The UUID does not directly identify an individual. However, since it can be used to associate multiple reports, it should be considered a pseudonymous identifier.

## 7. Sending Diagnostic Reports

Diagnostic reports are never transmitted automatically.

The user decides whether to export a report and send it to the developer by email. The "Report a problem" function opens the email application with a prepared text and diagnostic report; nothing is sent until the user sends the email themselves.

The report is sent using the email application or email service selected by the user.

By sending a report, the user voluntarily transmits the technical information contained in it to the developer.

Before sending a report, users should review its contents and be aware that technical details may reveal information about their device and network environment.

The email provider's own privacy policy also applies to the processing of the email.

The developer uses voluntarily submitted diagnostic reports exclusively to investigate technical issues and improve LeoSync.

## 8. Third-Party Services

LeoSync does not use external analytics, advertising, tracking or cloud SDKs.

No user data is automatically shared with advertising networks or analytics providers.

### 8.1 Distribution and Updates via Google Play

LeoSync is distributed and updated exclusively through Google Play. The app contains no update check of its own and makes no connection to GitHub or any other server for this purpose. Google's privacy policy applies to obtaining and updating the app through Google Play. Earlier versions distributed via GitHub are no longer maintained.

### 8.2 Help and Product Page

The menu item "Help & product page" opens the LeoSync product page (leotoolset.github.io/leosync) in the user's browser. No data from the app is passed on.

### 8.3 Other Data Flows

Data may leave the device as part of actions initiated by the user, particularly:

- Synchronization with the configured SMB network share.
- Voluntary submission of diagnostic reports by email.
- Sharing the history or a backup file via the Android share menu with an app chosen by the user.

The Android operating system and email or network services used by the user may independently process additional technical information as part of their own services. Their respective privacy policies apply.

## 9. Data Security

LeoSync uses security mechanisms provided by the Android operating system.

In particular, stored SMB passwords are protected using the Android Keystore.

The application is designed to avoid unnecessary data transfers and to perform data processing locally wherever possible.

The security of file transfers also depends on:

- The security of the local network.
- The configuration of the SMB server.
- The SMB version being used.
- Enabled authentication and encryption mechanisms.

Users should secure their network shares appropriately and avoid exposing SMB services to the public internet without adequate protection.

## 10. Data Storage and Deletion

LeoSync does not operate its own cloud storage for user data.

Files are stored on the Android device and at the SMB storage locations configured by the user.

**Backup (export/import):** LeoSync can export profiles, settings and, optionally, the synchronization status to a file. It contains, among other things, server addresses, share paths and user names. SMB passwords are included only on request and are then encrypted with an export password chosen by the user (AES-256); without an export password the file contains no passwords. The file is saved or shared wherever the user decides and should be protected accordingly. The app itself does not create automatic Android backups (allowBackup).

**Recycle bin and version protection:** When files in folder profiles are replaced or deleted, they are, depending on the settings, moved to a recycle-bin folder (`@LeoSync-Bin`) on the mobile device or the share, and permanently deleted automatically after the configured period or manually. These folders exist only on the user's own storage locations.

Local application data and settings can be deleted through Android application management settings. When the application is uninstalled, its associated local application data is removed according to Android system behavior.

Files already transferred to an SMB share are not automatically deleted when LeoSync is uninstalled.

These files must be managed directly on the respective storage system.

Diagnostic reports are initially exported locally. The developer only gains access to their contents when the user voluntarily sends them by email.

## 11. Rights of Data Subjects

Under applicable data protection legislation, individuals may have rights including:

- The right to request access to personal data being processed.
- The right to request correction of inaccurate data.
- The right to request deletion of personal data, where legally applicable.
- The right to object to certain types of data processing.

Since LeoSync does not automatically transmit user data to the developer, the developer generally has no access to users' locally stored photos, videos, settings or synchronization data.

For privacy-related inquiries, please use the contact information provided in Section 1.

Users in Switzerland may also contact the Swiss Federal Data Protection and Information Commissioner (FDPIC).

Website: https://www.edoeb.admin.ch/

## 12. Changes to This Privacy Policy

This privacy policy may be updated if LeoSync's features, technical data processing or applicable legal requirements change.

The latest version will be published on the official Leo project website.

## 13. Contact

For questions regarding the application or data privacy:

**Leo – Independent Software**

Country: Switzerland

Email: leotools@protonmail.com