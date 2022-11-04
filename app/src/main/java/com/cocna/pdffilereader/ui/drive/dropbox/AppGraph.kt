package com.cocna.pdffilereader.ui.drive.dropbox

interface AppGraph {
    val dropboxCredentialUtil: DropboxCredentialUtil
    val dropboxOAuthUtil: DropboxOAuthUtil
    val dropboxApiWrapper: DropboxApiWrapper
}