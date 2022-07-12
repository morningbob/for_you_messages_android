package com.bitpunchlab.android.foryoumessages.contacts

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bitpunchlab.android.foryoumessages.databinding.ItemContactBinding
import com.bitpunchlab.android.foryoumessages.models.User

class ContactsAdapter(var clickListener: ContactOnClickListener) : ListAdapter<User, ContactViewHolder>(ContactDiffCallback()) {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ContactViewHolder {
        return ContactViewHolder.from(parent)
    }

    override fun onBindViewHolder(holder: ContactViewHolder, position: Int) {
        val contact = getItem(position)
        holder.bind(contact, clickListener)
    }

}

class ContactViewHolder(val binding: ItemContactBinding) : RecyclerView.ViewHolder(binding.root) {

    fun bind(contact: User, onClickListener: ContactOnClickListener) {
        binding.contact = contact
        binding.clickListener = onClickListener
        binding.executePendingBindings()
    }

    companion object {
        fun from(parent: ViewGroup): ContactViewHolder {
            val layoutInflater = LayoutInflater.from(parent.context)
            val binding = ItemContactBinding.inflate(layoutInflater, parent, false)

            return ContactViewHolder(binding)
        }
    }
}

class ContactDiffCallback : DiffUtil.ItemCallback<User>() {
    override fun areItemsTheSame(oldItem: User, newItem: User): Boolean {
        return oldItem.userID == newItem.userID
    }

    override fun areContentsTheSame(oldItem: User, newItem: User): Boolean {
        return oldItem.userID == newItem.userID
    }

}

class ContactOnClickListener(val clickListener: (User) -> Unit) {
    fun onClick(contact: User) = clickListener(contact)
}

