package com.einkscreensaver.app.ui.adapter

import android.net.Uri
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.einkscreensaver.app.databinding.ItemImageSelectionBinding

class ImageSelectionAdapter(
    private val onImageClick: (Uri) -> Unit
) : ListAdapter<Uri, ImageSelectionAdapter.ImageViewHolder>(ImageDiffCallback()) {
    
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ImageViewHolder {
        val binding = ItemImageSelectionBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ImageViewHolder(binding)
    }
    
    override fun onBindViewHolder(holder: ImageViewHolder, position: Int) {
        holder.bind(getItem(position))
    }
    
    inner class ImageViewHolder(
        private val binding: ItemImageSelectionBinding
    ) : RecyclerView.ViewHolder(binding.root) {
        
        fun bind(imageUri: Uri) {
            Glide.with(binding.root.context)
                .load(imageUri)
                .centerCrop()
                .into(binding.imageView)
            
            binding.root.setOnClickListener {
                onImageClick(imageUri)
            }
        }
    }
    
    class ImageDiffCallback : DiffUtil.ItemCallback<Uri>() {
        override fun areItemsTheSame(oldItem: Uri, newItem: Uri): Boolean {
            return oldItem == newItem
        }
        
        override fun areContentsTheSame(oldItem: Uri, newItem: Uri): Boolean {
            return oldItem == newItem
        }
    }
}