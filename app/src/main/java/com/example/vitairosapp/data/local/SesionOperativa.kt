package com.example.vitairosapp.data.local

import java.security.MessageDigest
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.PBEKeySpec

interface AlmacenSesion {
    var inicioSesionMillis: Long?
}

sealed class ResultadoInicioSesion {
    data object Correcto : ResultadoInicioSesion()
    data object CamposVacios : ResultadoInicioSesion()
    data object CredencialesIncorrectas : ResultadoInicioSesion()
}

class VerificadorCredencialesLocales {

    fun verificar(usuario: String, contrasena: String): Boolean {
        val usuarioValido = MessageDigest.isEqual(
            usuario.trim().lowercase().toByteArray(Charsets.UTF_8),
            USUARIO_DEMOSTRACION.toByteArray(Charsets.UTF_8)
        )
        val derivada = derivar(contrasena)
        val contrasenaValida = MessageDigest.isEqual(derivada, HUELLA_CONTRASENA)
        return usuarioValido && contrasenaValida
    }

    private fun derivar(contrasena: String): ByteArray {
        val especificacion = PBEKeySpec(contrasena.toCharArray(), SAL, ITERACIONES, LONGITUD_BITS)
        return try {
            SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256").generateSecret(especificacion).encoded
        } finally {
            especificacion.clearPassword()
        }
    }

    companion object {
        const val USUARIO_DEMOSTRACION = "operador"
        private const val ITERACIONES = 60_000
        private const val LONGITUD_BITS = 256
        private val SAL = "a3f91c7e5b2d4086917ce2b05d38f14a".hexABytes()
        private val HUELLA_CONTRASENA =
            "7ce12b38167f5f4ec0fcfe3ec420f0c1ed65c7ab44cdb9f1b850bcfa7c51f92d".hexABytes()

        private fun String.hexABytes(): ByteArray =
            ByteArray(length / 2) { substring(it * 2, it * 2 + 2).toInt(16).toByte() }
    }
}

class SesionOperativa(
    private val almacen: AlmacenSesion,
    private val verificador: VerificadorCredencialesLocales = VerificadorCredencialesLocales(),
    private val reloj: () -> Long = System::currentTimeMillis,
) {

    val activa: Boolean
        get() {
            val inicio = almacen.inicioSesionMillis ?: return false
            val transcurrido = reloj() - inicio
            if (transcurrido < 0 || transcurrido > DURACION_MAXIMA_MILLIS) {
                almacen.inicioSesionMillis = null
                return false
            }
            return true
        }

    fun iniciar(usuario: String, contrasena: String): ResultadoInicioSesion {
        if (usuario.isBlank() || contrasena.isEmpty()) return ResultadoInicioSesion.CamposVacios
        if (!verificador.verificar(usuario, contrasena)) return ResultadoInicioSesion.CredencialesIncorrectas
        almacen.inicioSesionMillis = reloj()
        return ResultadoInicioSesion.Correcto
    }

    fun cerrar() {
        almacen.inicioSesionMillis = null
    }

    companion object {
        const val DURACION_MAXIMA_MILLIS = 12L * 60 * 60 * 1000
    }
}
