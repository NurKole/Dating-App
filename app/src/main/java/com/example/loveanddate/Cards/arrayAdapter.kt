package com.example.loveanddate.Cards

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.ImageView
import android.widget.TextView
import com.bumptech.glide.Glide
import com.bumptech.glide.RequestManager
import com.example.loveanddate.R

class arrayAdapter(context: Context, resourceId: Int, items: List<Cards>) : ArrayAdapter<Cards>(context, resourceId, items) {

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val cardItem = getItem(position)
        var itemView = convertView

        if (itemView == null) {
            val inflater = LayoutInflater.from(context)
            itemView = inflater.inflate(R.layout.item, parent, false)
        }

        // Populate the view with card_item data
        /*if (cardItem != null) {
            // Access views within the 'itemView' and set their properties
            val textViewName = itemView?.findViewById<TextView>(R.id.Name)
            val textViewAge = itemView?.findViewById<TextView>(R.id.age)

            textViewName?.text = cardItem.name
            textViewAge?.text = cardItem.age

            // You can also load the profile image here using an image loading library like Glide

        }*/

        val name = itemView?.findViewById<TextView>(R.id.name)
        val image = itemView?.findViewById<ImageView>(R.id.image)
        val age = itemView?.findViewById<TextView>(R.id.age)

        name?.text = cardItem?.name
        age?.text = cardItem?.age


        when (cardItem?.profileImageUrl) {
            "default" -> {
                Glide.with(itemView?.context)
                    .load(R.drawable.profile_foreground)
                    .into(image)
            }
            else -> {
                image?.setImageResource(0) // Clear the image by setting it to a placeholder (0 in this case)
                Glide.with(itemView?.context)
                    .load(cardItem?.profileImageUrl)
                    .into(image)
            }
        }

        return itemView!!
    }
}


