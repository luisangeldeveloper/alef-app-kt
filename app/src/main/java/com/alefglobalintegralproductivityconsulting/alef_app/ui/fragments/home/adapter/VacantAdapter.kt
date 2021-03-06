package com.alefglobalintegralproductivityconsulting.alef_app.ui.fragments.home.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.alefglobalintegralproductivityconsulting.alef_app.R
import com.alefglobalintegralproductivityconsulting.alef_app.core.BaseViewHolder
import com.alefglobalintegralproductivityconsulting.alef_app.core.utils.Timestamp
import com.alefglobalintegralproductivityconsulting.alef_app.data.model.Vacant
import com.alefglobalintegralproductivityconsulting.alef_app.data.model.VacantInfoExtra
import com.alefglobalintegralproductivityconsulting.alef_app.databinding.ItemVacantBinding
import java.util.ArrayList

class VacantAdapter(
    private var vacantList: List<Vacant>,
    private val itemClickListener: OnVacantClickListener
) : RecyclerView.Adapter<BaseViewHolder<*>>() {

    interface OnVacantClickListener {
        fun onVacantClick(vacant: Vacant, vacantInfoExtra: VacantInfoExtra?)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BaseViewHolder<*> {
        val itemBinding = ItemVacantBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        val holder = VacantViewHolder(itemBinding, parent.context)

        itemBinding.cvVacant.setOnClickListener {
            val position =
                holder.bindingAdapterPosition.takeIf { it != DiffUtil.DiffResult.NO_POSITION }
                    ?: return@setOnClickListener
            itemClickListener.onVacantClick(vacantList[position], vacantList[position].vacantInfoExtra)
        }

        return holder
    }

    override fun onBindViewHolder(holder: BaseViewHolder<*>, position: Int) {
        when (holder) {
            is VacantViewHolder -> holder.bind(vacantList[position])
        }
    }

    override fun getItemCount(): Int = vacantList.size

    fun setFilter(newWords: ArrayList<Vacant>) {
        this.vacantList = newWords
        notifyDataSetChanged()
    }

    private inner class VacantViewHolder(
        val binding: ItemVacantBinding,
        val context: Context
    ) : BaseViewHolder<Vacant>(binding.root) {
        override fun bind(item: Vacant) {
            with(binding) {
                addVancantTimestamp(item)

                tvLocation.text = item.location
                tvTitleVacant.text = item.title
                tvCompany.text = item.company
                tvDescription.text = item.description

                if ((item.firstSalary != -1 || item.secondSalary != -1) && item.paymentMethod.isNotEmpty()) {
                    llSalary.visibility = View.VISIBLE

                    if (item.firstSalary != -1 && item.secondSalary != -1) {
                        tvSalary.text = "$${item.firstSalary} - $${item.secondSalary} MXN"
                    } else if (item.firstSalary != -1) {
                        tvSalary.text = "$${item.firstSalary} MXN"
                    } else if (item.secondSalary != -1) {
                        tvSalary.text = "$${item.secondSalary} MXN"
                    }

                    tvPayments.text = item.paymentMethod
                } else {
                    llSalary.visibility = View.GONE
                }

                cbFavorite.isChecked = item.isFavorite
                cbFavorite.setOnCheckedChangeListener { _, checked ->
                    if (checked) {
                        Toast.makeText(context, "Agregado a favoritos", Toast.LENGTH_SHORT).show()
                    }
                }

                if (item.vacantInfoExtra!!.companyPaid) {
                    flCompanyPaid.setBackgroundColor(ContextCompat.getColor(context, R.color.purple_700))
                } else {
                    flCompanyPaid.setBackgroundColor(ContextCompat.getColor(context, R.color.white_grey))
                }
            }
//            Glide.with(context)
//                .load("https://image.tmdb.org/t/p/w500/${item.poster_path}")
//                .centerCrop()
//                .into(binding.imageViewMovie)

        }

        private fun addVancantTimestamp(vacant: Vacant) {
            with(binding) {
                val createdAt = (vacant.timestamp?.time?.div(1000L))?.let {
                    Timestamp.getTimeAgo(it.toInt())
                }

                tvTimestamp.text = createdAt
            }
        }
    }

}