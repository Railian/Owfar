package com.owfar.android.extensions

import android.text.SpannableStringBuilder
import android.text.Spanned
import android.text.style.StyleSpan
import java.util.*

fun spannable(init: SpanWithChildren.() -> Unit): SpanWithChildren {
    val spanWithChildren = SpanWithChildren()
    spanWithChildren.init()
    return spanWithChildren
}

interface Span {
    fun render(builder: SpannableStringBuilder)

    fun toCharSequence(): SpannableStringBuilder {
        val builder = SpannableStringBuilder()
        render(builder)
        return builder
    }
}

class SpanWithChildren(val what: Any? = null) : Span {
    val children = ArrayList<Span>()

    fun typeface(typeface: Int, init: SpanWithChildren.() -> Unit): SpanWithChildren =
            span(StyleSpan(typeface), init)

    fun span(what: Any, init: SpanWithChildren.() -> Unit): SpanWithChildren {
        val child = SpanWithChildren(what)
        child.init()
        children.add(child)
        return this
    }

    operator fun String.plus(c: CharSequence) {
        children.add(SpanWithText(this))
    }

    override fun render(builder: SpannableStringBuilder) {
        val start = builder.length

        for (c in children) {
            c.render(builder)
        }

        if (what != null) {
            builder.setSpan(what, start, builder.length, Spanned.SPAN_INCLUSIVE_EXCLUSIVE)
        }
    }
}

class SpanWithText(val content: String) : Span {
    override fun render(builder: SpannableStringBuilder) {
        builder.append(content)
    }
}