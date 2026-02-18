package com.example.quanlylichhoc.ui.custom

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.FrameLayout
import com.example.quanlylichhoc.databinding.ItemFeatureCardBinding

class FeatureCardView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {

    private val binding: ItemFeatureCardBinding

    init {
        binding = ItemFeatureCardBinding.inflate(LayoutInflater.from(context), this, true)
    }

    fun setData(title: String, iconResId: Int) {
        binding.tvTitle.text = title
        binding.imgIcon.setImageResource(iconResId)
    }
}
