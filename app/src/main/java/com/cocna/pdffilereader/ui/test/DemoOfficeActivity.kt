package com.cocna.pdffilereader.ui.test

import android.os.Environment
import android.view.LayoutInflater
import android.view.View
import android.webkit.MimeTypeMap
import androidx.documentfile.provider.DocumentFile
import androidx.recyclerview.widget.LinearLayoutManager
import com.anggrayudi.storage.file.DocumentFileCompat
import com.anggrayudi.storage.file.DocumentFileType
import com.anggrayudi.storage.file.extension
import com.anggrayudi.storage.file.search
import com.cocna.pdffilereader.databinding.ActivityBaseBinding
import com.cocna.pdffilereader.databinding.ActivityDemoOfficeBinding
import com.cocna.pdffilereader.ui.base.BaseActivity
import com.cocna.pdffilereader.ui.home.adapter.MyFilesAdapter
import com.cocna.pdffilereader.ui.home.model.MyFilesModel
import java.io.File

/**
 * Created by Thuytv on 12/07/2022.
 */
class DemoOfficeActivity : BaseActivity<ActivityDemoOfficeBinding>() {
    override val bindingInflater: (LayoutInflater) -> ActivityDemoOfficeBinding
        get() = ActivityDemoOfficeBinding::inflate
    private lateinit var lstFilePdf: ArrayList<MyFilesModel>

    private var PATH_DEFAULT_STORE = "/storage/emulated/0"
    private var myFilesAdapter: MyFilesAdapter? = null

    private val storageDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
    private val outputPDF = File(storageDir, "Converted_PDF.pdf")

    override fun initData() {
        lstFilePdf = ArrayList()
        myFilesAdapter = MyFilesAdapter(this, lstFilePdf, MyFilesAdapter.TYPE_VIEW_FILE, object : MyFilesAdapter.OnItemClickListener {
            override fun onClickItem(documentFile: MyFilesModel) {
                convertDocToPdf(documentFile)
            }

            override fun onClickItemMore(view: View, documentFile: MyFilesModel) {
            }

        })
        binding.rcvDemoOffice.apply {
            layoutManager = LinearLayoutManager(this@DemoOfficeActivity)
            adapter = myFilesAdapter
        }
        getAllFilePdf()
    }
    private fun convertDocToPdf(documentFile: MyFilesModel){
        val filePdf = FileToPDF()
        filePdf.WordToPDF(documentFile.uriPath, outputPDF.path)
    }
    override fun initEvents() {
    }

    private fun getAllFilePdf() {

        var root = DocumentFileCompat.getRootDocumentFile(this, "primary", true)
        if (root == null) {
            root = DocumentFile.fromFile(File(PATH_DEFAULT_STORE))
        }
        val mime = MimeTypeMap.getSingleton().getMimeTypeFromExtension("docx")!!
        val mimeDoc = MimeTypeMap.getSingleton().getMimeTypeFromExtension("doc")!!
        val pdfArray = root.search(true, DocumentFileType.FILE, arrayOf(mime, mimeDoc))
        if (pdfArray.isNotEmpty()) {
            for (item in pdfArray) {
                val model =
                    MyFilesModel(
                        name = item.name,
                        uriPath = item.uri.path,
                        uriOldPath = item.uri.path,
                        lastModified = item.lastModified(),
                        extensionName = item.extension,
                        length = item.length()
                    )
                lstFilePdf.add(model)
            }
        }
        myFilesAdapter?.updateData(lstFilePdf)
    }
}