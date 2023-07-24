package com.sanskar.projectmanagement.adapters

import android.content.Context
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.sanskar.projectmanagement.R

class LabelColorListItemAdapter(private val context: Context,
                                private var list: ArrayList<String>,
                                private val mSelectedColor: String)
    : RecyclerView.Adapter<RecyclerView.ViewHolder>(){

    var onItemClickListener: OnItemClickListener? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return MyViewHolder(
            LayoutInflater.from(context).inflate(R.layout.item_label_color, parent, false)
        )
    }

    override fun getItemCount(): Int = list.size

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val item = list[position]
        if(holder is MyViewHolder){
            holder.itemView.findViewById<View>(R.id.view_main).setBackgroundColor(Color.parseColor(item))
            if(item == mSelectedColor){
                holder.itemView.findViewById<ImageView>(R.id.iv_selected_color).visibility = View.VISIBLE
            }else{
                holder.itemView.findViewById<ImageView>(R.id.iv_selected_color).visibility = View.GONE
            }
            holder.itemView.setOnClickListener {
                if(onItemClickListener != null){
                    onItemClickListener!!.onClick(position, item)
                }
            }
        }
    }

    fun setOnClickListener(onClickListener: OnItemClickListener) {
        this.onItemClickListener = onClickListener

    }

    interface OnItemClickListener{
        fun onClick(position: Int, color: String)
    }

    class MyViewHolder(view: View): RecyclerView.ViewHolder(view)
}