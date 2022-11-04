package com.cocna.pdffilereader.ui.drive.dropbox

import android.content.Context

internal class AppGraphImpl(context: Context) : AppGraph {
    private val dropboxAppConfig = DropboxAppConfig()

    override val dropboxCredentialUtil by lazy { DropboxCredentialUtil(context.applicationContext) }

    override val dropboxOAuthUtil by lazy {
        DropboxOAuthUtil(
            dropboxAppConfig = dropboxAppConfig,
            dropboxCredentialUtil = dropboxCredentialUtil
        )
    }

    override val dropboxApiWrapper
        get() = DropboxApiWrapper(
            dbxCredential = dropboxCredentialUtil.readCredentialLocally()!!,
            clientIdentifier = dropboxAppConfig.clientIdentifier
        )
}