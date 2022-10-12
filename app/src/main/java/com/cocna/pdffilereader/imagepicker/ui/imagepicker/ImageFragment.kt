package com.cocna.pdffilereader.imagepicker.ui.imagepicker

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.SortedList
import com.cocna.pdffilereader.R
import com.cocna.pdffilereader.common.Common
import com.cocna.pdffilereader.common.Logger
import com.cocna.pdffilereader.databinding.ImagepickerFragmentBinding
import com.cocna.pdffilereader.imagepicker.helper.ImageHelper
import com.cocna.pdffilereader.imagepicker.helper.LayoutManagerHelper
import com.cocna.pdffilereader.imagepicker.listener.OnImageSelectListener
import com.cocna.pdffilereader.imagepicker.model.CallbackStatus
import com.cocna.pdffilereader.imagepicker.model.GridCount
import com.cocna.pdffilereader.imagepicker.model.Image
import com.cocna.pdffilereader.imagepicker.model.Result
import com.cocna.pdffilereader.imagepicker.ui.adapter.ImageGroupAdapter
import com.cocna.pdffilereader.imagepicker.ui.adapter.ImagePickerAdapter
import com.cocna.pdffilereader.imagepicker.widget.GridSpacingItemDecoration
import java.util.*
import kotlin.collections.ArrayList

class ImageFragment : BaseFragment() {

    private var _binding: ImagepickerFragmentBinding? = null
    private val binding get() = _binding!!

    private var bucketId: Long? = null
    private lateinit var gridCount: GridCount

    private lateinit var viewModel: ImagePickerViewModel

    private lateinit var imageAdapter: ImagePickerAdapter

    //    private lateinit var imageAdapterGroup: ImageGroupAdapter
    private lateinit var gridLayoutManager: GridLayoutManager
    private lateinit var itemDecoration: GridSpacingItemDecoration

    companion object {

        const val BUCKET_ID = "BucketId"
        const val GRID_COUNT = "GridCount"

        fun newInstance(bucketId: Long, gridCount: GridCount): ImageFragment {
            val fragment = ImageFragment()
            val args = Bundle()
            args.putLong(BUCKET_ID, bucketId)
            args.putParcelable(GRID_COUNT, gridCount)
            fragment.arguments = args
            return fragment
        }

        fun newInstance(gridCount: GridCount): ImageFragment {
            val fragment = ImageFragment()
            val args = Bundle()
            args.putParcelable(GRID_COUNT, gridCount)
            fragment.arguments = args
            return fragment
        }
    }

    private val selectedImageObserver = object : Observer<ArrayList<Image>> {
        override fun onChanged(it: ArrayList<Image>) {
            imageAdapter.setSelectedImages(it)
//            imageAdapterGroup.setSelectedImages(it)
            viewModel.selectedImages.removeObserver(this)
        }

    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        bucketId = arguments?.getLong(BUCKET_ID)
        gridCount = arguments?.getParcelable(GRID_COUNT)!!

        viewModel = requireActivity().run {
            ViewModelProvider(this, ImagePickerViewModelFactory(requireActivity().application))[ImagePickerViewModel::class.java]
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val config = viewModel.getConfig()

        imageAdapter =
            ImagePickerAdapter(requireActivity(), config, activity as OnImageSelectListener)
        gridLayoutManager = LayoutManagerHelper.newInstance(requireContext(), gridCount)
        gridLayoutManager.spanSizeLookup = object : GridLayoutManager.SpanSizeLookup() {
            override fun getSpanSize(position: Int): Int {
                val typeView = imageAdapter.getItemViewType(position)
                return if (typeView == ImagePickerAdapter.TYPE_HEADER) {
                    gridCount.portrait
                } else {
                    1
                }
            }
        }
        itemDecoration = GridSpacingItemDecoration(
            gridLayoutManager.spanCount,
            resources.getDimension(R.dimen.imagepicker_grid_spacing).toInt()
        )
//        imageAdapterGroup = ImageGroupAdapter(requireActivity(), config, activity as OnImageSelectListener, gridCount)

        _binding = ImagepickerFragmentBinding.inflate(inflater, container, false)

        binding.apply {
            root.setBackgroundColor(Color.parseColor(config.backgroundColor))
            progressIndicator.setIndicatorColor(Color.parseColor(config.progressIndicatorColor))
            recyclerView.apply {
                setHasFixedSize(true)
                layoutManager = gridLayoutManager
                addItemDecoration(itemDecoration)
                adapter = imageAdapter
            }
//            recyclerView.apply {
//                setHasFixedSize(true)
//                layoutManager = LinearLayoutManager(requireActivity())
//                adapter = imageAdapterGroup
//            }
        }

        viewModel.apply {
            result.observe(viewLifecycleOwner) {
                handleResult(it)
            }
            selectedImages.observe(viewLifecycleOwner, selectedImageObserver)
        }

        return binding.root
    }


    private fun handleResult(result: Result) {
        if (result.status is CallbackStatus.SUCCESS) {
            val images = ImageHelper.filterImages(result.images, bucketId)
            if (images.isNotEmpty()) {
//                imageAdapter.setData(images)
                binding.recyclerView.visibility = View.VISIBLE

                //Image group start
                val treeMapImage: TreeMap<String, java.util.ArrayList<Image>> = TreeMap()
                for (image in images) {
                    val key = image.dateModified ?: ""
                    if (treeMapImage.containsKey(key)) {
                        val lstImage = treeMapImage[key]
                        lstImage?.let {
                            it.add(image)
                            treeMapImage.put(key, it)
                        }
                    } else {
                        val lstImage = ArrayList<Image>()
                        lstImage.add(image)
                        treeMapImage[key] = lstImage
                    }
                }
                if (treeMapImage.size > 0) {
                    val sortData =
                        treeMapImage.toSortedMap(compareByDescending { Common.formatDateCompare(it) })
                    val lstImageHeader = ArrayList<Image>()
                    for (item in sortData.entries) {
                        lstImageHeader.add(Image(header = item.key, numberFiles = item.value.size))
                        lstImageHeader.addAll(item.value)
                    }
                    imageAdapter.setData(lstImageHeader)
//                    imageAdapterGroup.setData(sortData)
                }
                //Image group end


            } else {
                binding.recyclerView.visibility = View.GONE
            }
        } else {
            binding.recyclerView.visibility = View.GONE
        }

        binding.apply {
            emptyText.visibility =
                if (result.status is CallbackStatus.SUCCESS && result.images.isEmpty()) View.VISIBLE else View.GONE
            progressIndicator.visibility =
                if (result.status is CallbackStatus.FETCHING) View.VISIBLE else View.GONE
        }
    }

    override fun handleOnConfigurationChanged() {
//        val newSpanCount =
//            LayoutManagerHelper.getSpanCountForCurrentConfiguration(requireContext(), gridCount)
//        itemDecoration =
//            GridSpacingItemDecoration(
//                gridLayoutManager.spanCount,
//                resources.getDimension(R.dimen.imagepicker_grid_spacing).toInt()
//            )
//        gridLayoutManager.spanCount = newSpanCount
//        binding.recyclerView.addItemDecoration(itemDecoration)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun convertDate(d: String): Int {
        val arrayDate = d.split("/")
        if (arrayDate.isNotEmpty()) {
            return (arrayDate[2] + checkMonth(arrayDate[1]) + arrayDate[0]).toInt()
        }
        return 0
    }

    private fun checkMonth(strMonth: String): String {
        var month = strMonth
        if (strMonth.length == 1) {
            month = "0" + strMonth
        }
        return month
    }

}