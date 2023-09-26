package com.example.asynctask

import android.content.Context
import android.content.Intent
import android.media.session.MediaSession
import android.os.AsyncTask
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.Menu
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.core.content.ContextCompat.startActivity
import com.example.asynctask.api.baseApi
import org.json.JSONException
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL

@Suppress("DEPRECATION")
class MainActivity : AppCompatActivity() {
    lateinit var email: EditText
    lateinit var password: EditText
    lateinit var button: Button
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        email = findViewById(R.id.editTextEmail)
        password = findViewById(R.id.editTextPassword)
        button = findViewById(R.id.buttonLogin)

        button.setOnClickListener {
            val emailText = email.text.toString().trim()
            val passwordText = password.text.toString().trim()

            if (emailText.isEmpty() || passwordText.isEmpty()) {
                email.error = "Please Input Your Email"
                password.error = "Please Input Your password"
                Toast.makeText(applicationContext, "Tolong isi Email dan Password Anda", Toast.LENGTH_SHORT).show()
            } else {
                getToken(this, emailText, passwordText).execute()
            }
        }
    }

    private inner class getToken(val context: Context, val email: String, val password: String) :
        AsyncTask<Void, Void, String>() {

        override fun doInBackground(vararg p0: Void?): String {
            var result = ""
            val jsonObject = JSONObject().apply {
                put("email", email)
                put("password", password)
        }
            val jsonObjectString = jsonObject.toString()
            var httpURLConnection: HttpURLConnection? = null

            try {
                val url = URL(baseApi.baseurl + "Api/Auth")
                httpURLConnection = url.openConnection() as HttpURLConnection
                httpURLConnection.requestMethod = "POST"
                httpURLConnection.setRequestProperty("Content-Type", "application/json")
                httpURLConnection.connect()

                val outputWriter = OutputStreamWriter(httpURLConnection.outputStream)
                outputWriter.write(jsonObjectString)
                outputWriter.flush()

                val inputStreamReader = httpURLConnection.inputStream.reader()
                result = inputStreamReader.readText()

                if (httpURLConnection.responseCode == HttpURLConnection.HTTP_BAD_REQUEST) {
                    Handler(context.mainLooper).post {
                        Toast.makeText(
                            context,
                            "Password/Username yang anda masukkan salah",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                httpURLConnection?.disconnect()
            }

            return result
        }

        override fun onPostExecute(result: String?) {
            super.onPostExecute(result)
            if (result.isNullOrEmpty()) {
                return
            }

            try {
                val jsonObject = JSONObject(result)
                val token = jsonObject.getString("token")
                startActivity(Intent(context, testing::class.java))
            } catch (e: JSONException) {
                e.printStackTrace()
                Toast.makeText(context, "Error parsing JSON", Toast.LENGTH_SHORT).show()
            }
        }
    }
}