package com.example.dbtest

import android.util.Xml
import org.xmlpull.v1.XmlPullParser
import org.xmlpull.v1.XmlPullParserException
import java.io.IOException
import java.io.InputStream

private val ns: String? = null

data class Entry( val corp_code: String?, val corp_name: String?)

class StackOverflowXmlParser {

    @Throws(XmlPullParserException::class, IOException::class)
    fun parse(inputStream: InputStream): List<*> {
        inputStream.use { inputStream ->
            val parser: XmlPullParser = Xml.newPullParser()
            parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false)
            parser.setInput(inputStream, null)
            parser.nextTag()
            return readFeed(parser)
        }
    }

    @Throws(XmlPullParserException::class, IOException::class)
    private fun readFeed(parser: XmlPullParser): List<Entry> {
        val entries = mutableListOf<Entry>()

        parser.require(XmlPullParser.START_TAG, ns, "result")
        while (parser.next() != XmlPullParser.END_TAG) {
            if (parser.eventType != XmlPullParser.START_TAG) {
                continue
            }
            // Starts by looking for the list tag
            if (parser.name == "list") {
                entries.add(readEntry(parser))
            } else {
                skip(parser)
            }
        }
        return entries
    }

        @Throws(XmlPullParserException::class, IOException::class)
        private fun readEntry(parser: XmlPullParser): Entry {
            parser.require(XmlPullParser.START_TAG, ns, "list")
            var corp_code: String? = null
            var corp_name: String? = null

            while (parser.next() != XmlPullParser.END_TAG) {
                if (parser.eventType != XmlPullParser.START_TAG) {
                    continue
                }
                when (parser.name) {
                    "corp_code" -> corp_code = readCorpCode(parser)
                    "corp_name" -> corp_name = readCorpName(parser)
                    else -> skip(parser)
                }
            }
            return Entry(corp_code, corp_name)
        }

        // Processes title tags in the feed.
        @Throws(IOException::class, XmlPullParserException::class)
        private fun readCorpCode(parser: XmlPullParser): String {
            parser.require(XmlPullParser.START_TAG, ns, "corp_code")
            val corp_code = readText(parser)
            parser.require(XmlPullParser.END_TAG, ns, "corp_code")
            return corp_code
        }

        // Processes summary tags in the feed.
        @Throws(IOException::class, XmlPullParserException::class)
        private fun readCorpName(parser: XmlPullParser): String {
            parser.require(XmlPullParser.START_TAG, ns, "corp_name")
            val corp_name = readText(parser)
            parser.require(XmlPullParser.END_TAG, ns, "corp_name")
            return corp_name
        }

        // For the tags title and summary, extracts their text values.
        @Throws(IOException::class, XmlPullParserException::class)
        private fun readText(parser: XmlPullParser): String {
            var result = ""
            if (parser.next() == XmlPullParser.TEXT) {
                result = parser.text
                parser.nextTag()
            }
            return result
        }

        @Throws(XmlPullParserException::class, IOException::class)
        private fun skip(parser: XmlPullParser) {
            if (parser.eventType != XmlPullParser.START_TAG) {
                throw IllegalStateException()
            }
            var depth = 1
            while (depth != 0) {
                when (parser.next()) {
                    XmlPullParser.END_TAG -> depth--
                    XmlPullParser.START_TAG -> depth++
                }
            }
        }
    }