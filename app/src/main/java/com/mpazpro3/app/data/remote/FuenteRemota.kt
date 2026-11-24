package com.mpazpro3.app.data.remote

import com.mpazpro3.app.data.Instantanea
import com.mpazpro3.app.model.Perfil

/**
 * Fuente de datos remota: la única clase que habla con el ApiService. El repositorio le pide datos y
 * recibe siempre un ApiResult (nunca excepciones), ya con los textos de error en español.
 *
 * Se arma en AppContenedor y solo se usa en modo servidor (interruptor "Modo local" apagado).
 */
class FuenteRemota(
    private val api: () -> ApiService,
    private val conectividad: Conectividad,
    private val sesion: SesionApi
) {

    /**
     * TODO(E3-5a) Ejecuta una llamada a la API si hay red (y sesión, salvo en el login). Nunca lanza excepciones.
     *
     * Archivo: este. Qué hacer, en este orden:
     *  1. Si !conectividad.hayRed() -> ApiResult.SinConexion(ErroresApi.MSG_SIN_CONEXION) (sin intentar la llamada).
     *  2. Si [requiereSesion] y !sesion.activa -> ApiResult.SinConexion(ErroresApi.MSG_SIN_SESION_REMOTA).
     *  3. Si no, llamarApi { llamada(api()) }.
     * Aceptación: sin red, la llamada ni se intenta; con red y error 409 del servidor devuelve
     * ApiResult.Error(409, <mensaje del servidor>); en el login (requiereSesion = false) funciona sin token.
     */
    suspend fun <T> llamar(requiereSesion: Boolean = true, llamada: suspend (ApiService) -> T): ApiResult<T> =
        TODO("E3-5a: comprobar red y sesión y ejecutar la llamada con llamarApi")

    /**
     * TODO(E3-5b) Descarga todo lo que el servidor entrega al perfil y lo devuelve ya convertido al modelo.
     *
     * Qué hacer: con [llamar], pedir contenidos, recursos, actividades y asignaciones a todos los perfiles; estudiantes y
     * seguimientos solo si el perfil NO es UTP; informes solo si es DOCENTE (el servidor responde 403 si no).
     * Convertir cada lista con los mapeadores (E3-4) usando ApiResult.transformar y armar una Instantanea.
     * Las siete peticiones son independientes: se pueden lanzar en paralelo con coroutineScope { async { ... } }.
     * Si alguna falla, devolver ese fallo (SinConexion o Error) y NO una copia a medias.
     * Aceptación: para un docente se hacen 7 peticiones, para un estudiante 6 y para UTP 4; si /informes
     * responde 500 el resultado es Error y no se entrega ninguna lista.
     */
    suspend fun descargarInstantanea(perfil: Perfil): ApiResult<Instantanea> =
        TODO("E3-5b: descargar y convertir los datos del perfil")
}
