package com.loc.composethings

import androidx.recyclerview.widget.RecyclerView.ViewHolder

fun validateIndices(itemsSize: Int, from: Int?, to: Int?, onValid: (from: Int, to: Int) -> Unit) {
    if (
        from != null && to != null &&
        from < itemsSize && to < itemsSize
    ) {
        onValid(from, to)
    }
}

fun animateViewScale(viewHolder: ViewHolder?, scale: Float, animationDuration: Long) {
    viewHolder?.itemView?.animate()?.scaleX(scale)?.scaleY(scale)?.setDuration(animationDuration)
}