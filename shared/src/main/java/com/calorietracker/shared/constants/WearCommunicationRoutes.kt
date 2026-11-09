package com.calorietracker.shared.constants

/**
 * Rutas y capacidades estandarizadas para la comunicación mediante
 * el Wearable Data Layer entre el Smartwatch (Wear OS) y el Smartphone.
 */
object WearCommunicationRoutes {
    /**
     * Ruta para enviar registros de ingesta por voz desde el reloj al smartphone.
     */
    const val MEAL_RECORD_PATH = "/meal/record"

    /**
     * Ruta para sincronizar el balance calórico diario calculado desde el smartphone al reloj.
     */
    const val MEAL_SYNC_BALANCE_PATH = "/meal/sync_balance"

    /**
     * Capacidad declarada por el smartphone para procesar registros de comida con IA.
     */
    const val CAPABILITY_PHONE_AI_PROCESSOR = "meal_tracker_phone_processor"

    /**
     * Capacidad declarada por el reloj para recibir actualizaciones de balance.
     */
    const val CAPABILITY_WEAR_RECEIVER = "meal_tracker_wear_receiver"
}

