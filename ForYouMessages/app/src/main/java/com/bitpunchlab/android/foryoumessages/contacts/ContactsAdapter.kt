package com.bitpunchlab.android.foryoumessages.contacts

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bitpunchlab.android.foryoumessages.databinding.ItemContactBinding
import com.bitpunchlab.android.foryoumessages.models.Contact
import com.bitpunchlab.android.foryoumessages.models.ContactEntity
import com.bitpunchlab.android.foryoumessages.models.User

class ContactsAdapter(var clickListener: ContactOnClickListener) : ListAdapter<ContactEntity, ContactViewHolder>(ContactDiffCallback()) {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ContactViewHolder {
        return ContactViewHolder.from(parent)
    }

    override fun onBindViewHolder(holder: ContactViewHolder, position: Int) {
        val contact = getItem(position)
        holder.bind(contact, clickListener)
    }

}

class ContactViewHolder(val binding: ItemContactBinding) : RecyclerView.ViewHolder(binding.root) {

    fun bind(contact: ContactEntity, onClickListener: ContactOnClickListener) {
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

class ContactDiffCallback : DiffUtil.ItemCallback<ContactEntity>() {
    override fun areItemsTheSame(oldItem: ContactEntity, newItem: ContactEntity): Boolean {
        return oldItem.contactEmail == newItem.contactEmail
    }

    override fun areContentsTheSame(oldItem: ContactEntity, newItem: ContactEntity): Boolean {
        return oldItem.contactPhone == newItem.contactPhone
    }

}

class ContactOnClickListener(val clickListener: (ContactEntity) -> Unit) {
    fun onClick(contact: ContactEntity) = clickListener(contact)
}

