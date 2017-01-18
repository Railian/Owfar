package com.owfar.android.ui.broadcasts

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup

import com.owfar.android.R
import com.owfar.android.models.api.classes.InterestsGroup
import com.owfar.android.models.api.classes.Stream

import io.realm.RealmList

class ChannelsAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder>(),
        BroadcastGroupHolder.InterestsGroupListener, ChannelItemHolder.InterestListener {

    //region constants
    private val TYPE_GROUP = 1
    private val TYPE_INTEREST = 2
    //endregion

    //region fields
    private var recyclerView: RecyclerView? = null
    //endregion

    //region DataSource Methods
    private var interestsGroups: RealmList<InterestsGroup>? = null
    private var expandedInterestsGroup: InterestsGroup? = null
    private var expandedInterestsGroupView: View? = null

    fun setInterestsGroups(interestsGroups: RealmList<InterestsGroup>) {
        this.interestsGroups = interestsGroups
        notifyDataSetChanged()
    }

    private fun getInterestsGroup(adapterPosition: Int) = interestsGroups?.let {
        it[adapterPosition - (expandedInterestsGroup?.let { expanded ->
            if (adapterPosition > it.indexOf(expanded)) getInterestsCount(expanded) else 0
        } ?: 0)]
    }

    private fun getInterest(adapterPosition: Int) = expandedInterestsGroup?.let { expanded ->
        expanded.interests?.get(adapterPosition - (interestsGroups?.indexOf(expanded)?.let {
            if (it < 0) 0 else (it + 1)
        } ?: 0))
    }

    private fun getInterestsGroupsCount() = interestsGroups?.size ?: 0

    private fun getInterestsCount(interestsGroup: InterestsGroup?) = interestsGroup?.interests?.size ?: 0
    //endregion

    //region Adapter Methods
    override fun onAttachedToRecyclerView(recyclerView: RecyclerView?) {
        super.onAttachedToRecyclerView(recyclerView)
        this.recyclerView = recyclerView
    }

    override fun getItemViewType(adapterPosition: Int) = interestsGroups?.indexOf(expandedInterestsGroup)?.let { index ->
        if (adapterPosition in (index + 1)..(index + getInterestsCount(expandedInterestsGroup)))
            TYPE_INTEREST
        else TYPE_GROUP
    } ?: TYPE_GROUP


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder? {
        val inflater = LayoutInflater.from(parent.context)
        return when (viewType) {
            TYPE_GROUP -> BroadcastGroupHolder(inflater.inflate(R.layout.item_broadcast_group, parent, false)).apply {
                setInterestsGroupListener(this@ChannelsAdapter)
            }
            TYPE_INTEREST -> ChannelItemHolder(inflater.inflate(R.layout.item_broadcast, parent, false)).apply {
                setInterestListener(this@ChannelsAdapter)
            }
            else -> null
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (getItemViewType(position)) {
            TYPE_GROUP -> {
                val groupHolder = holder as BroadcastGroupHolder
                val interestsGroup = getInterestsGroup(position)
                groupHolder.configureWith(interestsGroup, interestsGroup === expandedInterestsGroup)
            }
            TYPE_INTEREST -> {
                val itemHolder = holder as ChannelItemHolder
                itemHolder.configureWith(getInterest(position)!!)
            }
        }
    }

    override fun getItemCount() = getInterestsGroupsCount() + getInterestsCount(expandedInterestsGroup)
    //endregion

    //region Private Tools
    private fun expand(interestsGroupView: View, interestsGroup: InterestsGroup) {
        collapse()
        interestsGroups?.indexOf(interestsGroup)?.let { index ->
            if (index < 0) return
            val interestsCount = getInterestsCount(interestsGroup)
            expandedInterestsGroup = interestsGroup
            expandedInterestsGroupView = interestsGroupView
            notifyItemRangeInserted(index + 1, interestsCount)
            (recyclerView?.getChildViewHolder(interestsGroupView) as BroadcastGroupHolder).expand()
        }
    }

    private fun collapse() {
        interestsGroups?.indexOf(expandedInterestsGroup)?.let { index ->
            if (index < 0) return
            val interestsCount = getInterestsCount(expandedInterestsGroup)
            expandedInterestsGroup = null
            notifyItemRangeRemoved(index + 1, interestsCount)
            (recyclerView!!.getChildViewHolder(expandedInterestsGroupView!!) as BroadcastGroupHolder).collapse()
            expandedInterestsGroupView = null
        }
    }
    //endregion

    //region InterestsGroupListener Implementation
    override fun onInterestsGroupClick(groupView: View, adapterPosition: Int) {
        getInterestsGroup(adapterPosition)?.let {
            if (it === expandedInterestsGroup) collapse()
            else expand(groupView, it)
        }
    }

    override fun onInterestsGroupCheckedChanged(adapterPosition: Int, isChecked: Boolean) {
        getInterestsGroup(adapterPosition)?.let {
            onCheckedChangeListener?.onInterestsGroupChecked(it, isChecked)
            it.isSubscribed = isChecked
            if (it === expandedInterestsGroup)
                notifyItemRangeChanged(adapterPosition + 1, getInterestsCount(it))
        }
    }
    //endregion

    //region InterestListener Implementation
    override fun onInterestClick(adapterPosition: Int) {
        onInterestClickListener?.let {
            expandedInterestsGroup?.let { expanded ->
                getInterest(adapterPosition)?.let { interest ->
                    it.onInterestClick(expanded, interest)
                }
            }
        }
    }

    override fun onInterestCheckedChanged(adapterPosition: Int, isChecked: Boolean) {
        getInterest(adapterPosition)?.let {
            onCheckedChangeListener?.onInterestChecked(it, isChecked)
            it.asInterest?.isSubscribed = isChecked
            (recyclerView?.getChildViewHolder(expandedInterestsGroupView) as BroadcastGroupHolder).let {
                it.configureIcbSubscribed(expandedInterestsGroup?.subscribedState)
            }
        }
    }
    //endregion

    //region OnInterestClickListener
    private var onInterestClickListener: OnInterestClickListener? = null

    fun setOnInterestClickListener(listener: OnInterestClickListener) {
        onInterestClickListener = listener
    }

    interface OnInterestClickListener {
        fun onInterestClick(interestsGroup: InterestsGroup, stream: Stream)
    }
    //endregion

    //region OnCheckedChangeListener
    private var onCheckedChangeListener: OnCheckedChangedListener? = null

    fun setOnCheckedChangedListener(listener: OnCheckedChangedListener) {
        onCheckedChangeListener = listener
    }

    interface OnCheckedChangedListener {
        fun onInterestsGroupChecked(interestsGroup: InterestsGroup, isChecked: Boolean)
        fun onInterestChecked(stream: Stream, isChecked: Boolean)
    }
    //endregion
}
