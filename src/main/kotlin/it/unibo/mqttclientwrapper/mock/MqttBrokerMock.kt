package it.unibo.mqttclientwrapper.mock

import it.unibo.mqttclientwrapper.api.MqttMessageType
import it.unibo.mqttclientwrapper.api.MqttTopicConst

/**
 * Mock that represent a iot.mqtt broker
 */
class MqttBrokerMock private constructor() {
    private val clientSubscribed: MutableMap<MqttMock, MutableList<String>>

    companion object {
        /**
         * @return the singleton instance
         */
        val instance = MqttBrokerMock()
    }

    init {
        clientSubscribed = HashMap()
    }

    /**
     * Method to connect a [MqttMock] to this broker
     * @param instance the client
     */
    fun connect(instance: MqttMock) {
        check(!clientSubscribed.containsKey(instance))
        clientSubscribed[instance] = mutableListOf()
    }

    /**
     * Method to disconnect a [MqttMock] to this broker
     * @param instance the client
     */
    fun disconnect(instance: MqttMock) {
        check(clientSubscribed.containsKey(instance))
        clientSubscribed.remove(instance)
    }

    /**
     * method to publish a message
     * @param topic the topic of the message
     * @param message the message to publish
     */
    fun publish(topic: String, message: MqttMessageType) {
        clientSubscribed.entries.map {
                Pair(
                    it.key,
                    it.value.filter { f -> checkTopicMatch(topic, f) }.toList()
                )
            }
            .forEach { it.second.forEach { t -> it.first.dispatch(t, topic, message) } }
    }

    /**
     * method to subscribe a [MqttMock] to a topic
     * @param instance the client
     * @param topicFilter the topic with possible wildcard
     */
    fun subscribe(instance: MqttMock, topicFilter: String) {
        check(clientSubscribed.containsKey(instance))
        clientSubscribed[instance]!!.add(topicFilter)
    }

    /**
     * method to unsubscribe a [MqttMock] to a previous subscribed topic
     * @param instance the client
     * @param topicFilter the topic previous subscribed
     */
    fun unsubscribe(instance: MqttMock, topicFilter: String) {
        check(clientSubscribed.containsKey(instance))
        clientSubscribed[instance]!!.remove(topicFilter)
    }

    private fun checkTopicMatch(topic: String, filter: String): Boolean {
        val topicSplitted = topic.split(MqttTopicConst.LEVEL_SEPARATOR).toTypedArray()
        val filterSplitted = filter.split(MqttTopicConst.LEVEL_SEPARATOR).toTypedArray()
        var index = 0
        while (index < topicSplitted.size && index < filterSplitted.size &&
            (topicSplitted[index] == filterSplitted[index] || filterSplitted[index] == MqttTopicConst.WILDCARD_SINGLE_LEVEL)) {

            index++
        }
        return index == filterSplitted.size && index == topicSplitted.size ||
                index == filterSplitted.size - 1 && filterSplitted[index] == MqttTopicConst.WILDCARD_MULTI_LEVEL
    }
}