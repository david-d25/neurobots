package space.davids_digital.neurobots.model

import kotlin.math.tanh

class NeuralNetwork(
    private val inputsN: Int,
    private val hiddenLayersN: Int,
    private val hiddenLayerNeuronsN: Int,
    private val outputsN: Int,
    minWeight: Double,
    maxWeight: Double
) {
    private val weights: Array<Array<DoubleArray>> = Array(hiddenLayersN + 1) { Array(0) { DoubleArray(0) } }

    init {
        for (layerId in 0 until hiddenLayersN + 1) {
            val layerNeuronsN = if (layerId == hiddenLayersN) outputsN else hiddenLayerNeuronsN
            weights[layerId] = Array(layerNeuronsN) { DoubleArray(0) }
            for (neuronTo in 0 until layerNeuronsN) {
                val neuronsFrom = if (layerId == 0) inputsN else hiddenLayerNeuronsN
                weights[layerId][neuronTo] = DoubleArray(neuronsFrom)
                for (neuronFrom in 0 until neuronsFrom) weights[layerId][neuronTo][neuronFrom] =
                    minWeight + Math.random() * (maxWeight - minWeight)
            }
        }
    }

    fun mutate(maxMutation: Double) {
        for (layerId in weights.indices) {
            for (neuronTo in weights[layerId].indices) {
                for (neuronFrom in weights[layerId][neuronTo].indices) {
                    weights[layerId][neuronTo][neuronFrom] += maxMutation * (1 - 2 * Math.random())
                }
            }
        }
    }

    fun getResponse(input: DoubleArray): DoubleArray {
        var prevSignals = input
        for (layerId in 0 until hiddenLayersN + 1) {
            val neuronsN = if (layerId == hiddenLayersN) outputsN else hiddenLayerNeuronsN
            val resultSignal = DoubleArray(neuronsN)
            for (neuronTo in 0 until neuronsN) {
                var sum = 0.0
                for (neuronFrom in 0 until if (layerId == 0) inputsN else prevSignals.size) sum += weights[layerId][neuronTo][neuronFrom] * prevSignals[neuronFrom]
                resultSignal[neuronTo] = activate(sum)
            }
            prevSignals = resultSignal
        }
        return prevSignals
    }

    private fun activate(value: Double) = tanh(value)
}