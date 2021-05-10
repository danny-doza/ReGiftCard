package com.csci4480.regiftcard.data

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.FragmentActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.csci4480.regiftcard.R
import com.csci4480.regiftcard.data.classes.Card
import com.firebase.ui.database.FirebaseRecyclerOptions
import com.google.firebase.database.FirebaseDatabase
import com.firebase.ui.database.FirebaseRecyclerAdapter as FirebaseRecyclerAdapter

class GrabCards() {
    companion object {
        private const val LOG_TAG = "448.GrabCards"


    }

    class CardTextViewHolder(itemView: View): RecyclerView.ViewHolder(itemView) {
        fun bind(item: Card) = with(itemView) {
            itemView.findViewById<TextView>(R.id.company_name).text = item.company_name
            itemView.findViewById<TextView>(R.id.denomination).text = item.card_worth.toString()
        }
    }

    private lateinit var cards_adapter: FirebaseRecyclerAdapter<Card, GrabCards.CardTextViewHolder>

    public fun grabCards(recycler_view: RecyclerView, activity: FragmentActivity, user_id: String) {
        Log.d(LOG_TAG, "grabCards: Attempting to grab cards.")
        recycler_view.hasFixedSize()
        recycler_view.layoutManager = LinearLayoutManager(activity)

        val cardsRef = FirebaseDatabase.getInstance().getReference().child("cards")
        val cardsQuery = cardsRef
        Log.d(LOG_TAG, "$cardsQuery")
        val cardsOptions = FirebaseRecyclerOptions.Builder<Card>().setQuery(cardsQuery, Card::class.java).build()

        cards_adapter = object: FirebaseRecyclerAdapter<Card, GrabCards.CardTextViewHolder>(cardsOptions) {
            override fun onBindViewHolder(holder: CardTextViewHolder, position: Int, model: Card) {
                Log.d("Binding", model.card_num)
                holder.bind(model)
            }

            override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CardTextViewHolder {
                var view = LayoutInflater.from(parent.context).inflate(R.layout.card_item, parent, false)
                return CardTextViewHolder(view)
            }
        }
        recycler_view.adapter = cards_adapter
    }

    public fun onStart() { cards_adapter.startListening() }
    public fun onStop() { cards_adapter.stopListening() }
}