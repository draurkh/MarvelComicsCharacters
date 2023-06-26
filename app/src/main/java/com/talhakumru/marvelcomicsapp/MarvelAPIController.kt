package com.talhakumru.marvelcomicsapp

import com.google.gson.Gson
import com.talhakumru.marvelcomicsapp.marvel_data.CharacterDataWrapper
import okhttp3.Call
import okhttp3.Callback
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import java.io.FileInputStream
import java.io.IOException
import java.math.BigInteger
import java.net.URL
import java.security.MessageDigest


class MarvelAPIController {
    val baseUrl = "http://gateway.marvel.com/v1/public"
    val publicKey = "2c0eb6a33651d7791e7da24edce19abe"
    val privateKey = "8c2f56fd47a13acb17ee127e59ebcc54552a04a7"
    var minNumber = 0
    val httpClient = OkHttpClient()

    companion object {
        val dataWrapper = CharacterDataWrapper()
        var listSize = 0
    }

    fun getGson(filter : String) : Boolean {
        println("getGson entered")

        val url : URL = createRequestURL(filter)
        asyncGet(url, object: Callback {
            override fun onFailure(call: Call, e: IOException) {
                e.printStackTrace()
            }

            override fun onResponse(call: Call, response: Response) {
                response.use { res ->
                    if (res.code in arrayOf(401, 405, 409)) throw IOException("Error code ${res.code} while fetching data")
                    val gson = Gson()
                    val json = res.body.source().inputStream().reader()
                    println(json)
                    val newData : CharacterDataWrapper = gson.fromJson(json, CharacterDataWrapper::class.java)
                    if (newData.data.results.isEmpty()) return
                    dataWrapper.append(newData)
                    if (dataWrapper.data.results.size <= minNumber) {
                        println("Screen is too big")
                        getGson("${filter}offset=${dataWrapper.data.results.size}&")
                    }
                }
            }
        })
        println("getGson exited")
        return true
    }

    fun asyncGet(url : URL, callback : Callback) : Call {
        println("get entered")
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
        return URL(baseUrl+"/characters${filters}ts=${ts}&apikey=${publicKey}&hash=${hash}")
    }

    // hash is md5 digest of (ts+privateKey+publicKey)
    fun createHash(ts : Long) : String {
        val bigInt = BigInteger(1, MessageDigest.getInstance("MD5").digest((ts.toString()+privateKey+publicKey).toByteArray(Charsets.UTF_8)))
        val hash = String.format("%032x", bigInt)
        println("ts:$ts - hash:$hash")
        return hash
    }

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
    fun get(url : URL) : Response? = runBlocking {
        println("get entered")
        val request = Request.Builder()
            .url(url)
            .get()
            .build()
        var response : Response? = null
        val responseTask = launch(Dispatchers.IO) {
            response = httpClient.newCall(request).execute()
        }
        responseTask.join()
        response
    }

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