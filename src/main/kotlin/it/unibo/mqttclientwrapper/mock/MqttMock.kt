package it.unibo.mqttclientwrapper.mock

import com.google.gson.*
import it.unibo.mqttclientwrapper.api.MqttClientBasicApi
import it.unibo.mqttclientwrapper.api.MqttMessageType
import java.lang.reflect.Type

/**
 * Mock implementation of a [MqttClientBasicApi] based on cast of [MqttMessageType]
 */
class MqttMock : MqttClientBasicApi {
    private val subscribed: MutableMap<String, MutableList<MqttMessageConsumer<*>>> = HashMap()
    private val broker: MqttBrokerMock = MqttBrokerMock.instance

    init {
        connect()
    }

    override fun connect() {
        broker.connect(this)
    }

    override fun disconnect() {
        broker.disconnect(this)
        subscribed.clear()
    }

    override fun close() { }

    override fun publish(topic: String, message: MqttMessageType) {
        broker.publish(topic, message)
    }

    override fun <T : MqttMessageType> subscribe(subscriber: Any, topicFilter: String, classMessage: Class<T>,
                                                 messageConsumer: (topic: String, message: T) -> Unit) {
        if (!subscribed.containsKey(topicFilter)) {
            broker.subscribe(this, topicFilter)
            subscribed[topicFilter] = mutableListOf()
        }
        subscribed[topicFilter]!!.add(MqttMessageConsumer(subscriber, messageConsumer, classMessage))
    }

    override fun unsubscribe(subscriber: Any, topicFilter: String) {
        subscribed[topicFilter]?.removeIf { c: MqttMessageConsumer<*> -> c.subscriber == subscriber }
        if (subscribed[topicFilter]!!.isEmpty()) {
            subscribed.remove(topicFilter)
            broker.unsubscribe(this, topicFilter)
        }
    }

    override fun <T> addSerializer(clazz: Class<T>, serializer: JsonSerializer<T>): MqttClientBasicApi = printUselessFunction()

    override fun <T> addSerializer(clazz: Class<T>, serializer: (T, Type, JsonSerializationContext) -> JsonElement
        ): MqttClientBasicApi = printUselessFunction()

    override fun <T> addDeserializer(clazz: Class<T>, deserializer: JsonDeserializer<T>): MqttClientBasicApi = printUselessFunction()

    override fun <T> addDeserializer(clazz: Class<T>, deserializer: (JsonElement, Type, JsonDeserializationContext) -> T
        ): MqttClientBasicApi = printUselessFunction()

    private fun printUselessFunction(): MqttMock {
        System.err.println("This function in mock implementation don't do nothing")
        return this
    }

    /**
     * method to deliver a message to the iot.mqtt client (used from [MqttBrokerMock])
     * @param filter the topic filter that had matched the message topic
     * @param topic the message topic
     * @param message the message
     */
    fun dispatch(filter: String, topic: String, message: MqttMessageType) {
        if (subscribed.containsKey(filter)) {
            subscribed[filter]?.forEach { c: MqttMessageConsumer<*> -> c.accept(topic, message) }
        }
    }

    private class MqttMessageConsumer<T>(
        val subscriber: Any,
        private val consumer: (topic: String, message: T) -> Unit,
        private val clazz: Class<T>) where T : MqttMessageType {

        fun accept(t: String, message: MqttMessageType) {
            consumer.invoke(t, clazz.cast(message))
        }
    }
}