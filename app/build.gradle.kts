import java.util.Properties
plugins {
    jacoco
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    // KSP: genera el código de Room (DAO, base de datos) al compilar.
    alias(libs.plugins.ksp)
}

// Datos de firma opcionales: archivo "keystore.properties" en la raíz del proyecto (ver
// keystore.properties.example). Está en .gitignore: nunca se sube al repositorio.
val propiedadesDeFirma = Properties().apply {
    val archivo = rootProject.file("keystore.properties")
    if (archivo.isFile) archivo.inputStream().use { load(it) }
}

/**
 * Lee un dato de firma, en este orden: gradle.properties (del usuario), variable de entorno o
 * keystore.properties. Si no existe devuelve null: el repositorio no contiene ninguna clave.
 */
fun datoDeFirma(nombre: String): String? =
    (project.findProperty(nombre) as String?)?.takeIf { it.isNotBlank() }
        ?: System.getenv(nombre)?.takeIf { it.isNotBlank() }
        ?: propiedadesDeFirma.getProperty(nombre)?.takeIf { it.isNotBlank() }

val firmaArchivo = datoDeFirma("MPAZPRO_STORE_FILE")
val firmaClaveAlmacen = datoDeFirma("MPAZPRO_STORE_PASSWORD")
val firmaAlias = datoDeFirma("MPAZPRO_KEY_ALIAS")
val firmaClaveLlave = datoDeFirma("MPAZPRO_KEY_PASSWORD")
/**
 * URL base del servidor (API_BASE_URL en gradle.properties). Es solo el valor inicial: en la app se
 * puede cambiar desde la pantalla "Servidor" del login sin recompilar.
 */
val apiBaseUrl: String = ((project.findProperty("API_BASE_URL") as String?)?.trim())
    ?.takeIf { it.isNotEmpty() } ?: "http://10.0.2.2:3000/"

/**
 * Experiencia 3. USAR_API solo define el valor INICIAL del interruptor "Modo local / Modo servidor"
 * (diálogo Servidor): false = arranca en modo local (solo Room, sin servidor); true = arranca en modo
 * servidor (API mediante la capa remota que se completa en los TODO(E3-n)). Después manda lo elegido
 * en el diálogo. Se cambia en gradle.properties o con -PUSAR_API=true.
 */
val usarApi: Boolean = (project.findProperty("USAR_API") as String?)?.trim()?.equals("true", ignoreCase = true) ?: false

val hayFirmaCompleta = listOf(firmaArchivo, firmaClaveAlmacen, firmaAlias, firmaClaveLlave).all { it != null }

android {
    namespace = "com.mpazpro3.app"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.mpazpro3.app"
        minSdk = 24
        targetSdk = 35
        versionCode = 1
        versionName = "1.0.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        buildConfigField("String", "API_BASE_URL", "\"${apiBaseUrl.replace("\\", "\\\\").replace("\"", "\\\"")}\"")
        buildConfigField("boolean", "USAR_API", usarApi.toString())
    }

    signingConfigs {
        // Solo se crea si los cuatro datos de firma están definidos; si no, el build release
        // se genera igual pero SIN firmar (APK "unsigned").
        if (hayFirmaCompleta) {
            create("release") {
                storeFile = file(firmaArchivo!!)
                storePassword = firmaClaveAlmacen
                keyAlias = firmaAlias
                keyPassword = firmaClaveLlave
                // Firma por JAR (V1) y por APK (V2): se piden ambas en la verificación de la Parte 6.
                enableV1Signing = true
                enableV2Signing = true
            }
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            if (hayFirmaCompleta) {
                signingConfig = signingConfigs.getByName("release")
            }
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }

    kotlinOptions {
        jvmTarget = "11"
    }

    buildFeatures {
        compose = true
        buildConfig = true // necesario para BuildConfig.API_BASE_URL y BuildConfig.USAR_API
    }
}

dependencies {
    // --- Navegación entre pantallas ---
    // navigation-compose 2.7.7 ya incluye el runtime de navegación. NO agregar
    // "navigation-runtime-android": esa coordenada no existe en la versión 2.7.7.
    implementation(libs.androidx.navigation.compose)

    // --- Íconos (flecha atrás, mostrar/ocultar clave, compartir, etc.) ---
    implementation(libs.androidx.material.icons.extended)

    // --- Persistencia local: Room (room-ktx aporta withTransaction y Flow) ---
    implementation(libs.androidx.room.runtime)
    implementation(libs.androidx.room.ktx)
    ksp(libs.androidx.room.compiler)

    // --- Experiencia 3: capa remota (Retrofit + OkHttp + Gson) ---
    implementation(libs.retrofit)
    implementation(libs.retrofit.converter.gson)
    implementation(libs.okhttp)
    implementation(libs.gson)
    implementation(libs.kotlinx.coroutines.android)
    // El registro de peticiones solo existe en debug (el código está en src/debug).
    debugImplementation(libs.okhttp.logging)

    // --- Dependencias estándar del proyecto (template de Android Studio) ---
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)

    // --- Pruebas unitarias (JVM, sin emulador) ---
    testImplementation(libs.junit)
    testImplementation(libs.okhttp.mockwebserver)
    testImplementation(libs.kotlinx.coroutines.test)
    testImplementation(libs.mockk)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    androidTestImplementation(libs.mockk.android)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)
}

// Informe de cobertura JVM de la lógica local; no incluye los TODO de la API postergada.
tasks.register<org.gradle.testing.jacoco.tasks.JacocoReport>("coberturaLocal") {
    dependsOn("testDebugUnitTest")
    executionData(fileTree(layout.buildDirectory.get().asFile) { include("**/testDebugUnitTest.exec", "**/testDebugUnitTest.ec") })
    classDirectories.setFrom(fileTree(layout.buildDirectory.dir("tmp/kotlin-classes/debug")) {
        include("com/mpazpro3/app/data/RepositorioLocal*.class")
    })
    sourceDirectories.setFrom(files("src/main/java"))
    reports { html.required.set(true); xml.required.set(true) }
}
