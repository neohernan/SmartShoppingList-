# Smart Shopping List

**Smart Shopping List** es una aplicacion Android nativa que te ayuda a planificar tus compras de manera inteligente. Puedes buscar productos con precios simulados de multiples supermercados, escanear codigos de barras con la camara, y generar listas de compras automaticamente usando inteligencia artificial a partir de una simple descripcion en lenguaje natural.

> Desarrollada con **Kotlin** y **Jetpack Compose**, siguiendo **Arquitectura Limpia** y las mejores practicas de Android moderno.

---

## Capturas de pantalla

| Pantalla | Vista previa |
|---|---|
| **Inicio** | `screenshots/home.png` |
| **Busqueda** | `screenshots/search.png` |
| **Escaner** | `screenshots/scanner.png` |
| **Listas** | `screenshots/lists.png` |
| **Generacion IA** | `screenshots/ai_generate.png` |

> Agrega las capturas en la carpeta `screenshots/` y actualiza las rutas.

---

## Funcionalidades

- **Busqueda de productos** — Consulta precios simulados en 4 supermercados: Coto, Disco, Vea y Carrefour.
- **Escaner de codigo de barras** — Usa ML Kit (Google) para escanear productos directamente con la camara, sin conexion a internet.
- **Generacion de listas con IA** — Describe lo que queres cocinar (ej: *"cena italiana para 4 personas"*) y la app genera automaticamente la lista de compras usando **OpenRouter**.
- **Inicio personalizado** — Sugerencias predictivas basadas en tu historial de busqueda.
- **Persistencia local** — Productos escaneados, listas e historial guardados con **Room** (SQLite).
- **Navegacion inferior** — Interfaz limpia con Bottom Navigation (Inicio, Buscar, Escaner).

---

## Tecnologias y herramientas

| Categoria | Tecnologia |
|---|---|
| **Lenguaje** | [Kotlin](https://kotlinlang.org/) |
| **UI** | [Jetpack Compose](https://developer.android.com/jetpack/compose), Material 3 |
| **Arquitectura** | MVVM + Clean Architecture |
| **Inyeccion de dependencias** | [Hilt](https://dagger.dev/hilt/) |
| **Base de datos local** | [Room](https://developer.android.com/training/data-storage/room) |
| **Escaner de barras** | [ML Kit Barcode Scanning](https://developers.google.com/ml-kit/vision/barcode-scanning) |
| **IA / LLM** | [OpenRouter API](https://openrouter.ai/) (modelo *meta-llama/llama-3.2-3b-instruct*) |
| **Networking** | [OkHttp](https://square.github.io/okhttp/) |
| **Serializacion JSON** | [Gson](https://github.com/google/gson) |
| **CI/CD** | [GitHub Actions](https://github.com/features/actions) |
| **Gradle** | Kotlin DSL + Version Catalog (`libs.versions.toml`) |

---

## Requisitos

- **Android Studio** Hedgehog (2023.1.1) o superior
- **JDK** 17+
- **Android SDK** 36 (compileSdk), minSdk 28
- **Gradle** 9.2.0
- **Kotlin** 2.2.21
- Dispositivo o emulador con **Android 9.0+** (API 28)

---

## Instalacion y ejecucion

### 1. Clonar el repositorio

```bash
git clone https://github.com/tu-usuario/smart-shopping-list.git
cd smart-shopping-list/MiAppDeCompras
```

### 2. Configurar las claves API

Crea o edita el archivo `local.properties` en la raiz del modulo `MiAppDeCompras/`:

```properties
sdk.dir=C\:\\Users\\tu-usuario\\AppData\\Local\\Android\\Sdk
OPENROUTER_API_KEY=sk-or-v1-tu-clave-aqui
```

> Obten tu clave gratuita en [openrouter.ai/keys](https://openrouter.ai/keys).

### 3. Compilar e instalar

```bash
# En Windows (PowerShell)
./gradlew clean installDebug

# En macOS / Linux
./gradlew clean installDebug
```

> Tambien podes abrir el proyecto en **Android Studio**, esperar que sincronice, y presionar **Run**.

---

## Configuracion de claves API

### OpenRouter

1. Registrate en [openrouter.ai](https://openrouter.ai/).
2. Crea una API key en [openrouter.ai/keys](https://openrouter.ai/keys).
3. Agregala al archivo `local.properties`:

```properties
OPENROUTER_API_KEY=sk-or-v1-tu-clave-real
```

4. Ejecuta **Build -> Clean Project -> Rebuild Project** para que se genere el `BuildConfig` con la clave.

> **Importante:** No subas `local.properties` al repositorio. Ya esta incluido en `.gitignore`.

---

## Estructura del proyecto

```
MiAppDeCompras/
├── app/
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/com/r1n1os/jetpackcomposetemplateopensource/
│   │   │   │   ├── data/
│   │   │   │   │   ├── local/           # Room DB, DAOs, entidades
│   │   │   │   │   ├── mock/            # Datos mock de precios
│   │   │   │   │   └── remote/          # GeminiAssistant (OpenRouter)
│   │   │   │   ├── presentation/
│   │   │   │   │   ├── screens/         # Composables (Home, Search, Scanner, etc.)
│   │   │   │   │   ├── viewmodels/      # ViewModels (Home, CreateList, etc.)
│   │   │   │   │   └── navigation/      # Configuracion de navegacion
│   │   │   │   └── ui/theme/            # Tema, colores, tipografia
│   │   │   └── res/                     # Recursos (drawables, strings, etc.)
│   │   ├── test/                        # Tests unitarios
│   │   └── androidTest/                 # Tests de instrumentacion
│   └── build.gradle.kts
├── gradle/
│   └── libs.versions.toml               # Catalogo de versiones
├── build.gradle.kts                     # Build raiz
└── README.md
```

---

## Generacion de listas con IA

La funcion de IA permite convertir descripciones en lenguaje natural en listas de compras estructuradas.

**Ejemplos de uso:**
- *"cena italiana para 4 personas"* -> `pasta, tomates, albahaca, queso parmesano, aceite de oliva, ajo`
- *"desayuno saludable"* -> `avena, leche, bananas, miel, frutos secos`
- *"comida mexicana para 2"* -> `tortillas, carne picada, frijoles, queso, jalapenos, crema agria`

La app usa el modelo gratuito **`meta-llama/llama-3.2-3b-instruct`** a traves de la API de OpenRouter.

---

## Licencia

```
MIT License

Copyright (c) 2026 Hernan

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.
```

---

## Contribuciones

Las contribuciones son bienvenidas. Si encontras un bug o tenes una sugerencia, abri un [issue](https://github.com/tu-usuario/smart-shopping-list/issues) o envia un pull request.

---

<p align="center">Hecho con ❤️ usando Kotlin y Jetpack Compose</p>
