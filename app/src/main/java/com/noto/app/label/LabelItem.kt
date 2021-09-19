package com.noto.app.label

import android.annotation.SuppressLint
import android.view.View
import com.airbnb.epoxy.EpoxyAttribute
import com.airbnb.epoxy.EpoxyHolder
import com.airbnb.epoxy.EpoxyModelClass
import com.airbnb.epoxy.EpoxyModelWithHolder
import com.noto.app.R
import com.noto.app.databinding.LabelItemBinding
import com.noto.app.domain.model.Label
import com.noto.app.domain.model.NotoColor
import com.noto.app.util.*

@SuppressLint("NonConstantResourceId")
@EpoxyModelClass(layout = R.layout.label_item)
abstract class LabelItem : EpoxyModelWithHolder<LabelItem.Holder>() {

    @EpoxyAttribute
    lateinit var label: Label

    @EpoxyAttribute
    open var isSelected: Boolean = false

    @EpoxyAttribute
    lateinit var color: NotoColor

    @EpoxyAttribute(EpoxyAttribute.Option.DoNotHash)
    lateinit var onClickListener: View.OnClickListener

    @EpoxyAttribute(EpoxyAttribute.Option.DoNotHash)
    lateinit var onLongClickListener: View.OnLongClickListener

    override fun bind(holder: Holder) = with(holder.binding) {
        val resources = root.resources
        val resourceColor = resources.colorResource(color.toResource())
        val backgroundColor = resources.colorResource(R.color.colorBackground)
        tvLabel.text = label.title
        tvLabel.setOnClickListener(onClickListener)
        tvLabel.setOnLongClickListener(onLongClickListener)

        if (isSelected) {
            tvLabel.animateBackgroundColor(backgroundColor, resourceColor)
            tvLabel.animateTextColor(resourceColor, backgroundColor)
        } else {
            tvLabel.animateLabelColors(fromColor = resourceColor, toColor = backgroundColor)
            tvLabel.animateTextColor(backgroundColor, resourceColor)
        }
        Unit
    }

    class Holder : EpoxyHolder() {
        lateinit var binding: LabelItemBinding
            private set

        override fun bindView(itemView: View) {
            binding = LabelItemBinding.bind(itemView)
        }
    }
}