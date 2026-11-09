# 🍏 CalorieTrack AI — Wear OS & Android Companion Hub

Sistema nativo en **Kotlin** con arquitectura multimódulo para el registro de ingesta calórica mediante **voz en Smartwatch (Wear OS)** y procesamiento inteligente en tiempo real con **Google Gemini AI** en un **Smartphone complementario**.

---

## 📑 Tabla de Contenidos
1. [Arquitectura y Flujo de Trabajo](#1-arquitectura-y-flujo-de-trabajo)
2. [Estructura del Proyecto Multimódulo](#2-estructura-del-proyecto-multimódulo)
3. [Configuración Inicial (API Key de Gemini)](#3-configuración-inicial-api-key-de-gemini)
4. [Guía de Pruebas y Despliegue](#4-guía-de-pruebas-y-despliegue)
   - [Escenario A: Los 2 dispositivos simulados (Emulador Reloj + Emulador Teléfono)](#escenario-a-2-dispositivos-simulados-en-pc-emulador--emulador)
   - [Escenario B: 1 dispositivo físico y 1 simulado (Celular físico por USB + Emulador Reloj)](#escenario-b-celular-físico--emulador-de-reloj-en-pc)
   - [Escenario C: Los 2 dispositivos físicos reales (Smartwatch + Celular por Bluetooth)](#escenario-c-2-dispositivos-físicos-reales-reloj-físico--celular-físico)
5. [Monitoreo y Diagnóstico en Tiempo Real (Logcat)](#5-monitoreo-y-diagnóstico-en-tiempo-real-logcat)
6. [Manejo de Errores Comunes y Soluciones](#6-manejo-de-errores-comunes-y-soluciones)

---

## 1. Arquitectura y Flujo de Trabajo

```
[Usuario dicta comida en Wear OS]
               │
               ▼
   ┌────────────────────────────────────────┐
   │         :wear (Smartwatch)             │
   │  • Interfaz circular en Compose        │
   │  • Botón central de micrófono 🎤        │
   │  • Cola local offline (OfflineMealQueue│
   └───────────────────┬────────────────────┘
                       │  MessageClient (/meal/record)
                       ▼
   ┌────────────────────────────────────────┐
   │         :mobile (Smartphone)           │
   │  • MobileMealListenerService (Segundo plano)
   │  • Google Gemini AI (gemini-1.5-flash) │
   │  • Base de datos Room (SQLite)         │
   │  • Cálculo de presupuesto calórico     │
   └───────────────────┬────────────────────┘
                       │  MessageClient / DataClient (/meal/sync_balance)
                       ▼
   ┌────────────────────────────────────────┐
   │         :wear (Smartwatch)             │
   │  • WearBalanceListenerService          │
   │  • Actualización de DataStore local    │
   │  • El CircularProgressIndicator avanza │
   │  • Muestra calorías restantes al día   │
   └────────────────────────────────────────┘
```

---

## 2. Estructura del Proyecto Multimódulo

El proyecto está desacoplado en 3 módulos Gradle con **Kotlin DSL (`.kts`)**:

- **`:shared`**: Librería compartida que contiene las constantes de ruta (`/meal/record`, `/meal/sync_balance`), DTOs serializables (`MealRecordRequest`, `NutritionAiResponse`, `DailyBalancePayload`) y utilidades de serialización JSON con `kotlinx.serialization`.
- **`:mobile`**: Aplicación de Smartphone con Jetpack Compose y Material 3. Aloja la base de datos Room, el servicio de escucha en segundo plano (`MobileMealListenerService`) y el cliente de IA con Google Gemini (`GeminiNutritionService`).
- **`:wear`**: Aplicación de reloj con Compose for Wear OS y Horologist. Contiene la carátula circular, captura de voz/micrófono, servicio de escucha de balance y la cola de persistencia offline (`OfflineMealQueue`).

> [!IMPORTANT]
> Ambos módulos ejecutables (`:mobile` y `:wear`) comparten obligatoriamente el mismo **`applicationId = "com.calorietracker.app"`** para cumplir con la regla de seguridad del Wearable Data Layer de Google Play Services.

---

## 3. Configuración Inicial (API Key de Gemini)

1. Obtén tu clave gratuita en [Google AI Studio](https://aistudio.google.com/) haciendo clic en **"Get API key"** ➔ **"Create API key"** (usa el nivel gratuito, no requiere tarjeta).
2. En la raíz del proyecto, abre o crea el archivo `local.properties`:
   ```properties
   GEMINI_API_KEY=AIzaSyTuClaveDeGoogleAiStudioAqui
   ```
3. *Resiliencia integrada:* El modelo configurado es **`gemini-1.5-flash`** (1,500 peticiones diarias gratuitas). Si te quedas sin conexión o sin API Key, el servicio activa un motor heurístico local automático para que la aplicación nunca se bloquee.

---

## 4. Guía de Pruebas y Despliegue

Elige la modalidad con la que deseas probar el proyecto:

---

### Escenario A: 2 Dispositivos Simulados en PC (Emulador + Emulador)
*Ideal para pruebas rápidas en la computadora o demostración de examen.*

1. **Crear y encender emuladores:**
   - En Android Studio ve a **Tools** ➔ **Device Manager**.
   - Inicia un emulador de **Teléfono** (ej. Pixel 8 con Google Play Store).
   - Inicia un emulador de **Reloj** (ej. Wear OS Small Round con Google Play Store).
2. **Vincular los emuladores:**
   - Ve a **Tools** ➔ **Wear OS Pairing Assistant**.
   - Selecciona el emulador del reloj y el del teléfono y presiona **Pair**.
3. **Instalar los módulos:**
   - En el selector de configuración superior de Android Studio, selecciona **`mobile`** y presiona **Run (Play ▶)** hacia el emulador de teléfono.
   - Cambia el selector a **`wear`** y presiona **Run (Play ▶)** hacia el emulador de reloj.
4. **Prueba:**
   - En el reloj emulado, presiona el micrófono y habla (o usa el fallback).
   - Observa cómo la ventana del teléfono de al lado se llena automáticamente con los datos calculados por Gemini AI y el reloj avanza su progreso circular.

---

### Escenario B: Celular Físico + Emulador de Reloj en PC
*La configuración híbrida: hablas al reloj en la pantalla de la PC y ves los datos en tu teléfono real.*

1. **Activar Depuración en tu celular físico:**
   - En tu celular ve a **Ajustes** ➔ **Acerca del teléfono** ➔ Toca **7 veces** en *Número de compilación*.
   - Ve a **Opciones de desarrollador** y activa **"Depuración por USB"**.
   - *(En marcas como Xiaomi/Redmi/POCO/Realme, activa también **"Instalar vía USB"**).*
2. **Conectar el celular a la PC:**
   - Conecta el cable USB y acepta el mensaje emergente en el teléfono: *"Permitir siempre la depuración USB desde esta computadora"*.
3. **Encender el emulador del reloj:**
   - En Android Studio enciende tu emulador de Wear OS desde el *Device Manager*.
4. **Abrir el puente de comunicación por ADB:**
   - En Android Studio, abre la pestaña **Terminal** (abajo) y ejecuta:
     ```powershell
     adb -d forward tcp:5601 tcp:5601
     ```
5. **Instalar las aplicaciones:**
   - Instala **`mobile`** en tu celular físico (dándole a Run ▶ o generando el APK en `Build` ➔ `Build APKs`).
   - Instala **`wear`** en el emulador del reloj (Run ▶).
   - *Nota:* Si Android Studio te pide vincularlos con la app **Google Pixel Watch** en tu teléfono, completa el asistente guiado.
6. **Activar micrófono en el emulador de PC:**
   - En la barra lateral del emulador de reloj, toca los tres puntos `...` (*Extended Controls*) ➔ **Microphone** ➔ Marca **"Virtual microphone uses host audio input"**.
7. **Prueba:**
   - Abre la app en tu celular en mano.
   - Toca el micrófono en el reloj de la PC y dicta: *"2 tacos de bistec y una limonada"*.
   - Tu celular consultará a Gemini AI y agregará la comida con sus macros en vivo.

---

### Escenario C: 2 Dispositivos Físicos Reales (Reloj físico + Celular físico)
*El entorno de producción 100% inalámbrico.*

1. **Emparejamiento estándar:**
   - Asegúrate de que tu Smartwatch físico (Galaxy Watch 4/5/6, Pixel Watch, TicWatch, etc.) esté vinculado normalmente por Bluetooth a tu teléfono mediante su app oficial (*Galaxy Wearable*, *Pixel Watch* o *Wear OS*).
2. **Instalación:**
   - Conecta tu celular a la PC e instala el módulo **`:mobile`**.
   - Conecta tu reloj por Wi-Fi Debugging / Bluetooth Debugging desde Android Studio e instala el módulo **`:wear`**.
3. **Prueba:**
   - No necesitas comandos de terminal ni cables: Google Play Services Wearable transmite los paquetes por Bluetooth en segundo plano de manera automática.

---

## 5. Monitoreo y Diagnóstico en Tiempo Real (Logcat)

Para ver la comunicación y la respuesta de la IA en vivo:

1. Abre la pestaña **Logcat** en Android Studio.
2. **Para ver el Reloj:** Selecciona el emulador del reloj en el desplegable y filtra por:
   ```text
   tag:WearMealRepository | tag:WearBalanceListener
   ```
   *(Verás: `Found connected node(s)`, `Sent meal to phone node...` y `Balance sync received`).*
3. **Para ver el Celular:** Selecciona tu celular en el desplegable y filtra por:
   ```text
   tag:MobileMealListener | tag:GeminiNutritionService
   ```
   *(Verás el mensaje recibido en segundo plano, la consulta al modelo `gemini-1.5-flash` y el desglose de calorías persistido en Room).*

---

## 6. Manejo de Errores Comunes y Soluciones

| Síntoma / Error | Causa | Solución |
| :--- | :--- | :--- |
| **`📦 1 offline (reintentar)` en el reloj** | El reloj no detecta el teléfono conectado en ese instante. | Normal. Es el modo offline protegiendo el dato. Revisa que el puente `adb -d forward tcp:5601 tcp:5601` esté activo o vincula los dispositivos con el *Wear OS Pairing Assistant*. Al tocar el botón se reenviará. |
| **`INSTALL_FAILED_USER_RESTRICTED` al dar Run** | Seguridad de teléfonos Xiaomi / Redmi / Realme / Oppo. | Activa la opción **"Instalar vía USB"** en las *Opciones de Desarrollador* de tu celular, o genera el APK manual desde `Build` ➔ `Build APKs` e instálalo tocándolo en el teléfono. |
| **El micrófono del emulador no escucha la voz de la PC** | Micrófono anfitrión deshabilitado en el emulador. | En la barra flotante del reloj pulsa `...` ➔ *Microphone* ➔ marca *"Virtual microphone uses host audio input"*. El código además cuenta con respaldo automático de comida de prueba. |
| **No se comunican los dispositivos** | Los módulos tenían `applicationId` diferente. | Ya corregido: ambos usan `com.calorietracker.app`. |
| **Límite de API de Gemini excedido** | Uso de modelo pesado (Pro) en lugar de Flash. | Utiliza siempre `gemini-1.5-flash`, el cual otorga 1,500 solicitudes por día en el nivel gratuito. |
