package com.ackileo.telematics.data.remote

object ApiConstants {

    private fun normalizePath(path: String): String {
        val withLeadingSlash =
            if (path.startsWith("/")) path else "/$path"

        return if (withLeadingSlash.endsWith("/")) {
            withLeadingSlash
        } else {
            "$withLeadingSlash/"
        }
    }

    fun baseUrl(apiScheme: String, apiHost: String, apiBasePath: String): String {
        return "$apiScheme://" +
                apiHost +
                normalizePath(apiBasePath)
    }
}
