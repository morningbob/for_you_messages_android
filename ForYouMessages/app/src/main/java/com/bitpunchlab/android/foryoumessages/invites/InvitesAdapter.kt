package com.bitpunchlab.android.foryoumessages.invites

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bitpunchlab.android.foryoumessages.databinding.ItemInviteBinding
import com.bitpunchlab.android.foryoumessages.models.Contact
import com.bitpunchlab.android.foryoumessages.models.User



class InvitesAdapter (var clickListener: InviteOnClickListener) : ListAdapter<Contact, InviteViewHolder>(InviteDiffCallback()) {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): InviteViewHolder {
        return InviteViewHolder.from(parent)
    }

    override fun onBindViewHolder(holder: InviteViewHolder, position: Int) {
        val contact = getItem(position)
        holder.bind(contact, clickListener)
    }

}

class InviteViewHolder(val binding: ItemInviteBinding) : RecyclerView.ViewHolder(binding.root) {

    fun bind(contact: Contact, onClickListener: InviteOnClickListener) {
        binding.contact = contact
        binding.clickListener = onClickListener
        binding.executePendingBindings()
    }

    companion object {
        fun from(parent: ViewGroup): InviteViewHolder {
            val layoutInflater = LayoutInflater.from(parent.context)
            val binding = ItemInviteBinding.inflate(layoutInflater, parent, false)

            return InviteViewHolder(binding)
        }
    }
}

class InviteDiffCallback : DiffUtil.ItemCallback<Contact>() {
    override fun areItemsTheSame(oldItem: Contact, newItem: Contact): Boolean {
        return oldItem.contactEmail == newItem.contactEmail
    }

    override fun areContentsTheSame(oldItem: Contact, newItem: Contact): Boolean {
        return oldItem.contactPhone == newItem.contactPhone
    }

}

class InviteOnClickListener(val clickListener: (Contact) -> Unit) {
    fun onClick(contact: Contact) = clickListener(contact)
}
