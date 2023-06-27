package com.talhakumru.marvelcomicsapp

import com.google.gson.Gson
import com.talhakumru.marvelcomicsapp.marvel_data.CharacterDataWrapper
import okhttp3.Call
import okhttp3.Callback
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import java.io.IOException
import java.math.BigInteger
import java.net.URL
import java.net.UnknownHostException
import java.security.MessageDigest

// This class contains methods for using Marvel API
class MarvelAPIController {
    private val baseUrl = "http://gateway.marvel.com/v1/public"
    private val pubK = "2c0eb6a33651d7791e7da24edce19abe"
    private val privateK = "8c2f56fd47a13acb17ee127e59ebcc54552a04a7"
    private val httpClient = OkHttpClient()

    companion object {
        val dataWrapper = CharacterDataWrapper()
        var listSize = 0
    }

    // download from Marvel and deserialize the data
    fun getData(filter : String, minNumberOfFirstFetch : Int) : Boolean {
        // println("entered getData")
        val url : URL = createRequestURL(filter)
        asyncGet(url, object: Callback {
            override fun onFailure(call: Call, e: IOException) {
                if (e is UnknownHostException) println("Connection cannot be established. Please reconnect and restart the app")
            }

            override fun onResponse(call: Call, response: Response) {
                response.use { res ->
                    if (res.code in arrayOf(401, 405, 409)) throw IOException("Error code ${res.code} while fetching data")
                    val gson = Gson()
                    val json = res.body.source().inputStream().reader()
                    val newData : CharacterDataWrapper = gson.fromJson(json, CharacterDataWrapper::class.java)
                    println(listSize)
                    if (newData.data.results.isEmpty())
                        // no new data is downloaded with the last request
                        return
                    dataWrapper.append(newData)
                    listSize = dataWrapper.data.results.size
                    if (listSize <= minNumberOfFirstFetch) {
                        // at least 4 pages of data must be downloaded at the start of the application
                        println("Screen is too big")
                        getData("${filter}offset=${listSize}&", minNumberOfFirstFetch)
                    }
                }
            }
        })
        // println("exited getData")
        return true
    }

    // fetches data from internet
    fun asyncGet(url : URL, callback : Callback) : Call {
        // println("entered asyncGet")
        val request = Request.Builder()
            .url(url)
            .get()
            .build()
        val call : Call = httpClient.newCall(request)
        call.enqueue(callback)
        return call
    }

    fun createRequestURL(filters: String) : URL {
        // ts is the timestamp in seconds that is needed by Marvel API to evaluate the URL
        val ts = System.currentTimeMillis() / 1000
        val hash = createHash(ts)
        return URL(baseUrl+"/characters${filters}ts=${ts}&apikey=${pubK}&hash=${hash}")
    }

    // hash is md5 digest of (ts+privateKey+publicKey)
    fun createHash(ts : Long) : String {
        val bigInt = BigInteger(1, MessageDigest.getInstance("MD5").digest((ts.toString()+privateK+pubK).toByteArray(Charsets.UTF_8)))
        val hash = String.format("%032x", bigInt)
        // println("ts:$ts - hash:$hash")
        return hash
    }

    //
    // deprecated methods
    /*
    fun getCharacters(filters : String) {
        println("getCharacters entered")

        val url = createRequestURL(filters)
        println("URL: $url")

        asyncGet(url, object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                e.printStackTrace()
            }

            override fun onResponse(call: Call, response: Response) {
                response.use {
                    if (!it.isSuccessful) throw IOException("Unexpected code $it")
                    val time = measureTimeMillis {
                        val reader = it.body!!.source().inputStream().reader()
                        val jsonReader = JsonReader(reader)
                        alignReader(jsonReader)
                        println("-----array başı-----")
                        try {
                            while (jsonReader.hasNext()) {
                                val character = Character_old()
                                jsonReader.beginObject()
                                while (jsonReader.hasNext()) {
                                    val name = jsonReader.nextName()
                                    if (name.equals("id")) character.id = jsonReader.nextInt()
                                    else if (name.equals("name")) character.name = jsonReader.nextString()
                                    else if (name.equals("series")) {
                                        jsonReader.beginObject()
                                        while (jsonReader.hasNext()) {
                                            if (jsonReader.nextName().equals("available")) character.numOfSeries = jsonReader.nextInt()
                                            else jsonReader.skipValue()
                                        }
                                        jsonReader.endObject()
                                    }
                                    else if (name.equals("thumbnail")) {
                                        jsonReader.beginObject()
                                        jsonReader.skipValue()
                                        val path = jsonReader.nextString()
                                        jsonReader.skipValue()
                                        val ext = jsonReader.nextString()
                                        jsonReader.endObject()

                                        var byteImage : ByteArray? = null
                                        try {
                                            val imageURL = URL("${path}.${ext}")
                                            val image = BitmapFactory.decodeStream(imageURL.openStream())
                                            val ostream = ByteArrayOutputStream()
                                            image.compress(Bitmap.CompressFormat.JPEG, 50, ostream)
                                            byteImage = ostream.toByteArray()
                                        } catch (e : Exception) {
                                            e.printStackTrace()
                                        } finally {
                                            character.image = byteImage
                                        }
                                    }
                                    else {
                                        jsonReader.skipValue()
                                    }
                                }
                                jsonReader.endObject()
                                list.add(character)
                                println("LIST SIZE: ${list.size}")
                            }
                        } catch (e : Exception) {
                            e.printStackTrace()
                        }
                        println("-----array sonu-----")
                        jsonReader.close()
                    }
                    it.close()
                    println("Compilation took $time ms")
                }
            }

        })
        println("getCharacters exited")
    }
*/
/*
    fun alignReader(jsonReader: JsonReader) {
        try {
            jsonReader.beginObject()
            while (jsonReader.hasNext()) {
                if (jsonReader.nextName().equals("data")) {
                    jsonReader.beginObject()
                    while (jsonReader.hasNext()) {
                        if (jsonReader.nextName().equals("results")) {
                            jsonReader.beginArray()
                            return
                        } else jsonReader.skipValue()
                    }
                    throw Exception("what is this madness, this is illegal")
                } else jsonReader.skipValue()
            }
            jsonReader.endObject()
        } catch (e : Exception) {
            e.printStackTrace()
        }
    }
*/
}