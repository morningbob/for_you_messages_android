package com.bitpunchlab.android.foryoumessages.base

import androidx.databinding.ViewDataBinding
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import androidx.databinding.library.baseAdapters.BR

abstract class BaseRecyclerViewAdapter<T>(private val clickListener: ((item: T) -> Unit)? = null) :
    ListAdapter<T, DataBindingViewHolder>(GenericDiffCallback(compareItems, compareContents)){
}

class DataBindingViewHolder<T>(private val binding: ViewDataBinding) :
        RecyclerView.ViewHolder(binding.root) {

            fun bind(item: T) {
                binding.setVariable(BR.item, item)
                binding.executePendingBindings()
            }
        }