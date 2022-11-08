package cat.copernic.fpshare.ui.activities

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
import cat.copernic.fpshare.R
import com.google.android.material.snackbar.BaseTransientBottomBar
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

class Register : AppCompatActivity() {

    private lateinit var btnRegistrarse: Button
    private lateinit var InputNombre: EditText
    private lateinit var InputPassword: EditText
    private lateinit var InputMail: EditText
    private lateinit var auth: FirebaseAuth


    override fun onCreate(savedInstanceState: Bundle?) {
        this.supportActionBar!!.hide()
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_registro)

        //Inicializacion de componentes
        InputNombre = findViewById(R.id.InputNombre)
        InputPassword = findViewById(R.id.InputPassword)
        InputMail = findViewById(R.id.InputMail)
        btnRegistrarse = findViewById(R.id.btnRegistrarse)


        //Inicializacion Firebase
        auth = Firebase.auth

        btnRegistrarse.setOnClickListener() {
            val nombre = InputNombre.text.toString()
            val password = InputPassword.text.toString()
            val mail = InputMail.text.toString()

            if (campoVacio(nombre, password, mail)) {
                registrar(password, mail)
            }

        }


    }

    fun campoVacio(nombre: String, password: String, mail: String): Boolean {
        return nombre.isNotEmpty() && password.isNotEmpty() && mail.isNotEmpty()
                && nombre.isNotBlank() && password.isNotBlank() && mail.isNotBlank()
    }

    fun registrar(password: String, mail: String) {

        auth.createUserWithEmailAndPassword(mail, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    startActivity(Intent(this, MainActivity::class.java))
                    finish()
                } else {
                    error()
                }
            }
    }

    fun error() {
        Snackbar.make(
            findViewById(R.id.registroLayout),
            "Register failed",
            BaseTransientBottomBar.LENGTH_SHORT
        ).show()
    }

}