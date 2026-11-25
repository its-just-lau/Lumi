package com.lumi.app.testutil

import com.lumi.app.R
import com.lumi.app.core.i18n.AppText
import java.io.File
import javax.xml.parsers.DocumentBuilderFactory

/**
 * Resuelve los textos de `values/strings.xml` (español) en pruebas unitarias,
 * donde no hay contexto de Android.
 */
object TestStrings {

    private val byId: Map<Int, String> by lazy {
        val file = listOf(
            File("src/main/res/values/strings.xml"),
            File("app/src/main/res/values/strings.xml")
        ).first { it.exists() }

        val document = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(file)
        val nodes = document.getElementsByTagName("string")
        val values = HashMap<String, String>()
        for (i in 0 until nodes.length) {
            val node = nodes.item(i)
            val name = node.attributes.getNamedItem("name").nodeValue
            values[name] = unescape(node.textContent)
        }

        R.string::class.java.fields.mapNotNull { field ->
            values[field.name]?.let { field.getInt(null) to it }
        }.toMap()
    }

    fun install() {
        AppText.testResolver = { id, args ->
            val template = byId[id] ?: "string#$id"
            if (args.isEmpty()) template else String.format(template, *args)
        }
    }

    private fun unescape(raw: String): String {
        var text = raw
        if (text.length >= 2 && text.startsWith("\"") && text.endsWith("\"")) {
            text = text.substring(1, text.length - 1)
        }
        return text
            .replace("\\n", "\n")
            .replace("\\'", "'")
            .replace("\\\"", "\"")
    }
}
