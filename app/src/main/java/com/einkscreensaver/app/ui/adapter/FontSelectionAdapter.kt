package com.einkscreensaver.app.ui.adapter

import android.graphics.Typeface
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.einkscreensaver.app.databinding.ItemFontSelectionBinding

class FontSelectionAdapter(
    private val onFontClick: (String) -> Unit
) : ListAdapter<String, FontSelectionAdapter.FontViewHolder>(FontDiffCallback()) {
    
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FontViewHolder {
        val binding = ItemFontSelectionBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return FontViewHolder(binding)
    }
    
    override fun onBindViewHolder(holder: FontViewHolder, position: Int) {
        holder.bind(getItem(position))
    }
    
    inner class FontViewHolder(
        private val binding: ItemFontSelectionBinding
    ) : RecyclerView.ViewHolder(binding.root) {
        
        fun bind(fontName: String) {
            binding.apply {
                tvFontName.text = fontName
                
                // Apply font style to preview text
                tvFontPreview.typeface = when (fontName) {
                    "Default" -> Typeface.DEFAULT_BOLD
                    "Monospace" -> Typeface.MONOSPACE
                    "Sans Serif" -> Typeface.SANS_SERIF
                    "Serif" -> Typeface.SERIF
                    "Condensed" -> Typeface.create("sans-serif-condensed", Typeface.NORMAL)
                    "Light" -> Typeface.create("sans-serif-light", Typeface.NORMAL)
                    "Thin" -> Typeface.create("sans-serif-thin", Typeface.NORMAL)
                    else -> Typeface.DEFAULT_BOLD
                }
                
                root.setOnClickListener {
                    onFontClick(fontName)
                }
            }
        }
    }
    
    class FontDiffCallback : DiffUtil.ItemCallback<String>() {
        override fun areItemsTheSame(oldItem: String, newItem: String): Boolean {
            return oldItem == newItem
        }
        
        override fun areContentsTheSame(oldItem: String, newItem: String): Boolean {
            return oldItem == newItem
        }
    }
}