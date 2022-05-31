package space.davids_digital.neurobots.world

import kotlin.math.tanh

class NeuralNetwork(
    val inputsN: Int,
    val hiddenLayersN: Int,
    val hiddenLayerNeuronsN: Int,
    val outputsN: Int
) {
    val weights: Array<Array<DoubleArray>> = Array(hiddenLayersN + 1) { Array(0) { DoubleArray(0) } }

    constructor(
        inputsN: Int,
        hiddenLayersN: Int,
        hiddenLayerNeuronsN: Int,
        outputsN: Int,
        initMinWeight: Double,
        initMaxWeight: Double
    ): this(inputsN, hiddenLayersN, hiddenLayerNeuronsN, outputsN) {
        for (layerId in 0 until hiddenLayersN + 1) {
            val layerNeuronsN = if (layerId == hiddenLayersN) outputsN else hiddenLayerNeuronsN
            weights[layerId] = Array(layerNeuronsN) { DoubleArray(0) }
            for (neuronTo in 0 until layerNeuronsN) {
                val neuronsFrom = if (layerId == 0) inputsN else hiddenLayerNeuronsN
                weights[layerId][neuronTo] = DoubleArray(neuronsFrom)
                for (neuronFrom in 0 until neuronsFrom)
                    weights[layerId][neuronTo][neuronFrom] =
                        initMinWeight + Math.random() * (initMaxWeight - initMinWeight)
            }
        }
    }

    fun mutate(maxMutation: Double): NeuralNetwork {
        for (layerId in weights.indices) {
            for (neuronTo in weights[layerId].indices) {
                for (neuronFrom in weights[layerId][neuronTo].indices) {
                    weights[layerId][neuronTo][neuronFrom] += maxMutation * (1 - 2 * Math.random())
                }
            }
        }
        return this
    }

    fun copy(): NeuralNetwork {
        val copy = NeuralNetwork(inputsN, hiddenLayersN, hiddenLayerNeuronsN, outputsN)
        for (layerId in copy.weights.indices) {
            val layerNeuronsN = if (layerId == hiddenLayersN) outputsN else hiddenLayerNeuronsN
            copy.weights[layerId] = Array(layerNeuronsN) { DoubleArray(0) }
            for (neuronTo in copy.weights[layerId].indices) {
                val neuronsFrom = if (layerId == 0) inputsN else hiddenLayerNeuronsN
                copy.weights[layerId][neuronTo] = DoubleArray(neuronsFrom)
                System.arraycopy(
                    weights[layerId][neuronTo], 0,
                    copy.weights[layerId][neuronTo], 0,
                    weights[layerId][neuronTo].size
                )
            }
        }
        return copy
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