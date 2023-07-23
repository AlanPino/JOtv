package com.joeldev.iptvplayer

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView

class CategoryAdapter(
    var categories: MutableList<String>,
    private val onCategorySelected: (String) -> Unit
) : RecyclerView.Adapter<CategoryViewHolder>() {

    lateinit var categorySelected: String

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CategoryViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        return CategoryViewHolder(layoutInflater.inflate(R.layout.category_item, parent, false))
    }

    override fun getItemCount() = categories.size

    override fun onBindViewHolder(holder: CategoryViewHolder, position: Int) {
        holder.bind(categories[position])

        val cardView = holder.itemView.findViewById<CardView>(R.id.background)

        if (position == categories.indexOf(categorySelected)) {
            cardView.setCardBackgroundColor(ContextCompat.getColor(holder.itemView.context, R.color.lighten))
        } else {
            cardView.setCardBackgroundColor(ContextCompat.getColor(holder.itemView.context, R.color.transparent))
        }

        holder.itemView.setOnClickListener(){
            onCategorySelected(categories[position])
            categorySelected = categories[position]
            notifyDataSetChanged()
        }
    }
}