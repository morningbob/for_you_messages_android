package com.bitpunchlab.android.foryoumessages.invites

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bitpunchlab.android.foryoumessages.databinding.ItemInviteBinding
import com.bitpunchlab.android.foryoumessages.models.Contact
import com.bitpunchlab.android.foryoumessages.models.User



class InvitesAdapter (var acceptClickListener: AcceptOnClickListener,
    var rejectClickListener: RejectOnClickListener) : ListAdapter<Contact, InviteViewHolder>(InviteDiffCallback()) {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): InviteViewHolder {
        return InviteViewHolder.from(parent)
    }

    override fun onBindViewHolder(holder: InviteViewHolder, position: Int) {
        val contact = getItem(position)
        holder.bind(contact, acceptClickListener, rejectClickListener)
    }

}

class InviteViewHolder(val binding: ItemInviteBinding) : RecyclerView.ViewHolder(binding.root) {

    fun bind(contact: Contact, acceptClickListener: AcceptOnClickListener,
        rejectClickListener: RejectOnClickListener) {
        binding.contact = contact
        binding.acceptClickListener = acceptClickListener
        binding.rejectClickListener = rejectClickListener
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

class AcceptOnClickListener(val clickListener: (Contact) -> Unit) {
    fun onClick(contact: Contact) = clickListener(contact)
}

class RejectOnClickListener(val clickListener: (Contact) -> Unit) {
    fun onClick(contact: Contact) = clickListener(contact)
}


