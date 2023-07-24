package com.sanskar.projectmanagement.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.sanskar.projectmanagement.R
import com.sanskar.projectmanagement.models.Board
import com.sanskar.projectmanagement.models.Card

open class CardsListItemsAdapter(private val context: Context,
                                 private var list: ArrayList<Card>):
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private var onClickListener: CardsListItemsAdapter.OnClickListener? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return MyViewHolder(
            LayoutInflater.from(context).inflate(R.layout.item_card, parent, false)
        )
    }

    override fun getItemCount(): Int {
        return list.size
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val model = list[position]

        if (holder is MyViewHolder) {
            holder.itemView.findViewById<TextView>(R.id.tv_card_name).text = model.name

            holder.itemView.setOnClickListener {
                if(onClickListener != null){
                    onClickListener!!.onClick(position)
                }
            }
        }
    }

    fun setOnClickListener(onClickListener: OnClickListener) {
        this.onClickListener = onClickListener

    }

    interface OnClickListener{
        fun onClick(position: Int)
    }

    class MyViewHolder(view: View): RecyclerView.ViewHolder(view)
}