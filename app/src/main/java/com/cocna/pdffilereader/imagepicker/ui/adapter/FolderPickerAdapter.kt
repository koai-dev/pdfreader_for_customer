package com.cocna.pdffilereader.imagepicker.ui.adapter

import android.content.Context
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.cocna.pdffilereader.R
import com.cocna.pdffilereader.imagepicker.helper.GlideHelper
import com.cocna.pdffilereader.imagepicker.listener.OnFolderClickListener
import com.cocna.pdffilereader.imagepicker.model.Folder

class FolderPickerAdapter(context: Context, private val itemClickListener: OnFolderClickListener) :
    BaseRecyclerViewAdapter<FolderPickerAdapter.FolderViewHolder?>(context) {

    private val folders: MutableList<Folder> = mutableListOf()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FolderViewHolder {
        val itemView = inflater.inflate(R.layout.imagepicker_item_folder, parent, false)
        return FolderViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: FolderViewHolder, position: Int) {
        val folder = folders[position]
        val count = folder.images.size
        val previewImage = folder.images[0]
        previewImage.uri?.let {
            GlideHelper.loadImage(holder.image, it)
        }

        holder.name.text = folder.name
        holder.count.text = count.toString()
        holder.itemView.setOnClickListener {
            itemClickListener.onFolderClick(folder)
        }
    }

    fun setData(folders: List<Folder>) {
        this.folders.clear()
        this.folders.addAll(folders)
        notifyDataSetChanged()
    }

    override fun getItemCount(): Int {
        return folders.size
    }

    class FolderViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val image: ImageView = itemView.findViewById(R.id.image_folder_thumbnail)
        val name: TextView = itemView.findViewById(R.id.text_folder_name)
        val count: TextView = itemView.findViewById(R.id.text_photo_count)

    }

}