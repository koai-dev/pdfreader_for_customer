package com.cocna.pdffilereader.ui.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.cocna.pdffilereader.R
import com.cocna.pdffilereader.common.*
import com.cocna.pdffilereader.databinding.FragmentCreatePdfResultBinding
import com.cocna.pdffilereader.imagepicker.model.Image
import com.cocna.pdffilereader.ui.base.BaseFragment
import com.cocna.pdffilereader.ui.home.dialog.LoadingAdsDialog
import com.cocna.pdffilereader.ui.home.model.MyFilesModel
import java.io.File


/**
 * Created by Thuytv on 16/09/2022.
 */
class CreateImageResultFragment : BaseFragment<FragmentCreatePdfResultBinding>(), View.OnClickListener {
    private var myFilesModel: MyFilesModel? = null

    override val bindingInflater: (LayoutInflater, ViewGroup?, Boolean) -> FragmentCreatePdfResultBinding
        get() = FragmentCreatePdfResultBinding::inflate

    override fun initData() {
        getBaseActivity()?.let {
            LoadingAdsDialog.newInstance(it, AppConfig.ID_ADS_INTERSTITIAL_RESULT_IMGTOPDF).show(childFragmentManager, "LOADING_ADS")
        }

        myFilesModel = arguments?.getParcelable(AppKeys.KEY_BUNDLE_DATA)
        myFilesModel?.let {
            binding.vlResultFileName.text = it.name
            binding.vlResultFileTime.text = Common.covertTimeDisplayResult(it.lastModified)
            binding.imvCoverPdf.setImageURI(it.uriSaveFile)
        }

        binding.ttToolbarPdf.text = getString(R.string.tt_result_success)

        getBaseActivity()?.showNativeAdsBottom(binding.adFrameResult, AppConfig.ID_ADS_NATIVE_RESULT)
    }

    override fun initEvents() {
        listenClickViews(binding.btnResultShareFile, binding.btnResultOpenFile, binding.btnResultBackToHome, binding.imvPdfBack)

    }

    override fun onClick(v: View?) {
        MultiClickPreventer.preventMultiClick(v)
        when (v?.id) {
            R.id.imvPdfBack -> {
                getBaseActivity()?.finish()
            }
            R.id.btn_result_share_file -> {
                myFilesModel?.uriPath?.let { File(it) }?.let { shareFile(it) }
            }
            R.id.btn_result_open_file -> {
                val bundle = Bundle()
                bundle.putParcelable(AppKeys.KEY_BUNDLE_DATA, myFilesModel)
                getBaseActivity()?.onNextScreen(PdfViewActivity::class.java, bundle, true)
            }
            R.id.btn_result_back_to_home -> {
                getBaseActivity()?.finish()
            }
        }
    }
}