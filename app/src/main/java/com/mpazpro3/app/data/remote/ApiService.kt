package com.mpazpro3.app.data.remote

import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query

/**
 * Todas las rutas del backend (ver backend/docs/API.md). Son funciones suspend: las respuestas
 * no exitosas lanzan HttpException, que ErroresApi traduce a un mensaje para el usuario.
 */
interface ApiService {

    // ---- Estado y acceso (RF-00) ----
    @GET("health")
    suspend fun salud(): SaludDto

    @POST("auth/login")
    suspend fun login(@Body solicitud: LoginRequest): LoginResponse

    /** Recibe el encabezado completo ("Bearer <token>") porque la app ya olvidó el token al cerrar sesión. */
    @POST("auth/logout")
    suspend fun logout(@Header("Authorization") autorizacion: String): Response<Unit>

    // ---- Contenidos (RF-01, RF-02, RF-03) ----
    @GET("contenidos")
    suspend fun contenidos(
        @Query("asignatura") asignatura: String?,
        @Query("estado") estado: String?
    ): List<ContenidoDto>

    @GET("contenidos/{id}")
    suspend fun contenido(@Path("id") id: String): ContenidoDto

    @POST("contenidos/{id}/aprobar")
    suspend fun aprobarContenido(@Path("id") id: String, @Body solicitud: ObservacionRequest): ContenidoResponse

    @POST("contenidos/{id}/rechazar")
    suspend fun rechazarContenido(@Path("id") id: String, @Body solicitud: ObservacionRequest): ContenidoResponse

    // ---- Recursos y actividades (RF-07, RF-08, RF-09) ----
    @GET("recursos")
    suspend fun recursos(@Query("contenidoId") contenidoId: String?): List<RecursoDto>

    @GET("actividades")
    suspend fun actividades(
        @Query("contenidoId") contenidoId: String?,
        @Query("tipo") tipo: String?
    ): List<ActividadDto>

    // ---- Planificación (RF-04, RF-05) ----
    @GET("asignaciones")
    suspend fun asignaciones(@Query("curso") curso: String?): List<AsignacionDto>

    @PUT("cursos/{curso}/asignaciones")
    suspend fun asignar(@Path("curso") curso: String, @Body solicitud: AsignarRequest): AsignarResponse

    // ---- Estudiantes y seguimiento (RF-06, RF-10, RF-11) ----
    @GET("estudiantes")
    suspend fun estudiantes(@Query("curso") curso: String?): List<EstudianteDto>

    @GET("seguimientos")
    suspend fun seguimientos(
        @Query("estudianteId") estudianteId: String?,
        @Query("contenidoId") contenidoId: String?
    ): List<SeguimientoDto>

    @POST("estudiantes/{estudianteId}/contenidos/{contenidoId}/material-revisado")
    suspend fun materialRevisado(
        @Path("estudianteId") estudianteId: String,
        @Path("contenidoId") contenidoId: String
    ): MaterialResponse

    @POST("estudiantes/{estudianteId}/actividades/{actividadId}/respuestas")
    suspend fun responderDesafio(
        @Path("estudianteId") estudianteId: String,
        @Path("actividadId") actividadId: String,
        @Body solicitud: RespuestaRequest
    ): DesafioResponse

    @POST("estudiantes/{estudianteId}/contenidos/{contenidoId}/evaluacion")
    suspend fun enviarEvaluacion(
        @Path("estudianteId") estudianteId: String,
        @Path("contenidoId") contenidoId: String,
        @Body solicitud: EvaluacionRequest
    ): EvaluacionResponse

    // ---- Resultados del curso (RF-06) ----
    @GET("cursos/{curso}/resultados.csv")
    suspend fun resultadosCsv(@Path("curso") curso: String): ResponseBody

    // ---- Informe pedagógico (RF-12, RF-13, RF-14) ----
    @GET("informes")
    suspend fun informes(@Query("estudianteId") estudianteId: String?): List<InformeDto>

    @POST("estudiantes/{estudianteId}/informe")
    suspend fun generarInforme(@Path("estudianteId") estudianteId: String): InformeResponse

    @POST("informes/{id}/validar")
    suspend fun validarInforme(@Path("id") id: String): InformeResponse
}
