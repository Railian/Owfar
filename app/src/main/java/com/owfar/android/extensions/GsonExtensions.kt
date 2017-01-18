package com.owfar.android.extensions

import com.google.gson.JsonElement

val JsonElement.asStringOrNull: String? get() = if (this.isJsonNull) null else asString