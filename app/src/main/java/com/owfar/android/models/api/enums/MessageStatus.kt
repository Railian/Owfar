package com.owfar.android.models.api.enums

enum class MessageStatus (val jsonName: String) {

    SENDING("sending"), FAILED("failed"), SENT("sent"), DELIVERED("delivered"), SEEN("seen");

    companion object {
        fun find(jsonName: String?): MessageStatus? = values().find { it.jsonName == jsonName }
    }
}
