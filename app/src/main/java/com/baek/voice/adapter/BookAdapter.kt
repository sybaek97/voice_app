package com.baek.voice.adapter

import android.text.Layout
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.baek.voice.R

class BookAdapter(private val mData: List<String>,  private val itemClickListener: OnItemClickListener) :
    RecyclerView.Adapter<BookAdapter.ViewHolder>() {

    interface OnItemClickListener{
        fun onItemClick(item:String)
    }
    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val textView: TextView = itemView.findViewById(R.id.textView)

        init {
            textView.setOnClickListener {
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    val item = mData[position]
                    itemClickListener.onItemClick(item)
                }
            }
        }
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view=LayoutInflater.from(parent.context).inflate(R.layout.book_item_view,parent,false)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int {

        return mData.size
    }
    fun getItemPosition(item: String): Int {
        return mData.indexOf(item)
    }
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = mData[position]
        holder.textView.text = item
    }


}