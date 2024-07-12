package com.baek.voice.common

inline fun <T> List<T>.findIndexed(predicate: (Int, T) -> Boolean): T? {
    for (index in indices) {
        val element = get(index)
        if (predicate(index, element)) {
            return element
        }
    }
    return null
}