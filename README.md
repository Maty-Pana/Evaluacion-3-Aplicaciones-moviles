# Guía de configuración del proyecto Android

## 1. Descripción del proyecto

- **Nombre del proyecto:** `JaniSPAKotlinAPP`
- **Módulos:** `:app`
- **Lenguaje:** Kotlin
- **Tipo:** Android Application
- **Build system:** Gradle (Kotlin DSL – `build.gradle.kts`)

---

## 2. Estructura principal del proyecto

- **Raíz**
  - `settings.gradle.kts`
  - `build.gradle.kts`
  - `gradle.properties`
  - `gradlew` / `gradlew.bat`
  - `local.properties`
  - `gradle/` (incluye `libs.versions.toml`)

- **Módulo app**
  - `app/build.gradle.kts`
  - `app/src/main/AndroidManifest.xml`
  - `app/src/main/java/...`
  - `app/src/main/res/...`

---

## 3. Configuración de Gradle y plugins

### 3.1. `settings.gradle.kts`

- Repositorios:
  - `google()`
  - `mavenCentral()`
  - `gradlePluginPortal()`
- Nombre del proyecto:
  - `rootProject.name = "JaniSPAKotlinAPP"`
- Módulos:
  - `include(":app")`

### 3.2. `build.gradle.kts` (raíz)

- Uso de catálogo de versiones (`gradle/libs.versions.toml`).
- Plugins (mediante alias):
  - `libs.plugins.android.application`
  - `libs.plugins.kotlin.android`

### 3.3. `app/build.gradle.kts`

**Plugins**

- `alias(libs.plugins.android.application)`
- `alias(libs.plugins.kotlin.android)`
- `id("kotlin-kapt")`

**Bloque `android`**

- `namespace = "com.janispaxano.JaniSPAKotlinAPP"`
- `compileSdk = 36`  
  > Sugerencia: bajar a una API realmente instalada (p.ej. 34 o 35).
- `defaultConfig`:
  - `applicationId = "com.janispaxano.JaniSPAKotlinAPP"`
  - `minSdk = 24`
  - `targetSdk = 36` (recomendable alinear con la API instalada)
  - `versionCode = 1`
  - `versionName = "1.0"`
  - `testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"`
- `buildTypes`:
  - `release` con ProGuard y `minifyEnabled = false`
- `buildFeatures`:
  - `viewBinding = true`
- `compileOptions`:
  - `sourceCompatibility = JavaVersion.VERSION_11`
  - `targetCompatibility = JavaVersion.VERSION_11`
- `kotlinOptions`:
  - `jvmTarget = "11"`

---

## 4. Dependencias principales

### 4.1. AndroidX / UI

- `androidx.core:core-ktx:1.13.1`
- `androidx.appcompat:appcompat:1.7.0`
- `com.google.android.material:material:1.12.0`
- `androidx.constraintlayout:constraintlayout:2.1.4`
- `androidx.recyclerview:recyclerview:1.3.2`
- `androidx.activity:activity-ktx:1.9.3`
- `androidx.fragment:fragment-ktx:1.8.3`

### 4.2. Corrutinas

- `org.jetbrains.kotlinx:kotlinx-coroutines-android:1.9.0`

### 4.3. Red (API REST)

- `com.squareup.retrofit2:retrofit:2.11.0`
- `com.squareup.retrofit2:converter-gson:2.11.0`
- `com.squareup.okhttp3:okhttp:4.12.0`
- `com.squareup.okhttp3:logging-interceptor:4.12.0`

### 4.4. Imágenes

- `com.github.bumptech.glide:glide:4.16.0`
- `kapt("com.github.bumptech.glide:compiler:4.16.0")`

### 4.5. Testing

- `junit:junit` (unit tests)
- `androidx.test.ext:junit`
- `androidx.test.espresso:espresso-core`

---

## 5. Pasos de configuración del entorno

1. **Instalar Android Studio (recomendado)**
   - Descargar desde:  
     https://developer.android.com/studio

2. **Instalar / configurar el JDK 11**
   - El proyecto usa:
     - `sourceCompatibility = JavaVersion.VERSION_11`
     - `jvmTarget = "11"`
   - Opciones:
     - Usar el JDK que viene integrado con Android Studio (lo más simple).
     - O instalar un JDK 11 externo, por ejemplo Temurin:  
       https://adoptium.net/temurin/releases/?version=11

3. **Configurar el Android SDK**
   - Abrir **Android Studio**.
   - Ir a: `Tools > SDK Manager`.
   - Pestaña **SDK Platforms**:
     - Instalar la versión de Android que se usará como `compileSdk` y `targetSdk`.
     - Si `compileSdk = 36` aún no está disponible, se recomienda ajustar a una API estable (por ejemplo 34 o 35) en `app/build.gradle.kts`.
   - Pestaña **SDK Tools**:
     - Activar al menos:
       - Android SDK Build-Tools  
       - Android SDK Platform-Tools  
       - Android SDK Tools  
       - Google USB Driver (si se va a usar dispositivo físico en Windows)

4. **Verificar `local.properties`**
   - Android Studio genera este archivo automáticamente al abrir el proyecto.
   - Contenido típico en Windows:
     ```properties
     sdk.dir=C:\\Users\\TU_USUARIO\\AppData\\Local\\Android\\Sdk
     ```
   - Asegurarse de que la ruta exista y corresponda a la instalación real del SDK.

---

## 6. Cómo abrir, sincronizar y compilar el proyecto

### 6.1. Abrir el proyecto

1. Abrir **Android Studio**.
2. Seleccionar `File > Open...`.
3. Elegir la carpeta raíz del repositorio:
   - `C:\Users\Matias\Desktop\DuocUC\4to\Android_Moviles`
4. Android Studio detectará automáticamente el proyecto Gradle.

### 6.2. Sincronizar con Gradle

1. Una vez abierto el proyecto, Android Studio pedirá sincronizar Gradle.
2. También se puede hacer manualmente:
   - Ícono **Sync Project with Gradle Files** (elefante con flecha) en la barra de herramientas.
3. Esperar a que termine:
   - Si hay errores relacionados con la versión de SDK:
     - Abrir `app/build.gradle.kts`.
     - Ajustar `compileSdk` y `targetSdk` a una API que esté instalada.
     - Volver a ejecutar **Sync Project with Gradle Files**.

### 6.3. Compilar el proyecto (Build)

- Desde Android Studio:
  - Menú: `Build > Make Project`
- Desde la terminal (Windows, `cmd.exe`), dentro de la carpeta del proyecto:

  ```bat
  cd C:\Users\Matias\Desktop\DuocUC\4to\Android_Moviles

  :: Compilar APK de debug
  gradlew.bat assembleDebug

---

# Usuarios de prueba

- Admin:
  - usuario: `admin@gmail.com`
  - contraseña: `1234`
- Cliente:
  - usuario: `cliente@gmail.com`
  - contraseña: `1234`
 
# ¿Donde se guardan las imagenes en el backend?
- Las imagenes se guardan en la seccion de archivos publicos en la libreria de xano

<img width="1334" height="273" alt="image" src="https://github.com/user-attachments/assets/a1685f1c-efbd-4b63-9b0c-aea1e39c9087" />




