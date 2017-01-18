package com.owfar.android.extensions

import io.realm.Realm
import io.realm.RealmObject

fun <E : RealmObject> generateAutoIncrementedId(realm: Realm, clazz: Class<E>, idFieldName: String): Long {
    val maxId = realm.where(clazz).max(idFieldName)
    return if (maxId == null) 0L else maxId.toLong() + 1L
}