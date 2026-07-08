package com.sapabps.ui.guestbook

import android.graphics.drawable.GradientDrawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.sapabps.R
import com.sapabps.model.GuestBook
import com.sapabps.model.QueueStatus
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class GuestBookAdapter(
    private val onItemClick: (GuestBook) -> Unit
) : RecyclerView.Adapter<GuestBookAdapter.ViewHolder>() {

    private val items = mutableListOf<GuestBook>()

    fun submitList(list: List<GuestBook>) {
        items.clear()
        items.addAll(list)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_guest_book, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = items[position]
        holder.bind(item)
    }

    override fun getItemCount(): Int = items.size

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvQueueNumber: TextView = itemView.findViewById(R.id.tvQueueNumber)
        private val tvStatus: TextView = itemView.findViewById(R.id.tvStatus)
        private val tvAgencyName: TextView = itemView.findViewById(R.id.tvAgencyName)
        private val tvRemarks: TextView = itemView.findViewById(R.id.tvRemarks)
        private val tvTimestamp: TextView = itemView.findViewById(R.id.tvTimestamp)

        fun bind(item: GuestBook) {
            tvQueueNumber.text = item.queueNumber
            tvStatus.text = item.status
            tvAgencyName.text = item.agencyName
            tvRemarks.text = item.remarks

            val sdf = SimpleDateFormat("dd MMM yyyy, HH:mm 'WIB'", Locale.getDefault())
            tvTimestamp.text = sdf.format(Date(item.timestamp))

            val bg = tvStatus.background.mutate() as GradientDrawable
            val colorRes = when (item.status) {
                QueueStatus.MENUNGGU.name -> R.color.statusMenunggu
                QueueStatus.DILAYANI.name -> R.color.statusDilayani
                QueueStatus.SELESAI.name -> R.color.statusSelesai
                QueueStatus.BATAL.name -> R.color.statusBatal
                else -> R.color.statusMenunggu
            }
            bg.setColor(itemView.context.getColor(colorRes))

            itemView.setOnClickListener { onItemClick(item) }
        }
    }
}
